package uk.ac.ebi.biosd.coriell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pri.util.M2codec;
import com.pri.util.stream.StreamPump;

public class Crowler
{
 static final int saveStep=100;
 
 private static Pattern linkPat = Pattern.compile("href\\s*=\\s*\"(.*?)\"");
 private static Pattern samplePat = Pattern.compile("/Sample_Detail.aspx.+?Ref=([^&]+)");
 private static Pattern familyPat = Pattern.compile("/FamilyTypeSubDetail.aspx.+?fam=([^&]+)");
 private static Pattern panelPat = Pattern.compile("/Panel_Detail.aspx.+?Ref=([^&]+)");
 
 private File cacheDir;
 private File logFileErr;
 private File logFileFull;
 private File docDir;
 private File coriellFile;
 
 private Coriell coriell;
 
 private Set<String> visitedLinks = new HashSet<String>();
 
 private static URL startPage = null;
 
 static int lastSavedCounter=0;

 static
 {
  try
  {
   startPage = new URL("http://ccr.coriell.org/");
  }
  catch(MalformedURLException e)
  {
   e.printStackTrace();
  }
 }
 /**
  * @param args
  * @throws IOException 
  */
 public static void main(String[] args) throws IOException
 {
  if( args.length != 1 )
  {
   System.err.println("Please specify cache path in command line");
   return;
  }
  
  Crowler crwlr = new Crowler( args[0] );
  
  crwlr.processURL( startPage, startPage, 0 );

  crwlr.shutdown();
 }
 
 public Crowler( String cachePath)
 {
  cacheDir = new File(cachePath);
  
  if( ! cacheDir.exists() )
   cacheDir.mkdirs();
  
  logFileErr = new File(cacheDir,".log.err");
  logFileFull = new File(cacheDir,".log.full");
  coriellFile = new File(cacheDir,".coriell");
  docDir = new File(cacheDir,"docs");
  
  logFileErr.delete();
  logFileFull.delete();
  
  docDir.mkdirs();
  
  if( coriellFile.exists() )
  {
   try
   {
    FileInputStream finp = new FileInputStream(coriellFile);
    ObjectInputStream ois = new ObjectInputStream( finp );
    
    coriell = (Coriell)ois.readObject();
    
    ois.close();
    finp.close();
   }
   catch(Exception e)
   {
    e.printStackTrace();
    return;
   }

  }
  else
   coriell = new Coriell();
 }
 
 public void shutdown() throws IOException
 {
  FileOutputStream fos = new FileOutputStream(coriellFile);
  ObjectOutputStream oos = new ObjectOutputStream( fos );
  
  oos.writeObject( coriell );
  
  oos.close();
  fos.close();
 }
 
 private void processURL( URL url, URL parent, int lvl ) throws IOException
 {
  String extURL = url.toExternalForm();
  
  Page page = null;

//  boolean load = true;
//  String sampleID;
//  String familyID;
//  String panelID;
  

  
  while(true)
  {
   try
   {
    visitedLinks.add(url.toExternalForm());

    page = getPage(url);

    if( page.url != url )
    {
     System.out.println("Redirection: "+url+" -> "+page.url);
     url=page.url;
     visitedLinks.add(url.toExternalForm());
    }
    
    
    System.out.println("Visiting: " + url + " Visited: " + visitedLinks.size()+" Level: "+lvl);
    
    FileWriter wr  = new FileWriter(logFileFull, true);
    
    wr.append(url.getFile()+" Referer: "+parent.getFile()+"\n");
    wr.close();

   }
   catch(IOException e)
   {
    try
    {
     FileWriter wr  = new FileWriter(logFileErr, true);
     
     wr.append(url.toExternalForm()+" Referer: "+parent.toExternalForm()+"\n");
     wr.close();
    }
    catch(IOException ioe)
    {
    }

    
    System.err.println("Can't load page: " + url + " Error: " + e.getMessage());
    return;
   }

   break;
  }

  
  Matcher mtch = samplePat.matcher(extURL);
  
  if( mtch.find() )
  {
   String sampleID = mtch.group(1);

   Sample s = CoriellSampleExtractor.processSample(url, page.content);
   s.setId(sampleID);
   coriell.addSample(s);
  }
  else
  {
   mtch = familyPat.matcher(extURL);
   
   if( mtch.find() )
   {
    String familyID = mtch.group(1);
    
    Family s = CoriellSampleExtractor.processFamily(url, page.content);
    s.setId(familyID);
    coriell.addFamily(s);
   }
   else
   {
    mtch = panelPat.matcher(extURL);
    
    if( mtch.find() )
    {
     String panelID = mtch.group(1);
     
     Panel s = CoriellSampleExtractor.processPanel(url, page.content);
     s.setId(panelID);
     coriell.addPanel(s);
    }
   }
  }
  
  
  List<URL> links = collectLinks(page.content, url);

  for(URL link : links)
  {
   if(visitedLinks.contains(link.toExternalForm()))
    continue;

   if( link.getPath().endsWith(".xls") || link.getPath().endsWith(".csv") )
    saveDocFile( link, url );
   else
    processURL(link, url, lvl+1);
  }
 }
 
 private void saveDocFile( URL link, URL refUrl ) throws IOException
 {
  String path = link.getPath();
  
  
  File outFile = new File(docDir,M2codec.encode(path)+path.substring(path.length()-4));
  
  if(outFile.exists())
   return;
  
  System.out.println("Saving file: "+path);
  
  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  try
  {
  HttpURLConnection conn;
  conn = (HttpURLConnection) link.openConnection();
  conn.connect();
  
  StreamPump.doPump(conn.getInputStream(), baos, true);
  conn.disconnect();
  }
  catch (IOException e)
  {
   FileWriter wr  = new FileWriter(logFileErr, true);
   
   wr.append(link.toExternalForm()+" Referer: "+refUrl.toExternalForm()+"\n");
   wr.close();

   return;
  }
  
  FileOutputStream fos = new FileOutputStream( outFile );
  fos.write( baos.toByteArray() );
  fos.close();
  
  visitedLinks.add(link.toExternalForm());
 }

 
 private File urlToPath( String url )
 {
  int hash = url.hashCode();
  
  int low = hash & 0xFF;
  int high = (hash>>8) & 0xFF;
  
  return new File(cacheDir,""+high+"/"+low+"/"+M2codec.encode(url));
 }
 
 private Page getPage( URL url ) throws IOException
 {
  Page page = new Page();
  File cacheFile = urlToPath(url.toExternalForm());

  if(cacheFile.exists())
  {
   ByteArrayOutputStream baos = new ByteArrayOutputStream();
   FileInputStream finp = new FileInputStream(cacheFile);
   StreamPump.doPump(finp, baos, true);
   finp.close();

   System.out.println("Cache matched: "+url);
   
   page.content = new String(baos.toByteArray(), "UTF-8");
   page.url = url;
   
   return page;
  }

  
  HttpURLConnection conn;
  conn = (HttpURLConnection) url.openConnection();
  conn.connect();
  
//  HttpURLConnection.setFollowRedirects(false);
//  int stat = conn.getResponseCode();
//  
//  if (stat >= 300 && stat <= 307 && stat != 306 && stat != HttpURLConnection.HTTP_NOT_MODIFIED ) 
//  {
//   String loc = conn.getHeaderField("Location");
//   conn.disconnect();
//   throw new ReditectedException( new URL(url,loc) );
//  }
  
  ByteArrayOutputStream baos = new ByteArrayOutputStream();

  StreamPump.doPump(conn.getInputStream(), baos, true);

  
  page.content = new String(baos.toByteArray(), "UTF-8");
  page.url = conn.getURL();
  
//  URL newURL = conn.getURL();
//  
//  if( !newURL.equals(url) )
//   System.out.println("  New URL: "+url );
  
  conn.disconnect();
  
  cacheFile = urlToPath(page.url.toExternalForm());
  
  cacheFile.getParentFile().mkdirs();
  
  PrintStream fwr = new PrintStream(cacheFile,"UTF-8");
  fwr.print(page.content);
  fwr.close();

  return page;
 }
 
 private List<URL> collectLinks( String page, URL url )
 {
  ArrayList<URL> links = new ArrayList<URL>(100);
  

   
   Matcher linkMatcher = linkPat.matcher(page);
   
   int end = 0;
   
   while( linkMatcher.find(end) )
   {
    end=linkMatcher.end();
    
    String link = page.substring(linkMatcher.start(1),linkMatcher.end(1));
    
    if( link.startsWith("javascript:")|| link.endsWith(".pdf") )
     continue;

    link = stripAmps( link );
    
    URL lUrl;
    try
    {
     lUrl = new URL(url, link );

     if( !lUrl.getProtocol().equals("http") || !lUrl.getHost().equals(url.getHost()) || link.startsWith("#") )
      continue;
     
     links.add(lUrl);
//     System.out.println("link="+lUrl);
    }
    catch(MalformedURLException e)
    {
    }
    
   }

  
  return links;
 }

 private String stripAmps( String str )
 {
  if( str.indexOf("&amp;") == -1 )
   return str;
  
  StringBuilder sb = new StringBuilder();
  
  int len = str.length();
  int ptr = 0;
  
  while( ptr+5 < len )
  {
   int pos = str.indexOf("&amp;", ptr);
   
   if( pos == -1 )
    break;
   
   sb.append(str.substring(ptr,pos+1) );
   ptr=pos+5;
  }
  
  sb.append(str.substring(ptr));
 
  String res = sb.toString();
  
  if( res.indexOf(" ") != -1 )
  {
   sb.setLength(0);
   
   len = res.length();
   ptr = 0;
   
   while( ptr < len )
   {
    int pos = res.indexOf(" ", ptr);
    
    if( pos == -1 )
     break;
    
    sb.append(res.substring(ptr,pos) ).append("%20");
    ptr=pos+1;
   }
   
   sb.append(res.substring(ptr));
   
   res = sb.toString();

  }

  
//  System.out.println("Stripped: "+str+" -> "+res);
  
  return res;
 }
 
}

package uk.ac.ebi.biosd.coriell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pri.util.M2codec;
import com.pri.util.stream.StreamPump;

public class Crowler
{
 private static Pattern linkPat = Pattern.compile("href\\s*=\\s*\"(.*?)\"");
 private static Pattern samplePat = Pattern.compile("/Sample_Detail.aspx.+?Ref=(\\w+)");
 private static Pattern familyPat = Pattern.compile("/FamilyTypeSubDetail.aspx.+?fam=(\\w+)");
 private static Pattern panelPat = Pattern.compile("/Panel_Detail.aspx.+?Ref=(\\w+)");
 
 private static File cacheDir = new File("x:/cache");
 
 private Set<String> visitedLinks = new HashSet<String>();
 
 private Map<String, Sample> samples = new HashMap<String, Sample>();

 private static URL startPage = null;

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
  */
 public static void main(String[] args)
 {
  Crowler crwlr = new Crowler();
  
  crwlr.processURL( startPage, 0 );

 }
 
 public Crowler()
 {
  if( ! cacheDir.exists() )
   cacheDir.mkdirs();
 }
 
 private void processURL( URL url, int lvl )
 {
  Page page = null;

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

   }
   catch(IOException e)
   {
    System.err.println("Can't load page: " + url + " Error: " + e.getMessage());
    return;
   }

   break;
  }

  List<URL> links = collectLinks(page.content, url);

  for(URL link : links)
  {
   if(visitedLinks.contains(link.toExternalForm()))
    continue;

   processURL(link, lvl+1);
  }
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
    
    if( link.startsWith("javascript:"))
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

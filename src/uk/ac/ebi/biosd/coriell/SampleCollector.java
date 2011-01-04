package uk.ac.ebi.biosd.coriell;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.pri.util.stream.StreamPump;

public class SampleCollector
{
 static final String sampleUrl = "http://ccr.coriell.org/Sections/Search/Sample_Detail.aspx?PgId=166&Ref=";
 
 static File cacheDir ;
 static File sampleIdDir ;
 static File errorLog ;


 /**
  * @param args
  * @throws IOException 
  */
 public static void main(String[] args) throws IOException
 {
  cacheDir = new File(args[0]);
  sampleIdDir = new File(cacheDir, "sample.id");
  errorLog=new File(cacheDir, ".log.err");
  
  Coriell coriell = new Coriell();
  
  Set<String> ids = new HashSet<String>();
  
  for( File f : sampleIdDir.listFiles() )
  {
   int n=0, k=0;
   
   FileReader fr = new FileReader(f);
   BufferedReader bfrd = new BufferedReader( new FileReader(f) );
   
   String line = null;
   while ( (line=bfrd.readLine()) != null )
   {
    if( ids.add( line.trim() ) ) k++;
    n++;
   }
   
   fr.close();
   
   System.out.println(f.getAbsolutePath()+": "+n+"/"+k);
  }
  
  System.out.println("IDs: "+ids.size());
  
  int n=0;
  for(String id : ids )
  {
   n++;
   System.out.print("Loading sample ("+n+"): "+id);
   
   if( id.length() < 5 )
   {
    System.out.println("  ID too short");

    FileWriter wr  = new FileWriter(errorLog, true);
    wr.append("Sample error: "+id+" ID too short\n");
    wr.close();
    
    continue;
   }
   
   try
   {
    String page = loadSample(id);
    
    if( page.indexOf("<font color='red' size='4'>Not Found</font>") != -1 )
    {
     System.out.println(" NOT FOUND");
     continue;
    }
    
    Sample s = CoriellSampleExtractor.processSample(new URL(sampleUrl+id), page);
    s.setId(id);
    coriell.addSample(s);

   }
   catch (IOException e)
   {
    System.out.println(" IO Error");
    
    FileWriter wr  = new FileWriter(errorLog, true);
    wr.append("Sample error: "+id+" "+e.getMessage()+"\n");
    wr.close();
   }
  }
  
  FileOutputStream fos = new FileOutputStream(new File(cacheDir,".coriell"));
  ObjectOutputStream oos = new ObjectOutputStream( fos );
  
  oos.writeObject( coriell );
  
  oos.close();
  fos.close();

 }

 static String loadSample(String id) throws IOException
 {
  
  String path = id.substring(0,2)+'/'+id.substring(2,4)+'/';
  
  File sampPath = new File(cacheDir, path);
  File sampFile = new File(sampPath, id );

  if( sampFile.exists() )
  {
   System.out.println(" CACHED");
   
   FileInputStream fis = new FileInputStream(sampFile);
   ByteArrayOutputStream baos = new ByteArrayOutputStream();
   StreamPump.doPump( fis, baos, true );
   
   return new String(baos.toByteArray(),"UTF-8");
  }
  
  URL sUrl = new URL(sampleUrl+id);
  
  HttpURLConnection conn;
  conn = (HttpURLConnection) sUrl.openConnection();
  conn.connect();
  
  ByteArrayOutputStream baos = new ByteArrayOutputStream();

  StreamPump.doPump(conn.getInputStream(), baos, true);
  conn.disconnect();

  sampPath.mkdirs();
  
  FileOutputStream fos = new FileOutputStream(sampFile);
  fos.write(baos.toByteArray());
  fos.close();

  System.out.println("  DONE");
 
  return new String(baos.toByteArray(),"UTF-8");
 }
}

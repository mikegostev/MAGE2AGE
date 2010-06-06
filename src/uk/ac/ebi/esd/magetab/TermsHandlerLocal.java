package uk.ac.ebi.esd.magetab;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import moda2.SDRFparser;
import moda2.SDRFparser.Value;

import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;


public class TermsHandlerLocal
{
 static final String valueTSR="TSR";
 static final String unitType="UnitType";
 static final String unitValue="UnitValue";
 static final String unitTSR="UnitTSR";
 
 public static void main(String[] args) throws IOException
 {
  if( args.length != 1 )
  {
   System.err.println("Please, provide working directory");
   System.exit(1);
  }
  
  File wDir = new File( args[0] );
  
  if( ! wDir.isDirectory() )
  {
   System.err.println("Working directory doesn't exist");
   System.exit(1);
  }
 
  
  ResponseHandler<String> responseHandler = new BasicResponseHandler();

  Map<String,Collection<String>> hdrs = new HashMap<String,Collection<String>>();
  
  int i=1;
  
  for(File dir : wDir.listFiles())
  {
   
   if( ! dir.isDirectory() )
    continue;
   
   String id = dir.getName();
   
   File sdrf = new File(dir, dir.getName()+".sdrf.txt");
   
   try
   {
    
    System.out.println(" Trying: "+id);
    

    SDRFparser parser = new SDRFparser( new FileReader(sdrf) );
    
    
    for( HashMap<String,HashMap<String,HashSet<Value>>> srcMap : parser.getNodes().get("Source Name").values() )
    {
     for( Map.Entry<String,HashMap<String,HashSet<Value>>> me : srcMap.entrySet() )
     {
      for( String k : me.getValue().keySet() )
      {
       String name = null;
       
       if( k == null )
        name = me.getKey() ;
       else
        name = k;
       
       Collection<String> expLst = hdrs.get(name);
       
       if( expLst == null )
       {
        expLst = new ArrayList<String>();
        hdrs.put(name, expLst);
       }
       
       expLst.add(id);
      }
     }
    }
    
   }
   catch(Exception e)
   {
    System.out.println(id+ " failured: ("+e.getMessage()+")");
    e.printStackTrace();
   }
   
  }
  
  PrintStream rep = new PrintStream(new File(wDir,"report.txt"));
  
  MGEDTerms mged = new MGEDTerms();
  
  HashSet<String> standard = new HashSet<String>();
  
  rep.println("Headers: ");
  
  for(Map.Entry<String,Collection<String>> me : hdrs.entrySet() )
  {
   String h = me.getKey();
   
   if( mged.contains(h))
   {
    rep.print("[MGED] ");
    standard.add(h);
   }
   
   rep.println(h);
   
   for( String exp : me.getValue() )
   {
    rep.print("   ");
    rep.print("{"+exp+"}");
   }
   
   rep.println("");
  }
  
  rep.println("Standard: ");
  for(String h : standard)
  {
   rep.println(h);
  }

  rep.close();
  
  return;
 }

 static class Path
 {
  String first;
  String second;
  
  String qual;
 }
 
 static class Block
 {
  String name;
  Path path;
  
  List<Path> qualifiers = new ArrayList<Path>(10);
 }
}
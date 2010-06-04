package uk.ac.ebi.esd.magetab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import moda2.SDRFparser;
import moda2.SDRFparser.Value;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;


public class AE2Age
{
 static final String valueTSR="TSR";
 static final String unitType="UnitType";
 static final String unitValue="UnitValue";
 static final String unitTSR="UnitTSR";
 
 public static void main(String[] args) throws IOException
 {
  if( args.length != 2 )
  {
   System.err.println("Please, provide working directory and id file");
   System.exit(1);
  }
  
  File wDir = new File( args[0] );
  
  if( ! wDir.isDirectory() )
  {
   System.err.println("Working directory doesn't exist");
   System.exit(1);
  }
 
  File idFile = new File(wDir,args[1]);
  
  List<String> ids = new ArrayList<String>(1000);
  
  FileReader idRd = new FileReader(idFile);
  BufferedReader in = new BufferedReader(idRd);
  
  String str=null;
  
  while( (str = in.readLine()) != null )
  {
   str=str.trim();
   
   if( str.length() > 0)
    ids.add(str);
  }
  
  in.close();
  
  System.out.println("Loaded "+ids.size()+" IDs");
  
  HttpClient httpclient = new DefaultHttpClient();


  ResponseHandler<String> responseHandler = new BasicResponseHandler();

  Set<String> hdrs = new HashSet<String>();
  
  int i=0;
  
  for(String id : ids)
  {
   
   try
   {
    i++;
    
    String url = "http://www.ebi.ac.uk/microarray-as/ae/files/"+id+"/"+id+".sdrf.txt";
    
    System.out.println(String.valueOf(i)+" Trying: "+url);
    
    HttpGet httpget = new HttpGet(url); 
    String responseBody = httpclient.execute(httpget, responseHandler);
    
    System.out.println(String.valueOf(i)+" Got "+id+" size: "+responseBody.length());

    SDRFparser parser = new SDRFparser( new StringReader(responseBody) );
    
   
    url = "http://www.ebi.ac.uk/microarray-as/ae/files/"+id+"/"+id+".idf.txt";
    
    System.out.println(String.valueOf(i)+" Trying: "+url);
    
    httpget = new HttpGet(url); 
    responseBody = httpclient.execute(httpget, responseHandler);
    
    System.out.println(String.valueOf(i)+" Got "+id+" size: "+responseBody.length());
    
    String invTitle=null;
    String pubmed=null;
    String link = "http://www.ebi.ac.uk/microarray-as/ae/experiments/"+id;

     in = new BufferedReader( new StringReader(responseBody) );

     str=null;
     
     while( (str = in.readLine()) != null )
     {
      String[] strArr = str.split("[\\t]");

      if( "Investigation Title".equals(strArr[0]) )
       invTitle = strArr[1];
      else if ( "PubMed ID".equals(strArr[0]) && strArr.length == 2 )
       pubmed = strArr[1];
     }

    
     List<Block> header = new ArrayList<Block>();
     
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
        
        Block cBlock = null;
        
        for( Block b : header )
        {
         if( b.name.equals(name) )
         {
          cBlock = b;
          break;
         }
        }
        
        if( cBlock == null )
        {
         cBlock = new Block();
         cBlock.name = name;
         
         Path pth = new Path();
         
         cBlock.path = pth;
         
         pth.first=me.getKey();
         pth.second = k;
         
         header.add( cBlock );
        }
        
        SDRFparser.Value val = me.getValue().get(k).iterator().next();
        
        
        if( val.valueTSR != null )
        {
         boolean found=false;
         
         for( Path qp : cBlock.qualifiers )
         { if( qp.first.equals( valueTSR ) )
          {
           found=true;
           break; 
          }
         }
         
         if( ! found )
         {
          Path qp = new Path();
          qp.first = valueTSR;
          
          cBlock.qualifiers.add(qp);
         }
        }
        
        if( val.unitType != null )
        {
         boolean found=false;
         
         for( Path qp : cBlock.qualifiers )
         { if( qp.first.equals( unitType ) )
          {
           found=true;
           break; 
          }
         }
         
         if( ! found )
         {
          Path qp = new Path();
          qp.first = unitType;
          
          cBlock.qualifiers.add(qp);
         }
        }
        
        if( val.unitValue != null )
        {
         boolean found=false;
         
         for( Path qp : cBlock.qualifiers )
         { if( qp.first.equals( unitValue ) )
          {
           found=true;
           break; 
          }
         }
         
         if( ! found )
         {
          Path qp = new Path();
          qp.first = unitValue;
          
          cBlock.qualifiers.add(qp);
         }
        }
        
        
        if( val.unitTSR != null )
        {
         boolean found=false;
         
         for( Path qp : cBlock.qualifiers )
         { if( qp.first.equals( unitTSR ) )
          {
           found=true;
           break; 
          }
         }
         
         if( ! found )
         {
          Path qp = new Path();
          qp.first = unitTSR;
          
          cBlock.qualifiers.add(qp);
         }
        }
       }
      }
     }
     
//     System.out.println("Header:");
//
//     for( Block b : header )
//     {
//      System.out.print( b.name+',');
//      
//      for( Path qp : b.qualifiers )
//       System.out.print( b.name+'['+qp.first+"],");
//     }
//     System.out.print('\n');

    
     File ageOutFile = new File(wDir, id+".age.txt");
    
     FileWriter fwr = new FileWriter(ageOutFile);
     
     fwr.write("\nGroup\tAE Link");
     
     if( invTitle != null )
      fwr.write("\tDescription");

     if( pubmed != null )
      fwr.write("\tPubMedID");
     
     fwr.write('\n');
     
     fwr.write(id+"\t"+link);
     
     if( invTitle != null )
      fwr.write("\t"+invTitle);
     
     if( pubmed != null )
      fwr.write("\t"+pubmed);

     fwr.write("\n\nSample");

     for( Block b : header )
     {
      String h = null;
      if( STDTerms.containsTerm(b.name) )
       h=b.name;
      else
       h="{"+b.name+"}";
      
      fwr.write( "\t"+h);
      
      for( Path qp : b.qualifiers )
       fwr.write( "\t"+h+"["+qp.first+"]");
     }
     fwr.write("\tbelongsTo\n");

     for( Map.Entry<String,HashMap<String,HashMap<String,HashSet<Value>>>> me : parser.getNodes().get("Source Name").entrySet() )
     {
      fwr.write(me.getKey());
      
      for( Block b : header )
      {
       HashMap<String,HashSet<Value>> fst = me.getValue().get(b.path.first);
       
       if( fst == null )
       {
        for( int j=0; j < b.qualifiers.size()+1; j++ )
         fwr.write("\t");
        
        continue;
       }
       
       Value v = fst.get(b.path.second).iterator().next();
       
       fwr.write("\t");
       if( v.value != null )
        fwr.write(v.value);
      
       for( Path qp : b.qualifiers )
       {
        String val = null;
        
        if( qp.first.equals(valueTSR) )
         val=v.valueTSR;
        else if( qp.first.equals(unitType) )
         val=v.unitType;
        else if( qp.first.equals(unitValue) )
         val=v.unitValue;
        else if( qp.first.equals(unitTSR) )
         val=v.unitTSR;

        fwr.write("\t");
        if( val != null )
         fwr.write(val);
       }
      }
      
      fwr.write("\t"+id+"\n");
     }
     
     fwr.close();

    
   }
   catch(Exception e)
   {
    System.out.println(id+ " failured");
    e.printStackTrace();
   }
   
  }
  
  System.out.println("Headers: ");
  for(String h : hdrs)
   System.out.println(h);
  
  
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

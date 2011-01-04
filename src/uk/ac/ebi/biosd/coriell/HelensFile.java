package uk.ac.ebi.biosd.coriell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelensFile
{

 /**
  * @param args
  * @throws IOException 
  */
 public static void main(String[] args) throws IOException
 {
  RandomAccessFile file = new RandomAccessFile( new File(args[0]) , "r");

  Map<String, List<Sample>> collMap = new HashMap<String, List<Sample>>();
  
  String line = null;
  
  while( (line=file.readLine()) !=  null )
  {
   if( line.startsWith("Sample Name") )
    break;
  }
  
  if( line == null )
  {
   System.out.println("Content not found");
   return;
  }
  
  String[] hdrs = line.split("\t");
  
  for(int i=0; i < hdrs.length; i++ )
  {
   String hd = hdrs[i];
   
   int ind = hd.indexOf('[');
   if( ind != -1 )
    hdrs[i] = hd.substring(ind+1, hd.length()-1);
  }
  
  while( (line=file.readLine()) !=  null )
  {
   String[] vals = line.split("\t");

   String collName = vals[vals.length-1].trim();
   
   List<Sample> coll = collMap.get(collName);
   if( coll == null )
   {
    coll = new ArrayList<Sample>();
    collMap.put(collName, coll);
   }
   
   Sample s = new Sample();
   
   s.setId(vals[0].trim());
   
   for( int i=1; i< vals.length-1; i++ )
    s.addAttibute(hdrs[i], new Value(vals[i].trim()));
   
   coll.add(s);
  }

  int n=0;
  
  File outDir = new File(args[1]);
 
  for( Map.Entry<String, List<Sample>> me : collMap.entrySet() )
  {
   n++;
   
   CollectionMapping.CollectionInfo clInf = CollectionMapping.map.get(me.getKey());
   
   File outFile = new File( outDir, clInf.getId()+".txt");
   FileWriter f = new FileWriter(outFile);
  
   f.write("Group\tData Source\tReference\tLink\tDescription\nGCO-"+clInf.getId()+"\tCoriell\tyes\t"+clInf.getLink()+"\t"+clInf.getDescription()+"\n\nSample\tbelongsTo\n*\tGCO-"+clInf.getId()+"\n\nSample");
   
   for( int i=1; i< hdrs.length-1; i++ )
    f.write("\tCharacteristics{"+hdrs[i]+"}");
   
   
   for( Sample s : me.getValue() )
   {
    f.write("\n"+s.getId());
    
    for( int i=1; i< hdrs.length-1; i++ )
    {
     String v = s.getAttribites().get(hdrs[i]).get(0).toString();
     
     f.write("\t"+v);
    }

   }
   
   f.close();
  
  }
  
 }

}

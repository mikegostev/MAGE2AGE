package uk.ac.ebi.esd.magetab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import moda2.SDRFparser;
import moda2.SDRFparser.Value;

public class Mage_CollectSampleTerms
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
 
  List<Map<String,String>> persons = new ArrayList<Map<String,String>>(5);
  List<Map<String,String>> terms = new ArrayList<Map<String,String>>(5);
  List<Map<String,String>> pubs = new ArrayList<Map<String,String>>(5);

  Set<String> unitTypes = new HashSet<String>();
  Set<String> characTypes = new HashSet<String>();
  Set<String> otherHeaders = new HashSet<String>();
  
  
  for( File expDir : wDir.listFiles() )
  {
   if( ! expDir.isDirectory() )
    continue;
   
   String expName = expDir.getName();
   
//   if( expName.indexOf("-242") == -1 ) //TODO remove after debugging
//    continue;
   
   System.out.println("Working with the experiment: "+expName );
   
   File idfFile = new File( expDir, expName+".idf.txt" );
   File sdrfFile = new File( expDir, expName+".sdrf.txt" );
 
   if(  ! sdrfFile.canRead() )
   {
    System.out.println("SDRF file :"+sdrfFile.getAbsolutePath()+" doesn't exist or isn't readable. Skiping the experiment.");
    continue;
   }

   SDRFparser parser = new SDRFparser( new FileReader(sdrfFile) );
   
//   HashMap<String, HashMap<String, HashMap<String, HashSet<Value>>>> sampleMap = parser.getNodes().get("Source Name");
   
   String invTitle=null;
   String expDescr=null;
   String relsDate=null;
   String expDate=null;

   Map<String,String> comments = new TreeMap<String, String>();
   
   persons.clear();
   terms.clear();
   pubs.clear();
   
   if( idfFile.canRead() )
   {
    BufferedReader in = new BufferedReader( new FileReader(idfFile) );

    String str=null;
    
    while( (str = in.readLine()) != null )
    {
     String[] strArr = str.split("[\\t]");

     if( "Investigation Title".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0 )
      invTitle = strArr[1];
     else if( "Experiment Description".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0  )
      expDescr = strArr[1];
     else if( "Public Release Date".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0  )
      relsDate = strArr[1];
     else if( "Date of Experiment".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0  )
      expDate = strArr[1];
     else if( strArr[0].startsWith("Person ") )
      processIDFObjLine("Person ",strArr,persons);
     else if( strArr[0].startsWith("Term ") )
      processIDFObjLine("Term ",strArr,terms);
     else if( strArr[0].startsWith("Publication ") )
      processIDFObjLine("Publication ",strArr,pubs);
     else if( strArr[0].startsWith("PubMed ") )
      processIDFObjLine("",strArr,pubs);
     else if( strArr[0].startsWith("Comment[") && strArr.length > 1 && strArr[1].trim().length() > 0 )
      comments.put(strArr[0].substring(8,strArr[0].length()-1), strArr[1]);
     
    }
    
    in.close();
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
       
       if( name.startsWith("Characteristics") )
       {
        String chr = name.substring(15).trim();
        if( chr.charAt(0) == '[' && chr.charAt(chr.length()-1) == ']' )
         chr=chr.substring(1,chr.length()-1).trim();
        else
         System.err.println("Invalid characteristics: "+name);
        
        characTypes.add(chr);
       }
       else
        otherHeaders.add(name);
       
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
       unitTypes.add(val.unitType);
       
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
   
   System.out.println("Header:");

   for( Block b : header )
   {
    System.out.print( b.name+',');
    
    for( Path qp : b.qualifiers )
     System.out.print( b.name+'['+qp.first+"],");
   }
   System.out.print('\n');

  
   if( true )
    continue;
   
   File ageOutFile = new File(expDir, expName+".age.txt");
  
   FileWriter fwr = new FileWriter(ageOutFile);
   
   fwr.write("\nGroup");
   
   if( invTitle != null )
    fwr.write("\tDescription");
   
   fwr.write('\n');
   
   fwr.write(expName);
   
   if( invTitle != null )
    fwr.write("\t"+invTitle);

   fwr.write("\n\nSample");

   for( Block b : header )
   {
    fwr.write( "\t"+b.name);
    
    for( Path qp : b.qualifiers )
     fwr.write( "\t"+b.name+'['+qp.first+"]");
   }
   fwr.write("\tbelongsTo\n");

   List<Map.Entry<String,HashMap<String,HashMap<String,HashSet<Value>>>>> sampls = new ArrayList<Map.Entry<String,HashMap<String,HashMap<String,HashSet<Value>>>>>();
   
   sampls.addAll(parser.getNodes().get("Source Name").entrySet());
   
   Collections.sort(sampls, new Comparator<Map.Entry<String,HashMap<String,HashMap<String,HashSet<Value>>>>>()
   {
    @Override
    public int compare(Entry<String, HashMap<String, HashMap<String, HashSet<Value>>>> o1,
      Entry<String, HashMap<String, HashMap<String, HashSet<Value>>>> o2)
    {
     return o1.getKey().compareTo(o2.getKey());
    }
   });
   
   int sid=1;
   for( Map.Entry<String,HashMap<String,HashMap<String,HashSet<Value>>>> me : sampls )
   {
    fwr.write("SAE-"+expName+"-"+(sid++));
    
    for( Block b : header )
    {
     HashMap<String,HashSet<Value>> fst = me.getValue().get(b.path.first);
     
     if( fst == null )
     {
      for( int i=0; i < b.qualifiers.size()+1; i++ )
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
    
    fwr.write("\t"+expName+"\n");
   }
   
   fwr.close();
  }
  
  System.out.println("Unit types: "+unitTypes);
  System.out.println("Characteristics: "+characTypes);
  System.out.println("Other headers: "+otherHeaders);
  
  return;
 }

 private static void processIDFObjLine(String string, String[] strArr, List<Map<String, String>> persons)
 {
  int dif=strArr.length -1 - persons.size();
  if( dif > 0 )
  {
   for( int i=0; i < dif; i++ )
    persons.add(null);
  }
  
  String attr = strArr[0].substring(string.length());
  
  for( int i=0; i < strArr.length -1; i++ )
  {
   String val = strArr[i+1].trim();
   
   if( val.length() == 0 )
    continue;
   
   Map<String, String> obj = persons.get(i);
   
   if( obj == null )
   {
    obj=new TreeMap<String, String>();
    persons.set(i, obj);
   }
   
   obj.put(attr, val);
  }
  
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

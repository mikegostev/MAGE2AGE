package uk.ac.ebi.esd.pride;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pri.util.StringUtils;


public class ReadPrideData
{

 static final String experimentId     = "PRIDE Experiment Accession";
 static final String sampleId         = "DOI (Digital Object Identifier)";
 static final String pubMedId         = "PubMed ID (CiteXplore)";
 static final String description      = "Experiment Short Label";
 /**
  * @param args
  * @throws IOException
  */
 static Set<String>  sampleAttribures = new HashSet<String>();

 static
 {
  sampleAttribures.add("Sample Name");
  sampleAttribures.add("Sample Description Comment");
  sampleAttribures.add("Taxonomy Term (NEWT / NCBI Taxon)");
  sampleAttribures.add("Taxonomy ID (NEWT / NCBI Taxon)");
  sampleAttribures.add("Tissue Ontology Term (BRENDA)");
  sampleAttribures.add("BRENDA ID (Tissue)");
  sampleAttribures.add("Cell Type Term (CL)");
  sampleAttribures.add("CL ID (Cell Type)");
  sampleAttribures.add("Gene Ontology Term (GO)");
  sampleAttribures.add("GO ID (Gene Ontology)");
  sampleAttribures.add("Human Disease Term (DOID)");
  sampleAttribures.add("DOID ID (Human Disease)");
 }

 static class Experiment
 {
  String id;
  
  Map<String,String> attributes = new LinkedHashMap<String,String>();
  Map<String,Sample> samples = new LinkedHashMap<String,Sample>();
  
 }
 
 static class Sample
 {
  String id;
  
  Map<String,String> attributes = new LinkedHashMap<String,String>();
 }
 
 static Map<String, Experiment> experiments = new HashMap<String, Experiment>();
 
 public static void main(String[] args) throws IOException
 {
  File wDir = new File( args[0] );
  
  File prideFile = new File( wDir, args[1] );
  
  BufferedReader rd = new BufferedReader(new FileReader(prideFile));

  String str;

  List<String> header = new ArrayList<String>(30);

  str = rd.readLine();
  StringUtils.splitExcelString(str, ",", header);
  
  List<String> parts = new ArrayList<String>(30);

  List<String> sampleAttributes = new ArrayList<String>();

  for(String h : header )
   if( sampleAttribures.contains(h) )
    sampleAttributes.add(h);

  
  while((str = rd.readLine()) != null)
  {
   parts.clear();
   StringUtils.splitExcelString(str, ",", parts);

   Sample cSamp = new Sample();
   Experiment cExp = new Experiment();
   
   for( int i=0; i< header.size(); i++ )
   {
    if( i >= parts.size() )
     break;
    
    String hdr = header.get(i);
    
    String val = parts.get(i).trim();
    
    if( val.length() == 0 )
     continue;
    
    if( experimentId.equals(hdr) )
     cExp.id=val;
    else if( sampleId.equals(hdr) )
     cSamp.id=val;
    else if( sampleAttribures.contains(hdr) )
     cSamp.attributes.put(hdr, val);
    else if( pubMedId.equals(hdr) )
     cExp.attributes.put("PubMedID", val);
    else if( description.equals(hdr) )
     cExp.attributes.put("Description", val);
    else
     cExp.attributes.put(hdr, val);
   }
   
   cExp.attributes.put("Pride Link", "http://www.ebi.ac.uk/pride/directLink.do?experimentAccessionNumber="+cExp.id);
   
   Experiment eExp = experiments.get(cExp.id);
   
   if( eExp == null )
   {
    eExp = cExp;
    experiments.put(cExp.id, eExp);
   }
   
   eExp.samples.put(cSamp.id, cSamp);
  
  }

  Map<String, String> m = new LinkedHashMap<String, String>();
  
  Set<String> expAttr = new HashSet<String>();
  Set<String> smpAttr = new HashSet<String>();
  
  PrintStream out = System.out;
  
  out.println("Found "+experiments.size()+" experiments");
  
  
  for( Experiment e : experiments.values() )
  {
   out.println("   Experiment: "+e.id+" (samples: "+e.samples.size()+")");
   
   for( Map.Entry<String, String> me: e.attributes.entrySet() )
   {
    out.println("     "+me.getKey()+" = "+me.getValue());
   
    expAttr.add(me.getKey());
   }
   
   if( e.samples.size() == 0 )
    continue;
   
   out.println("   ++Samples");
 
   for( Sample s : e.samples.values() )
   {
    out.println("       Sample: " + s.id + " (attrs: " + s.attributes.size() + ")");

    for(Map.Entry<String, String> me : s.attributes.entrySet())
    {
     out.println("         " + me.getKey() + " = " + me.getValue());
     smpAttr.add(me.getKey());
    }
   }
  }
  
  out.println("\nExpreriment attributes");
  
  for(String s : expAttr)
   out.println(s);

  out.println("\nSample attributes");
  for(String s : smpAttr)
   out.println(s);
  
  
  List<String> localSampAttr = new ArrayList<String>();
 
  for( Experiment e : experiments.values() )
  {
   out = new PrintStream(new File(wDir,e.id+".age.txt"));
   
   out.print("Group");
   
   for( String key: e.attributes.keySet() )
    out.print("\t"+key);
   
   out.println();
   
   out.print(e.id);
   
   for( String key: e.attributes.keySet() )
    out.print("\t"+e.attributes.get(key));
   
   out.println("\n");

   
   if( e.samples.size() == 0 )
    continue;
   
   localSampAttr.clear();
   
   for( String sa : sampleAttributes )
   {
    for( Sample s : e.samples.values() )
    {
     if( s.attributes.containsKey(sa) )
     {
      localSampAttr.add(sa);
      break;
     }
    }
   }
 
   out.print("Sample");
   for( String lsa : localSampAttr )
    out.print("\t"+lsa);

   out.println("\tbelongsTo");
   
   for( Sample s : e.samples.values() )
   {
    out.print(s.id);

    for( String lsa : localSampAttr )
    {
     String val = s.attributes.get(lsa);
     
     if( val == null )
      val="";
     
     out.print("\t"+val);
    }
    
    out.println("\t"+e.id);

   }
   
   out.close();
  }

 
 }

}

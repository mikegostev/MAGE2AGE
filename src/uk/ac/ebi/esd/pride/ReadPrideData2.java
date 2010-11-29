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

import uk.ac.ebi.age.util.StringUtil;


public class ReadPrideData2
{

 static final String experimentId     = "Project Name";//"PRIDE Experiment Accession";
 static final String sampleId         = "Sample Name";//"DOI (Digital Object Identifier)";
 static final String sampleName       = "Sample Name";
 static final String pubMedId         = "PubMed ID (CiteXplore)";
 static final String description      = experimentId;//"Experiment Short Label";
 static final String expTitle         = "Experiment Title";
 static final String refLine          = "Reference Line";
 static final String contactName      = "Contact Name";
 static final String brendaID         = "BRENDA ID (Tissue)";
 static final String doi              = "DOI (Digital Object Identifier)";
 /**
  * @param args
  * @throws IOException
  */
 static Map<String,String>  sampleAttribures = new LinkedHashMap<String,String>();
 static Map<String,String>  contactAttribures = new LinkedHashMap<String,String>();
 static Map<String,String>  publicationAttribures = new LinkedHashMap<String,String>();

 static
 {
//  sampleAttribures.add("Sample Name");
  sampleAttribures.put("Sample Description Comment",null);
  sampleAttribures.put("Taxonomy Term (NEWT / NCBI Taxon)",null);
  sampleAttribures.put("Taxonomy ID (NEWT / NCBI Taxon)",null);
  sampleAttribures.put("Tissue Ontology Term (BRENDA)",null);
  sampleAttribures.put(brendaID,null);
  sampleAttribures.put("Cell Type Term (CL)",null);
  sampleAttribures.put("CL ID (Cell Type)",null);
  sampleAttribures.put("Gene Ontology Term (GO)",null);
  sampleAttribures.put("GO ID (Gene Ontology)",null);
  sampleAttribures.put("Human Disease Term (DOID)",null);
  sampleAttribures.put("DOID ID (Human Disease)",null);
  
  contactAttribures.put(contactName,null);
  contactAttribures.put("Institution",null);
  contactAttribures.put("Contact Details (Email)",null);
  
  publicationAttribures.put(pubMedId,null);
  publicationAttribures.put(doi,null);
  publicationAttribures.put(refLine,null);
 }

 static class Experiment
 {
  String id;
  
  Map<String,String> attributes = new LinkedHashMap<String,String>();
  Map<String,Map<String,String>> samples = new LinkedHashMap<String,Map<String,String>>();
  Map<String,Map<String,String>> publications = new LinkedHashMap<String,Map<String,String>>();
  Map<String,Map<String,String>> contacts = new LinkedHashMap<String,Map<String,String>>();
  
 }
 
// static class Sample
// {
//  String id;
//  String name;
//  
//  Map<String,String> attributes = new LinkedHashMap<String,String>();
// }
 
 static Map<String, Experiment> experiments = new HashMap<String, Experiment>();
 
 public static void main(String[] args) throws IOException
 {
  File wDir = new File( args[0] );
  
  File prideFile = new File( wDir, args[1] );
  
  BufferedReader rd = new BufferedReader(new FileReader(prideFile));

  String str;

  List<String> header = new ArrayList<String>(30);

  str = rd.readLine();
  StringUtil.splitExcelString(str, ",", header);
  
  List<String> parts = new ArrayList<String>(30);

  List<String> sampleAttributes = new ArrayList<String>();

  for(String h : header )
   if( sampleAttribures.containsKey(h) )
    sampleAttributes.add(h);

  Map<String,String> valMap = new HashMap<String, String>();
  
  while((str = rd.readLine()) != null)
  {
   valMap.clear();
   parts.clear();
   StringUtil.splitExcelString(str, ",", parts);

   for(int i = 0; i < header.size(); i++)
   {
    if(i >= parts.size())
     break;

    String hdr = header.get(i);

    String val = parts.get(i).trim();

    if(val.length() == 0)
     continue;

    valMap.put(hdr, val);
   }

   String projId = valMap.get(experimentId);
   String sampName = valMap.get(sampleId);

   if(projId == null || sampName == null)
    continue;

   String sampId = sampName+valMap.get(brendaID);
   
   Experiment eExp = experiments.get(projId);

   String prideID = valMap.get("PRIDE Experiment Accession");

   if(eExp == null)
   {
    experiments.put(projId, eExp = new Experiment());
    eExp.id = projId;
    
    eExp.attributes.put("Data Source", "Pride");
    
    if( prideID != null )
     eExp.attributes.put("Link","http://www.ebi.ac.uk/pride/directLink.do?experimentAccessionNumber="+prideID);
    
    eExp.attributes.put("Description", valMap.get(description));
    
    if( valMap.containsKey(expTitle) )
     eExp.attributes.put("{"+expTitle+"}", valMap.get(expTitle) );
     
   }

   Map<String, String> cSamp = eExp.samples.get(sampId);

   if(cSamp == null)
   {
    eExp.samples.put(sampId, cSamp = new LinkedHashMap<String, String>());

    cSamp.put("Name", sampName);
    
    for(String sa : sampleAttributes)
    {
     String av = valMap.get(sa);

     if(av != null)
     {
       cSamp.put("{"+sa+"}", av);
     }
    }
    
    if( prideID != null )
     cSamp.put("{Pride ID}", prideID);
   }
   String contId = valMap.get(contactName);

   if(contId != null)
   {
    Map<String, String> cont = eExp.contacts.get(contId);

    if(cont == null)
    {
     eExp.contacts.put(contId, cont = new LinkedHashMap<String, String>());

     for(String sa : contactAttribures.keySet())
     {
      String av = valMap.get(sa);

      if(av != null)
       cont.put("{"+sa+"}", av);
     }
    }
   }

   String pubId = valMap.get(refLine);

   if(pubId != null)
   {
    Map<String, String> pub = eExp.publications.get(pubId);

    if(pub == null)
    {
     eExp.publications.put(pubId, pub = new LinkedHashMap<String, String>());

     for(String sa : publicationAttribures.keySet())
     {
      String av = valMap.get(sa);

      if(av != null)
      {
       if( sa.equals(pubMedId))
        pub.put("PubMed ID", av);
       else if( sa.equals(doi))
        pub.put("DOI", av);
       else
        pub.put("{"+sa+"}", av);
      }
     }
    }
   }

   
 /*    
   Experiment cExp = new Experiment();

   for(int i = 0; i < header.size(); i++)
   {
    if(i >= parts.size())
     break;

    String hdr = header.get(i);

    String val = parts.get(i).trim();

    if(val.length() == 0)
     continue;

    if(experimentId.equals(hdr))
     cExp.id = val;
    else if(sampleId.equals(hdr))
     cSamp.id = val;
    else if(sampleName.equals(hdr))
     cSamp.name = val;
    else if(sampleAttribures.contains(hdr))
     cSamp.attributes.put(hdr, val);
    else if(pubMedId.equals(hdr))
     cExp.attributes.put("PubMed ID", val);
    else if(description.equals(hdr))
     cExp.attributes.put("Description", val);
    else
     cExp.attributes.put("{" + hdr + "}", val);
   }

   cExp.attributes.put("Data Source", "Pride");
   cExp.attributes.put("Link", "http://www.ebi.ac.uk/pride/directLink.do?experimentAccessionNumber=" + cExp.id);
  */  
  }

  Map<String, String> m = new LinkedHashMap<String, String>();
  
  Set<String> expAttr = new HashSet<String>();
  Set<String> smpAttr = new HashSet<String>();
  
  PrintStream out = System.out;
  
  out.println("Found "+experiments.size()+" experiments");
  
  
  for( Experiment e : experiments.values() )
  {
   if(e.samples.size() <= 10)
    continue;

   out = new PrintStream(new File(wDir, e.id + ".age.txt"));

   out.print("Group");

   for(String key : e.attributes.keySet())
    out.print("\t" + key);

   out.println();

   String grpIg = "GPR-" + e.id;

   out.print(grpIg);

   for(String key : e.attributes.keySet())
    out.print("\t" + e.attributes.get(key));

   out.println("\n");

   if(e.contacts.size() != 0)
   {
    printMap(e.contacts, "Person", "?c", "contactOf", grpIg, out);

    out.println();
   }

   if(e.publications.size() != 0)
   {
    printMap(e.publications, "Publication", "?p", "publicationAbout", grpIg, out);

    out.println();
   }

   if(e.samples.size() == 0)
    continue;

   printMap(e.samples, "Sample", grpIg + "-", "belongsTo", grpIg, out);

   out.close();
  }

  
  }

  private static void printMap(Map<String,Map<String,String>> attrs, String obj, String idPfx, String rel, String grp, PrintStream out )
  {
   Map<String,String> hdrs = new LinkedHashMap<String,String>();
   
   for( Map<String,String> line : attrs.values() )
    for( String hdname : line.keySet() )
     hdrs.put(hdname, null);
   
   out.print(obj);
   for( String hdname : hdrs.keySet() )
    out.print("\t"+hdname);
   
   out.print("\t"+rel);
   
   out.println();
   
   int i=0;
   for( Map<String,String> line : attrs.values() )
   {
    i++;
    out.print(idPfx+i);

    for( String hdname : line.keySet() )
    {
     String val = line.get(hdname);
     
     if( val == null )
      val="";
     out.print("\t"+val);
    }
   
    out.print("\t"+grp);
    
    out.println();
   }
  
  }
}

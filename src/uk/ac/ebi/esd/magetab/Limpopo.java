package uk.ac.ebi.esd.magetab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mged.magetab.error.ErrorCode;
import org.mged.magetab.error.ErrorItem;
import org.mged.magetab.error.ErrorItemFactory;

import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SDRFNode;
import uk.ac.ebi.arrayexpress2.magetab.exception.ErrorItemListener;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.magetab.exception.ValidateException;
import uk.ac.ebi.arrayexpress2.magetab.handler.ParserMode;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.arrayexpress2.magetab.validator.AbstractValidator;
import uk.ac.ebi.arrayexpress2.magetab.validator.Validator;

public class Limpopo
{

 /**
  * @param args
  */
 public static void main(String[] args)
 {
  MAGETABParser parser = new MAGETABParser(ParserMode.READ_ONLY);

  // add an error item listener to the parser
  parser.addErrorItemListener(new ErrorItemListener()
  {

   public void errorOccurred(ErrorItem item)
   {
    // locate the error code from the enum, to check the generic message
    ErrorCode code = null;
    for(ErrorCode ec : ErrorCode.values())
    {
     if(item.getErrorCode() == ec.getIntegerValue())
     {
      code = ec;
      break;
     }
    }

    if(code != null)
    {
     // this just dumps out some info about the type of error
     System.out.println("Listener reported error...");
     System.out.println("\tError Code: " + item.getErrorCode() + " [" + code.getErrorMessage() + "]");
     System.out.println("\tError message: " + item.getMesg());
     System.out.println("\tCaller: " + item.getCaller());
    }
   }
  });

  // make a new validator
  Validator<MAGETABInvestigation> validator = new AbstractValidator<MAGETABInvestigation>()
  {

   public boolean validate(MAGETABInvestigation validatorSource) throws ValidateException
   {
    // this doesn't really do any validation, just generates one error
    // item then throws an exception to indicate it failed
    ErrorItem item = ErrorItemFactory.getErrorItemFactory().generateErrorItem("Not really doing any validation", 1,
      this.getClass());

    fireErrorItemEvent(item);
    throw new ValidateException(item, true, "Validation failed");
   }
  };

  // set our dummy validator on the parser
  // parser.setValidator(validator);

  // now, parse from a file
  // File idfFile = new
  // File("/home/mike/ESD/AE/AE-EXP/E-MEXP-242/E-MEXP-242.idf.txt");
  // File idfFile = new File("F:/BioSD/ae/E-ATMX-12/E-ATMX-12.idf.txt");

  Set<String> unitTypes = new HashSet<String>();
  Set<String> characTypes = new HashSet<String>();
  Set<String> otherHeaders = new HashSet<String>();

  List<Map<String,String>> persons = new ArrayList<Map<String,String>>(5);
  List<Map<String,String>> terms = new ArrayList<Map<String,String>>(5);
  List<Map<String,String>> pubs = new ArrayList<Map<String,String>>(5);

  List<String> persKeys = new LinkedList<String>();
  List<String> termsKeys = new LinkedList<String>();
  List<String> pubsKeys = new LinkedList<String>();
  
  Map<String,String> comments = new LinkedHashMap<String, String>();

  Map<String,Map<String,String>> termMap = new HashMap<String, Map<String,String>>();

  
  File wDir = new File("F:/BioSD/ae/");
  File outDir = new File("F:/BioSD/ae/age-tab");

  for(File expDir : wDir.listFiles())
  {
   try
   {
    if(!expDir.isDirectory())
     continue;

    String expName = expDir.getName();

    String expId="GAE-"+expName;
    
    
    String invTitle=null;
    String expDescr=null;
    String relsDate=null;
    String expDate=null;

    comments.clear();
    persons.clear();
    terms.clear();
    pubs.clear();

    persKeys.clear();
    termsKeys.clear();
    pubsKeys.clear();
    
    File idfFile = new File(expDir, expName + ".idf.txt");

    try
    {

     if(idfFile.canRead())
     {
      BufferedReader in = new BufferedReader(new FileReader(idfFile));

      String str = null;

      while((str = in.readLine()) != null)
      {
       String[] strArr = str.split("[\\t]");

       if("Investigation Title".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0)
        invTitle = strArr[1];
       else if("Experiment Description".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0)
        expDescr = strArr[1];
       else if("Public Release Date".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0)
        relsDate = strArr[1];
       else if("Date of Experiment".equals(strArr[0]) && strArr.length > 1 && strArr[1].trim().length() > 0)
        expDate = strArr[1];
       else if(strArr[0].startsWith("Person "))
       {
        String key = strArr[0].substring(7);
        persKeys.add(key);
        processIDFObjLine(key, strArr, persons);
       }
       else if(strArr[0].startsWith("Term Source "))
       {
        String key = strArr[0].substring(12);
        termsKeys.add(key);
        processIDFObjLine(key, strArr, terms);
       }
       else if(strArr[0].startsWith("Publication "))
       {
        String key = strArr[0].substring(12);
        pubsKeys.add(key);
        processIDFObjLine(key, strArr, pubs);
       }
       else if(strArr[0].startsWith("PubMed "))
       {
        String key = "PubMed ID";
        pubsKeys.add(key);
        processIDFObjLine(key, strArr, pubs);
       }
       else if(strArr[0].startsWith("Comment["))
        comments.put(strArr[0].substring(8, strArr[0].length() - 1), strArr[1]);

      }

      in.close();
     }
    }
    catch (Exception e) 
    {
     e.printStackTrace();
    }
    
    // do parse
    System.out.println("Parsing " + idfFile.getAbsolutePath() + "...");

    // need to get the url of this file, as the parser only takes urls
    MAGETABInvestigation investigation = parser.parse(idfFile.toURI().toURL());

    PrintStream out=null;
    // System.out.println( investigation.SDRF.lookupRootNodes() );

    Collection<? extends SDRFNode> nodes = investigation.SDRF.lookupNodes("Source Name");
    
    if( nodes.size() == 0 )
     continue;
    
    File outF = new File(outDir,expName+".age.txt");
   
    out=null;
    try
    {
     out = new PrintStream(outF);
    }
    catch(FileNotFoundException e)
    {
     System.out.println("Can't open file for writing: "+outF.getAbsolutePath());
     continue;
    }
    
    out.print("Group\tDescription\tSource\tLink");
    
    if(expDescr!=null)
     out.print("\tExperiment Description");
    
    if( relsDate!= null)
     out.print("\tPublic Release Date");

    if( expDate!= null)
     out.print("\tDate of Experiment");
    
    for(String comm : comments.keySet() )
     out.print("\tComment{"+comm+"}");
    
    out.print("\n");
    
    out.print(expId);
    out.print("\t");
    out.print(invTitle);
    out.print("\tArray Experss\thttp://www.ebi.ac.uk/arrayexpress/experiments/"+expName);

    if(expDescr!=null)
     out.print("\t"+expDescr);
    
    if( relsDate!= null)
     out.print("\t"+relsDate);

    if( expDate!= null)
     out.print("\t"+expDate);
    
    for(String comm : comments.values() )
     out.print("\t"+comm);
    
    out.print("\n");
    
    persKeys.add("contactOf");
    for( Map<String,String> p : persons )
     p.put("contactOf", expId);

    persKeys.add("publicationAbout");
    for( Map<String,String> p : pubs )
     p.put("publicationAbout", expId);
    
    printBlock(persKeys,persons,"Person",out);
    printBlock(pubsKeys,pubs,"Publication",out);
   
    if( terms.size() > 0 )
    {
     termMap.clear();
    
     for( Map<String,String> t : terms)
      termMap.put(t.get("Name"), t);
    
     out.print("\nTerm Source\tFile\tVersion");
     
     for( Map.Entry<String, Map<String,String>> me : termMap.entrySet())
     {
      out.print("\n");
      out.print(me.getKey());
      out.print("\t");
      out.print(me.getValue().get("File"));
      out.print("\t");
      out.print(me.getValue().get("Version"));
     }
    
    }
    
    
    ArrayList<SDRFNode> sampls = new ArrayList<SDRFNode>(nodes.size());
    sampls.addAll(nodes);
    
    Collections.sort(sampls, new Comparator<SDRFNode>()
    {
     @Override
     public int compare(SDRFNode o1, SDRFNode o2)
     {
      return o1.values()[0].compareTo(o2.values()[0]);
     }
    });
    
    String[] hdrs = sampls.get(0).headers();

    out.println("\n\nSample\tName");
    
    String mainAttr = null;
    for(int i = 1; i < hdrs.length; i++)
    {
     String name = hdrs[i];

     if(name.startsWith("Characteristics"))
     {
      Qualified qname = parseQualified(name);

      characTypes.add(qname.qualifier);
      out.print("\tCharacteristics{"+qname.qualifier+"}");
      
      mainAttr = "Characteristics{"+qname.qualifier+"}";
     }
     else if(name.startsWith("Unit"))
     {
      Qualified qname = parseQualified(name);

      unitTypes.add(qname.qualifier);
      
      out.print("\t"+mainAttr+"[Unit{"+qname.qualifier+"}]");
      
      mainAttr=mainAttr+"[Unit{"+qname.qualifier+"}]";
     }
     else if( name.equalsIgnoreCase("Term Source REF") )
     {
      out.print("\t"+mainAttr+"[Term Source]");
     }
     else if( name.startsWith("Comment") )
     {
      Qualified qname = parseQualified(name);
      out.print("\tComment{"+qname.qualifier+"}");
      mainAttr="Comment{"+qname.qualifier+"}";
     }
     else
     {
      mainAttr = name;
      otherHeaders.add(name);
      out.print("\t"+name);
     }
    }
    
    out.print("\tbelongsTo");

    
    for( int i=0; i < sampls.size(); i++ )
    {
     out.print("\nSAE-"+expName+"-"+(i+1));
     
     String[] vals = sampls.get(i).values();
     
     for( int j=0; j < vals.length; j++)
      out.print("\t"+vals[j]);
     
     out.print(expId);
    }

    
    if( out != null )
     out.close();
    // print out the parsed investigation
    // System.out.println(investigation);

   }
   catch(ParseException e)
   {
    // This happens if parsing failed.
    // Any errors here will also have been reported by the listener
    e.printStackTrace();
   }
   catch(MalformedURLException e)
   {
    // This is if the url from the file is bad
    e.printStackTrace();
   }

  }

  System.out.println("\nCharacteristics: ");
  for(String s : characTypes)
   System.out.println(" " + s);

  System.out.println("\nUnits: ");
  for(String s : unitTypes)
   System.out.println(" " + s);

  System.out.println("\nOther props: ");
  for(String s : otherHeaders)
   System.out.println(" " + s);

 }

 static class Qualified
 {
  String attr;
  String qualifier;
 }

 static Qualified parseQualified(String str)
 {
  Qualified q = new Qualified();

  int pos = str.indexOf('[');

  if(pos == -1)
  {
   q.attr = str;
   return q;
  }

  q.attr = str.substring(0, pos).trim();

  q.qualifier = str.substring(pos + 1).trim();

  if(q.qualifier.charAt(q.qualifier.length() - 1) != ']')
   System.err.println("Invalid qualified: " + str);
  else
   q.qualifier = q.qualifier.substring(0, q.qualifier.length() - 1).trim();

  return q;
 }

 private static void processIDFObjLine(String key, String[] strArr, List<Map<String, String>> persons)
 {
  int dif=strArr.length -1 - persons.size();
  if( dif > 0 )
  {
   for( int i=0; i < dif; i++ )
    persons.add(null);
  }
  
  
  for( int i=0; i < strArr.length -1; i++ )
  {
   String val = strArr[i+1].trim();
   
   if( val.length() == 0 )
    continue;
   
   Map<String, String> obj = persons.get(i);
   
   if( obj == null )
   {
    obj=new HashMap<String, String>();
    persons.set(i, obj);
   }
   
   obj.put(key, val);
  }
  
 }
 
 private static void printBlock( List<String> keys, List<Map<String,String>> data, String name, PrintStream out)
 {
  if( data.size() == 0 )
   return;
  
   Iterator<String> kIter = keys.iterator();
   while( kIter.hasNext() )
   {
    String key = kIter.next();
    boolean has=false;
    for( Map<String,String> obj : data )
    {
     if( obj.containsKey(key) )
     {
      has=true;
      break;
     }
    }
    
    if( !has )
     kIter.remove();
   }

  out.print("\n");
  out.print(name);
  
  for( String k : keys )
   out.print("\t"+k);
  
  int ind=1;
  for( Map<String,String> obj : data )
  {
   out.print("\n?"+(ind++));

   for( String k : keys )
   {
    out.print("\t");
    
    String val = obj.get(k);
    
    if(val != null)
     out.print(val);
   }
  }
 }
}

package moda2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SDRFparser
{
 static public class Value
 {
  public String                  value;
  public String                  valueTSR;
  public String                  unitType;
  public String                  unitValue;
  public String                  unitTSR;
  HashMap<String, String> comments;

  public int hashCode()
  {
   return value.hashCode();
  }

  public boolean equals(Object obj)
  {
   Value v = (Value) obj;
   if(value.compareTo(v.value) != 0)
    return false;
   else
    return true;
  }
 }

 static class ColumnDescriptor
 {
  String kind;
  int    linkedColumnNumber;
  String nodeType;
  String attClass;
  String attType;
  String attSubtype;
  String unitType;
  String commentType;
 }

 static Set<String>              nodeTypes                 = new HashSet<String>(Arrays.asList(
   "Source Name",
   "Sample Name",
   "Extract Name",
   "Labeled Extract Name",
   "Hybridization Name",
   "Scan Name",
   "Normalization Name",
   "Array Data File",
   "Derived Array Data File",
   "Array Data Matrix File",
   "Derived Array Data Matrix File",
   "Image File", "Array Design File",
   "Array Design REF",
   "Technology Type",
   "Assay Name",
   "Protocol REF"));
 
 static Set<String>              attributeTypes            = new HashSet<String>(Arrays.asList(
   "Characteristics",
   "Provider",
   "Material Type",
   "Label",
   "Factor Value",
   "FactorValue",
   "Performer",
   "Date",
   "Parameter Value",
   "Description"));
 
 static Set<String> attributesCommentsAllowed = new HashSet<String>(Arrays.asList("Provider", "Performer", "Parameter Value"));
 
  HashMap<String, Integer> counters = new HashMap<String, Integer>();

 private HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashSet<Value>>>>> nodes = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashSet<Value>>>>>();
 private HashMap<String, HashMap<String, Vector<String>>> edges = new HashMap<String, HashMap<String, Vector<String>>>();

 
 public static void main(String[] args) throws IOException
 {
  File baseDir = new File( args[0] );
  
  if( ! baseDir.isDirectory() )
  {
   System.err.println("File '"+args[0]+"' is not directory");
   System.exit(1);
  }
  
  Set<String> terms = new HashSet<String>();
  
  for( File subdir : baseDir.listFiles() )
  {
   if( ! subdir.isDirectory() )
    continue;
   
   File sdrfFile = new File( subdir, subdir.getName()+".sdrf.txt");
   
   if( ! sdrfFile.exists() )
   {
    System.err.println("SDRF file '"+sdrfFile.getAbsolutePath()+"' doesn't exist");
    continue;
   }
   
   System.out.println("Processing : "+sdrfFile.getAbsolutePath());
   
   SDRFparser parser = new SDRFparser( new FileReader(sdrfFile) );
  
   for( HashMap<String,HashMap<String,HashSet<Value>>> srcMap : parser.getNodes().get("Source Name").values() )
   {
    for( Map.Entry<String,HashMap<String,HashSet<Value>>> me : srcMap.entrySet() )
    {
//     System.out.println("Level 0: "+me.getKey());
     
     for( String k : me.getValue().keySet() )
     {
      if( k == null )
       terms.add(me.getKey());
      else
       terms.add( me.getKey()+"["+k+"]");
     
      Value val = me.getValue().get(k).iterator().next();
      
      if( val.comments != null )
       System.out.println("Comments: "+val.comments.size());
     }
    }
   }
   
//   display(parser.getNodes().get("Source Name"), 0);

  }
  
  for( String term : terms )
  {
   System.out.println("Term: "+term);
  }
  
  //  display(nodes, 0);
//System.out.println("-----------------");
//display(edges, 0);
//  display(parser.getNodes().get("Source Name"), 0);

 }
 
 
 public HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashSet<Value>>>>> getNodes()
 {
  return nodes;
 }
  
 public HashMap<String, HashMap<String, Vector<String>>> getEdges()
 {
  return edges;
 }

 
 public SDRFparser(Reader rd) throws IOException
 {
  
  BufferedReader in = new BufferedReader(rd);
  String str;
  boolean headerFound = false;
  ColumnDescriptor[] colDefs = null;
  
  Pattern pattern = Pattern.compile("\"?([^\\[]+)(\\[(.*)\\])?\\s*(\\((.+)\\))?\"?");
  
  while((str = in.readLine()) != null)
  {
   if(str.startsWith("#") || str.startsWith("[") || str.length() == 0)
    continue;
   
   String[] strArr = str.split("[\\t]");
  
   if( strArr.length == 0 || strArr[0].trim().length() == 0 )
    continue;
  
   for( int i=0; i < strArr.length; i++ )
   {
    if( strArr[i].length() >= 2 && strArr[i].charAt(0) == '"' && strArr[i].charAt(strArr[i].length()-1) == '"' )
     strArr[i]=strArr[i].substring(1,strArr[i].length()-1);
   }
   
   if(!headerFound)
   {
    headerFound = true;
    colDefs = new ColumnDescriptor[strArr.length];
    int curNodeIdx = -1;
    int curAttIdx = -1;
    int curUnitIdx = -1;
    int prevNodeIdx = -1;
    
    for(int curColIdx = 0; curColIdx < strArr.length; curColIdx++)
    {
     colDefs[curColIdx] = new ColumnDescriptor();
     Matcher matcher = pattern.matcher(strArr[curColIdx]);
     matcher.find();
     
     String header = matcher.group(1).trim();
     String inBrackets = (matcher.group(3) != null) ? matcher.group(3).trim() : null;
   
     if(nodeTypes.contains(header))
     {
      colDefs[curColIdx].kind = "node";
      colDefs[curColIdx].linkedColumnNumber = prevNodeIdx;
      colDefs[curColIdx].nodeType = header;
      
      if(header.compareTo("Protocol REF") != 0)
       prevNodeIdx = curColIdx;
      
      curNodeIdx = curColIdx;
      curUnitIdx = -1;
      curAttIdx = -1;
     }
     else if(attributeTypes.contains(header) || curAttIdx == -1 && (header.compareTo("Term Source REF") == 0 || header.compareTo("Comment") == 0)
       || (header.compareTo("Comment") == 0 && !attributesCommentsAllowed.contains(colDefs[curAttIdx].attClass)))
     {
      colDefs[curColIdx].kind = "attribute";
      colDefs[curColIdx].linkedColumnNumber = curNodeIdx;
      curAttIdx = curColIdx;
      colDefs[curColIdx].attClass = header;
      colDefs[curColIdx].attType = inBrackets;
      
      if(matcher.group(5) != null)
       colDefs[curColIdx].attSubtype = matcher.group(5).trim();
      
      curUnitIdx = -1;
     }
     else if(header.compareTo("Unit") == 0)
     {
      colDefs[curColIdx].kind = "unit";
      colDefs[curColIdx].linkedColumnNumber = curAttIdx;
      curUnitIdx = curColIdx;
      colDefs[curColIdx].unitType = inBrackets;
     }
     else if(header.compareTo("Term Source REF") == 0)
     {
      if(curUnitIdx != -1)
       colDefs[curColIdx].linkedColumnNumber = curUnitIdx;
      else
       colDefs[curColIdx].linkedColumnNumber = curAttIdx;
      curUnitIdx = -1;
      colDefs[curColIdx].kind = "tsr";
     }
     else if(header.compareTo("Comment") == 0)
     {
      colDefs[curColIdx].kind = "comment";
      colDefs[curColIdx].linkedColumnNumber = curAttIdx;
      curUnitIdx = -1;
      colDefs[curColIdx].commentType = inBrackets;
     }
     else
     {
      throw new RuntimeException("Can't determine column type: "+header);
     }
    }
   }
   else
   {
    String curNodeType = "";
    String curNodeValue = "";
    String curAttClass = "";
    String curAttType = "";
    
    Value curAttValue = null;
    
    Vector<String> protocolChain = new Vector<String>();
    
    for(int curColIdx = 0; curColIdx < strArr.length; curColIdx++)
    {
     if(colDefs[curColIdx].kind.compareTo("node") == 0)
     {
      curNodeType = colDefs[curColIdx].nodeType;
    
      if(curNodeType.endsWith(" REF"))
       strArr[curColIdx] = generateUniqueName(strArr[curColIdx]);
      
      curNodeValue = strArr[curColIdx];
      
      if(curNodeType.compareTo("Protocol REF") == 0)
       protocolChain.add(curNodeValue);
      else if(colDefs[curColIdx].linkedColumnNumber != -1)
      {
       addToHashMap(colDefs[colDefs[curColIdx].linkedColumnNumber].nodeType + ":" + strArr[colDefs[curColIdx].linkedColumnNumber], curNodeType + ":"
         + curNodeValue, edges, protocolChain);
       protocolChain = new Vector();
      }
      addToHashMap(curNodeType, curNodeValue, nodes, new HashMap<String, HashMap<String, HashSet<Value>>>());
     }
     else if(colDefs[curColIdx].kind.compareTo("attribute") == 0)
     {
      curAttClass = colDefs[curColIdx].attClass;
      curAttType = colDefs[curColIdx].attType;
      
      if(colDefs[curColIdx].attSubtype != null)
       curAttType += "(" + colDefs[curColIdx].attSubtype + ")";
      
      curAttValue = new Value();
      curAttValue.value = strArr[curColIdx];
      
      HashSet<Value> valueSet = (HashSet<Value>) addToHashMap(curAttClass, curAttType, nodes.get(curNodeType).get(curNodeValue), new HashSet());
      
      valueSet.add(curAttValue);
     }
     else if(colDefs[curColIdx].kind.compareTo("unit") == 0)
     {
      curAttValue.unitValue = strArr[curColIdx];
      curAttValue.unitType = colDefs[curColIdx].unitType;
     }
     else if(colDefs[curColIdx].kind.compareTo("tsr") == 0)
     {
      if(colDefs[colDefs[curColIdx].linkedColumnNumber].kind.compareTo("unit") == 0)
       curAttValue.unitTSR = strArr[curColIdx];
      else
       curAttValue.valueTSR = strArr[curColIdx];
     }
     else if(colDefs[curColIdx].kind.compareTo("comment") == 0)
     {
      if(curAttValue.comments == null)
       curAttValue.comments = new HashMap<String, String>();
      curAttValue.comments.put(colDefs[curColIdx].commentType, strArr[curColIdx]);
     }
    }
   }
  }
  in.close();

  //  display(nodes, 0);
//  System.out.println("-----------------");
//  display(edges, 0);
//  display(nodes.get("Source Name"), 0);
  
 }

 String generateUniqueName(String name)
 {
  int val = 1;
  if(!counters.containsKey(name))
   counters.put(name, new Integer(2));
  else
  {
   val = counters.get(name);
   counters.put(name, new Integer(val + 1));
  }
  return name + ":" + val;
 }

 static Object addToHashMap(String key, String value, HashMap head, Object tail)
 {
  HashMap target;
  if(head.containsKey(key))
   target = (HashMap)head.get(key);
  else
  {
   target = new HashMap<String,Object>();
   head.put(key, target);
  }
  if(!target.containsKey(value))
  {
   target.put(value, tail);
   return tail;
  }
  else
   return target.get(value);
 }


 static void display(Object nodes, int gap)
 {
  if(nodes instanceof HashMap)
  {
   Iterator it = ((HashMap) nodes).keySet().iterator();
   while(it.hasNext())
   {
    Object key = it.next();
    for(int i = 0; i < gap; i++)
     System.out.print(" ");
    System.out.println(key);
    display(((HashMap) nodes).get(key), gap + 5);
   }
  }
  else if(nodes instanceof HashSet)
  {
   Iterator it = ((HashSet) nodes).iterator();
   while(it.hasNext())
   {
    Value item = (Value) (it.next());
    for(int i = 0; i < gap; i++)
     System.out.print(" ");
    System.out.println("value:" + item.value + " valueTSR:" + item.valueTSR + " unitType:" + item.unitType + " unitValue:" + item.unitValue + " unitTSR:"
      + item.unitTSR + " comments:" + item.comments + " hash:" + item.hashCode());
   }
  }
  else
  {
   Iterator it = ((Vector) nodes).iterator();
   while(it.hasNext())
   {
    String item = (String) (it.next());
    for(int i = 0; i < gap; i++)
     System.out.print(" ");
    System.out.println(item);
   }
  }
 }
}
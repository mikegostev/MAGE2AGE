package uk.ac.ebi.biosd.coriell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoriellStat
{
 static final int EG_SAMPLES_COLL = Integer.MAX_VALUE;
 static final int EG_SAMPLES_ATTR = 5;

 /**
  * @param args
  * @throws IOException 
  */
 public static void main(String[] args) throws IOException
 {
  File cacheDir = new File(args[0]);
  File coriellFile = new File(cacheDir, ".coriell");
  Coriell coriell = null;

  Map<String,Collection> collections = new HashMap<String,Collection>();
  AttrStat stat = new AttrStat();
  stat.setName("Samples");
  
  if(!coriellFile.exists())
   return;

  try
  {
   FileInputStream finp = new FileInputStream(coriellFile);
   ObjectInputStream ois = new ObjectInputStream(finp);

   coriell = (Coriell) ois.readObject();

   ois.close();
   finp.close();
  }
  catch(Exception e)
  {
   e.printStackTrace();
   return;
  }

  System.out.println("Samples: "+coriell.getSamples().size());
  System.out.println("Panels: "+coriell.getPanels().size());
  System.out.println("Families: "+coriell.getFamilies().size());
  
  try
  {
   FileWriter fw = new FileWriter( new File(cacheDir,"sample.ids") );
   for( Sample s : coriell.getSamples().values() )
    fw.write(s.getId()+"\n");
   
   fw.close();
  }
  catch(IOException e)
  {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
  
  for( Sample s : coriell.getSamples().values() )
  {
   Collection c = null;
   stat.getCounter().inc();
   
   List<Value> collVal = s.getAttribites().get("Collection");
   String collName = collVal.get(0).toString();
   
   c = collections.get(collName);
   
   if( c == null )
   {
    c=new Collection();
    c.setName(collName);
    collections.put(collName, c);
   }
   
   c.getCounter().inc();

   if( c.getSamples().size() < EG_SAMPLES_COLL )
    c.addSample(s.getId());

   
  
   for( Map.Entry<String, List<Value>> me : s.getAttribites().entrySet() )
   {
    String attrName = me.getKey();
    
    AttrStat atst = stat.getSubStat(attrName);
    atst.getCounter().inc();
    
    if( atst.getSamples().size() < EG_SAMPLES_ATTR )
     atst.addSample(s.getId());
    
    String nValTag = "Values: "+me.getValue().size();
    AttrStat nvStat = atst.getSubStat(nValTag);
    nvStat.getCounter().inc();
    
    if( nvStat.getSamples().size() < EG_SAMPLES_ATTR )
     nvStat.addSample(s.getId());
   
    int nLinks = 0;
    
    for( Value v : me.getValue() )
     if( v.getLinks() != null && v.getLinks().size() > nLinks )
      nLinks = v.getLinks().size();
   
    if( nLinks > 0 )
    {
     String nLnkTag = "Links: " + nLinks;
     AttrStat nLnkStat = nvStat.getSubStat(nLnkTag);
     nLnkStat.getCounter().inc();

     if(nLnkStat.getSamples().size() < EG_SAMPLES_ATTR)
      nLnkStat.addSample(s.getId());
    }
    
    if( "Subcollection".equals(attrName) )
    {
     String scName = me.getValue().get(0).toString();
     
     Collection sc = c.getSubCollection(scName);
     
     if( sc == null )
     {
      sc  = new Collection();
      sc.setName(scName);
      c.addSubcollection(sc);
     }
     
     sc.getCounter().inc();

     if( sc.getSamples().size() < 5 )
      sc.addSample(s.getId());
    }
   }
  }
  
  System.out.println("Collections: "+collections.size());
  
  FileWriter collStatFile = new FileWriter( new File( cacheDir, ".collections") );
  
  for( Map.Entry<String, Collection> me : collections.entrySet() )
  {
   Collection coll = me.getValue();
   
   System.out.println("  "+coll.getName()+" ("+coll.getCounter()+") "); //+coll.getSamples()
  
   if( coll.getSubCollections() != null )
   {
    for( Collection sc : coll.getSubCollections().values() )
     System.out.println("     "+sc.getName()+" ("+sc.getCounter()+") "); //+sc.getSamples()
   }
   
   collStatFile.write(me.getKey()+"\n");
   
   Object[] sIDs = me.getValue().getSamples().toArray();
   Arrays.sort(sIDs);
   
   for( Object sid : sIDs )
    collStatFile.write(" "+sid+"\n");
  }
  
  collStatFile.close();
  
  System.out.println("Attributes: ");

  printStat( stat, 0 );
  
//  out.println();
//  for( AttrStat sst : stat.getSubStats() )
//   out.println(sst.getName());
 }
 
 private static void printStat( AttrStat st, int deep )
 {
  for(int i = 0; i < deep; i++)
   System.out.print("  ");

  System.out.println(st.getName()+" ("+st.getCounter()+") "+st.getSamples());
  
  if( st.getSubStats() != null )
  {
   for( AttrStat sst : st.getSubStats() )
    printStat(sst, deep+1);
  }
  
 }
}

package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pri.util.Counter;

public class Collection implements Serializable
{
 private String                       name;
 private Map<String,Collection>             subCollections;
 private Set<String> samples = new HashSet<String>();
 private Counter                      counter = new Counter();

 public String getName()
 {
  return name;
 }

 public void setName(String name)
 {
  this.name = name;
 }

 public Map<String,Collection> getSubCollections()
 {
  return subCollections;
 }

 public Collection getSubCollection( String name )
 {
  if( subCollections == null )
   return null;
  
  return subCollections.get(name);
 }

 public java.util.Collection<String> getSamples()
 {
  return samples;
 }

 public Counter getCounter()
 {
  return counter;
 }
 
 public void addSubcollection( Collection c )
 {
  if( subCollections == null )
   subCollections = new HashMap<String,Collection>();
  
  subCollections.put(c.getName(),c);
 }
 
 public void addSample( String sId )
 {
  samples.add(sId);
 }
}

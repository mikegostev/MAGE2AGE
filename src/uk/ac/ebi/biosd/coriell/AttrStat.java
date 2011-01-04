package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pri.util.Counter;

public class AttrStat implements Serializable
{
 private String                       name;
 private Set<String> samples = new HashSet<String>();
 private Counter                      counter = new Counter();
 private Map<String,AttrStat> subStat = null;

 public String getName()
 {
  return name;
 }

 public void setName(String name)
 {
  this.name = name;
 }


 public java.util.Collection<String> getSamples()
 {
  return samples;
 }

 public Counter getCounter()
 {
  return counter;
 }
 
 public void addSample( String sId )
 {
  samples.add(sId);
 }

 public void addSubStat( AttrStat stt )
 {
  if( subStat == null )
   subStat = new HashMap<String, AttrStat>();
  
  subStat.put(stt.getName(), stt);
 }

 public AttrStat getSubStat( String name )
 {
  if( subStat == null )
   subStat = new HashMap<String, AttrStat>();
  
  AttrStat stt = subStat.get(name);
  
  if( stt == null )
  {
   stt = new AttrStat();
   stt.setName(name);
  
   subStat.put(name, stt);
  }
  
  return stt;
 }

 public java.util.Collection<AttrStat> getSubStats()
 {
  if( subStat == null)
   return null;
 
  return subStat.values();
 }
 
}

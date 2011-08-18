package uk.ac.ebi.sampletab;

import java.util.Collection;

public class DerivedFromRelationExtractor implements ValueExtractor
{
 private int order;
 private Collection<String> blacklist;
 private Sample obj;
 private boolean delivered;

 public DerivedFromRelationExtractor( Collection<String> bl, int ord )
 {
  order = ord;
  blacklist = bl;
 }
 
 @Override
 public void setSample(Sample sample)
 {
  obj = sample;
  delivered=false;
 }

 @Override
 public String extract()
 {
  if( delivered )
   return "";
  
  delivered = true;

  int i=0;
  
  for( Sample dfs : obj.getDeriverFromSamples() )
  {
   if( blacklist != null && blacklist.contains(dfs.getValue()) )
    continue;
   
   if( i == order )
    return dfs.getValue();
   
   i++;
  }
  
  return "";

 }

 @Override
 public boolean hasValue()
 {
  return ! delivered;
 }
}

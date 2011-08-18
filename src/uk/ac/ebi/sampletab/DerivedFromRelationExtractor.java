package uk.ac.ebi.sampletab;

import java.util.Collection;

public class DerivedFromRelationExtractor implements ValueExtractor
{
 private int order;
 private Collection<String> blacklist;

 public DerivedFromRelationExtractor( Collection<String> bl, int ord )
 {
  order = ord;
  blacklist = bl;
 }
 
 @Override
 public String extract(Sample samp)
 {
  int i=0;
  
  for( Sample dfs : samp.getDeriverFromSamples() )
  {
   if( blacklist != null && blacklist.contains(dfs.getValue()) )
    continue;
   
   if( i == order )
    return dfs.getValue();
   
   i++;
  }
  
  return "";
 }

}

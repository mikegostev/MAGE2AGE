package uk.ac.ebi.sampletab;

import java.util.Collection;

public class GroupRelationExtractor implements ValueExtractor
{
 private int order;
 private Collection<String> blacklist;

 public GroupRelationExtractor( Collection<String> bl, int ord )
 {
  order = ord;
  blacklist = bl;
 }
 
 @Override
 public String extract(Sample samp)
 {
  int i=0;
  
  for( Group g : samp.getGroups() )
  {
   if( blacklist != null && blacklist.contains(g.getValue()) )
    continue;
   
   if( i == order )
    return g.getValue();
   
   i++;
  }
  
  return "";
 }

}

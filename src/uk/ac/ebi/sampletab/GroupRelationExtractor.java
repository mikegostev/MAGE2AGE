package uk.ac.ebi.sampletab;

import java.util.Collection;

public class GroupRelationExtractor implements ValueExtractor
{
 private int order;
 private Collection<String> blacklist;
 private Sample obj;
 private boolean delivered;

 public GroupRelationExtractor( Collection<String> bl, int ord )
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
  
  for( Group g : obj.getGroups() )
  {
   if( blacklist != null && blacklist.contains(g.getValue()) )
    continue;
   
   if( i == order )
    return g.getValue();
   
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

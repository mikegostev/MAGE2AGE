package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.List;


public class Sample extends AnnotatedObject
{
 private int block;

 private List<Sample> derivatives;
 private List<Sample> derivedFrom;
 private List<Group> groups;
 
 
 public Sample addDerivative(Sample sample)
 {
  if( derivatives == null )
  {
   derivatives = new ArrayList<Sample>();
   
   derivatives.add(sample);
   
   return sample;
  }
  
  for( Sample ds : derivatives )
   if( ds.getValue().equals(sample.getValue()) )
    return ds;
  
  derivatives.add(sample);
  
  return sample;
 }

 public Sample addDerivedFrom(Sample sample)
 {
  if( derivedFrom == null )
  {
   derivedFrom = new ArrayList<Sample>();
   
   derivedFrom.add(sample);
   
   return sample;
  }
  
  for( Sample ds : derivedFrom )
   if( ds.getValue().equals(sample.getValue()) )
    return ds;
  
  derivedFrom.add(sample);
  
  return sample;
 }

 public Group addGroup(Group group)
 {
  if( groups == null )
  {
   groups = new ArrayList<Group>();
   
   groups.add(group);
   
   return group;
  }
  
  for( Group ds : groups )
   if( ds.getValue().equals(group.getValue()) )
    return ds;
  
  groups.add(group);
  
  return group;
  
 }

 public int getBlock()
 {
  return block;
 }

 public void setBlock(int block)
 {
  this.block = block;
 }

}

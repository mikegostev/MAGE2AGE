package uk.ac.ebi.sampletab;

import java.util.List;

public class SimpleAttributeExtractor implements ValueExtractor
{
 private String name;
 private int order;
 private Attribute obj;
 private boolean delivered;
 
 public SimpleAttributeExtractor( String name, int ord )
 {
  this.name=name;
  order = ord;
 }

 @Override
 public void setSample(Sample obj)
 {
  this.obj = obj.getAnnotation(name);
  delivered=false;
 }

 @Override
 public String extract()
 {
  if( delivered || obj == null )
   return "";
  
  delivered = true;

  List<Attribute> vals = obj.getValues();
  
  if( vals.size() <= order )
   return "";
  
  return vals.get(order).getValue();
 }

 @Override
 public boolean hasValue()
 {
  return ! delivered;
 }

}

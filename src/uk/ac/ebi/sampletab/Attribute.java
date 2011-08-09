package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.List;

public class Attribute extends AnnotatedObject
{
 private String name; 
 private List<String> vals;
 
 public Attribute( String name, String val )
 {
  setValue(val);
  
  this.name = name;
 }
 
 public void addValue( String v )
 {
  if( vals == null )
  {
   vals = new ArrayList<String>();
   
   vals.add(super.getValue());
  }
  
  vals.add(v);
 }
 
 public String getValue()
 {
  if( vals == null )
   return super.getValue();
  
  return vals.get(0);
 }
 
 public int getValueNumber()
 {
  return vals == null? 1: vals.size();
 }

 public String getName()
 {
  return name;
 }

 public void setName(String name)
 {
  this.name = name;
 }
}


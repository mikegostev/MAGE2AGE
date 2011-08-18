package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Attribute extends AnnotatedObject
{
 private String name; 
 private List<Attribute> vals;
 private int order;
 
 public Attribute( String name, String val, int ord )
 {
  setValue(val);
  
  this.name = name;
  order=ord;
 }
 
 public Attribute addValue( String v, int ord )
 {
  if( vals == null )
  {
   vals = new ArrayList<Attribute>();
   
   vals.add(this);
  }
  
  Attribute nattr = new Attribute(getName(), v, ord);
  
  vals.add(nattr);
  
  return nattr;
 }
 
 public String getValue()
 {
  if( vals == null )
   return super.getValue();
  
  return vals.get(0).getValue();
 }
 
 public List<Attribute> getValues()
 {
  if( vals != null )
   return vals;
   
  return vals=Collections.singletonList( this );
 }
 
 public int getValuesNumber()
 {
  return vals == null? 1: vals.size();
 }

 public String getName()
 {
  return name;
 }

// public void setName(String name)
// {
//  this.name = name;
// }
 
 public String toString()
 {
  if( vals == null )
   return getValue();
  
  StringBuilder sb = new StringBuilder();
  
  sb.append('[');
  
  for( Attribute a : vals)
   sb.append(a.getValue()).append(',');

  sb.setCharAt(sb.length()-1, ']');
  
  return sb.toString();
 }

 public int getOrder()
 {
  return order;
 }

 public void setOrder(int order)
 {
  this.order = order;
 }
 
 public boolean equals( Object o )
 {
  Attribute othat = (Attribute) o;
  
  if( getValuesNumber() != othat.getValuesNumber() )
   return false;
  
  if( getValuesNumber() == 1 )
  {
   if( ! getValue().equals( othat.getValue() ) )
    return false;
   
   if( getAnnotations() != null )
   {
    if( othat.getAnnotations() == null || getAnnotations().size() != othat.getAnnotations().size() )
     return false;
    
    for( Attribute q : getAnnotations() )
    {
     Attribute othq = othat.getAnnotation( q.getName() );
     
     if( ! q.equals(othq) )
      return false;
    }
   }
   
   return true;
  }
  
  List<Attribute> myVals = getValues();
  List<Attribute> othVals = getValues();
  
  for( int i=0; i < myVals.size(); i++ )
   if( ! myVals.get(i).equals( othVals.get(i)) )
    return false;
  
  return true;
 }
}


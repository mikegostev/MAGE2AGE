package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.List;

public class QualifiedAttributeExtractor implements ValueExtractor
{
 protected String name;
 protected Attribute obj;
 protected List<QualifierExtractor> qualifierExtractors = new ArrayList<QualifierExtractor>();
 protected int pos;
 protected boolean delivered;

 public QualifiedAttributeExtractor( String name )
 {
  this.name = name;
 }

 public void addQualifierExtractor( QualifierExtractor ex )
 {
  qualifierExtractors.add(ex);
 }
 
 @Override
 public void setSample(Sample sample)
 {
  obj = sample.getAnnotation(name);

  pos = 0 ;
  delivered = false;
  
  for( QualifierExtractor ve : qualifierExtractors )
   ve.setAttribute(obj);
 }

 @Override
 public String extract()
 {
  if( obj == null || pos >= obj.getValues().size() )
   return "";

  if( ! delivered )
  {
   delivered = true;
   return obj.getValues().get(pos).getValue();
  }
  
  for( ValueExtractor ve : qualifierExtractors )
   if( ve.hasValue() )
    return "";
  
  pos++;  
    
  if( pos >= obj.getValues().size() )
   return "";
  
  Attribute nAttr = obj.getValues().get(pos);
  
  for( QualifierExtractor ve : qualifierExtractors )
   ve.setAttribute(nAttr);
   
  return nAttr.getValue();
 }

 @Override
 public boolean hasValue()
 {
//  if( ! delivered || pos < ( obj.getValues().size() - 1 ) )
//   return true;
  
  if( obj == null || pos >= obj.getValues().size() )
   return false;
  
  return true;
 }

}

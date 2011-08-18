package uk.ac.ebi.sampletab;

import java.util.List;

public class AttributeValueExtractor implements ValueExtractor
{
 private List<String> path;
 private int order;

 public AttributeValueExtractor( List<String> pth, int ord )
 {
  path = pth;
 }
 
 @Override
 public String extract( Sample samp )
 {
  AnnotatedObject obj = samp;
  
  Attribute attr=null;
  
  for( String pel : path )
  {
   attr = obj.getAnnotation(pel);
   
   if( attr == null )
    return "";
   
   obj=attr;
  }
  
  if( order == 0 )
   return attr.getValue();
  
  return attr.getValues().get(order).getValue();
 }
}

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
 public String extract( )
 {
  ord+=order;
  
  AnnotatedObject obj = samp;
  
  Attribute attr=null;
  
  for( String pel : path )
  {
   attr = obj.getAnnotation(pel);
   
   if( attr == null )
    return null;
   
   obj=attr;
  }
  
  if( ord == 0 )
   return attr.getValue();
  
  return attr.getValues().get(ord).getValue();
 }
}

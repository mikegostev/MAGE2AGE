package uk.ac.ebi.sampletab;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnnotatedObject
{
 private String value;

 Map<String,AnnotatedObject> annotations = null;

 public String getValue()
 {
  return value;
 }

 public void setValue(String accession)
 {
  this.value = accession;
 }
 
 void addAnnotation( String name, AnnotatedObject value )
 {
  if( annotations == null )
   annotations = new LinkedHashMap<String, AnnotatedObject>();
  
  annotations.put(name, value);
 }
 
 public AnnotatedObject getAnnotation( String key )
 {
  if( annotations == null )
   return null;
  
  return annotations.get(key);
 }
 
 public Collection<AnnotatedObject> getAnnotations()
 {
  if( annotations != null )
   return annotations.values();
  
  return null;
 }
}

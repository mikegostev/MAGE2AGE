package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContainerObject extends AnnotatedObject
{
 private Map<String, List<WellDefinedObject>> attachedObjects = new HashMap<String, List<WellDefinedObject>>();
 
 public List<WellDefinedObject> getAttachedObjects(String s)
 {
  List<WellDefinedObject> list = attachedObjects.get(s);
  
  if( list == null )
   attachedObjects.put(s, list = new ArrayList<WellDefinedObject>() );
  
  return list;
 }
 
 public Collection<String> getAttachedClasses()
 {
  return attachedObjects.keySet();
 }
}

package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Attributed implements Serializable
{
 private Map<String, List<Value> > attrs = new HashMap<String, List<Value> >();
 
 
 public void addAttibute(String name, Value value)
 {
  List<Value> vals = attrs.get(name);
  
  if( vals == null )
  {
   vals = new ArrayList<Value>(5);
   attrs.put(name, vals);
  }
  
  vals.add( value );
 }


 public Map<String, List<Value> > getAttribites()
 {
  return attrs;
 }

}

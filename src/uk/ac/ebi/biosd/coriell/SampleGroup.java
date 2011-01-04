package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SampleGroup implements ObjGroup, Serializable
{
 private List<String> samples = new ArrayList<String>(20);
 
 /* (non-Javadoc)
  * @see uk.ac.ebi.biosd.coriell.ObjGroup#addSample(java.lang.String)
  */
 @Override
 public void addSample( String s )
 {
  samples.add(s);
 }

 /* (non-Javadoc)
  * @see uk.ac.ebi.biosd.coriell.ObjGroup#getSamples()
  */
 @Override
 public List<String> getSamples()
 {
  return samples;
 }
}

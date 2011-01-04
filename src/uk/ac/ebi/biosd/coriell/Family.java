package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;

public class Family extends SampleGroup implements Serializable
{
 private String id;

 public String getId()
 {
  return id;
 }

 public void setId(String id)
 {
  this.id = id;
 }

}

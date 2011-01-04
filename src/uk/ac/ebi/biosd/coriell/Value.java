package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Value implements Serializable
{
 private static final long serialVersionUID = 1L;

 private String val;
 private List<Link> links;

 public Value()
 {}

 public Value( String v )
 {
  val=v;
 }
 
 public String toString()
 {
  return val;
 }

 public List<Link> getLinks()
 {
  return links;
 }

 public void addLink( Link l )
 {
  if( links == null )
   links = new ArrayList<Link>(3);
 }
 
 public void setValue(String val)
 {
  this.val = val;
 }

 public void setLinks(List<Link> lnks )
 {
  links=lnks;
 }
}

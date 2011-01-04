package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExternalLink implements Serializable
{
 private String name;
 private List<Link> links = new ArrayList<Link>(4);
 
 public String getName()
 {
  return name;
 }
 
 public void setName(String name)
 {
  this.name = name;
 }
 
 public List<Link> getLinks()
 {
  return links;
 }
 
 public void addLink( Link l )
 {
  links.add(l);
 }
 
 public void addLink( String title, String url )
 {
  links.add( new Link(title,url) );
 }

 public String toString()
 {
  StringBuilder sb = new StringBuilder(200);
  
  sb.append(name).append(":\n");
  
  for( Link l : links )
   sb.append("\t").append(l).append("\n");
  
  return sb.toString();
 }
}

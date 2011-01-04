package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sample extends Attributed implements Serializable
{
 private List<Publication> publications = new ArrayList<Publication>(10);
 private List<ExternalLink> extLinks;
 private String id;


 public void addPublication(Publication cPub)
 {
  publications.add(cPub);
 }

 public void addExternalLink(ExternalLink cLnk)
 {
  if( extLinks == null )
   extLinks = new ArrayList<ExternalLink>(10);
  
  extLinks.add( cLnk);
 }

 public List<Publication> getPublications()
 {
  return publications;
 }

 public List<ExternalLink> getExtLinks()
 {
  return extLinks;
 }

 public String getId()
 {
  return id;
 }

 public void setId(String id)
 {
  this.id = id;
 }

}

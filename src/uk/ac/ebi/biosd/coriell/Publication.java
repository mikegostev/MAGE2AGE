package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;

public class Publication implements Serializable
{
 String title;
 String pubMed;

 public Publication()
 {}
 
 public Publication(String title, String pubMed)
 {
  super();
  this.title = title;
  this.pubMed = pubMed;
 }

 public String getTitle()
 {
  return title;
 }

 public void setTitle(String title)
 {
  this.title = title;
 }

 public String getPubMed()
 {
  return pubMed;
 }

 public void setPubMed(String pubMed)
 {
  this.pubMed = pubMed;
 }

 
}

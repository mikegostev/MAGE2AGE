package uk.ac.ebi.biosd.coriell;

import java.io.Serializable;

public class Link implements Serializable
{
 private String title;
 private String url;

 public Link()
 {}
 
 public Link(String title2, String url2)
 {
  title=title2;
  url=url2;
 }

 public String getTitle()
 {
  return title;
 }

 public void setTitle(String title)
 {
  this.title = title;
 }

 public String getUrl()
 {
  return url;
 }

 public void setUrl(String url)
 {
  this.url = url;
 }
 
 public String toString()
 {
  return url+" ("+title+")";
 }
}

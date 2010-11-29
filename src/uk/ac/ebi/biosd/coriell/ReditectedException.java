package uk.ac.ebi.biosd.coriell;

import java.net.URL;

public class ReditectedException extends Exception
{
 private URL redirURL;
 
 public ReditectedException( URL u )
 {
  redirURL = u;
 }
 
 public URL getURL()
 {
  return redirURL;
 }
}

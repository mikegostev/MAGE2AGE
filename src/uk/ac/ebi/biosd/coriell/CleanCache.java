package uk.ac.ebi.biosd.coriell;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CleanCache
{
 static Matcher match = Pattern.compile("Panel_FPDetail|Sample_FPDetail|FamilyTypeSubDetail|^\\.").matcher("");
 /**
  * @param args
  */
 public static void main(String[] args)
 {
  File cacheDir = new File(args[0]);

  process(cacheDir);
 }

 static void process( File f )
 {
  if( ! f.isDirectory() )
  {
   match.reset(f.getName());
   
   if( ! match.find() )
   {
    System.out.println("Deleting: "+f.getName());
    f.delete();
   }
  }
  else
  {
   for( File sd : f.listFiles() )
    process(sd);
  }
 }
}

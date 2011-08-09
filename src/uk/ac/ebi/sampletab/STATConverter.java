package uk.ac.ebi.sampletab;

import java.io.File;
import java.io.PrintStream;

import uk.ac.ebi.sampletab.STParser.Submission;

public class STATConverter
{

 /**
  * @param args
  */
 public static void main(String[] args)
 {
  File infile = new File("c:\\temp\\sample.sampletab.txt");
  File outfile = new File("c:\\temp\\sample.age.txt");
 
  
  try
  {
   PrintStream out = new PrintStream(outfile);

   Submission sub = STParser.readST(infile);
  
   System.out.println(sub.getAttachedObjects(STParser.TERMSOURCE).size());
   
   out.close();
  }
  catch(Exception e)
  {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }

 }

}

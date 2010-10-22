package uk.ac.ebi.esd.magetab;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.mged.magetab.error.ErrorCode;
import org.mged.magetab.error.ErrorItem;
import org.mged.magetab.error.ErrorItemFactory;

import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SDRFNode;
import uk.ac.ebi.arrayexpress2.magetab.exception.ErrorItemListener;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.magetab.exception.ValidateException;
import uk.ac.ebi.arrayexpress2.magetab.handler.ParserMode;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
import uk.ac.ebi.arrayexpress2.magetab.validator.AbstractValidator;
import uk.ac.ebi.arrayexpress2.magetab.validator.Validator;

public class Limpopo
{

 /**
  * @param args
  */
 public static void main(String[] args)
 {
  try
  {
   MAGETABParser parser = new MAGETABParser(ParserMode.READ_ONLY);

   // add an error item listener to the parser
   parser.addErrorItemListener(new ErrorItemListener()
   {

    public void errorOccurred(ErrorItem item)
    {
     // locate the error code from the enum, to check the generic message
     ErrorCode code = null;
     for(ErrorCode ec : ErrorCode.values())
     {
      if(item.getErrorCode() == ec.getIntegerValue())
      {
       code = ec;
       break;
      }
     }

     if(code != null)
     {
      // this just dumps out some info about the type of error
      System.out.println("Listener reported error...");
      System.out.println("\tError Code: " + item.getErrorCode() + " [" + code.getErrorMessage() + "]");
      System.out.println("\tError message: " + item.getMesg());
      System.out.println("\tCaller: " + item.getCaller());
     }
    }
   });

   // make a new validator
   Validator<MAGETABInvestigation> validator = new AbstractValidator<MAGETABInvestigation>()
   {

    public boolean validate(MAGETABInvestigation validatorSource) throws ValidateException
    {
     // this doesn't really do any validation, just generates one error
     // item then throws an exception to indicate it failed
     ErrorItem item = ErrorItemFactory.getErrorItemFactory().generateErrorItem("Not really doing any validation", 1, this.getClass());

     fireErrorItemEvent(item);
     throw new ValidateException(item, true, "Validation failed");
    }
   };

   // set our dummy validator on the parser
//   parser.setValidator(validator);

   // now, parse from a file
   File idfFile = new File("/home/mike/ESD/AE/AE-EXP/E-MEXP-242/E-MEXP-242.idf.txt");

   // do parse
   System.out.println("Parsing " + idfFile.getAbsolutePath() + "...");

   // need to get the url of this file, as the parser only takes urls
   MAGETABInvestigation investigation = parser.parse(idfFile.toURI().toURL());

//   System.out.println( investigation.SDRF.lookupRootNodes() );
   
   for( SDRFNode node : investigation.SDRF.lookupRootNodes() )
   {
    System.out.println( node.getNodeName() + " <-> " + Arrays.asList( node.headers() ) );
   }
   
   // print out the parsed investigation
//   System.out.println(investigation);
  }
  catch(ParseException e)
  {
   // This happens if parsing failed.
   // Any errors here will also have been reported by the listener
   e.printStackTrace();
  }
  catch(MalformedURLException e)
  {
   // This is if the url from the file is bad
   e.printStackTrace();
  }
 }

}

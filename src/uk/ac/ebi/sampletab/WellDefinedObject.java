package uk.ac.ebi.sampletab;

public class WellDefinedObject extends AnnotatedObject
{
 private String className;
 
 public WellDefinedObject( String cls ) 
 {
  className = cls;
  
  if( ! Names.object2Properties.containsKey(cls) )
   throw new STParseException("Invalid class name");
 }
}

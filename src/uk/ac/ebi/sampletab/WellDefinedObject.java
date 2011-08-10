package uk.ac.ebi.sampletab;


public class WellDefinedObject extends AnnotatedObject
{
 private String className;
// private Map<String, Attribute>
 
 public WellDefinedObject( String cls ) 
 {
  className = cls;
  
  if( ! Names.object2Properties.containsKey(cls) )
   throw new STParseException("Invalid class name");
 }
 
 public void setAttribute( Attribute attr )
 {
  if( ! className.equals( Names.propertyToObject.get(attr.getName()) ) )
   throw new STParseException("Invalid property '"+attr.getName()+"' for class '"+className+"'");

  super.addAnnotation(attr.getName(), attr);
 }
 
 public Attribute getAttribute( String atName )
 {
  return (Attribute)super.getAnnotation(atName);
 }
}

package uk.ac.ebi.sampletab;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pri.util.SpreadsheetReader;
import com.pri.util.stream.StreamPump;


public class STParser2
{
 
 
 public static Submission readST( File modFile ) throws IOException
 {
  ByteArrayOutputStream bais = new ByteArrayOutputStream();

  FileInputStream fis = new FileInputStream(modFile);
  StreamPump.doPump(fis, bais, false);
  fis.close();

  bais.close();

  byte[] barr = bais.toByteArray();
  String enc = "UTF-8";

  if(barr.length >= 2 && (barr[0] == -1 && barr[1] == -2) || (barr[0] == -2 && barr[1] == -1))
   enc = "UTF-16";

  String text = new String(bais.toByteArray(), enc);
  
  Submission sub = new Submission();
  
  SpreadsheetReader reader = new SpreadsheetReader( text );
  
  List<String> parts = new ArrayList<String>(100);
  
  boolean sampleSection = false;
  List<String> headerLine = null; 
  
  while( reader.readLine(parts) != null )
  {
   int emp=0;
   for( int k=parts.size()-1; k>=0 ; k-- )
    if( parts.get(k).trim().length() == 0 )
     emp++;
    else
     break;
    
   if( emp == parts.size() )
    continue;
   
   parts = parts.subList(0, parts.size()-emp);
   
   String p0 = parts.get(0).trim();
   
   if( p0.length() == 0 || p0.startsWith("#") || p0.equals("[MSI]") )
    continue;
   
   if( p0.equals("[SCD]") )
   {
    sampleSection = true;
    continue;
   }
   
   if( ! sampleSection )
   {
    if(! Names.propertyToObject.containsKey(p0))
     throw new STParseException("Unknown tag: '" + p0 + "' Line: " + reader.getLineNumber());

    String objName = Names.propertyToObject.get(p0);

    if(objName.equals(Names.SUBMISSION))
    {
     if(parts.size() != 2)
      throw new STParseException("Invalid number of values for tag: '" + p0 + "' Expected: 1");

     sub.addAnnotation(p0, new Attribute(p0, parts.get(1)) );
    }
    else
    {
     List<AnnotatedObject> objs = sub.getAttachedObjects(objName);
     
     if( objs.size() < parts.size()-1 )
      for( int k=0; k < parts.size()-1-objs.size(); k++ )
       objs.add(null);
     
     
     for( int i=0; i < parts.size()-1; i++ )
     {
      AnnotatedObject a = objs.get(i);
      
      if( a == null )
       objs.set(i, a=new AnnotatedObject());
      
      String nm = p0.substring(objName.length()+1);
      
      a.addAnnotation(nm, new Attribute(nm, parts.get(i+1) ) );
     }
    } 
   }
   else
   {
    if( headerLine == null )
    {
     headerLine = new ArrayList<String>( parts.size() );
     
     for( String p : parts )
      headerLine.add( p.trim() );
     
     if( ! headerLine.get(0).equals(SAMPLENAME) )
      throw new STParseException("The first column should be "+SAMPLENAME+" Line: "+reader.getLineNumber());
    }
    else
    {
     if( parts.size() > headerLine.size() )
      throw new STParseException("Some values are beyond the annotation. Line: "+reader.getLineNumber());
     
     Sample lastSample = null;
     Sample sample = null;
     Group group = null;
     Attribute attribute = null;
     
     int blockNum=0;
     
     int runlen = parts.size();
    
     for( int i=0; i < runlen; i++ )
     {
      String hdr = headerLine.get(i);
      
      if( SAMPLENAME.equals(hdr) )
      {
       blockNum++;
       
       
       if( group != null )
       {
        if( group.getValue() == null )
         throw new STParseException("Group has no accession. Line: "+reader.getLineNumber());
        
        group = sub.addGroup( group );
        
        group.addSample(sample);
        sample.addGroup( group );
        
        group = null;
       }
       else if( sample != null )
       {
        if( sample.getValue() == null )
         throw new STParseException("Sample has no accession. Line: "+reader.getLineNumber());

        sample = sub.addSample( sample );
        
        if( lastSample != null )
        {
         lastSample.addDerivative( sample );
         sample.addDerivedFrom( lastSample );
        }
        
        lastSample = sample;
       }
       
       sample = new Sample();
       sample.setBlock(blockNum);
       
       sample.addAnnotation(SAMPLENAME_AGE, attribute = new Attribute(SAMPLENAME_AGE, parts.get(i) ) );
      }
      if( GROUPNAME.equals(hdr) )
      {
       blockNum++;
       
       if( sample != null )
       {
        if( sample.getValue() == null )
         throw new STParseException("Sample has no accession. Line: "+reader.getLineNumber());

        sample = sub.addSample( sample );
        
        if( lastSample != null )
        {
         lastSample.addDerivative( sample );
         sample.addDerivedFrom( lastSample );
        }
        
        lastSample = sample;
       }
       
       group = new Group();
       group.setBlock(blockNum);
       
       group.addAnnotation(GROUPNAME_AGE, attribute = new Attribute(GROUPNAME_AGE, parts.get(i) ) );
      }
      else if( TERMSOURCEREF.equals(hdr) )
      {
       List<AnnotatedObject> tss = sub.getAttachedObjects( TERMSOURCE );
       
       if( tss == null )
        throw new STParseException("No term sources defined. Line: "+reader.getLineNumber() );
       
       String tsRef = parts.get(i).trim();
       
       if( tsRef.length() == 0 )
        continue;
       
       boolean found = false;
       
       for( AnnotatedObject a : tss )
       {
        if( tsRef.equals( a.getAnnotation( "Name" ).getValue() ) )
        {
         found = true;
         break;
        }
       }
       
       if( ! found )
        throw new STParseException("No such term source defined: '"+tsRef+"'. Line: "+reader.getLineNumber() );
       
       attribute.addAnnotation(TERMSOURCEREF, new Attribute(TERMSOURCEREF,tsRef));
      }
      else if( UNIT.equals( hdr ))
      {
       String value = parts.get(i).trim();
       
       if( value.length() > 0 )
        attribute.addAnnotation(UNIT, new Attribute(UNIT,value));
      }
      else if( hdr.endsWith(ACCESSIONSUFFIX) )
      {
       if( group != null )
        group.setValue(parts.get(i).trim());
       else
        sample.setValue(parts.get(i).trim());
      }
      else
      {
       String value = parts.get(i).trim();

       AnnotatedObject host = group != null ? group : sample;

       Attribute attr = (Attribute) host.getAnnotation(hdr);

       if(attr != null)
        attr.addValue(value);
       else
        host.addAnnotation(hdr, new Attribute(hdr, value));
      }
     }
     
     
     if( group != null )
     {
      if( group.getValue() == null )
       throw new STParseException("Group has no accession. Line: "+reader.getLineNumber());
      
      group = sub.addGroup( group );
      
      group.addSample(sample);
      sample.addGroup( group );
     }
     else if( sample != null )
     {
      if( sample.getValue() == null )
       throw new STParseException("Sample has no accession. Line: "+reader.getLineNumber());

      sample = sub.addSample( sample );
      
      if( lastSample != null )
      {
       lastSample.addDerivative( sample );
       sample.addDerivedFrom( lastSample );
      }
     }
     
     
     
    }
    
   }
  }
  
  return sub;
 }
}

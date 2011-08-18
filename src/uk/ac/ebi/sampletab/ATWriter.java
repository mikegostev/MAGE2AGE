package uk.ac.ebi.sampletab;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ATWriter
{
 public static final int MAX_INLINE_REPEATS=3;
 
 public static final char TAB = '\t';
 
 private static Pattern attrPattern = Pattern.compile("^\\s*(\\S+)\\s*\\[\\s*(.+)\\s*\\]\\s*");
 
 public static void writeAgeTab(Submission sub, OutputStream outStream) throws IOException
 {
  PrintStream out = new PrintStream(outStream, false, "UTF-8");

  out.print('|');
  
  out.print( Definitions.SUBMISSION );
  out.print( TAB );
  out.println( sub.getValue() );
  
  for( Attribute attr : sub.getAnnotations() )
  {
   String atName = attr.getName();
   
   if( atName.startsWith(Definitions.SUBMISSION) )
    atName = atName.substring(Definitions.SUBMISSION.length()+1);

   out.print( atName );
   out.print( TAB );
   out.println( attr.getValue() );
  }

  for( String atCl : sub.getAttachedClasses() )
  {
   List<WellDefinedObject> objs = sub.getAttachedObjects(atCl);
   
   if( objs.size() == 0 )
    continue;
   
   out.print(atCl);
   
   for( WellDefinedObject obj : objs )
   {
    out.print( TAB );
    out.print( obj.getValue() );
   }
   
  }

  out.println();

  for( String atCl : sub.getAttachedClasses() )
  {
   List<WellDefinedObject> objs = sub.getAttachedObjects(atCl);
   
   if( objs.size() == 0 )
    continue;
   
   out.println();
   out.print('|');
   out.print( atCl );

   for( WellDefinedObject obj : objs )
   {
    out.print( TAB );
    out.print( obj.getValue() );
   }

   out.println();

   
   for( String fld : Definitions.object2Properties.get(atCl) )
   {
    boolean has = false;
    
    for( WellDefinedObject obj : objs )
    {
     if( obj.getAnnotation(fld) != null )
     {
      has = true;
      break;
     }
    }
    
    if( has )
    {
     if( fld.startsWith(atCl) )
      out.print(fld.substring(atCl.length()+1) );

     for( WellDefinedObject obj : objs )
     {
      out.print( TAB );
      out.print( obj.getAnnotation(fld).getValue() );
     }
     
     out.println();
    }
   }
   
  }
  
  for( Group grp : sub.getGroups() )
  {
   out.println();
   out.print('|');
   out.print(Definitions.GROUP);
   out.print(TAB);
   out.println(grp.getValue());
   
   for( Attribute attr : grp.getAnnotations() )
   {
    String attrName = attr.getName();
    
    if( attrName.startsWith(Definitions.SUBMISSION) )
     attrName = attrName.substring(Definitions.SUBMISSION.length()+1);
    
    out.print(attr.getName());
    
    if( attr.getValuesNumber() == 1 )
    {
     out.print(TAB);
     out.println(attr.getValue());
    }
    else
    {
     for( Attribute valAt : attr.getValues() )
     {
      out.print(TAB);
      out.print(valAt.getValue());
     }
     
     out.println();
    }
   }
   
   for( String atOCls : grp.getAttachedClasses() )
   {
    List<WellDefinedObject> objs = grp.getAttachedObjects( atOCls );
    
    for( WellDefinedObject o : objs )
    {
     out.print(TAB);
     out.print(o.getValue());
    }
    
    out.println();
   }
   
  }
  
  for( List<Sample> sBlock : sub.getSampleBlocks() )
  {
   List<Attribute> protoAttrMap = new LinkedList<Attribute>();
   List<Group> protoGrpMap = new LinkedList<Group>();
   List<Sample> protoDervMap = new LinkedList<Sample>();
   
   Sample s0 = sBlock.get(0);
   
   for( Attribute at : s0.getAnnotations() )
    protoAttrMap.add(at);

   if( s0.getGroups() != null )
   {
    for( Group g : s0.getGroups() )
     protoGrpMap.add(g);
   }
   
   if( s0.getDeriverFromSamples() != null )
   {
    for( Sample s : s0.getDeriverFromSamples() )
     protoDervMap.add( s );
   }

   AttributeInfo sampleAttrInfo = new AttributeInfo( null );
   
   for( int i=1; i < sBlock.size(); i++ )
   {
    Sample s = sBlock.get(i);
    
    collectAttributesInfo(s, sampleAttrInfo);
    
    Iterator<Attribute> attIter = protoAttrMap.iterator();
    
    while( attIter.hasNext() )
    {
     Attribute at = attIter.next();
     Attribute chkat = s.getAnnotation( at.getName() );
     
     if( chkat == null || ! at.equals( chkat ) )
      attIter.remove();
    }
    
    if( s.getGroups() == null )
     protoGrpMap.clear();
    else
    {
     Iterator<Group> grpIter = protoGrpMap.iterator();
     
     while( grpIter.hasNext() )
     {
      Group g = grpIter.next();
      
      boolean found = false;
      
      for( Group sg : s.getGroups() )
      {
       if( sg.getValue().equals(g.getValue()) )
       {
        found=true;
        break;
       }
      }
      
      if( ! found )
       grpIter.remove();
     }
    }
    
    if( s.getDeriverFromSamples() == null )
     protoDervMap.clear();
    else
    {
     Iterator<Sample> drvIter = protoDervMap.iterator();
     
     while( drvIter.hasNext() )
     {
      Sample dfs = drvIter.next();
      
      boolean found = false;
      
      for( Sample sdfs : s.getDeriverFromSamples() )
      {
       if( sdfs.getValue().equals(dfs.getValue()) )
       {
        found=true;
        break;
       }
      }
      
      if( ! found )
       drvIter.remove();
     }
    }

    
    if( protoAttrMap.size() > 0 || protoGrpMap.size() > 0 || protoDervMap.size() > 0 )
    {
     out.println();
     out.print(Definitions.SAMPLE);
     
     int valLines = 1;
     
     for( Attribute at : protoAttrMap )
     {
      String attrName = ageAttributeName(at.getName()) ;
      
      if( at.getValuesNumber() > 1 && at.getValuesNumber() <= MAX_INLINE_REPEATS && at.getAnnotations() == null )
      {
       for(int j=0; j < at.getValuesNumber(); j++)
       {
        out.print(TAB);
        out.print( attrName );
       }
      }
      else
      {
       out.print(TAB);
       out.print(attrName);

       if( at.getAnnotations() == null )
       {
        for( Attribute q : at.getAnnotations() )
        {
         out.print(TAB);
         out.print(attrName+Definitions.QUALIFIERBRACKETS[0]+q.getName()+Definitions.QUALIFIERBRACKETS[1]);
        }
       }
       
       if( at.getValuesNumber() > valLines )
        valLines = at.getValuesNumber();
      }
     }
     
     for( int j=0; j < protoGrpMap.size(); j++ )
     {
      out.print(TAB);
      out.print(Definitions.BELONGSTO);
     }

     for( int j=0; j < protoDervMap.size(); j++ )
     {
      out.print(TAB);
      out.print(Definitions.DERIVEDFROM);
     }
     
     out.println();
     
     for( int j=0; j < valLines; j++ )
     {
      out.print( j==0?Definitions.PROTOTYPEID:"" );
      out.print(TAB);
      
      for( Attribute at : protoAttrMap )
      {
       if( at.getValuesNumber() > 1 && at.getValuesNumber() <= MAX_INLINE_REPEATS && ( at.getAnnotations() == null || at.getAnnotations().size() == 0 ) )
       {
        if( j == 0 )
        {
         for(int k=0; k < at.getValuesNumber(); k++)
         {
          out.print(TAB);
          out.print( at.getValues().get(k) );
         }
        }
       }
       else
       {
        Attribute cVal = j < at.getValuesNumber()?at.getValues().get(j):null;
        
        out.print(TAB);
        out.print( cVal!=null?cVal.getValue():"" );
        
        if( at.getAnnotations() == null )
        {
         for( Attribute q : at.getAnnotations() )
         {
          out.print(TAB);
          
          if( cVal==null )
           out.print("");
          else
          {
           Attribute qVal = cVal.getAnnotation(q.getName());
           out.print( qVal!=null?qVal.getValue():"" );
          }
         }
        }
       }
      }
     }
     
    }
    
   }
  }
  
 }
 
 private static void collectAttributesInfo(AnnotatedObject s, AttributeInfo parentAttrInfo )
 {
  if( s.getAnnotations() == null )
   return;
  
  for( Attribute a : s.getAnnotations() )
  {
   AttributeInfo atInf = parentAttrInfo.getInfo( a.getName() );
   
   if( atInf == null )
   {
    atInf = new AttributeInfo( a.getName() );
    parentAttrInfo.addAttributeInfo( atInf );
   }
   else
   {
    if( a.getValuesNumber() > atInf.getValuesNumber() )
     atInf.setValuesNumber( a.getValuesNumber() );
   }

   collectAttributesInfo(a, atInf);
  }
  
 }

 private static String ageAttributeName( String name )
 {
  Matcher mtch = attrPattern.matcher(name);
  
  if( mtch.matches() )
   return mtch.group(1)+Definitions.CUSTIOMCLASSBRACKETS[0]+mtch.group(2)+Definitions.CUSTIOMCLASSBRACKETS[1];
  
  return Definitions.CUSTIOMCLASSBRACKETS[0]+name+Definitions.CUSTIOMCLASSBRACKETS[1];
 }
 
}

package uk.ac.ebi.biosd.coriell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import com.pri.util.stream.StreamPump;

public class CoriellSampleExtractor
{
 
 private static StringBuilder sb = new StringBuilder(1000);
 /**
  * @param args
  * @throws IOException 
  */
 public static void main(String[] args) throws IOException
 {
//  String url="http://ccr.coriell.org/Sections/Search/Sample_Detail.aspx?Ref=GM19139&PgId=166";
  String url="http://ccr.coriell.org/Sections/BrowseCatalog/FamilyTypeSubDetail.aspx?fam=Y024&PgId=402";
  
  URL smpUrl = new URL( url );
  
  HttpURLConnection conn = (HttpURLConnection)smpUrl.openConnection();
  
  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  StreamPump.doPump(conn.getInputStream(), baos, true);
  
  Family sample = processFamily(smpUrl, new String(baos.toByteArray(),"UTF-8"));
  
  System.out.println(sample);
  
  conn.disconnect();

 }
 
 public static Panel processPanel( URL panUrl, String page ) throws IOException
 {
  Tidy tidy = new Tidy();
  tidy.setShowWarnings(false);
  tidy.setQuiet(true);
  tidy.setXHTML(false);
  tidy.setInputEncoding("UTF-8");
  
  Document doc = tidy.parseDOM( new StringReader(page), null);

  Panel p = new Panel();
  
  NodeList nds = doc.getElementsByTagName("h2");
  
  for( int i=0; i < nds.getLength(); i++ )
  {
   NodeList snds = nds.item(i).getChildNodes();

   for( int j=0; j< snds.getLength(); j++ )
   {
    String val = snds.item(j).getNodeValue();
    
    if( "Overview".equals(val) )
    {
     collectAttributes(nds.item(i).getNextSibling(), p, panUrl);
    }
   }
  }
  
  collectSampleIDs(doc, p);
  
  return p;
 }
 
 public static Family processFamily( URL panUrl, String page ) throws IOException
 {
  Tidy tidy = new Tidy();
  tidy.setShowWarnings(false);
  tidy.setQuiet(true);
  tidy.setXHTML(false);
  tidy.setInputEncoding("UTF-8");
  
  Document doc = tidy.parseDOM( new StringReader(page), null);
  
  Family p = new Family();
  
  collectSampleIDs(doc, p);
  
  return p;
 }

 
 public static Sample processSample( URL smpUrl, String page ) throws IOException
 {
  
  Tidy tidy = new Tidy();
  tidy.setShowWarnings(false);
  tidy.setQuiet(true);
  tidy.setXHTML(false);
  tidy.setInputEncoding("UTF-8");
  
  Document doc = tidy.parseDOM( new StringReader(page), null);
  
//  StringBuilder sb = new StringBuilder(1000);
  
  Sample s = new Sample();
  
 
  NodeList nds = doc.getElementsByTagName("h2");
  
  for( int i=0; i < nds.getLength(); i++ )
  {
   NodeList snds = nds.item(i).getChildNodes();

   for( int j=0; j< snds.getLength(); j++ )
   {
    String val = snds.item(j).getNodeValue();
    
    
    if( "Overview".equals(val) )
    {
     collectAttributes(nds.item(i).getNextSibling(), s, smpUrl);
    }
    else if( "Publications".equals(val) )
    {
     Node table = nds.item(i).getNextSibling();

     Publication cPub = null;
     
     NodeList rows = table.getChildNodes();
     for( int k=0; k< rows.getLength(); k++ )
     {
      Node row = rows.item(k);

      if( ! "tr".equals(row.getNodeName()) )
       continue;
      
      NodeList cells = rows.item(k).getChildNodes();
      
      sb.setLength(0);
      convertNode2Text(cells.item(0),sb);
      String cellVal = sb.toString().trim();
      
      if( cellVal.length() <=1  )
      {
       cPub=null;
       continue;
      }

      if( cPub == null )
      {
       cPub = new Publication();
       cPub.setTitle(cellVal);
       
       s.addPublication( cPub );
      }
      else
      {
       int pos = cellVal.indexOf(":");
       cPub.setPubMed(cellVal.substring(pos+1).trim());
      }
     }
    }
    else if( "External Links".equals(val) )
    {
     Node table = nds.item(i).getNextSibling();

     ExternalLink cLnk = null;
     
     NodeList rows = table.getChildNodes();
     for( int k=0; k< rows.getLength(); k++ )
     {
      Node row = rows.item(k);

      if( ! "tr".equals(row.getNodeName()) )
       continue;
      
      NodeList cells = rows.item(k).getChildNodes();
      
      sb.setLength(0);
      convertNode2Text(cells.item(0),sb);
      String cellVal0 = sb.toString().trim();
      
      if( cellVal0.length() > 1)
      {
       cLnk = new ExternalLink();
       cLnk.setName(cellVal0);
       
       s.addExternalLink( cLnk );
      }
      
      Node cellCont = cells.item(1).getFirstChild();
      
      if( "a".equals(cellCont.getNodeName()) )
      {
       Link l = new Link();
       String value=cellCont.getAttributes().getNamedItem("href").getNodeValue();
       l.setUrl(value);

       sb.setLength(0);
       convertNode2Text(cellCont,sb);

       l.setTitle(sb.toString());

       cLnk.addLink(l);
      }

     }
    }
   }
  }
 
  
  return s;
 }

 private static void collectSampleIDs( Document doc, ObjGroup grp )
 {
  Node pnlTbl = null;
  NodeList tbls = doc.getElementsByTagName("table");
  
  for( int k=0; k< tbls.getLength(); k++ )
  {
   Node tb = tbls.item(k);
   Node idNode = tb.getAttributes().getNamedItem("id");
   if( idNode!= null && "grdRef".equals(idNode.getNodeValue() ) )
   {
    pnlTbl=tb;
    break;
   }
  }
  
  if( pnlTbl == null )
   return;
  
  NodeList rows = pnlTbl.getChildNodes();
  for( int k=1; k< rows.getLength(); k++ )
  {
   Node row = rows.item(k);
   
   sb.setLength(0);
   convertNode2Text(row.getFirstChild(),sb);

   grp.addSample(sb.toString());
  }
 }
 
 private static void collectAttributes( Node table, Attributed s, URL baseURL ) throws MalformedURLException
 {
  NodeList rows = table.getChildNodes();
  
  String attrName = null;
  
  for( int k=0; k< rows.getLength(); k++ )
  {
   Node row = rows.item(k);
   
   if( ! "tr".equals(row.getNodeName()) )
    continue;
   
   NodeList cells = rows.item(k).getChildNodes();
   
   Node cell0 = cells.item(0);
   Node cell0bold = cell0.getFirstChild();
   
   if( cell0bold != null )
   {
    if(!"b".equals(cell0bold.getNodeName()))
     break;

    Node cell0text = cell0bold.getFirstChild();
    attrName = cell0text.getNodeValue();
   }
   
   Node cellCont = cells.item(1).getFirstChild();
   
   if( "span".equals(cellCont.getNodeName()) )
    cellCont=cellCont.getFirstChild();
   
   if( cellCont == null )
    continue;
   
   Value v = new Value();
   
   sb.setLength(0);
   convertNode2Text(cellCont,sb);
   v.setValue( sb.toString() );
   v.setLinks( collectLinks(cellCont, null) );

   
//   if( "a".equals(cellCont.getNodeName()) )
//   {
//    value=cellCont.getAttributes().getNamedItem("href").getNodeValue();
//    URL ref = new URL(baseURL, value);
//
//    sb.setLength(0);
//    convertNode2Text(cells.item(1),sb);
//
//    s.addAttibute(attrName,sb.toString());
//    System.out.println(attrName+" = "+sb.toString());
//    
//    attrName = attrName+"[link]";
//    value=ref.toExternalForm();
//   }
//   else
//   {
//    sb.setLength(0);
//    convertNode2Text(cellCont,sb);
//    value = sb.toString();
//   }
//   
//   s.addAttibute(attrName,value);

   s.addAttibute(attrName, v);
   
   System.out.println(attrName+" = "+v.toString());
  }

 }
 
 private static List<Link> collectLinks( Node nd, List<Link> lst )
 {
  if( nd.getNodeType() != Node.ELEMENT_NODE )
   return lst;
  
  if( "a".equalsIgnoreCase(nd.getNodeName()) )
  {
   if( lst == null )
    lst = new ArrayList<Link>(5);
   
   Link l = new Link();
   
   sb.setLength(0);
   convertNode2Text(nd,sb);
   l.setTitle( sb.toString() );
   l.setUrl(nd.getAttributes().getNamedItem("href").getNodeValue());
   
   lst.add(l);
   
   return lst;
  }
  
  NodeList chld = nd.getChildNodes();
  
  for( int i=0; i < chld.getLength(); i++ )
   lst = collectLinks(chld.item(i), lst);
  
  return lst;
 }
 
 private static void convertNode2Text( Node nd, StringBuilder sb )
 {
  if( nd.getNodeType() == Node.TEXT_NODE )
  {
   sb.append(nd.getNodeValue() );
   return;
  }

  if( nd.getNodeType() == Node.ELEMENT_NODE )
  {
   if( "br".equals(nd.getNodeName() ) )
   {
    sb.append("\n");
    return;
   }
   
   NodeList lst = nd.getChildNodes();
   
   for( int i=0; i < lst.getLength(); i++ )
   {
    convertNode2Text(lst.item(i), sb);
   }
  }
 
  
  
 }
 
}

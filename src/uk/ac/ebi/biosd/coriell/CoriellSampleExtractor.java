package uk.ac.ebi.biosd.coriell;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

public class CoriellSampleExtractor
{
 /**
  * @param args
  * @throws IOException 
  */
 public static void main(String[] args) throws IOException
 {
  String url="http://ccr.coriell.org/Sections/Search/Sample_Detail.aspx?Ref=GM19139&PgId=166";

  URL smpUrl = new URL( url );
  
  HttpURLConnection conn = (HttpURLConnection)smpUrl.openConnection();
  
  Tidy tidy = new Tidy();
  tidy.setXHTML(false);
  tidy.setInputEncoding("UTF-8");
  
  Document doc = tidy.parseDOM(conn.getInputStream(), null);
  
  conn.disconnect();
  
  StringBuilder sb = new StringBuilder(1000);
  
  NodeList nds = doc.getElementsByTagName("h2");
  
 
  for( int i=0; i < nds.getLength(); i++ )
  {
   NodeList snds = nds.item(i).getChildNodes();

   for( int j=0; j< snds.getLength(); j++ )
   {
    String val = snds.item(j).getNodeValue();
    
    System.out.println("Node: "+val);
    
    if( "Overview".equals(val) )
    {
     Node table = nds.item(i).getNextSibling();

     NodeList rows = table.getChildNodes();
     for( int k=0; k< rows.getLength(); k++ )
     {
      Node row = rows.item(k);
      
      if( ! "tr".equals(row.getNodeName()) )
       continue;
      
      NodeList cells = rows.item(k).getChildNodes();
      
      Node cell0 = cells.item(0);
      Node cell0bold = cell0.getFirstChild();
      
      if( ! "b".equals(cell0bold.getNodeName() ) )
       continue;
      
      Node cell0text = cell0bold.getFirstChild();
      String name = cell0text.getNodeValue();

      Node cellCont = cells.item(1).getFirstChild();
      
      String value = null;
      
//      if( "span".equals(cellCont.getNodeName()) )
//       cellCont=cellCont.getFirstChild();
      
      if( "a".equals(cellCont.getNodeName()) )
      {
       value=cellCont.getAttributes().getNamedItem("href").getNodeValue();
       URL ref = new URL(smpUrl, value);
       
       value=ref.toExternalForm();
      }
      else
      {
       sb.setLength(0);
       convertNode2Text(cellCont,sb);
       value = sb.toString();
      }
      
      System.out.println(name+" = "+value);
     }
    
    }
   }
  }
  
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
   NodeList lst = nd.getChildNodes();
   
   for( int i=0; i < lst.getLength(); i++ )
   {
    convertNode2Text(lst.item(i), sb);
   }
  }
 
  
  
 }
 
}

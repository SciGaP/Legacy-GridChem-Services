package org.gridchem.service.sync.gpir;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

/** Simple utility class for creating an XML Reader
 *
 * @author
 *
 */
     
final class XML
{ /** create a new XML reader */

  final public static org.xml.sax.XMLReader makeXMLReader()  
  throws Exception 
  { 
      final SAXParserFactory saxParserFactory = 
          SAXParserFactory.newInstance(); 
      final SAXParser saxParser = saxParserFactory.newSAXParser(); 
      final XMLReader parser = saxParser.getXMLReader(); 
      return parser;
  }
}

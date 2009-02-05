package com.unwiredappeal.tivo.metadata;


import java.io.*;
import org.xml.sax.*;

public class TxtParser extends BaseParser
{					
	private void parseString(String str) throws SAXException
	{
		
	    contentHandler.startDocument();
	    contentHandler.startElement(namespaceURI, "txt", "txt", attribs);
	    
	    startCData();
		contentHandler.characters(str.toCharArray(), 0, str.length()); 		 
		endCData();
		contentHandler.endElement(namespaceURI, "txt", "txt");
		contentHandler.endDocument();
	}
	
	//===========================================================
	// XML Reader Interface Implementation
	//===========================================================
	public void parse(InputSource source) throws IOException, SAXException
	{
		Reader r =source.getCharacterStream();
		StringWriter w = new StringWriter();
		int c;
		while((c = r.read()) >= 0)
			w.write(c);
		parseString(w.toString());
	}
	
	public void parse(String uri) throws SAXException 
	{
	}
}

package com.unwiredappeal.tivo.metadata;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.*;

public class PyTivoParser extends BaseParser
{					
	
	public Map<String, String> elementMap = new HashMap<String, String>();

	private void parse(BufferedReader r) throws SAXException
	{
	    contentHandler.startDocument();
	    contentHandler.startElement(namespaceURI, "pytivo", "pytivo", attribs);

	    boolean closeElement = false;
	    String lastElementData = null;
	    String key = null;;
	    try {
		    String str;
		    while((str = r.readLine()) != null) {
		    	if (closeElement && str.startsWith(" ") || str.startsWith("\t")) {
		    		str.trim();
		    		str = " " + str;
		    		contentHandler.characters(str.toCharArray(), 0, str.length());
		    		lastElementData += str;
		    		continue;
		    	}
		    	if (closeElement) {
		 		    endCData();
		 		    contentHandler.endElement(namespaceURI, key, key);
		 		    closeElement = false;
		 		    elementMap.put(key, lastElementData);
		    	}		    	
		    	String split[] = str.split(":", 2);
		    	if (split.length != 2)
		    		continue;
		    	key = split[0].trim();
		    	String value = split[1].trim();
	 		    contentHandler.startElement(namespaceURI, key, key, attribs);
	 		    startCData();
	 		    lastElementData = new String(value);
	 		    contentHandler.characters(value.toCharArray(), 0, value.length());
	 		    closeElement = true;
		    }
		    if (closeElement) {
	 		    endCData();
	 		    contentHandler.endElement(namespaceURI, key, key);            	    	
		    }
			contentHandler.endElement(namespaceURI, "pytivo", "pytivo");
			contentHandler.endDocument();
	    } catch(IOException e) {
	    	throw new SAXException(e);
	    }
	}
	
	//===========================================================
	// XML Reader Interface Implementation
	//===========================================================
	public void parse(InputSource source) throws IOException, SAXException
	{
		BufferedReader r = new BufferedReader(source.getCharacterStream());
	    parse(r);
	}
	
	public void parse(String uri) throws SAXException 
	{
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new URI(uri).toURL().openStream()));
			parse(r);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

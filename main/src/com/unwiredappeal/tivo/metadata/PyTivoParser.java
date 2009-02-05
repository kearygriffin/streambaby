package com.unwiredappeal.tivo.metadata;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.*;

public class PyTivoParser extends BaseParser
{					

	private void parse(BufferedReader r) throws SAXException
	{
	    contentHandler.startDocument();
	    contentHandler.startElement(namespaceURI, "pytivo", "pytivo", attribs);
	    
	    try {
		    String str;
		    while((str = r.readLine()) != null) {
		    	String split[] = str.split(":", 2);
		    	if (split.length != 2)
		    		continue;
		    	String key = split[0].trim();
		    	String value = split[1].trim();
	 		    contentHandler.startElement(namespaceURI, key, key, attribs);
	 		    /*
		    	int index = value.indexOf('|');
		    	if (index >= 0) {
		    		String pieces[] = value.split("\\|");
		    		for (String p : pieces) {
		    			contentHandler.startElement(namespaceURI, "piece", "piece", attribs);
		    			startCData();
		    			contentHandler.characters(p.toCharArray(), 0, p.length());
		    			endCData();
		    			contentHandler.endElement(namespaceURI, "piece", "piece");
		    		}
		    	} else {
		    	*/
		 		    startCData();
		 		    contentHandler.characters(value.toCharArray(), 0, value.length());
		 		    endCData();
		 		/*
		    	}
		    	*/
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

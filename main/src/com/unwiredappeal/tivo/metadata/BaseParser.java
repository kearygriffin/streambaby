package com.unwiredappeal.tivo.metadata;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public abstract class BaseParser implements XMLReader{
	public static final String LEX_PROP = "http://xml.org/sax/properties/lexical-handler";
	
	ContentHandler contentHandler = null;
	AttributesImpl attribs = new AttributesImpl();
	String namespaceURI = "";
	LexicalHandler lexHandler = null;
	
	protected void startCData() throws SAXException {
		if (lexHandler != null)
			lexHandler.startCDATA();
	}
	protected void endCData() throws SAXException {
		if (lexHandler != null)
			lexHandler.endCDATA();
	}

	public void setContentHandler(ContentHandler handler)
	{
		contentHandler = handler;
	}

	public ContentHandler getContentHandler()
	{
		return contentHandler;
	}
	
	public boolean getFeature(String s)		
	{
        return false;
	}

	public void setFeature(String s, boolean b)
	{
	}

	public Object getProperty(String s)
	{
        return null;
	}

	public void setProperty(String s, Object o)
	{
		if (s.equals(LEX_PROP)) {
			lexHandler = (LexicalHandler)o;
		}
	}

	public void setEntityResolver(EntityResolver e)
	{
	}

	public EntityResolver getEntityResolver()
	{
		return null;
	}

	public void setDTDHandler(DTDHandler d)
	{
	}

	public DTDHandler getDTDHandler()
	{
		return null;
	}

	public void setErrorHandler(ErrorHandler handler)
	{
	}

	public ErrorHandler getErrorHandler()
	{
		return null;
	}	
	
	
}

package com.unwiredappeal.tivo.metadata;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.utils.Log;

public abstract class BaseMetadataModule implements StreamBabyModule, MetadataModule{
	private static String DEFAULT_XSL = "system/echo.xsl";
	public Object getModule(int moduleType) {
		if (moduleType == StreamBabyModule.STREAMBABY_MODULE_METADATA)
			return this;
		else
			return null;
	}

	public int getSimplePriority() {
		return StreamBabyModule.DEFAULT_PRIORITY;
	}

	public int getMetadataPriority() {
		return StreamBabyModule.DEFAULT_PRIORITY;
	}

	public boolean initialize(StreamBabyModule parent) {
		return true;
	}
	
	protected  boolean transform(MetaData m, SAXSource source, String xsl, String defaultXsl) {
		if (defaultXsl == null)
			defaultXsl = DEFAULT_XSL;
		
		List<String> xslFiles = new LinkedList<String>();
		if (xsl != null) {
			String[] xslSplit = xsl.split(",");
			for (String fn : xslSplit) {
				String xslFileName = StreamBabyConfig.convertRelativePath(fn, StreamBabyConfig.streamBabyDir + File.separator + "stylesheets");
				File f = new File(xslFileName);
				if (f.exists())
					xslFiles.add(xslFileName);
			}
		}
		if (xslFiles.isEmpty()) {
			String xslFileName = StreamBabyConfig.convertRelativePath(defaultXsl, StreamBabyConfig.streamBabyDir + File.separator + "stylesheets");
			File f = new File(xslFileName);
			if (!f.exists())
				return false;
			xslFiles.add(xslFileName);
		}
		
		// construct a transformer using the echo stylesheet
		TransformerFactory factory = TransformerFactory.newInstance();
		

		String resultStr = null;
		for (String xslFile : xslFiles) {
			if (resultStr != null)
				source = new SAXSource(new InputSource(new StringReader(resultStr)));
			StreamSource xslSource = new StreamSource(xslFile);
			Transformer transformer;
			try {
				transformer = factory.newTransformer(xslSource);
			} catch (TransformerConfigurationException e) {
				Log.error("Unable to load xslt transformer:" + e);
				continue;
			}
	
			// transform the SAXSource to the result
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				Log.error("Unable to transform: " + e);
				continue;
			}
			resultStr = sw.toString();
		}
		if (resultStr != null)
			m.setString(resultStr);
		return resultStr != null;
	}

	public File findMeta(File dir, String name, String ext) {
		File f = new File(dir, name + ext);
		if (!f.exists())
			f = new File(dir.getAbsolutePath() + File.separatorChar + ".meta", name + ext);
		if (!f.exists())
			f = new File(dir, "default" + ext);
		if (!f.exists())
			f = new File(dir.getAbsolutePath() + File.separatorChar + ".meta", "default" + ext);
		if (!f.exists())
			return null;
		return f;
	}
	public String readMeta(MetaData m, File dir, String name, String ext) {
		File f = findMeta(dir, name, ext);
		if (f == null)
			return null;
		
		try {
			m.setBaseUrl(f.toURL().toExternalForm());
		} catch (MalformedURLException e2) {
		}
		Reader r = null;;
		StringWriter w = null;
		try {
			r = new BufferedReader(new FileReader(f));
			 w = new StringWriter();
			char[] buf = new char[4096];
			int len;
			while((len = r.read(buf)) > 0) {
				w.write(buf, 0, len);
			}
			r.close();
			w.close();
			return w.toString();
		} catch (IOException e) {
			if (w != null)
				try {
					w.close();
				} catch (IOException e1) {
				}
			if (r != null)
				try {
					r.close();
				} catch (IOException e1) {
				}
			return null;
		}
	}

	

}
package com.unwiredappeal.tivo.metadata;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.xml.sax.InputSource;

import com.unwiredappeal.tivo.config.ConfigurableObject;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.TempFileManager;

public abstract class BaseMetadataModule extends ConfigurableObject implements StreamBabyModule, MetadataModule{
	private static String DEFAULT_XSL = "system/echo.xsl";
	
	public static Map<String, Templates> cachedTransformers = new HashMap<String, Templates>();
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
		this.populateConfig();
		return true;
	}
	
	public static synchronized Templates getXsltTransformer(File xsl) throws TransformerConfigurationException {
		Templates tp = cachedTransformers.get(xsl);
		if (tp != null)
			return tp;
		// construct a transformer using the echo stylesheet
		StreamSource xslSource = new StreamSource(xsl);
		TransformerFactoryImpl factory = new TransformerFactoryImpl();		
		tp = factory.newTemplates(xslSource);
		return tp;

	}
	
	protected  boolean transform(MetaData m, SAXSource source, String xsl, String defaultXsl) {

	    //String cssFileName = StreamBabyConfig.convertRelativePath(StreamBabyConfig.cfgMetaCSS.getValue() + "-" + GLOBAL.y_res + ".css", StreamBabyConfig.streamBabyDir + File.separator + "stylesheets");

		if (defaultXsl == null)
			defaultXsl = DEFAULT_XSL;
		
		List<File> xslFiles = new LinkedList<File>();
		if (xsl != null) {
			String[] xslSplit = xsl.split(",");
			for (String fn : xslSplit) {
				String xslFileName = StreamBabyConfig.convertRelativePath(fn, StreamBabyConfig.streamBabyDir + File.separator + "stylesheets");
				File f = new File(xslFileName);
				if (f.exists())
					xslFiles.add(new File(xslFileName));
			}
		}
		if (xslFiles.isEmpty()) {
			String xslFileName = StreamBabyConfig.convertRelativePath(defaultXsl, StreamBabyConfig.streamBabyDir + File.separator + "stylesheets");
			File f = new File(xslFileName);
			if (!f.exists())
				return false;
			xslFiles.add(new File(xslFileName));
		}
		
		

		String resultStr = null;
		for (File xslFile : xslFiles) {
			if (resultStr != null)
				source = new SAXSource(new InputSource(new StringReader(resultStr)));
			Transformer transformer;
			try {
				Templates cachedXsl = getXsltTransformer(xslFile);
				transformer = cachedXsl.newTransformer();
				//transformer = factory.newTransformer(xslSource);
			} catch (TransformerConfigurationException e) {
				Log.error("Unable to load xslt transformer:" + e);
				continue;
			}

			//transformer.setParameter("stylesheet", cssFileName);
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
		Log.debug(resultStr);
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
		
		m.setReference(f);
		try {
			m.setBaseUrl(f.toURL().toExternalForm());
		} catch (MalformedURLException e2) {
		}
		Reader r = null;;
		StringWriter w = null;
		try {
			InputStream is = new FileInputStream(f);
			r = new BufferedReader(new UnicodeReader(is, "UTF-8"));
			//r = new BufferedReader(new FileReader(f));
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

	
	protected static Map<String, String> artworkMap = new HashMap<String, String>();
	protected String writeArtwork(URI uri, byte[] data, String ext) {
		String id = uri.toString() + ext;
		String filename = artworkMap.get(id);
		if (filename!= null)
			return filename;
		File f;
		try {
			f = TempFileManager.createTempFile("art-", ext);
			filename = f.getAbsolutePath();
			f.deleteOnExit();
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			raf.write(data);
			raf.close();
			return filename;
		} catch (IOException e) {
			Log.error("Error creating artwork file");
			return null;
		}
	}
}

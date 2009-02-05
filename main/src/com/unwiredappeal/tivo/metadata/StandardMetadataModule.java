package com.unwiredappeal.tivo.metadata;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.config.ConfigurationManager;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.Utils;

public class StandardMetadataModule extends BaseMetadataModule {

	private static Pattern pyTivoPattern = Pattern.compile("^\\w+\\s+:\\s+.*");
	private static Pattern pyTivoTitlePattern= Pattern.compile(".*^title\\s+:\\s+([^\n\r]+)$.*", Pattern.MULTILINE|Pattern.DOTALL);

	private void setPyTivoTitle(String data, MetaData meta) {
		Matcher m = pyTivoTitlePattern.matcher(data);
		if (m.matches()) {
			String title = m.group(1);
			if (title != null)
				meta.setTitle(title);
		}
	}
	public boolean handlePyTivo(String data, MetaData m) {
		PyTivoParser parser = new PyTivoParser();
		InputSource inputSource = new InputSource(new StringReader(data));
		SAXSource source = new SAXSource(parser, inputSource);
		boolean b = transform(m, source, StreamBabyConfig.cfgPyTivoXsl.getValue(),
				null);
		if (b)
			setPyTivoTitle(data, m);
		return b;
	}

	public String getFirstText(Document doc, String el) {
		NodeList nl = doc.getDocumentElement().getElementsByTagName(el);
		String txt = null;
		if (nl != null) {
			if (nl.getLength() > 0) {
				Node textnode = nl.item(0).getFirstChild();
				if (textnode != null)
					txt= textnode.getTextContent();
			}
		}		
		return txt;
	}
	public boolean handleXmlMetadata(String data, MetaData m) {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new InputSource(new StringReader(
					data)));
			// normalize text representation
			doc.getDocumentElement().normalize();

			String root = doc.getDocumentElement().getNodeName();
			if (root == null)
				return false;
			String title = getFirstText(doc, "title");
			if (title != null) {
				String epTitle = getFirstText(doc, "episodeTitle");
				if (epTitle != null) {
					title = title + " - " + epTitle;
				}
			}
			int index = root.lastIndexOf(':');
			if (index >= 0) {
				root = root.substring(index + 1);
			}
			root = root.toLowerCase();
			String xslConfig = "xsl." + root;
			String def = null;
			ConfigEntry e = ConfigurationManager.inst.getConfigEntry(xslConfig);
			if (e != null)
				def = e.defaultValue;
			String xsl = ConfigurationManager.inst.getStringProperty(xslConfig,
					def);
			if (xsl == null || xsl.length() == 0) {
				xsl = root + ".xsl";
			}
			boolean b = transform(m, new SAXSource(new InputSource(new StringReader(data))), xsl,
					null);
			if (b && title != null)
				m.setTitle(title);
			return b;

		} catch (ParserConfigurationException e) {
			Log.error("Error parsing xml: " + e);
		} catch (SAXException e) {
			Log.error("SAX exception in xml: " + e);
		} catch (IOException e) {
		}

		return false;
	}

	public boolean isPyTivo(String str) {
		String nstr = str.trim();
		String[] split = nstr.split("\r\n|\r|\n", 1);
		if (split.length == 0)
			return false;
		Matcher m = pyTivoPattern.matcher(split[0].replaceAll("[\\n\\r]*", ""));
		return m.matches();
	}

	public boolean handleTxtMetadata(String data, MetaData m) {
		if (isPyTivo(data))
			return handlePyTivo(data, m);
		else {
			TxtParser parser = new TxtParser();
			InputSource inputSource = new InputSource(new StringReader(data));
			SAXSource source = new SAXSource(parser, inputSource);
			return transform(m, source, StreamBabyConfig.cfgTxtXsl.getValue(),
					"system/untxtxml.xsl");
		}
	}

	public boolean handleHtmlMetadata(File htmlFile, MetaData m) {
		try {
			m.setUrl(htmlFile.toURL().toExternalForm());
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	public boolean handleImage(File f, MetaData m) {
		m.setImage(f);
		return true;
	}

	public boolean setMetadata(MetaData m, URI uri, VideoInformation vi) {
		if (!Utils.isFile(uri))
			return false;
		File f = new File(uri);
		File metaHtml = findMeta(f.getParentFile(), f.getName(), ".html");
		if (metaHtml != null && handleHtmlMetadata(metaHtml, m))
			return true;
		File img;
		img = findMeta(f.getParentFile(), f.getName(), ".png");
		if (img == null)
			img = findMeta(f.getParentFile(), f.getName(), ".gif");
		if (img == null)
			img = findMeta(f.getParentFile(), f.getName(), ".jpg");
		if (img != null)
			return handleImage(img, m);
		String metaXml = readMeta(m, f.getParentFile(), f.getName(), ".xml");
		if (metaXml != null && handleXmlMetadata(metaXml, m))
			return true;
		String metaTxt = readMeta(m, f.getParentFile(), f.getName(), ".txt");
		if (metaTxt != null && handleTxtMetadata(metaTxt, m))
			return true;
		return false;
	}

}

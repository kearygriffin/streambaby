package com.unwiredappeal.tivo.metadata;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
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
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.Utils;

public class StandardMetadataModule extends BaseMetadataModule {

	private static Pattern pyTivoPattern = Pattern.compile("^\\w+\\s*:.*");
	//private static Pattern pyTivoTitlePattern= Pattern.compile(".*^title\\s+:\\s+([^\n\r]+)$.*", Pattern.MULTILINE|Pattern.DOTALL);
	private static Pattern urlSchemePattern = Pattern.compile("\\w+://.*");

	private String getMetaTitle(String title, String episodeTitle, String seriesTitle, String isEpisodic) {
		String t = null;
		if (title != null)
			t = title;
		// If we are an episode, we can do better?
		if (isEpisodic != null && isEpisodic.equals("true") && seriesTitle != null && seriesTitle.length() > 0 && episodeTitle != null && episodeTitle.length() > 0)
			t = seriesTitle + " - " + episodeTitle;
		else if (title != null && title.length() > 0)
			t = title;
		else if (seriesTitle != null && seriesTitle.length() > 0)
			t = seriesTitle;
		else if (episodeTitle != null && episodeTitle.length() > 0)
			t = episodeTitle;
		return t;
		
	}
	private void setPyTivoTitle(PyTivoParser p, MetaData meta) {
		/*
		Matcher m = pyTivoTitlePattern.matcher(data);
		if (m.matches()) {
			String title = m.group(1);
			if (title != null)
				meta.setTitle(title);
		}
		*/
		String episodeTitle = p.elementMap.get("episodeTitle");
		String title  = p.elementMap.get("title");
		String seriesTitle  = p.elementMap.get("seriesTitle");
		String isEpisodic = p.elementMap.get("isEpisode");
		String t = getMetaTitle(title, episodeTitle, seriesTitle, isEpisodic);
		if (t != null)
			meta.setTitle(t);
		meta.setTextDescription(p.elementMap.get("description"));
		if (isEpisodic != null && isEpisodic.equals("true") && seriesTitle != null && seriesTitle.length() > 0 && episodeTitle != null && episodeTitle.length() > 0) {
			meta.setSeriesTitle(seriesTitle);
			meta.setEpisodeTitle(episodeTitle);
		}		
	}
	
	public boolean initialize(StreamBabyModule parent) {
		super.initialize(parent);
		return true;
	}
	public boolean handlePyTivo(String data, MetaData m) {
		PyTivoParser parser = new PyTivoParser();
		InputSource inputSource = new InputSource(new StringReader(data));
		SAXSource source = new SAXSource(parser, inputSource);
		boolean b = transform(m, source, StreamBabyConfig.cfgPyTivoXsl.getValue(),
				null);
		if (b) {
			m.setSimpleMetadata(true);
			setPyTivoTitle(parser, m);
		}
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
			String episodeTitle = getFirstText(doc, "episodeTitle");
			String title  = getFirstText(doc, "title");
			String seriesTitle  = getFirstText(doc, "seriesTitle");
			String isEpisodic = getFirstText(doc, "isEpisode");
			String t = getMetaTitle(title, episodeTitle, seriesTitle, isEpisodic);
			m.setTextDescription(getFirstText(doc, "description"));
			if (isEpisodic != null && isEpisodic.equals("true") && seriesTitle != null && seriesTitle.length() > 0 && episodeTitle != null && episodeTitle.length() > 0) {
				m.setSeriesTitle(seriesTitle);
				m.setEpisodeTitle(episodeTitle);
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
			if (b && t != null) {
				m.setSimpleMetadata(true);
				m.setTitle(t);
			}
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
			m.setTextDescription(data);
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
	
	public boolean handleVidMeta(VideoInformation vi, MetaData m) {
		Map<String, String> metaMap;
		metaMap = vi.getMetadataMap();
		if (metaMap.size() == 0)
			return false;
		if (metaMap.get("name") != null)
			m.setTitle(metaMap.get("name"));
		
		StringBuffer data = new StringBuffer();
		data.append("<meta>\n");
		for (Map.Entry<String, String> e : metaMap.entrySet()) {
			data.append("<" + e.getKey() + ">");
			data.append("<![CDATA[");
			data.append(e.getValue());
			data.append("]]>");
			data.append("</" + e.getKey() + ">");			
		}
		data.append("</meta>");
		SAXSource source = new SAXSource(new InputSource(new StringReader(data.toString())));
		return transform(m, source, StreamBabyConfig.cfgMetaXsl.getValue(), null);
		

	}

	public boolean handleUrlMetadata(String urlData, MetaData m) {
		String[] split = urlData.split("\r\n|\r|\n");
		if (split.length == 0)
			return false;
		Matcher match = urlSchemePattern.matcher(split[0].toLowerCase());
		if (match.matches()) {
			m.setUrl(split[0]);
			return true;
		}
		
		for (String line : split) {
			String es[] = line.split("=", 2);
			if (es.length == 2) {
				if (es[0].compareToIgnoreCase("url") == 0) {
					m.setUrl(es[1]);
					return true;
				}
			}
		}
		return false;

	}
	public boolean setMetadata(MetaData m, URI uri, VideoInformation vi) {
		if (!Utils.isFile(uri))
			return false;
		File f = new File(uri);
		String metaTxt = readMeta(m, f.getParentFile(), f.getName(), ".txt");
		if (metaTxt != null && handleTxtMetadata(metaTxt, m))
			return true;
		String metaXml = readMeta(m, f.getParentFile(), f.getName(), ".xml");
		if (metaXml != null && handleXmlMetadata(metaXml, m))
			return true;
		if (m.isBasicInfoOnly())
			return false;
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
		
		String metaUrl = readMeta(m, f.getParentFile(), f.getName(), ".url");
		if (metaUrl != null && handleUrlMetadata(metaUrl, m))
			return true;
		
		// do we have metadata from the video parser?
		if (vi != null && !StreamBabyConfig.cfgDisableVidInfoMeta.getBool()) {
			//VideoInformation vi = VideoInformation.getVideoInformation(de);
			if (vi != null && vi.getMetadataMap().size() > 0)
				return handleVidMeta(vi, m);			
		}
		return false;
	}

}

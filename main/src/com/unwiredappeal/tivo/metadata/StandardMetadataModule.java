package com.unwiredappeal.tivo.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.utils.Utils;

public class StandardMetadataModule implements StreamBabyModule, MetadataModule {

	private static Pattern pyTivoPattern = Pattern.compile("^\\w+\\s+:\\s+.*");
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
	
	public boolean handlePyTivo(String data, MetaData m) {
		return false;
	}
	
	public boolean handleXmlMetadata(String data, MetaData m) {
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
			m.setString(data);
			return true;
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
		File f= new File(uri);
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
		String metaXml = readMeta(f.getParentFile(), f.getName(), ".xml");
		if (metaXml != null && handleXmlMetadata(metaXml, m))
			return true;
		String metaTxt = readMeta(f.getParentFile(), f.getName(), ".txt");
		if (metaTxt != null && handleTxtMetadata(metaTxt, m))
			return true;
		return false;
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
	public String readMeta(File dir, String name, String ext) {
		File f = findMeta(dir, name, ext);
		if (f == null)
			return null;
		
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

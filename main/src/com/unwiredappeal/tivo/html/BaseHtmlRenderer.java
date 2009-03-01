package com.unwiredappeal.tivo.html;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.unwiredappeal.tivo.utils.TempFileManager;

public abstract class BaseHtmlRenderer implements SBHtmlRenderer {
	public boolean isModified = false;
	protected BufferedImage[] bis = null;
	protected String baseUrl = null;
	protected String url = null;
	protected String html = null;
	protected int width = -1, height = -1;
	protected boolean urlBased = false;
	protected String cssFileName;
	
	public void setHtmlDocument(String html, String urlBase) {
		this.url = null;
		this.baseUrl = urlBase;
		this.html = html;
		urlBased = false;
		setModified();
	}
	public void setUrlDocument(String url, String urlBase) {
		this.url = url;
		this.baseUrl = urlBase;
		urlBased = true;
		setModified();
	}
	
	public String htmlToFileUrl() {
		File f = null;
		String furl = null;
		FileWriter w = null;
		try {
			f = TempFileManager .createTempFile("info-", ".html");
			furl = f.toURL().toExternalForm();
			w = new FileWriter(f);
			w.write(html);
			w.close();
		} catch (IOException e) {
			if (w != null)
				try {
					w.close();
				} catch (IOException e1) {
				}
			if (f != null)
				f.delete();
			return null;
		}				
		return furl;
	}
	
	public boolean isModified() {
		return isModified;
	}
	
	public void setModified() {
		isModified = true;
		bis = null;
	}

	public void setModified(boolean b) {
		isModified = b;
	}
	
	public void setBaseCSS(String fn) {
		cssFileName = fn;
	}
}

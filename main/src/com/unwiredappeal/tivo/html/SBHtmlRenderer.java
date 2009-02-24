package com.unwiredappeal.tivo.html;

import java.awt.image.BufferedImage;

public interface SBHtmlRenderer {
	public BufferedImage[] getImages(int width, int height);
	public void setHtmlDocument(String html, String urlBase);
	public void setUrlDocument(String url, String urlBase);
	public void setBaseCSS(String css);
}

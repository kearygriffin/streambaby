package com.unwiredappeal.tivo.html.fs;

import java.awt.image.BufferedImage;
import java.io.File;


import com.unwiredappeal.tivo.html.BaseHtmlRenderer;

public class FlyingSaucerRenderer extends BaseHtmlRenderer {

	
	public FlyingSaucerRenderer() {		
	}

	public BufferedImage getImage(int width, int height) {
		if (!isModified() && this.width == width && this.height == height)
			return bi;
		String rurl = this.url;
		File fileToDelete = null;
		if (!urlBased) {
			rurl = htmlToFileUrl();
			if (rurl == null)
				return null;
			fileToDelete = new File(rurl);
		}
		SBFSJava2DRenderer r = new SBFSJava2DRenderer(rurl, baseUrl, width, height);
		bi = r.getImage();
		if (bi != null) {
			setModified(false);
			this.width = width;
			this.height = height;
		}
		if (fileToDelete != null)
			fileToDelete.delete();
		
		return bi;
	}
	
}

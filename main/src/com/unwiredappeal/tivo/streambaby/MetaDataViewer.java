package com.unwiredappeal.tivo.streambaby;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.swing.Java2DRenderer;

import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BViewPlus;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.metadata.MetaData;
import com.unwiredappeal.tivo.utils.TempFileManager;
import com.unwiredappeal.tivo.views.VText;

public class MetaDataViewer {
	public BView getView(MetaData meta, BView parent, int x, int y, int width, int height) {
		BView v = null;
		if (!meta.hasMetaData())
			return null;
		if (meta.getMetadataType() == MetaData.METADATA_URL) {
			if (StreamBabyConfig.cfgForceTidy.getBool()) {
				meta.convertUrl();
			} else {
				String url = meta.getUrl().toLowerCase();
				if (url != null && (url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith(".png"))) {
					meta.convertUrl();
				}
			}
		}
		if (meta.getMetadataType() == MetaData.METADATA_STRING) {
			VText vt = new VText(parent, x, y, width, height, "small");
			vt.setFlags(VText.RSRC_HALIGN_LEFT|VText.RSRC_VALIGN_TOP|VText.RSRC_TEXT_WRAP);
			vt.setValue(meta.getMetadata());
			v = vt;
		} else if (meta.getMetadataType() == MetaData.METADATA_IMAGE){
			BufferedImage bi = meta.getImage();
			if (bi != null) {
				v = setImage(parent, x, y, width, height, bi, true);
			}
		} else if (meta.getMetadataType() == MetaData.METADATA_URL || meta.getMetadataType() == MetaData.METADATA_HTML) {
			String baseUrl = null;
			if (meta.getUrl() != null) {
				try {
					URL url = new URL(meta.getUrl());
					baseUrl = url.toExternalForm();
					if (baseUrl.lastIndexOf('/') != baseUrl.length()-1) {
						int index = baseUrl.lastIndexOf('/');
						if (index >= 0) {
							baseUrl = baseUrl.substring(0, index);
						}
					}
				} catch (MalformedURLException e) {
				}
			}
			String url = null;
			File f = null;
			if (meta.getMetadataType() == MetaData.METADATA_URL)
				url = meta.getUrl();
			else {
				FileWriter w = null;
				try {
					f = TempFileManager .createTempFile("info-", ".html");
					url = f.toURL().toExternalForm();
					baseUrl = new File(f.getParent()).toURL().toExternalForm();
					w = new FileWriter(f);
					w.write(meta.getMetadata());
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
			}
			//BufferedImage bi = Graphics2DRenderer.renderToImage(url, width, height);
			Java2DRenderer j2d = new Java2DRenderer(url, baseUrl, width, height);
			j2d.setBufferedImageType(BufferedImage.TYPE_INT_ARGB);
			BufferedImage bi = j2d.getImage();
			if (bi != null)
				v = setImage(parent, x, y, width, height, bi, false);
			if (f != null)
				f.delete();
		}
		return v;
	}
	
	private BView setImage(BView parent, int x, int y, int width, int height, BufferedImage bi, boolean doScale) {
		BView v;
		BViewPlus vp = new BViewPlus(parent, x, y, width, height, true);
		v = vp;
		Image scaledImage = bi;
		if (doScale && (bi.getWidth() != width || bi.getHeight() != height)) {
			int iwidth;
			int iheight;

			if (StreamBabyConfig.cfgInfoPreserveAspect.getBool()) {
				float scale= Math.min((float)width/bi.getWidth(), (float)height/bi.getHeight());
				iwidth = (int)(bi.getWidth()*scale);
				iheight = (int)(bi.getHeight()*scale);
			} else {
				iwidth= width;
				iheight = height;
			}
			scaledImage = bi.getScaledInstance(iwidth, iheight, BufferedImage.SCALE_DEFAULT);
		}
		v.setResource(vp.createImage(scaledImage));
		return v;
	}
}

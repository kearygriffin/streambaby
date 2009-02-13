package com.unwiredappeal.tivo.streambaby;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;

import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BViewPlus;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.html.SBHtmlRenderer;
import com.unwiredappeal.tivo.html.SBHtmlRendererFactory;
import com.unwiredappeal.tivo.metadata.MetaData;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.TempFileManager;
import com.unwiredappeal.tivo.views.BScrollableText;
import com.unwiredappeal.tivo.views.SBScrollPanePlus;

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
			BScrollableText st = new BScrollableText(parent, x, y, width, height, "small");
			//vt.setFlags(VText.RSRC_HALIGN_LEFT|VText.RSRC_VALIGN_TOP|VText.RSRC_TEXT_WRAP);
			st.setValue(meta.getMetadata());
			v = st;
		} else if (meta.getMetadataType() == MetaData.METADATA_IMAGE){
			BufferedImage bi = meta.getImage();
			if (bi != null) {
				v = setImage(parent, x, y, width, height, bi, true);
			}
		} else if (meta.getMetadataType() == MetaData.METADATA_URL || meta.getMetadataType() == MetaData.METADATA_HTML) {
			String baseUrl = null;
			if (meta.getUrl() != null) {
				/*
				try {
					URL url = new URL(meta.getUrl());
					baseUrl = url.toExternalForm();
					if (baseUrl.lastIndexOf('/') != baseUrl.length()-1) {
						int index = baseUrl.lastIndexOf('/');
						if (index >= 0) {
							baseUrl = baseUrl.substring(0, index+1);
						}
					}
				} catch (MalformedURLException e) {
				}
				*/
				baseUrl = meta.getUrl();
			}
			if (baseUrl == null) {
				try {
					baseUrl = new File(TempFileManager.tmpDirName).toURL().toExternalForm();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			String url = null;
			if (meta.getMetadataType() == MetaData.METADATA_URL)
				url = meta.getUrl();
			try {
				SBHtmlRenderer r = SBHtmlRendererFactory.getRenderer();
				if (r == null)
					return null;
				if (url != null)
					r.setUrlDocument(url, baseUrl);
				else
					r.setHtmlDocument(meta.getMetadata(), baseUrl);
				BufferedImage [] bis = r.getImages(width, height);
				if (bis != null) {
					SBScrollPanePlus sp = new SBScrollPanePlus(parent, x, y, width, height);
					v = sp;
					int offy = 0;
					for (BufferedImage bi : bis) {
						setImage(sp, 0, offy, bi.getWidth(), bi.getHeight(), bi, false);
						offy+= height;
					}
					sp.refresh();
					//v = setImage(parent, x, y, width, height, bi, false);
				}
			} catch(Exception e) {
				Log.error("Unable to render HTML: " + e.getMessage());
			}
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

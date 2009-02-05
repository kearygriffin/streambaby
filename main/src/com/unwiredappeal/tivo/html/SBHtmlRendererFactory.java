package com.unwiredappeal.tivo.html;

import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.utils.Log;

public class SBHtmlRendererFactory {
	
	public static String currentRenderer = null; 
	//public static final String defaultRenderer = "com.unwiredappeal.tivo.html.fs.FlyingSaucerRenderer";
	//public static final String defaultRenderer = "com.unwiredappeal.tivo.html.cobra.CobraRenderer";
	public static final String defaultRenderer = "com.unwiredappeal.tivo.html.htmlkit.HtmlKitRenderer";
	public static String[] renderClasses = new String[] {
			"com.unwiredappeal.tivo.html.fs.FlyingSaucerRenderer",
			"com.unwiredappeal.tivo.html.cobra.CobraRenderer",
			"com.unwiredappeal.tivo.html.htmlkit.HtmlKitRenderer"
			
			
	};
	public static SBHtmlRenderer getRenderer() {
		if (currentRenderer != null) {
			SBHtmlRenderer r = getRenderer(currentRenderer);
			if (r == null)
				Log.error("Can't instantiate current renderer:" + currentRenderer);
			return r;
		}
		
		
		String cl = StreamBabyConfig.cfgHtmlRenderer.getValue();
		if (cl == null || cl.length() == 0)
			cl = defaultRenderer;
		SBHtmlRenderer r;
		// try the default renderer first
		r = getRenderer(cl);
		if (r != null) {
			setCurrentRenderer(cl);
			return r;
		}
		
		for (String clr : renderClasses) {
			r = getRenderer(clr);
			if (r != null) {
				setCurrentRenderer(clr);
				return r;
			}
		}
		
		Log.error("Couldn't load any html renderers");
		return null;
	}
	
	protected synchronized static void setCurrentRenderer(String cl) {
		currentRenderer = cl;
	}
	protected static SBHtmlRenderer getRenderer(String cl) {
		SBHtmlRenderer r = null;
		try {
			r = (SBHtmlRenderer)Class.forName(cl).newInstance();
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (ClassNotFoundException e) {
		} catch (Exception e) { }
		return r;
		
	}
}

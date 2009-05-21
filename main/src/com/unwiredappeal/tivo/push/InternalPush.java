package com.unwiredappeal.tivo.push;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.config.ConfigurableObject;
import com.unwiredappeal.tivo.config.ConfigurationManager;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.metadata.MetaData;
import com.unwiredappeal.tivo.modules.VideoFormats;
import com.unwiredappeal.tivo.modules.VideoModuleHelper;
import com.unwiredappeal.tivo.utils.Log;

public class InternalPush extends ConfigurableObject implements PushHandler {
	public static double EST_SIZE_FACTOR = 1.025;
	public static int MAX_BITRATE = 16000;
	
	public static ConfigEntry cfgDirs = new ConfigEntry(
			"tivo",
			new tivoEntryHandler(),
			"tivo's that we can push to"
			);	

	private LinkedList<Tivo> tivos = new LinkedList<Tivo>();
	
	private static InternalPush instance = new InternalPush();

	public InternalPush() {
		this.populateConfig();
	}
	public boolean canPush(DirEntry de, int qual) {
		String mimeType = VideoModuleHelper.inst.getVideoMimeType(de, qual);
		return mimeType != null;
	}

	@SuppressWarnings("unchecked")
	public synchronized List<Tivo> getTivos() {
		return (List<Tivo>)tivos.clone();
	}

	public boolean pushVideo(URL baseUri, DirEntry de, Tivo tivo, int qual) {
		Log.info("intPush: " + de.getUri() + ", tivo: " + tivo);
		String mimeType = VideoModuleHelper.inst.getVideoMimeType(de, qual);
		if (mimeType == null)
			return false;
		
		Mind mind = new Mind(tivo.getMind());
		if (!mind.login(tivo.getUsername(), tivo.getPassword())) {
			mind.printErrors();
			Log.error("Failed to login to tivo-push-control: " + tivo.getUsername() + ", " + tivo.getPassword());
			return false;
		}
		PushNamedStream ps = new PushNamedStream(de, qual, tivo.getTsn());
		ps.setContentType(mimeType);		
		Hashtable<String, String> videoInfo = new Hashtable<String, String>();		
		// tsn, url, title, description, mime, duration
		//String url = "http://192.168.1.40:9032/MyMovies/Oktapodi.mp4?Format=video/mp4";
		String url = baseUri.toString() + ps.getStreamName();
		
		videoInfo.put("url", url);
		videoInfo.put("mime", mimeType);
		videoInfo.put("tsn", "tsn:" + tivo.getTsn());
		videoInfo.put("duration", Long.toString(de.getVideoInformation().getDuration() / 1000));
		
		double estSize;

		if (qual != VideoFormats.QUALITY_SAME || VideoModuleHelper.inst.canStream(de)) {
			int bitrate;
			if (qual == VideoFormats.QUALITY_SAME)
				bitrate = de.getVideoInformation().getBitRate();
			else
				bitrate = VideoModuleHelper.inst.getBitRateForQual(qual);		
			estSize = ((de.getVideoInformation().getDuration() / 1000.0) * (bitrate*1000))/8;
		}
		else { 
			estSize = -1;
			//int bitrate = MAX_BITRATE;		
			//estSize = ((de.getVideoInformation().getDuration() / 1000.0) * (bitrate*1000))/8;

		}
		
		videoInfo.put("size", Long.toString((long)(estSize*EST_SIZE_FACTOR)));
		MetaData meta = new MetaData();
		de.getMetadata(meta);
		String title = de.getName();
		if (meta.getTitle() != null)
			title = meta.getTitle();
			
		if (StreamBabyConfig.cfgUsePushFolders.getBool() && meta.getSeriesTitle() != null && meta.getEpisodeTitle() != null) {
			videoInfo.put("title", meta.getSeriesTitle());
			videoInfo.put("subtitle", meta.getEpisodeTitle());
			videoInfo.put("source", meta.getSeriesTitle());			
		} else {
			videoInfo.put("title", title);
			videoInfo.put("subtitle", title);
			videoInfo.put("source", title);
		}
		
		if (meta.getTextDescription() != null)
			videoInfo.put("description", meta.getTextDescription());
		try {
			boolean b = mind.pushVideo(videoInfo);
			if (!b) {
				mind.printErrors();				
				Log.error("failed to push video.");
				ps.closeNamedStream();
				return false;
			}
		} catch (MalformedURLException e) {
			Log.error("Failed to push video: " + e.getMessage());
			ps.closeNamedStream();
			return false;
			
		}
		return true;
	}
	
	public synchronized void addTivoTsn(String tsn, String name) {
		for (Tivo tivo : tivos) {
			if (tivo.getTsn().compareTo(tsn) == 0)
				return;
		}
		// Not already there, add it using the default tivo username and password
		String username = StreamBabyConfig.cfgTivoUsername.getValue();
		String password = StreamBabyConfig.cfgTivoPassword.getValue();
		String mind = StreamBabyConfig.cfgTivoMind.getValue();
		if (username == null || password == null || username.length() == 0 || password.length() == 0)
			return;
		String tivoName = name;
		if (name == null)
			tivoName = "Tivo-" + tsn.substring(tsn.length()-4);
		Tivo tivo = new Tivo(tivoName, tsn, username, password, mind, false);
		tivo.setAuto(true);
		tivos.add(0, tivo);
	}
	
	public synchronized void addTivo(String name, String tsn, String username, String password, String mind, boolean isExternal) {
		tivos.add(new Tivo(name, tsn, username, password, mind, isExternal));
	}

	public static InternalPush getInstance() {
		return instance;
	}
	
	private static class tivoEntryHandler implements ConfigurationManager.propertyHandler {

		public tivoEntryHandler() {
		}

		public void process(ConfigurableObject te, String key, String value) {
			/*
			if (value != null && value.length() > 2) {
				if (value.charAt(0) == '\"' && value.charAt(value.length()-1) == '\"') {
					value = value.substring(1, value.length()-1);
				}
			}
			*/
			String username = ConfigurationManager.inst.getStringProperty(key + ".username", null);
			String password = ConfigurationManager.inst.getStringProperty(key + ".password", null);
			String tsn = ConfigurationManager.inst.getStringProperty(key + ".tsn", null);
			String mind = ConfigurationManager.inst.getStringProperty(key + ".mind", null);
			boolean isExternal = ConfigurationManager.inst.getBooleanProperty(key + ".external", false);

			if (mind == null || mind.length() == 0)
				mind = StreamBabyConfig.cfgTivoMind.getValue();
			if (username == null || username.length() == 0)
				username = StreamBabyConfig.cfgTivoUsername.getValue();
			if (password == null || password.length() == 0)
				password = StreamBabyConfig.cfgTivoPassword.getValue();
			if (username == null || password == null || tsn == null || username.length() == 0 || password.length() == 0 || tsn.length() == 0) {
				Log.debug("Error: Tivo: " + value + ", not all info supplied");
				return;
			}
			
			((InternalPush)te).addTivo(value, tsn, username, password, mind, isExternal);
			Log.debug("Added tivo: " + value + ", tsn: " + tsn);
			
		}

	}

}

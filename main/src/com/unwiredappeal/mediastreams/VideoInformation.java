package com.unwiredappeal.mediastreams;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.unwiredappeal.tivo.utils.InfoCache;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.modules.VideoFormats;
import com.unwiredappeal.tivo.modules.VideoModuleHelper;

public class VideoInformation extends InfoCache.Cacheable {
	
	private Map<String, String> metadataMap = new HashMap<String, String>();
	private static final String CACHE_NAME = VideoInformation.class.toString();
	public long videoLength;
	//String audioCodec;
	//String videoCodec;
	public static final String UNK = VideoFormats.UNKNOWN_FORMAT;
	String containerFormat = UNK;
	//long fileLength;
	URI uri;
	boolean isValid;
	float aspect;
	long duration = -1;
	private int bitRate = 0;
	private double fps = 0;
	private int width = 0;
	private int height = 0;
	private int audioBps = 0;
	int audioChannels = 0;
	long fileLength;
	long startPosition = 0;
	private String audioCodec  = UNK;
	private String videoCodec  = UNK;
	boolean isFile = false;
	float pixelAspect = 0;
	public VideoInformation(URI uri) {
		this.uri= uri;
		if (Utils.isFile(uri)) {
			isFile = true;
			fileLength = new File(uri).length();
		}
		isValid = VideoModuleHelper.inst.fillVideoInformation(uri, this);
		if (isValid) {
			isValid = confirmVideoFormats();
		}
	}
	
	public boolean isValid() {
		return isValid && duration > 0;
	}
	
	public boolean confirmVideoFormats() {
		if (duration < 1)
			return false;
		// Should check various video combinations
		return true;
	}
	
	public static String hashFile(java.io.File f) {
		String hash = f.getPath();
		if (f.exists()) {
			try {
				hash = f.getCanonicalPath();
				long len = f.length();
				hash = "file:len." + len + "." + hash;
			} catch (IOException e) {
			}
		}
		return hash;
	}
	public static synchronized VideoInformation getVideoInformation(URI uri) {
		String hash;
		if (Utils.isFile(uri)) {
			File f= new File(uri);
			hash = hashFile(f);
		} else {
			hash = "uri:" + uri.toString();
		}
		VideoInformation vinfo = (VideoInformation)InfoCache.getInstance().getEntry(CACHE_NAME, hash);
		if (vinfo != null)
			return vinfo;
		// Doesn't exist, create it and store it
		vinfo = new VideoInformation(uri);
		InfoCache.getInstance().putEntry(CACHE_NAME, hash, vinfo);
		return vinfo;
	}
	
	@Override
	public boolean isExpired() {
		if (isFile) {
			File f = new File(uri);
			if (f.exists() && f.isFile() && f.length() == fileLength)
				return false;
			return true;
		}
		return false;
	}
	public long getDuration() {
		return duration;
	}
	
	public void setStartPosition(long p) {
		startPosition = p;
	}
	public long getStartPosition() {
		return startPosition;
	}
	public void setDuration(long dur) {
		duration = dur;
	}
	public long getVideoLength() {
		return getDuration();
	}
	

	public void setVideoCodec(String codecString) {
		videoCodec = codecString != null ? codecString : UNK;
		
	}

	public void setAudioCodec(String codecString) {
		audioCodec = codecString != null ? codecString : UNK;;		
	}

	public String getContainerFormat() {
		return containerFormat;
	}

	public void setContainerFormat(String containerFormat) {
		this.containerFormat = containerFormat != null ? containerFormat : UNK;;
	}

	public String getAudioCodec() {
		return audioCodec;
	}

	public String getVideoCodec() {
		return videoCodec;
	}

	public void setVideoLength(long videoLength) {
		this.videoLength = videoLength;
	}

	public void setBitRate(int i) {
		this.bitRate = i;
		
	}
	
	public int getBitRate() {
		return this.bitRate;
	}

	public double getFps() {
		return fps;
	}

	public void setFps(double fps) {
		this.fps = fps;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getAudioBps() {
		return audioBps;
	}

	public void setAudioBps(int audioBps) {
		this.audioBps = audioBps;
	}

	public int getAudioChannels() {
		return audioChannels;
	}

	public void setAudioChannels(int audioChannels) {
		this.audioChannels = audioChannels;
	}
	
	public void setPixelAspect(float a) {
		this.pixelAspect = a;
	}
	
	public float getPixelAspect() {
		if (pixelAspect == 0)
			return 1.0f;
		return pixelAspect;
	}
	
	public void setAspect(float aspect) {
		this.aspect = aspect;
	}
	public float getAspect() {
		if (aspect != 0)
			return aspect;
		if (getWidth() == 0 || getHeight() == 0) {
			return 16/9.0f;
		}
		return ((float)getWidth() / (float)getHeight()) * getPixelAspect();
	}

	public String toString() {
		String beg = "uri: " + uri + ", Container: " + containerFormat + ", Duration: " + duration/1000.0 + " seconds";
		String audio = "Audio: " + audioCodec + " " + audioBps + "HZ" + " " + audioChannels + " channels";
		String video = "Video: " + videoCodec + " " + width + "x" + height + " " + fps + " fps";
		video = video + "\n  PixAspect: " + getPixelAspect() + ", Aspect: " + getAspect();
		return beg + "\n" + video + "\n" + audio;
	}
	


	public Map<String, String> getMetadataMap() {
		return metadataMap;
	}
	
	public void setMetadataItem(String key, String val) {
		metadataMap.put(key, val);
	}

	public String getMetdataItem(String key) {
		return metadataMap.get(key);
	}
}

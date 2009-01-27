package com.unwiredappeal.tivo.vm.ffjava;


import com.sun.jna.Platform;
import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.config.ConfigurableObject;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.utils.Log;

import net.sf.ffmpeg_java.FFmpegMgr;

public class FFmpegJavaConfig extends ConfigurableObject {

	public static boolean isWindows = Platform.isWindows();
	public static boolean isLinux = Platform.isLinux();
	
	public static ConfigEntry cfgAvUtilLib = new ConfigEntry(
			"ffmpegjava.avutil",
			 isWindows ? "avutil-49" : "avutil",
			"path of libavutil  to load"
			);
	public static ConfigEntry cfgAvCodecLib = new ConfigEntry(
			"ffmpegjava.avcodec",
			isWindows  ? "avcodec-52" : "avcodec",
			"path of libavcodec to load"
			);
	public static ConfigEntry cfgAvFormatLib = new ConfigEntry(
			"ffmpegjava.avformat",
			isWindows  ? "avformat-52" : "avformat",
			"path of libavformat to load"
			);	
	public static ConfigEntry cfgSwScaleLib = new ConfigEntry(
			"ffmpegjava.swscale",
			isWindows  ? "swscale-0" : "swscale",
			"path of libswscale to load"
			);	
	//public static net.sf.ffmpeg_java.v51.AVCodecLibrary avCodec_51;
	//public static net.sf.ffmpeg_java.v51.AVFormatLibrary avFormat_51;
	//public static net.sf.ffmpeg_java.v52.AVCodecLibrary avCodec_52;
	//public static net.sf.ffmpeg_java.v52.AVFormatLibrary avFormat_52;

	protected static boolean hasFFmpegLibrary = false;
	protected static boolean isInited = false;
	public static boolean hasSwScale = true; // assume true until we fail
	public  static FFmpegJavaConfig inst = new FFmpegJavaConfig();
	public int _avCodecVersion;
	public int _avCodecMajor;
	
	protected FFmpegJavaConfig() { 
		populateConfig();
	}
	
	public synchronized void initFFmpegLibrary() {
		if (isInited)
			return;
		isInited = true;
		FFmpegMgr.logger = new FFmpegMgr.Logger() {
			public void log(String s) {
				Log.debug(s);
			}
		};		


		try {
			String nativePath = StreamBabyConfig.nativeDir;
			FFmpegMgr.LibDefaults def = new FFmpegMgr.LibDefaults(
					52, 52,
					cfgAvUtilLib.value,
					cfgAvCodecLib.value,
					cfgAvFormatLib.value,
					cfgSwScaleLib.value
					);
			def.logLevel = StreamBabyConfig.inst._DEBUG ? 1 : -1;
			
			FFmpegMgr.LibDefaults ret  = FFmpegMgr.initLibrary(def, nativePath);
			if (ret == null) {
				hasFFmpegLibrary = false;
				return;
			}
			
			hasSwScale = FFmpegMgr.hasSwScale;
			_avCodecVersion = FFmpegMgr.getAvCodecVersion();

			hasFFmpegLibrary = true;
		} catch(Throwable e) {
			hasFFmpegLibrary = false;
		}
	}
	
	public  boolean useFFmpegLibrary() {
		if (!isInited)
			initFFmpegLibrary();
		return hasFFmpegLibrary;
	}
	

}

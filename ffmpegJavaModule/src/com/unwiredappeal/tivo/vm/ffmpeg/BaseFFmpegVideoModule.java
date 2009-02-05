package com.unwiredappeal.tivo.vm.ffmpeg;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ffmpeg_java.FFMPEGLibrary;

import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.tivo.modules.BaseVideoHandlerModule;
import com.unwiredappeal.tivo.modules.VideoFormats;

public class BaseFFmpegVideoModule extends BaseVideoHandlerModule {

	
	public static final String containerFormatsArray[] = {"mp4" , "mpeg", "avi", "matroska", "asf", "mp3", "mpegvideo", "flv" };
	// codecs:  ac3, mp4a, h264 mpeg2video, mp2, (special case: 0x0000 == ac3)
	protected  static Map<String, String> containerMap = new HashMap<String, String>();
	protected static Map<String, String> codecMap = new HashMap<String, String>();
	protected static Map<String, String> audioCodecMap = new HashMap<String, String>();
	protected  static Map<String, String> videoCodecMap = new HashMap<String, String>();

	static {
		containerMap.put("mp4", VideoFormats.CONTAINER_MP4);
		containerMap.put("mpeg", VideoFormats.CONTAINER_MPEGPS);
		containerMap.put("avi", VideoFormats.CONTAINER_AVI);
		containerMap.put("matroska", VideoFormats.CONTAINER_MKV);
		containerMap.put("asf", VideoFormats.CONTAINER_WMV);
		containerMap.put("mp3", VideoFormats.CONTAINER_MP3);
		containerMap.put("mpegvideo", VideoFormats.CONTAINER_MPEGES);
		containerMap.put("flv", VideoFormats.CONTAINER_FLV);
		
		audioCodecMap.put("mp2", VideoFormats.AUDIO_CODEC_MP2);
		audioCodecMap.put("mp3", VideoFormats.AUDIO_CODEC_MP3);
		audioCodecMap.put("ac3", VideoFormats.AUDIO_CODEC_AC3);
		audioCodecMap.put("liba52", VideoFormats.AUDIO_CODEC_AC3);		
		audioCodecMap.put("mp4a", VideoFormats.AUDIO_CODEC_AAC);
		audioCodecMap.put("aac", VideoFormats.AUDIO_CODEC_AAC);
		audioCodecMap.put("mpeg4aac", VideoFormats.AUDIO_CODEC_AAC);

		videoCodecMap.put("h264",VideoFormats.VIDEO_CODEC_H264);
		videoCodecMap.put("mpeg2video", VideoFormats.VIDEO_CODEC_MPEG2);
		videoCodecMap.put("mpeg1video", VideoFormats.VIDEO_CODEC_MPEG1);
		
		codecMap.putAll(audioCodecMap);
		codecMap.putAll(videoCodecMap);

	}
	public static final List<String> containerFormatsList =  Arrays.asList(containerFormatsArray);
	
	public static final double[] mpegFps = new double[] {
		23.976, 24, 25, 29.97, 30
	};
	
	//public static AllowableFormats streamableFormats = new AllowableFormats(new Formats(new String[] { VideoFormats.CONTAINER_MPEGPS }, new String[] { VideoFormats.VIDEO_CODEC_MPEG2 }, new String[] {VideoFormats.AUDIO_CODEC_AC3, VideoFormats.AUDIO_CODEC_MP2}), null);
	//public static AllowableFormats previewableFormats = new AllowableFormats(new Formats(containerMap.values().toArray(new String[0]), videoCodecMap.values().toArray(new String[0]), new String[] { "*" }), null);
	//public static AllowableFormats transcodableFormats = new AllowableFormats(new Formats(containerMap.values().toArray(new String[0]), videoCodecMap.values().toArray(new String[0]), new String[] { "*" }), null);
	/*
	public static AllowableFormats streamableFormats = new AllowableFormats(
				// allowed
				Arrays.asList(new Format[] { 
						new Format(VideoFormats.CONTAINER_MPEGPS, VideoFormats.VIDEO_CODEC_MPEG2, VideoFormats.AUDIO_CODEC_AC3),
						new Format(VideoFormats.CONTAINER_MPEGPS, VideoFormats.VIDEO_CODEC_MPEG2, VideoFormats.AUDIO_CODEC_MP2)
				}),
				// disallowed
				null
			);
	*/

	protected String aviTagDecode(int codec_tag) {
		long id = (codec_tag & 0xffffffffL);
		byte[] bytes = new byte[4];
		bytes[0] = (byte)(id & 0xff);
		bytes[1] = (byte)((id >>> 8) & 0xff);
		bytes[2] = (byte)((id >>> 16) & 0xff);
		bytes[3] = (byte)((id >>> 24) & 0xff);
		String str = null;
		try {
			for (int i=0;i<4;i++) {
				char c = (char)bytes[i];
				if (!Character.isLetterOrDigit(c))
					return null;
			}
			str = new String(bytes);
		} catch(Exception e) { }
		return str;
	}

	public double av_q2d(FFMPEGLibrary.AVRational a) {
		return a.num / (double) a.den;
	}
	
	protected static void setContainerFormat(String formatString,
			VideoInformation vidinfo) {
		String format = null;
		if (formatString != null) {
			String split[] = formatString.split(",");
			for (int i=0;i<split.length && format == null;i++) {
				if (containerFormatsList.contains(split[i]))
					format = split[i];
			}
			vidinfo.setContainerFormat(translateContainer(format));
		}
	}
	
	protected static String translateContainer(String format) {
		String id = containerMap.get(format);
		if (id == null)
			id = VideoFormats.UNKNOWN_FORMAT;
		return id;
	}

	protected static String translateCodec(String format) {
		String id = codecMap.get(format);
		if (id == null)
			id = VideoFormats.UNKNOWN_FORMAT;
		return id;
	}



}

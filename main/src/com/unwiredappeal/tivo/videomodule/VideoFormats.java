package com.unwiredappeal.tivo.videomodule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import com.unwiredappeal.mediastreams.VideoInformation;

public class VideoFormats {
	
	public static final String UNKNOWN_FORMAT = "UNK";
	public static final String CONTAINER_MP4 = "mp4";
	public static final String CONTAINER_AVI = "avi";
	public static final String CONTAINER_MKV = "mkv";
	public static final String CONTAINER_MPEGPS = "mpeg";
	public static final String CONTAINER_MPEGTS = "mpegts";
	public static final String CONTAINER_WMV = "wmv";
	public static final String CONTAINER_MPEGES = "mpeges";
	public static final String CONTAINER_TIVO = "tivo";
	public static final String CONTAINER_RAW = "raw";
	public static final String VIDEO_CODEC_NONE = "none";
	
	public static final String AUDIO_CODEC_MP2 = "mp2";
	public static final String AUDIO_CODEC_AC3 = "ac3";
	public static final String AUDIO_CODEC_AAC = "aac";
	public static final String AUDIO_CODEC_MP3  = "mp3";
	public static final String AUDIO_CODEC_WMA2 = "wma2";
	public static final String AUDIO_CODEC_NONE = "none";
	
	public static final String VIDEO_CODEC_H264 = "h264";
	public static final String VIDEO_CODEC_MPEG2 = "mp2v";
	public static final String VIDEO_CODEC_MPEG1 = "mp1v";
	public static final String VIDEO_CODEC_VC1 = "vc1";
	public static final String CONTAINER_MP3 = "mp3";
	public static final String CONTAINER_FLV = "flv";
	
	public static final int QUALITY_AUTO = -2;
	public static final int QUALITY_SAME = -1;
	
	public static final int QUALITY_LOWEST = 1;
	public static final int QUALITY_LOW = 2;
	public static final int QUALITY_MEDIUMLOW = 3;
	public static final int QUALITY_MEDIUM = 4;
	public static final int QUALITY_MEDIUMHIGH = 5;
	public static final int QUALITY_HIGH = 6;
	public static final int QUALITY_HIGHEST = 7;

	public static final int LAST_QUALITY = 7;
	public static final int LOWEST_AUTO_BITRATE = 512;
	
	
	public static final Collection<String> wildCardList = Arrays.asList(new String[] { "*" } );

	protected static boolean checkFormatOk(Collection<String> allowed, Collection<String> disallowed, String s) {
		if (allowed == null)
			return false;
		if (disallowed != null && disallowed.contains(s))
			return false;
		if (allowed.contains("*") || allowed.contains(s))
			return true;
		return false;
	}
	
	public static boolean checkFormatIn(Format format, Collection<Format> flist) {
		if (format == null)
			return false;
		if (flist == null)
			return false;
		for (Format f : flist) {
			if (!(f.videoCodec.equals("*") || f.videoCodec.equals(format.videoCodec)))
				continue;
			if (!(f.audioCodec.equals("*") || f.audioCodec.equals(format.audioCodec)))
				continue;
			if (!(f.container.equals("*") || f.container.equals(format.container)))
				continue;
			return true;
		}
		return false;
	}
	
	public static boolean isAllowed(AllowableFormats a, VideoInformation vid) {
		if (a == null)
			return false;
		if (vid == null)
			return false;
		Collection<Format> allowed = a.allowed;
		Collection<Format> disallowed = a.disallowed;
		if (allowed == null)
			return false;
		if (!checkFormatIn(new Format(vid.getContainerFormat(), vid.getVideoCodec(), vid.getAudioCodec()), allowed))
			return false;
		if (!checkFormatIn(new Format(vid.getContainerFormat(), vid.getVideoCodec(), vid.getAudioCodec()), disallowed))
			return true;
		else
			return false;
	}
	
	public static class Format  {
		public String videoCodec;
		public String audioCodec;
		public String container;
		public Format(String c, String v, String a) {
			this.container = c;
			this.videoCodec = v;
			this.audioCodec = a;
		}
		public Format(String s) {
			if (s == null)
				return;
			String split[] = s.split(",");
			if (split.length > 0)
				container = split[0];
			else
				container = "*";
			if (split.length > 1)
				videoCodec = split[1];
			else
				videoCodec = "*";			
			if (split.length > 2)
				audioCodec = split[2];
			else
				audioCodec = "*";
			
		}
	}
	/*
	public static class Formats {
		public List<String> containers;
		public List<String> videoCodecs;
		public List<String> audioCodecs;

		public Formats() { }
		public Formats(String[] cf, String[] video, String[] audio) {
			containers = asList(cf);
			videoCodecs = asList(video);
			audioCodecs = asList(audio);
		}

		public Formats(List<String> cf, List<String> video, List<String> audio) {
			containers = cf;
			videoCodecs = video;
			audioCodecs = audio;
		}
		private List<String> asList(String[] a) {
			if (a == null)
				return null;
			return Arrays.asList(a);
		}
	}
	*/
	
	public static class AllowableFormats {
		public Collection<Format> allowed;
		public Collection<Format> disallowed;
		public AllowableFormats(Collection<Format> a,  Collection<Format> d) {
			allowed = a;
			disallowed = d;
		}
		
		public AllowableFormats(String allowedString, String disallowedString) {
			allowed = createFormatList(allowedString);
			disallowed = createFormatList(disallowedString);
		}
		
		public static Collection<Format> createFormatList(String str) {
			if (str == null || str.length() == 0)
				return null;
			Collection<Format> list = new ArrayList<Format>();
			String split[] = str.split(";");
			for (String s : split) {
				list.add(new Format(s));
			}
			return list;
		}
	}

	public static Collection<Format> createFormatList(Collection<String> containers,
			Collection<String> videoCodecs, Collection<String> audioCodecs) {
			Collection<Format> list = new ArrayList<Format>();
		
			for (String c : containers) {
				for (String v : videoCodecs) {
					for (String a : audioCodecs) {
						list.add(new Format(c, v, a));
					}
				}
			}
			return list;
	}

	public static Collection<String>asList(String f) {
		return Arrays.asList(new String[] { f }) ;
	}
}

package com.unwiredappeal.tivo.videomodule;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import com.unwiredappeal.mediastreams.PreviewGenerator;
import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.mediastreams.VideoInputStream;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.Utils;

public class VideoModuleHelper {
	public static VideoModuleHelper inst = new VideoModuleHelper();
	public List<VideoHandlerModule> modules = new ArrayList<VideoHandlerModule>();
	
	public boolean hasModule(String className) {
		Iterator<VideoHandlerModule> it = modules.iterator();
		while(it.hasNext()) {
			VideoHandlerModule m = it.next();
			if (m.getClass().getCanonicalName().compareTo(className) == 0)
				return true;
			
		}
		return false;
	}
	public boolean addModule(String className) {
		if (hasModule(className))
			return true;
		try {
			Class<? extends StreamBabyModule> cl = Class.forName(className).asSubclass(StreamBabyModule.class);
			StreamBabyModule sm = null;
			if (cl != null)
				sm = (StreamBabyModule)cl.newInstance();
			
			VideoHandlerModule m = null;
			if (sm != null)
				m  = (VideoHandlerModule)sm.getModule(StreamBabyModule.STREAMBABY_MODULE_VIDEO);
			if (m != null && m.initialize()) {
				modules.add(m);
				return true;
			} else 
				return false;
		} catch (InstantiationException e) {
			Log.error("Unable to load videoModule: " + className + ", (InstExcept)Err: " + e.getMessage());
			return false;
		} catch (IllegalAccessException e) {
			Log.error("Unable to load videoModule: " + className + ", (IllAccessExcept)Err: " + e.getMessage());
			return false;
		} catch (ClassNotFoundException e) {
			Log.error("Unable to load videoModule: " + className + ", (CNotFound)Err: " + e.getMessage());
			return false;
		}
	}
	
	public boolean canStreamVideo(URI uri, VideoInformation vinfo) {
		Iterator<VideoHandlerModule> it = modules.iterator();
		while(it.hasNext()) {
			VideoHandlerModule m = it.next();
			if (m.canStream(uri, vinfo)) {
				return true;
			}
				
		}
		return false;
		
	}
	
	public boolean canStreamOrTranscodeVideo(URI uri, VideoInformation vinfo) {
		boolean disableTranscode = StreamBabyConfig.cfgDisableTranscode.getBool();
		Iterator<VideoHandlerModule> it = modules.iterator();
		while(it.hasNext()) {
			VideoHandlerModule m = it.next();
			if (m.canStream(uri, vinfo)) {
				return true;
			}
			if (!disableTranscode && m.canTranscode(uri, vinfo))
				return true;
				
		}
		return false;
		
	}

	public boolean canTranscode(URI uri, VideoInformation vinfo) {
		boolean disableTranscode = StreamBabyConfig.cfgDisableTranscode.getBool();
		if (disableTranscode)
			return false;
		Iterator<VideoHandlerModule> it = modules.iterator();
		while(it.hasNext()) {
			VideoHandlerModule m = it.next();
			if (m.canTranscode(uri, vinfo))
				return true;
				
		}
		return false;
		
	}

	
	public boolean canStream(URI uri, VideoInformation vinfo) {
		Iterator<VideoHandlerModule> it = modules.iterator();
		while(it.hasNext()) {
			VideoHandlerModule m = it.next();
			if (m.canStream(uri, vinfo)) {
				return true;
			}
		}
		return false;		
	}

	
	public static interface GetPriority {
		int getPriority(VideoHandlerModule m);
	}
	private Iterator<VideoHandlerModule> getModulesIterator(final GetPriority p) {
		List<VideoHandlerModule> sortedList = new ArrayList<VideoHandlerModule>(modules);
		Collections.sort(sortedList, new Comparator<VideoHandlerModule>() {

			public int compare(VideoHandlerModule o1, VideoHandlerModule o2) {
				return p.getPriority(o2) - p.getPriority(o1);
			}
			
		});
		return sortedList.iterator();
	}
	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vinfo, long startPosition) {
		VideoInputStream st = null;
		Iterator<VideoHandlerModule> it = getModulesIterator(new GetPriority() { 
				public int getPriority(VideoHandlerModule m) 
				{ return m.getPriorities().streamPriority; }
			}
		);
		while(st == null && it.hasNext()) {
			VideoHandlerModule m = it.next();
			if (m.canStream(uri, vinfo)) {
				try {
					st = m.openStreamableVideo(uri, vinfo, startPosition);
				} catch (IOException e) {

				}
			}
				
		}
		return st;
		
	}
	public VideoInputStream openTranscodedVideo(URI uri, VideoInformation vinfo, long startPos, int qual) {
		boolean disableTranscode = StreamBabyConfig.cfgDisableTranscode.getBool();
		if (disableTranscode)
			return null;
		VideoInputStream st = null;
		Iterator<VideoHandlerModule> it = getModulesIterator(new GetPriority() { 
			public int getPriority(VideoHandlerModule m) 
			{ return m.getPriorities().transcodePriority; }
		}
	);
		while(st == null && it.hasNext()) {
			VideoHandlerModule m = it.next();
			if (m.canTranscode(uri, vinfo)) {
				try {
					st = m.openTranscodedVideo(uri, vinfo,  startPos, qual);
				} catch (IOException e) {

				}
			}
				
		}
		return st;
		
	}
	public PreviewGenerator getPreviewHandler(URI uri, VideoInformation vinfo, boolean realtime) {
		PreviewGenerator st = null;
		Iterator<VideoHandlerModule> it = getModulesIterator(new GetPriority() { 
			public int getPriority(VideoHandlerModule m) 
			{ return m.getPriorities().previewPriority; }
		}
		);
		while(st == null && it.hasNext()) {
			VideoHandlerModule m = it.next();
			if (m.canPreview(uri, vinfo, realtime)) {
				st = m.getPreviewHandler(uri, vinfo, realtime);
			}
				
		}
		return st;		
	}
	
	
	public boolean handleSpecial(URI uri, VideoInformation vidinfo) {
		if (Utils.isFile(uri)) {
			if (uri.getPath().toLowerCase().endsWith(".tivo")) {
				vidinfo.setContainerFormat(VideoFormats.CONTAINER_TIVO);
			}
			if (vidinfo.getBitRate() <= 0) {
				int br = (int)(8 * ((new File(uri).length()) / (vidinfo.getDuration() / 1000.0f)));
				Log.debug("Guessing bitrate for " + uri + " to: " + br);
				vidinfo.setBitRate(br);
			}
		}
		return true;
	}
	public boolean fillVideoInformation(URI uri, VideoInformation vidinfo) {
		Log.debug("GetVidInfo: " + uri);
		Iterator<VideoHandlerModule> it = getModulesIterator(new GetPriority() { 
			public int getPriority(VideoHandlerModule m) 
			{ return m.getPriorities().fillVideoPriority; }
		}
		);
		boolean b = false;
		while(!b && it.hasNext()) {
			VideoHandlerModule m = it.next();
			b = m.fillVideoInformation(uri, vidinfo);
		}
		if (b == true) {
			b = handleSpecial(uri, vidinfo);
		}
		return b;				
	}
	
	public boolean canPreview(URI uri, VideoInformation vi, boolean realtime) {
		Iterator<VideoHandlerModule> it = modules.iterator();
		boolean b = false;
		while(!b && it.hasNext()) {
			VideoHandlerModule m = it.next();
			b = m.canPreview(uri, vi, realtime);
		}
		return b;				
				
	}
	
	public boolean hasRealtimePreview() {
		Iterator<VideoHandlerModule> it = modules.iterator();
		boolean b = false;
		while(!b && it.hasNext()) {
			VideoHandlerModule m = it.next();
			b = m.canPreview(true);
		}
		return b;				
		
	}
	public int getModuleCount() {
		return modules.size();
	}
	public int getBitRateForQual(int qual) {
		int br = StreamBabyConfig.inst.getVideoBr(qual) + StreamBabyConfig.inst.getAudioBr(qual);
		Log.debug("Bitrate for quality: " + br);
		return br;
	}
	public VideoInputStream openVideo(URI deUri,
			VideoInformation videoInformation, long startPosition, int qual) {
		if (qual == VideoFormats.QUALITY_AUTO) {
			// openVideo should never be called with QUALITY_AUTO
			Log.error("ERROR!: openVideo called with QUALITY_AUTO.  This should never happen. Assuming QUALITY_SAME");
			qual = VideoFormats.QUALITY_SAME;
		}
		VideoInputStream vis = null;
		if (qual == VideoFormats.QUALITY_SAME || videoInformation.getBitRate() <= getBitRateForQual(qual)) {
			Log.debug("quality setting is above quality of video, streaming normally");
			vis = openStreamableVideo(deUri, videoInformation, startPosition);
		}
		if (vis != null)
			return vis;
		return openTranscodedVideo(deUri, videoInformation, startPosition, qual);
	}
}

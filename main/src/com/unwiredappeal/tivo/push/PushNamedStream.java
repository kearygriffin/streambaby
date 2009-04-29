package com.unwiredappeal.tivo.push;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.unwiredappeal.mediastreams.AutoPreviewGenerationManager;
import com.unwiredappeal.mediastreams.VideoInputStream;
import com.unwiredappeal.mediastreams.AutoPreviewGenerationManager.PreviewTask;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.modules.VideoModuleHelper;
import com.unwiredappeal.tivo.streambaby.PreviewCacheUtils;
import com.unwiredappeal.tivo.streambaby.PreviewCacheUtils.UriCallback;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.NamedStream;

public class PushNamedStream extends NamedStream {
	DirEntry de;
	int qual;
	String tsn;
	long creationTime;
	public static final long MAX_LIVE_HOURS = 12;
	public static final long MAX_LIVE_TIME = (1000L * 60) * 60 * MAX_LIVE_HOURS; 
	public static Set<PushNamedStream> pushNamedStreams = new HashSet<PushNamedStream>();
	public PushNamedStream(DirEntry de, int qual, String tsn) {
		super();
		this.de = de;
		this.qual = qual;
		this.tsn = tsn;
		this.creationTime = System.currentTimeMillis();
	}
	
	public InputStream open() throws IOException {
		VideoInputStream vis = VideoModuleHelper.inst.openVideo(de, 0L, qual);
		if (vis == null)
			throw new IOException("Can't open video: " + de.getName());
		return vis.getInputStream();
			
	}
	
	public synchronized static void addPushNamedStream(PushNamedStream p) {
		pushNamedStreams.add(p);
	}
	
	/*
	public synchronized static void removePushNamedStream(PushNamedStream p) {
		pushNamedStreams.remove(p);
		NamedStream.deregisterStream(p.getStreamName());
	}
	*/
	
	public synchronized static void cleanPushNamedStreams() {
		long curTime = System.currentTimeMillis();
		Iterator<PushNamedStream> it = pushNamedStreams.iterator();
		while(it.hasNext()) {
			PushNamedStream p = it.next();
			if (curTime - p.creationTime > MAX_LIVE_TIME) {
				Log.info("Removing expired PushNamedStream: " + p.getStreamName());
				NamedStream.deregisterStream(p.getStreamName());
				it.remove();
			}
		}
	}
	public static void schedule(Timer t) {
		t.schedule(new PushTimerTask(t), 60*5*1000L);
	}
	
	public static class PushTimerTask extends TimerTask {
		public Timer timer;
		public PushTimerTask(Timer t) {
			timer = t;
		}
		
		@Override
		public void run() {
			//Log.debug("PushTimerTask runninng...");
			cleanPushNamedStreams();
			schedule(timer);
		}
	}
	
}

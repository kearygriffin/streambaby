package com.unwiredappeal.tivo.videomodule;

import java.io.IOException;
import java.net.URI;

import com.unwiredappeal.mediastreams.PreviewGenerator;
import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.mediastreams.VideoInputStream;


public interface VideoHandlerModule {
	public static final String CONTAINER_MP4 = "mp4";
	public static int MAX_PRIORITY = 100;
	public static int MIN_PRIORITY = 0;
	public static int DEFAULT_PRIORITY = 50;
	
	public boolean initialize();
	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vi, long startPosition) throws IOException;
	public VideoInputStream openTranscodedVideo(URI uri, VideoInformation vi, long startPosition, int qual) throws IOException;
	public PreviewGenerator getPreviewHandler(URI uri, VideoInformation vi, boolean realtime);
	public boolean fillVideoInformation(URI uri, VideoInformation vidinfo);
	public boolean canStream(URI uri, VideoInformation vinfo);
	public boolean canPreview(URI  uri, VideoInformation vinfo, boolean realtime);
	public boolean canTranscode(URI uri, VideoInformation vinfo);
	public boolean canPreview(boolean realtime);
	public VideoHandlerPriorities getPriorities();

	public static class VideoHandlerPriorities {
			public int previewPriority = DEFAULT_PRIORITY;
			public int fillVideoPriority = DEFAULT_PRIORITY;
			public int streamPriority = DEFAULT_PRIORITY;
			public int transcodePriority = DEFAULT_PRIORITY;
		}
	}

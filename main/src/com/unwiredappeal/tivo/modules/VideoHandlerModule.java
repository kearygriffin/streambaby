package com.unwiredappeal.tivo.modules;

import java.io.IOException;
import java.net.URI;

import com.unwiredappeal.mediastreams.PreviewGenerator;
import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.mediastreams.VideoInputStream;


public interface VideoHandlerModule {
	public boolean initialize(StreamBabyModule parentMod);
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
			public int previewPriority = StreamBabyModule.DEFAULT_PRIORITY;
			public int fillVideoPriority = StreamBabyModule.DEFAULT_PRIORITY;
			public int streamPriority = StreamBabyModule.DEFAULT_PRIORITY;
			public int transcodePriority = StreamBabyModule.DEFAULT_PRIORITY;
			public VideoHandlerPriorities() { }
			public VideoHandlerPriorities(final int p) {
			}
			public void setPriority(int p) {
				previewPriority = p;
				fillVideoPriority = p;
				streamPriority = p;
				transcodePriority = p;				
			}
		}
	}

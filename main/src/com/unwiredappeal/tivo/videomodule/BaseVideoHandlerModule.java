package com.unwiredappeal.tivo.videomodule;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import com.unwiredappeal.mediastreams.PreviewGenerator;
import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.mediastreams.VideoInputStream;
import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.config.ConfigurableObject;
import com.unwiredappeal.tivo.videomodule.VideoFormats;
import com.unwiredappeal.tivo.videomodule.VideoFormats.AllowableFormats;
import com.unwiredappeal.tivo.videomodule.VideoFormats.Format;

public class BaseVideoHandlerModule extends ConfigurableObject implements VideoHandlerModule {
	
	public VideoHandlerPriorities pris = new VideoHandlerPriorities();

	public BaseVideoHandlerModule() {
		this.populateConfig();
	}
	public boolean initialize() {
		return true;
	}
	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vi, long startPosition) throws IOException {
		return null;
	}
	public VideoInputStream openTranscodedVideo(URI uri, VideoInformation vi, long startPosition, int br_kbps) throws IOException {
		return null;
	}
	public PreviewGenerator getPreviewHandler(URI uri, VideoInformation vi, boolean realtime) {
		return null;
	}
	public boolean fillVideoInformation(URI uri, VideoInformation vidinfo) {
		return false;
	}
	public boolean canStream(URI uri, VideoInformation vinfo) {
		return isFormatStreamable(vinfo);
	}
	
	public boolean canPreview(boolean realtime) {
		return false;
	}
	public boolean canPreview(URI  uri, VideoInformation vinfo, boolean realtime) {
		return canPreview(realtime) && canStream(uri, vinfo);
	}
	public boolean canTranscode(URI uri, VideoInformation vinfo) {
		return isFormatTranscodable(vinfo);
	}
	

	public boolean isFormatStreamable(VideoInformation vidinfo) {
		return VideoFormats.isAllowed(getStreamableFormats(), vidinfo);
	}
	
	public boolean isFormatTranscodable(VideoInformation src) {
		return VideoFormats.isAllowed(getTranscodeSrcFormats(), src); /* && VideoFormats.isAllowed(getTranscodeDstFormats(), dst) */
	}
	

	
	private static AllowableFormats nullFormats = new AllowableFormats("", "");
	public AllowableFormats getStreamableFormats() {
		return nullFormats;
	}
	
	public AllowableFormats getTranscodeSrcFormats() {
		return nullFormats;
	}

	public AllowableFormats configFormats(ConfigEntry a, ConfigEntry d, AllowableFormats def) {
		Collection<Format> allowed = null;
		Collection<Format> disallowed = null;
		if (def != null) {
			allowed = def.allowed;
			disallowed = def.disallowed;
		}
		if (!a.getValue().equals("default"))
			allowed = AllowableFormats.createFormatList(a.getValue());
		if (!d.getValue().equals("default"))
			disallowed = AllowableFormats.createFormatList(d.getValue());
		return new AllowableFormats(allowed, disallowed);

//		if (!cfgPreviewableFormats.getValue().equals("default") || !cfgNotPreviewableFormats.getValue().equals("default"))
//			previewableFormats = new AllowableFormats(cfgPreviewableFormats.getValue(), cfgNotPreviewableFormats.getValue());
	}
	public VideoHandlerPriorities getPriorities() {
		return pris;
	}
		

	public void setPriorities(VideoHandlerPriorities vpris) {
		pris = vpris;
	}

	public void setPriorities(int pri) {
		pris.fillVideoPriority = pri;
		pris.previewPriority = pri;
		pris.streamPriority = pri;
		pris.transcodePriority = pri;
	}
	
	public void setPriorities(int fpri, int ppri, int spri, int tpri) {
		pris.fillVideoPriority = fpri;
		pris.previewPriority = ppri;
		pris.streamPriority = spri;
		pris.transcodePriority = tpri;
	}


	/*
	public AllowableFormats getTranscodeDstFormats() {
		return nullFormats;
	}
	*/
	

	
	
}

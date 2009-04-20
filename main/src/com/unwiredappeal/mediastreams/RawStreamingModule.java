package com.unwiredappeal.mediastreams;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.RandomAccessFileInputStream;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.modules.BaseVideoHandlerModule;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.modules.VideoFormats;

public class RawStreamingModule extends BaseVideoHandlerModule implements StreamBabyModule {



	public final static int STREAM_PRI = 10;
	public boolean initialize(StreamBabyModule parentMod) {
		super.initialize(parentMod);
		this.getPriorities().streamPriority = STREAM_PRI;
		return true;
	}

	
	public boolean isRawFile(URI uri) {
		if (!Utils.isFile(uri))
			return false;
		return uri.getPath().toLowerCase().endsWith(".raw");
	}
	
	public boolean fillVideoInformation(URI uri, VideoInformation vidinfo) {
		if (isRawFile(uri)) {
			vidinfo.setContainerFormat(VideoFormats.CONTAINER_RAW);
			return true;
		}
		return false;
	}

	public boolean canStream(URI uri, VideoInformation vinfo) {
		if (isRawFile(uri))
			return true;
		return false;
	}
	
	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vi, long startPosition) throws IOException {
		if (isRawFile(uri)) {
			String mimeType = "video/mpeg";
			String contentType = "video/mpeg";
			
			File ffmt = new File(uri.getPath() + ".fmt");
			if (ffmt.exists()) {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(ffmt.toURL().openStream()));
					if (br != null) {
						String s = br.readLine();
						if (s != null) {
							if (s.equals("null"))
								mimeType = null;
							else 
								mimeType = s;
							contentType = s;
							s = br.readLine();
							if (s != null)
								contentType = s;
						}
					}
				} catch(IOException e) { }
			}
			Log.warn("opening raw file with mimeType: " + mimeType + ", contentType: " + contentType);
			File f = new File(uri);
			InputStream is = new RandomAccessFileInputStream(f);
			VideoInputStreamWrapper vis = new VideoInputStreamWrapper(vi.getDuration(), is, vi, mimeType);
			vis.setContentType(contentType);
			vis.canRandomPosition = false;
			return vis;
		}
		return null;	
	}
	
	public Object getModule(int moduleType) {
		if (moduleType == StreamBabyModule.STREAMBABY_MODULE_VIDEO)
			return this;
		else
			return null;
	}


	public String getStreamableMimeType(URI uri, VideoInformation vinfo) {
		if (isRawFile(uri)) {
			String mimeType = "video/mpeg";
			String contentType = "video/mpeg";
			
			File ffmt = new File(uri.getPath() + ".fmt");
			if (ffmt.exists()) {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(ffmt.toURL().openStream()));
					if (br != null) {
						String s = br.readLine();
						if (s != null) {
							if (s.equals("null"))
								mimeType = null;
							else 
								mimeType = s;
							contentType = s;
							s = br.readLine();
							if (s != null)
								contentType = s;
						}
					}
				} catch(IOException e) { }
			}
			return mimeType;
		}
		return null;	
	}


	public String getTranscodeMimeType(URI uri, VideoInformation vinfo, int qual) {
		return null;
	}

}

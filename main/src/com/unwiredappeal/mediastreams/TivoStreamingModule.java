package com.unwiredappeal.mediastreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import com.unwiredappeal.tivo.utils.RandomAccessFileInputStream;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.modules.BaseVideoHandlerModule;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.modules.VideoFormats;
import com.unwiredappeal.tivo.modules.VideoFormats.AllowableFormats;
import com.unwiredappeal.tivo.modules.VideoFormats.Format;

public class TivoStreamingModule extends BaseVideoHandlerModule implements StreamBabyModule {

	public final static String TIVO_CONTENT_TYPE = "video/x-tivo-mpeg";

	public static AllowableFormats streamableFormats = new AllowableFormats(
			// allowed
			Arrays.asList(new Format[] { 
					new Format(VideoFormats.CONTAINER_TIVO, "*", "*")
					//new Format(VideoFormats.CONTAINER_TIVO, "*", "*")
			}),
			// disallowed
			null
		);	
	

	public final static int STREAM_PRI = 10;
	public boolean initialize(StreamBabyModule parentMod) {
		super.initialize(parentMod);
		this.getPriorities().streamPriority = STREAM_PRI;
		return true;
	}
	
	@Override
	public AllowableFormats getStreamableFormats() {
		return streamableFormats;
	}
	
	
	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vi, long startPosition) throws IOException {
		if (!Utils.isFile(uri))
			return null;
		//String filename = new File(uri).getAbsolutePath();
		File f = new File(uri);
		InputStream is = new RandomAccessFileInputStream(f);
		VideoInputStreamWrapper vis = new VideoInputStreamWrapper(vi.getDuration(), is, vi, "video/mpeg");
		vis.setContentType(TIVO_CONTENT_TYPE);
		vis.canRandomPosition = false;
		return vis;
			
	}
	
	public Object getModule(int moduleType) {
		if (moduleType == StreamBabyModule.STREAMBABY_MODULE_VIDEO)
			return this;
		else
			return null;
	}

}

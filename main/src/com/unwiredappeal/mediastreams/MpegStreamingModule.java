package com.unwiredappeal.mediastreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import com.unwiredappeal.mediastreams.mpeg.StreamableMpeg;
import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.modules.BaseVideoHandlerModule;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.modules.VideoFormats;
import com.unwiredappeal.tivo.modules.VideoFormats.AllowableFormats;
import com.unwiredappeal.tivo.modules.VideoFormats.Format;

public class MpegStreamingModule extends BaseVideoHandlerModule implements StreamBabyModule {
	
	
	//public static AllowableFormats streamableFormats = new AllowableFormats(new Formats(new String[] { VideoFormats.CONTAINER_MPEGPS }, new String[] { VideoFormats.VIDEO_CODEC_MPEG2 }, new String[] {VideoFormats.AUDIO_CODEC_AC3, VideoFormats.AUDIO_CODEC_MP2}), null);
	//public static AllowableFormats streamableFormats = new AllowableFormats(new Formats(new String[] { VideoFormats.CONTAINER_MPEGPS }, new String[] { "*" }, new String[] { "*" }), null);
	
	public static AllowableFormats streamableFormats = new AllowableFormats(
			// allowed
			Arrays.asList(new Format[] { 
					new Format(VideoFormats.CONTAINER_MPEGPS, "*", "*")
					//new Format(VideoFormats.CONTAINER_TIVO, "*", "*")
			}),
			// disallowed
			null
		);	
	

	public static String[] mpegExts = new String[] { "mpeg", "mpg", "vob", "mpg2", "mpeg2" };
	
	public static ConfigEntry cfgFillVideoInfo = new ConfigEntry(
			"mpegmod.fillvidinfo",
			"true",
			"Allow mpegmodule to parse video informaton"
			);
	
	public static ConfigEntry cfgStreamableFormats = new ConfigEntry(
			"mpegmod.streamformats",
			"default",
			"list of formats mpegmodule should attempt to stream"
			);
	
	public static ConfigEntry cfgNotStreamableFormats = new ConfigEntry(
			"mpegmod.streamformats.disallow",
			"default",
			"list of formats mpegmodule should not attempt to stream"
			);

	public static final int FILL_VIDEO_PRIORITY = 35;
	public static final int STREAM_VIDEO_PRIORITY = 60;

	@Override
	public boolean initialize(StreamBabyModule parentMod) {
		super.initialize(parentMod);
		getPriorities().fillVideoPriority = FILL_VIDEO_PRIORITY;
		getPriorities().streamPriority = STREAM_VIDEO_PRIORITY;
		StreamableMpeg.logger = new StreamableMpeg.Logger() {
			public void log(String s) {
				Log.debug(s);
			}
		};
		streamableFormats = configFormats(cfgStreamableFormats, cfgNotStreamableFormats, streamableFormats);

		//if (!cfgStreamableFormats.getValue().equals("default") || !cfgNotStreamableFormats.getValue().equals("default"))
			//streamableFormats = new AllowableFormats(cfgStreamableFormats.getValue(), cfgNotStreamableFormats.getValue());
		return true;
	}
	@Override
	public AllowableFormats getStreamableFormats() {
		return streamableFormats;
	}
	
	public boolean fillVideoInformation(URI uri, VideoInformation vidinfo) {
		if (!cfgFillVideoInfo.getBool())
			return false;
		if (!Utils.isFile(uri))
			return false;
		String filename = new File(uri).getAbsolutePath();
		int dotPos = filename.lastIndexOf('.');
		if (dotPos < 0)
			return false;
		String ext = filename.substring(dotPos+1).toLowerCase();
		if (!Arrays.asList(mpegExts).contains(ext))
			return false;
		try {
			StreamableMpeg mpeg = new StreamableMpeg(new File(uri), 0);
			if (mpeg.getSubDuration() <= 0)
				return false;
			mpeg.setPictureInfo();
			vidinfo.setFps(mpeg.frameRate);
			vidinfo.setWidth(mpeg.width);
			vidinfo.setHeight(mpeg.height);
			vidinfo.setAspect(mpeg.aspect);
			vidinfo.setContainerFormat(VideoFormats.CONTAINER_MPEGPS);
			vidinfo.setDuration(mpeg.getSubDuration());
			vidinfo.setAudioCodec(VideoFormats.UNKNOWN_FORMAT);
			vidinfo.setVideoCodec(VideoFormats.VIDEO_CODEC_MPEG2);
			mpeg.close();
			Log.debug(filename + ":" + vidinfo);

			return true;
		} catch (IOException e) {
			return false;
		}
		
	}
	

	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vi, long startPosition) throws IOException {
		if (!Utils.isFile(uri))
			return null;
		//String filename = new File(uri).getAbsolutePath();
		File f = new File(uri);
		StreamableMpeg mis = new StreamableMpeg(f, startPosition, vi.getDuration());
		VideoInputStreamWrapper vis = new VideoInputStreamWrapper(mis.getSubDuration(), mis, vi, "video/mpeg");
		return vis;
			
	}

	public Object getModule(int moduleType) {
		if (moduleType == StreamBabyModule.STREAMBABY_MODULE_VIDEO)
			return this;
		else
			return null;
	}
	
	
	public static void main(String[] argv) throws Exception {
		String src = argv[0];
		String dst = argv[1];
		long pos = Long.parseLong(argv[2]);
		System.err.println("In: " + src + ", out: " + dst + ", pos: " + pos);

		StreamableMpeg.logger = new StreamableMpeg.Logger() {
			public void log(String s) {
				Log.debug(s);
			}
		};		
		InputStream is = new StreamableMpeg(new File(src), pos);
		OutputStream os = new FileOutputStream(new File(dst));
		final int IO_BUFFER_SIZE = 4 * 1024;

		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = is.read(b)) != -1) {
			os.write(b, 0, read);
		}
		os.close();
		is.close();
		System.out.println("Done");
	}

}

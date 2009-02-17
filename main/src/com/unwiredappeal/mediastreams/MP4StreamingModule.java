package com.unwiredappeal.mediastreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.Arrays;

import com.unwiredappeal.mediastreams.mp4.StreamableMP4;
import com.unwiredappeal.mediastreams.mp4.StreamableMP4.BArray;
import com.unwiredappeal.mediastreams.mp4.StreamableMP4.BArrayFactory;
import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.modules.BaseVideoHandlerModule;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.modules.VideoFormats;
import com.unwiredappeal.tivo.modules.VideoFormats.AllowableFormats;
import com.unwiredappeal.tivo.modules.VideoFormats.Format;
import com.unwiredappeal.virtmem.MappedFileMemoryManager;
import com.unwiredappeal.virtmem.MemChunk;

public class MP4StreamingModule extends BaseVideoHandlerModule implements StreamBabyModule {
	
	public static class VirtMemBackedBArray extends StreamableMP4.BaseBArray implements  StreamableMP4.BArray {

		MemChunk memChunk;
		public VirtMemBackedBArray(int size) {
			super(size);
			//memChunk = MappedFileMemoryManager.manager.alloc(size);
			memChunk = MappedFileMemoryManager.manager.alloc(size);
		}
		public void free() {
			if (memChunk != null)
				memChunk.free();
			memChunk = null;
		}

		public int get(long index) {
			int ret = memChunk.get((int)index) & 0xff;
			//System.err.println("get: " + index + ", " + ret);
			return ret;
		}

		public void set(long index, int v) {
			v = v & 0xff;
			memChunk.set((int)index, v);
		}
		public void readFromFile(RandomAccessFile fp, long offset, long len)
				throws IOException {
			final int BSIZE = 4096;
			byte[] bytes = new byte[BSIZE];
			while(len > 0) {
				int rl = Math.min((int)len, BSIZE);
				fp.read(bytes, 0, rl);//
				memChunk.write((int)offset, bytes, 0, rl);
				offset += rl;
				len -= rl;
			}
			
		}
		
	}
	public static class VirtMemBArrayFactory implements BArrayFactory {
		public static int MAX_BYTE_BACKED_SIZE = 100 * 1024; // 100k
		public BArray getBArray(int size) {
			if (size <= MAX_BYTE_BACKED_SIZE)
				return new StreamableMP4.ByteArrayBackedBArray(size);
			else
				return new VirtMemBackedBArray(size);
		}

	}

	//public static AllowableFormats streamableFormats = new AllowableFormats(new Formats(new String[] { VideoHandlerModule.CONTAINER_MP4 }, new String[] { "*" }, new String[] { "*" }), null);
	public static AllowableFormats streamableFormats = new AllowableFormats(
			// allowed
			Arrays.asList(new Format[] { 
					new Format(VideoFormats.CONTAINER_MP4, VideoFormats.VIDEO_CODEC_H264, VideoFormats.AUDIO_CODEC_AC3),
					new Format(VideoFormats.CONTAINER_MP4, VideoFormats.VIDEO_CODEC_H264, VideoFormats.AUDIO_CODEC_AAC)
			}),
			// disallowed
			null
		);	
	
	public static String[] mp4Exts = new String[] { "mp4" };
	
	public static ConfigEntry cfgFillVideoInfo = new ConfigEntry(
			"mp4mod.fillvidinfo",
			"true",
			"Allow mp4module to parse video informaton"
			);
	
	
	public static ConfigEntry cfgStreamableFormats = new ConfigEntry(
			"mp4mod.streamformats",
			"default",
			"list of formats mp4module should attempt to stream"
			);
	
	public static ConfigEntry cfgNotStreamableFormats = new ConfigEntry(
			"mp4mod.streamformats.disallow",
			"default",
			"list of formats mp4module should not attempt to stream"
			);

	@Override
	public AllowableFormats getStreamableFormats() {
		return streamableFormats;
	}
	
	@Override
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
		if (!Arrays.asList(mp4Exts).contains(ext))
			return false;
		try {
			StreamableMP4 mp4 = new StreamableMP4(new File(uri), 0, false);
			if (mp4.getSubDuration() <= 0)
				return false;
			vidinfo.setWidth(mp4.width);
			vidinfo.setHeight(mp4.height);
			vidinfo.setContainerFormat(VideoFormats.CONTAINER_MP4);
			vidinfo.setDuration(mp4.getSubDuration());
			if (mp4.formats.contains("avc1"))
				vidinfo.setVideoCodec(VideoFormats.VIDEO_CODEC_H264);
			else
				vidinfo.setVideoCodec(VideoFormats.UNKNOWN_FORMAT);
			if (mp4.formats.contains("mp4a"))
				vidinfo.setAudioCodec(VideoFormats.AUDIO_CODEC_AAC);
			else if (mp4.formats.contains("ac-3"))
				vidinfo.setAudioCodec(VideoFormats.AUDIO_CODEC_AC3);

			else
				vidinfo.setAudioCodec(VideoFormats.UNKNOWN_FORMAT);

			Log.debug(filename + ":" + vidinfo);
			mp4.close();
			return true;
		} catch (IOException e) {
			return false;
		}
		
	}

	@Override
	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vidinfo, long startPosition)
			throws IOException {
		if (!Utils.isFile(uri))
			return null;

		StreamableMP4 mis = new StreamableMP4(new File(uri), startPosition, StreamBabyConfig.cfgMp4Interleave.getBool());
		long subDur = mis.getSubDuration();
		if (subDur <= 0)
			subDur = vidinfo.getDuration();
		return new VideoInputStreamWrapper(subDur, mis, vidinfo, "video/mp4");
	}
	public Object getModule(int moduleType) {
		if (moduleType == StreamBabyModule.STREAMBABY_MODULE_VIDEO)
			return this;
		else
			return null;
	}

	public static final int FILL_VIDEO_PRIORITY = 35;
	public static final int STREAM_VIDEO_PRIORITY = 60;

	@Override
	public boolean initialize(StreamBabyModule parentMod) {
		super.initialize(parentMod);
		getPriorities().fillVideoPriority = FILL_VIDEO_PRIORITY;
		getPriorities().streamPriority = STREAM_VIDEO_PRIORITY;		
		StreamableMP4.logger = new StreamableMP4.Logger() {
			public void log(String s) {
				Log.debug(s);
			}
		};

		StreamableMP4.bfact = new VirtMemBArrayFactory();
		streamableFormats = configFormats(cfgStreamableFormats, cfgNotStreamableFormats, streamableFormats);
		//if (!cfgStreamableFormats.getValue().equals("default") || !cfgNotStreamableFormats.getValue().equals("default"))
			//streamableFormats = new AllowableFormats(cfgStreamableFormats.getValue(), cfgNotStreamableFormats.getValue());
		
		
		return true;
	}
	
	public static void main(String[] argv) throws Exception {
		String src = argv[0];
		String dst = argv[1];
		long pos = Long.parseLong(argv[2]);
		System.err.println("In: " + src + ", out: " + dst + ", pos: " + pos);
		StreamableMP4.bfact = new VirtMemBArrayFactory();
		StreamableMP4.logger = new StreamableMP4.Logger() {
			public void log(String s) {
				Log.debug(s);
			}
		};

		StreamableMP4 is = new StreamableMP4(new File(src), pos, true);
		System.err.println("SubDur: " + is.getSubDuration()/1000.0);
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

	public boolean isProfileOk(int profile, int level) {
		return (profile <= 100 && level <= 41);
	}
	public boolean canStream(URI uri, VideoInformation vinfo) {
		boolean b = super.canStream(uri, vinfo);
		if (!b)
			return b;
		
		
		// Ok, so far so good.
		Integer profile = (Integer)vinfo.getCodecExtra("mp4_profile");		
		Integer level = (Integer)vinfo.getCodecExtra("mp4_level");
		if (level != null && profile != null) {
			return isProfileOk(profile.intValue(), level.intValue());
		}
		
		try {
			StreamableMP4 mp4 = new StreamableMP4(new File(uri).getAbsoluteFile(), Long.MAX_VALUE, false);
			mp4.close();
			vinfo.setCodecExtra("mp4_profile", new Integer(mp4.profile));
			vinfo.setCodecExtra("mp4_level", new Integer(mp4.profileLevel));			
			return isProfileOk(mp4.profile, mp4.profileLevel);
		} catch (IOException e) {
			return false;
		}
	}



}

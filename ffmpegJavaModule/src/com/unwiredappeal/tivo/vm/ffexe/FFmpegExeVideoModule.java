package com.unwiredappeal.tivo.vm.ffexe;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unwiredappeal.mediastreams.PreviewGenerator;
import com.unwiredappeal.mediastreams.SocketProcessVideoInputStream;
import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.mediastreams.VideoInputStream;
import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.streambaby.PreviewWindow;
import com.unwiredappeal.tivo.utils.PropertyReplacer;
import com.unwiredappeal.tivo.utils.SocketProcessInputStream;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.AvailableSocket.SocketNotAvailable;
import com.unwiredappeal.tivo.videomodule.StreamBabyModule;
import com.unwiredappeal.tivo.videomodule.VideoFormats;
import com.unwiredappeal.tivo.videomodule.VideoFormats.AllowableFormats;
import com.unwiredappeal.tivo.vm.ffmpeg.BaseFFmpegVideoModule;

public class FFmpegExeVideoModule extends BaseFFmpegVideoModule implements StreamBabyModule {
	
	private static final long WAIT_FOR_FFMPEG_INIT = 150;
	public static String ffmpegPath;

	public static ConfigEntry cfgFFmpegPath = new ConfigEntry(
			"ffmpeg.path",
			System.getProperty("os.name").startsWith("Windows")  ? StreamBabyConfig.nativeDir + File.separator + "ffmpeg.exe": "ffmpeg",
			"path of ffmpeg"
			);
	
	/*
	public static ConfigEntry cfgFFmpegStreamArgs = new ConfigEntry(
			"ffmpegexe.streamargs",
			"-acodec copy -vcodec copy -f vob -async 1 -v 0",
			"Arguments to use when streaming from ffmpeg"
			);

	public static ConfigEntry cfgFFmpegAudioOnlyStreamArgs = new ConfigEntry(
			"ffmpegexe.audiostreamargs",
			"-acodec copy -vn -f mp3 -v 0",
			"Arguments to use when streaming pure audio from ffmpeg"
			);

	public static ConfigEntry cfgFFmpegVideoOnlyStreamArgs = new ConfigEntry(
			"ffmpegexe.audiostreamargs",
			"-vcodec copy -an -f vob -v 0",
			"Arguments to use when streaming pure audio from ffmpeg"
			);

	public static ConfigEntry cfgFFmpegStreamingAudioMime = new ConfigEntry(
			"ffmpegexe.stream.audio.mime",
			"audio/mp3",
			"mimetype to use when stream from ffmpeg in audio-only-mode"
			);

	public static ConfigEntry cfgFFmpegStreamingMime = new ConfigEntry(
			"ffmpegexe.stream.mime",
			"video/mpeg",
			"mimetype to use when stream from ffmpeg"
			);

	
	public static ConfigEntry cfgFFmpegStreamingVideoMime = new ConfigEntry(
			"ffmpegexe.stream.video.mime",
			"video/mpeg",
			"mimetype to use when streaming from ffmpeg in video-only mode"
			);
	*/
	public static ConfigEntry cfgFFmpegTranscodeArgs = new ConfigEntry(
			"ffmpegexe.transcode",
			"-acodec ac3 -vcodec mpeg2video -f vob -async 1 -r ${closest.mpeg.fps} -v 0",
			"Arguments to use when transcoding from ffmpeg"
			);
	
	public static ConfigEntry cfgFFmpegSameQArgs = new ConfigEntry(
			"ffmpegexe.transcode.sameqargs",
			"-sameq -ab 192k",
			"Argument to pass for ffmpeg to transcode at same qual as orig"
			);
	
	public static ConfigEntry cfgFFmpegBpsQualArgs = new ConfigEntry(
			"ffmpegexe.transcode.qualargs",
			"-bufsize 4096k -b ${bitrate}k -maxrate 8000k -ab ${abitrate}k -s ${xres}x${yres}",
			"Arguments to pass to ffmpeg to transcode at a particular kbps"
			);

	public static ConfigEntry cfgFFmpegTranscodeMime = new ConfigEntry(
			"ffmpegexe.transcode.mime",
			"video/mpeg",
			"mimetype to use when transcoding from ffmpeg"
			);

	
	
	public static ConfigEntry cfgFFmpegPreviewArgs = new ConfigEntry(
			"ffmpegexe.preview",
			"-r 1 -f mjpeg -v 0",
			"Arguments to use when transcoding from ffmpeg"
			);
	

	public static ConfigEntry cfgPreviewableFormats = new ConfigEntry(
			"ffmpegexe.previewformats",
			"*,*,*",
			"list of formats the ffmpegexe module should attempt to preview"
			);
	
	public static ConfigEntry cfgNotPreviewableFormats = new ConfigEntry(
			"ffmpegexe.previewformats.disallow",
			"*,none,*;tivo,*,*;raw,*,*",
			"list of formats ffmpegexe should not attempt to preview"
			);

	public static ConfigEntry cfgTranscodableFormats = new ConfigEntry(
			"ffmpegexe.transcodeformats",
			"*,*,*",
			"list of formats the ffmpegexe module should attempt to transcode"
			);
	
	public static ConfigEntry cfgNotTranscodableFormats = new ConfigEntry(
			"ffmpegexe.transcodeformats.disallow",
			"*,none,*;mpeges,*,*;tivo,*,*;raw,*,*",
			"list of formats ffmpegexe should not attempt transcode"
			);
	
	/*
	public static ConfigEntry cfgStreamableFormats = new ConfigEntry(
			"ffmpegexe.streamformats",
			"",
			"list of formats the ffmpegexe module should stream"
			);
	
	public static ConfigEntry cfgNotStreamableFormats = new ConfigEntry(
			"ffmpegexe.streamformats.disallow",
			"*,*,*",
			"list of formats ffmpegexe should not attempt to stream"
			);
	*/

	
	public static AllowableFormats transcodableFormats = new AllowableFormats(
			// allowed
			VideoFormats.createFormatList(containerMap.values(), VideoFormats.wildCardList, VideoFormats.wildCardList),
			// disallowed
			null
		);
	
	public static AllowableFormats previewableFormats = new AllowableFormats(
			// allowed
			VideoFormats.createFormatList(containerMap.values(), VideoFormats.wildCardList, VideoFormats.wildCardList),
			// disallowed
			null
		);
	
	public static AllowableFormats streamableFormats;
	
	/*
	@Override
	public AllowableFormats getStreamableFormats() {
		return streamableFormats;
	}
	*/

	public FFmpegExeVideoModule() {
		populateConfig();
	}

	@Override 
	public AllowableFormats getTranscodeSrcFormats() {
		return transcodableFormats;
	}


	public boolean canPreview(URI uri, VideoInformation vidinfo, boolean realtime) {
		return realtime == false && VideoFormats.isAllowed(previewableFormats, vidinfo);
	}

	public PreviewGenerator getPreviewHandler(URI uri, VideoInformation vi, boolean realtime) {
		return new FFmpegExePreviewer();
	}
	public boolean canPreview(boolean realtime) {
		return realtime == false;
	}
	
	public static int VIDEO_PRIORITIES = 40;
	@Override
	public boolean initialize() {
		setPriorities(VIDEO_PRIORITIES);
		
		ffmpegPath = cfgFFmpegPath.getValue();
		
		previewableFormats = configFormats(cfgPreviewableFormats, cfgNotPreviewableFormats, previewableFormats);
		transcodableFormats = configFormats(cfgTranscodableFormats, cfgNotTranscodableFormats, transcodableFormats );
		//streamableFormats = configFormats(cfgStreamableFormats, cfgNotStreamableFormats, null);

		Log.debug("FFmpegPath: " + ffmpegPath);
		Log.info("FFmpegExeModule: Loaded");
		return true;

	}
	
	public static class FFmpegExePreviewer implements PreviewGenerator {

		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		SocketProcessInputStream ss;
		//InputStream is;
		public URI uri;
		String filename;
		public VideoInformation vidinfo;
		public int sw;
		public int sh;
		int nextSec = -1;
		
		public int qscale = 32 - Math.min(Math.max(StreamBabyConfig.cfgPreviewQuality.getInt() / 3, 1), 31);
	
		public  float quality = (StreamBabyConfig.cfgPreviewQuality.getInt() / 100.0f);
		
		//private ImageWriter jpgWriter = null;
		//private ImageReader jpgReader;

		public FFmpegExePreviewer() {
			/*
	        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
	        if (iter.hasNext()) {
	        	jpgWriter= iter.next();
	        }
	        Iterator<ImageReader> riter = ImageIO.getImageReadersByFormatName("jpg");
	        if (riter.hasNext()) {
	        	jpgReader= riter.next();
	        }
		*/

		}
		
		public void close() {
			if (ss != null)
				ss.close();
			ss = null;
		}

		public byte[] getFrameImageData(int secs) {
			if (secs != nextSec) {
				if (!_open(secs)) {
					return null;
				}
			}
		   if (ss != null) {
			   
			   try {
				   bs.reset();
				   int c;
				   int last = -1;
				   while(true) {
					   c = ss.read();
					   if (c < 0)
						   return null;
					   bs.write(c);
					   if (last == 0xff && c == 0xd9) {
						   break;
					   }
					   last = c;
				   }
				   nextSec++;
				   return bs.toByteArray();
			   } catch(IOException e) {
				   return null;
			   }
		   }
		   return null;
		}

		public boolean isRealtime() {
			return true;
		}

		public boolean open(URI uri, VideoInformation vinfo, int sw, int sh) {
			this.uri = uri;
			this.vidinfo = vinfo;
			this.sw = sw;
			this.sh = sh;
			this.nextSec = 0;
			if (!Utils.isFile(uri))
				return false;
			filename = new File(uri).getAbsolutePath();
			
			
			return _open(0);
		}
		
		public boolean _open(int secs) {
			if (ss != null) {
				ss.close();
				ss = null;
			}
			try {
				ss = new SocketProcessInputStream();
			} catch (Exception e) {
				return false;
			}
			this.nextSec = secs;
			String sockStr = "tcp://127.0.0.1:" + ss.getPort();
			String scale = PreviewWindow.small_PREVIEW_WIDTH + "x" + PreviewWindow.small_PREVIEW_HEIGHT;
			String[] args = new String[] { "-ss", Integer.toString(secs), "-i", filename, "-s", scale, "-qscale", Integer.toString(qscale) };
			List<String> argsList = new ArrayList<String>();
			argsList.addAll(Arrays.asList(args));
			addArgsToList(cfgFFmpegPreviewArgs.getValue(), argsList);
			argsList.add(sockStr);
			Process p = null;
			try {
				 p = runFFmpeg(argsList.toArray(new String[0]));
			} catch(IOException e) {
				
			}
			if (p == null) {
				ss.close();
				return false;
			}
			ss.attachProcess(p);
			RunnableInputDrainer dos = new RunnableInputDrainer(p.getInputStream(), false);
			RunnableInputDrainer des = new RunnableInputDrainer(p.getErrorStream(), false);

			Thread tdos = new Thread(dos);
			Thread tdes = new Thread(des);
			tdos.start();
			tdes.start();
			
			return true;
			
		}

		public void prepare(int secs, int delta) {
			// TODO Auto-generated method stub
			
		}
		
	}
	@Override
	public boolean fillVideoInformation(URI uri, VideoInformation vidinfo) {
		
		if (!Utils.isFile(uri))
			return false;
		String filename = new File(uri).getAbsolutePath();

		boolean res = true;
		
		String info = captureErrorStream(new String[]  { "-i", filename });
		if (info == null)
			return false;
		long dur = 0;
		String ffmpegFormatStr = null;
		int bitrate = 0;
		String audioCodec = null;
		int channels = 0;
		int audioBps = 0;
		String videoCodec = null;
		double fps = 0;
		int width = 0;
		int height = 0;
		double startpos;
		
		ffmpegFormatStr = parseString(info, ".*^Input #0, (.*), from.*$.*");
		bitrate = parseInt(info, ".* bitrate: (\\d*).*");
		audioBps = parseInt(info, "\\s*Stream.*:.*Audio:.* (\\d*) Hz.*");
		audioCodec = parseString(info, "\\s*Stream.*:.*Audio: (\\w+).*");
		videoCodec = parseString(info, "\\s*Stream.*:.*Video: (\\w+).*");
		String channelsStr = parseString(info, "\\s*Stream.*:.*Audio:[^$]* Hz, ([^$]*)");
		Log.debug("ChannelStr to parse: " + channelsStr);
		if (channelsStr != null) {
			if (channelsStr.compareTo("stereo") == 0)
				channels = 2;
			else if (channelsStr.compareTo("mono") == 0) {
				channels = 1;
			} else if (channelsStr.length() > 2 && channelsStr.charAt(1) == '.' || channelsStr.charAt(1) == ':') {
				try {
					channels = Integer.parseInt(channelsStr.substring(0, 1)) + Integer.parseInt(channelsStr.substring(2, 3));
				} catch(NumberFormatException e) { }
			} else if(channelsStr.compareTo("5:1") == 0) {
				channels = 6;
			} else if (channelsStr.compareTo("channels") == 0){
				channels = parseInt(info, "\\S*Audio: .* (\\d*) channels.*");
			}
		}
		startpos = parseDouble(info, "\\s*start: ([0-9\\.]+).*");
		dur = parseDuration(info, "\\s*Duration: ([\\d\\.:]+).*");
		fps = parseDouble(info, "\\s*Stream.*:.*Video: .* ([0-9\\.]+) fps.*");
		if (fps == 0)
			fps = parseDouble(info, "\\s*Stream.*:.*Video: .* ([0-9\\.]+) tb\\(.*");

		width = parseInt(info, "\\s*Stream.*:.*Video: .* (\\d+)x\\d+.*");
		height = parseInt(info, "\\s*Stream.*:.*Video: .* \\d+x(\\d+).*");
		int aspectNum = parseInt(info, "\\s*Stream.*:.*Video: .* PAR (\\d+):\\d+.*");
		int aspectDen = parseInt(info, "\\s*Stream.*:.*Video: .* PAR \\d+:(\\d+).*");

		
		setContainerFormat(ffmpegFormatStr, vidinfo);
		vidinfo.setStartPosition((long)(startpos * 1000L));
	    vidinfo.setDuration(dur);
	    vidinfo.setBitRate(bitrate);
	    // Ubuntu doesn't have ac3 support, so if this is mpegps format stream and can't figure out audio
	    // it is more than likely ac3
	    if ((audioCodec == null || audioCodec.compareTo("0x0000") == 0) && vidinfo.getContainerFormat() != null && vidinfo.getContainerFormat().equals(VideoFormats.CONTAINER_MPEGPS))
    		vidinfo.setAudioCodec(VideoFormats.AUDIO_CODEC_AC3);
    	else if (audioCodec == null)
    		vidinfo.setAudioCodec(VideoFormats.AUDIO_CODEC_NONE);
    	else
    		vidinfo.setAudioCodec(translateCodec(audioCodec));
		vidinfo.setAudioChannels(channels);
		vidinfo.setAudioBps(audioBps);
	    
		if (videoCodec == null)
			vidinfo.setVideoCodec(VideoFormats.VIDEO_CODEC_NONE);
		vidinfo.setVideoCodec(translateCodec(videoCodec));
		vidinfo.setFps(fps);
		vidinfo.setWidth(width);
		vidinfo.setHeight(height);
		if (aspectNum > 0 && aspectDen > 0) {
			vidinfo.setPixelAspect((float)aspectNum /(float)aspectDen);
		}
		res = true;
		Log.info("VideoInfo:\n" + vidinfo.toString());
		return res;

	}
	
	private int parseInt(String str, String patStr) {
		String parsed = parseString(str, patStr);
		int ret = 0;
		if (parsed != null) {
			try {
				ret = Integer.parseInt(parsed);
			} catch(NumberFormatException e) { }
		}
		return ret;
	}
	
	private double parseDouble(String str, String patStr) {
		String parsed = parseString(str, patStr);
		double ret = 0;
		if (parsed != null) {
			try{
				ret = Double.parseDouble(parsed);
			} catch(NumberFormatException e) { }

		}
		return ret;
		
	}
	
	long parseDuration(String str, String patStr) {
		String parsed = parseString(str, patStr);
		double duration = 0;
		if (parsed != null) {
			try {
				double d = 0;
				String[] parts = parsed.split(":");
				double mul = 1;
				for (int i=parts.length-1;i>=0;i--) {
					double val = Double.parseDouble(parts[i]);
					d += val * mul;
					mul = mul * 60;
				}
				duration = d;
			} catch(NumberFormatException e) { }
		}
		return (long)(duration*1000);
	}
	
	private String _parseString(String str, String patStr) {
		String ret = null;
		Pattern p = Pattern.compile(patStr, Pattern.DOTALL|Pattern.MULTILINE);
		Matcher m = p.matcher(str);
		if (m.matches()) {
			//debug.print("MATCHES!");
			ret = m.group(1);
		}
		return ret;
	}
	
	private String parseString(String str, String patStr) {
		String[] split = str.split("\r\n|\r|\n");
		for (int i=0;i<split.length;i++) {
			String r = _parseString(split[i], patStr);
			if (r != null)
				return r;
		}
		return null;
	}
	
	
	private VideoInputStream openVideo(URI uri, VideoInformation vi, long startPosition, String ffargs, String mime) throws IOException {
		if (!Utils.isFile(uri))
			return null;
		String filename = new File(uri).getAbsolutePath();
		
		SocketProcessVideoInputStream ss;
		try {
			ss = new SocketProcessVideoInputStream(vi, startPosition, mime);
		} catch (SocketNotAvailable e) {
			throw new IOException();
		}
		String sockStr = "tcp://127.0.0.1:" + ss.getPort();
		long sp = startPosition; // + vi.getStartPosition();
		String[] args = new String[] { "-ss", Double.toString(sp/1000.0), "-i", filename };
		List<String> argsList = new ArrayList<String>();
		argsList.addAll(Arrays.asList(args));
		addArgsToList(ffargs, argsList);
		argsList.add(sockStr);
		Process p = runFFmpeg(argsList.toArray(new String[0]));
		if (p == null) {
			ss.close();
			return null;
		}
		ss.attachProcess(p);
		RunnableInputDrainer dos = new RunnableInputDrainer(p.getInputStream(), StreamBabyConfig.inst._DEBUG);
		RunnableInputDrainer des = new RunnableInputDrainer(p.getErrorStream(), StreamBabyConfig.inst._DEBUG);

		Thread tdos = new Thread(dos);
		Thread tdes = new Thread(des);
		tdos.start();
		tdes.start();
		try {
			Thread.sleep(WAIT_FOR_FFMPEG_INIT);
		} catch (InterruptedException e) {

		}
		try {
			p.exitValue();
			Log.error("FFmpeg failed to continue running... Assuming error");
			// Not good...
			ss.close();
			ss = null;
		} catch(IllegalThreadStateException e) {
		}
		return ss;
		
	}
	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vi, long startPosition) throws IOException {
		/*
		String args = cfgFFmpegStreamArgs.getValue();
		String mime = FFmpegExeVideoModule.cfgFFmpegStreamingMime.getValue();
		if (vi.getVideoCodec() == VideoFormats.VIDEO_CODEC_NONE) {
			args = cfgFFmpegAudioOnlyStreamArgs.getValue();
			mime = cfgFFmpegStreamingAudioMime.getValue();
		} else if (vi.getAudioCodec() == VideoFormats.AUDIO_CODEC_NONE) {
			args = cfgFFmpegVideoOnlyStreamArgs.getValue();
			mime = cfgFFmpegStreamingVideoMime.getValue();
		}
		return openVideo(uri, vi, startPosition, args, mime);
		*/
		return null;
	}

	public VideoInputStream openTranscodedVideo(URI uri, VideoInformation vi, long startPosition, int qual) throws IOException {
		//String addArgs = " -aspect " + vi.getAspect();
		String addArgs;
		int abr;
		int xres;
		int yres;
		int vbr;
		if (qual == VideoFormats.QUALITY_SAME) {
			addArgs = cfgFFmpegSameQArgs.getValue();
			xres = vi.getWidth();
			yres = vi.getHeight();
			abr = vi.getAudioBps();
			vbr = vi.getBitRate() - abr;
		} else {
			addArgs = cfgFFmpegBpsQualArgs.getValue();
			int chans = StreamBabyConfig.inst.getAudioChannels(qual);
			if (chans > 0) {
				addArgs += " -ac " + chans;
			}			
			abr = StreamBabyConfig.inst.getAudioBr(qual);
			abr = ((abr+31)/32) * 32;
			abr = Math.max(abr, 64);
			yres = vi.getHeight();
			xres = vi.getWidth();
			int maxy = StreamBabyConfig.inst.getYRes(qual);
			maxy = ((maxy+31)/32) * 32;
			if (maxy < yres) {
				xres = (int)(xres * (maxy/(float)yres));
				xres = ((xres+31)/32) * 32;
				yres = maxy;
			}
			vbr = StreamBabyConfig.inst.getVideoBr(qual);

		}
		PropertyReplacer pr = new PropertyReplacer();
		pr.set("bitrate", vbr);
		pr.set("abitrate", abr);
		pr.set("xres", xres);
		pr.set("yres", yres);
		pr.set("closest.mpeg.fps", getClosestMpegRate(vi.getFps()));
		if (addArgs.length() > 0)
			addArgs = " " + addArgs;
		
		String args = pr.parseProperties(cfgFFmpegTranscodeArgs.getValue() + addArgs);

		Log.debug("Using ffmpeg transcode args: " + args);

				
		return openVideo(uri, vi, startPosition, args, cfgFFmpegTranscodeMime.getValue());
	}
	
	public double getClosestMpegRate(double fps) {
		double delta = Double.MAX_VALUE;
		double foundFps = 29.97;
		for (int i=0;i<mpegFps.length;i++) {
			double thisDelta = Math.abs(mpegFps[i]-fps);
			if (thisDelta < delta) {
				delta = thisDelta;
				foundFps = mpegFps[i];
			}
		}
		return foundFps;
	}
	
	private static void addArgsToList(String argsString, List<String> args) {
		String[] pieces = argsString.split(" ");
		for (int i=0;i<pieces.length;i++)
			args.add(pieces[i]);
	}


	private static class RunnableInputDrainer implements Runnable {
		private BufferedReader reader;
		private boolean dump = false;
		public RunnableInputDrainer(InputStream is, boolean dump) {
			this.reader = new BufferedReader(new InputStreamReader(is));
			this.dump = dump;
		}
		public RunnableInputDrainer(InputStream is) {
			this(is, false);
		}
		public void run() {
			try {
				String  v;
				while((v = reader.readLine()) != null) {
					if (dump) {
						Log.debug(v);
					}
				}
			} catch (IOException e) {

			}
			
		}
	}
	public static Process runFFmpeg(String opts[]) throws IOException {
		String[] cmdline = new String[opts.length+1];
		cmdline[0] = ffmpegPath;
		System.arraycopy(opts, 0, cmdline, 1, opts.length);
		// Should fix this to only do this if it would log...
		//if (StreamBabyConfig.inst.DEBUG) {
			String ffmpegCmd = "";
			for (int i=0;i<cmdline.length;i++) {
				ffmpegCmd = ffmpegCmd + cmdline[i] + " ";
			}
			ffmpegCmd = ffmpegCmd.trim();
			Log.info("FFmpegCmd: " + ffmpegCmd);
		//}
		Process ffmpeg = Runtime.getRuntime().exec(cmdline);
		return ffmpeg;
	}
	public String captureErrorStream(String opts[]) {
		try {
			Process ffmpeg = runFFmpeg(opts);
			InputStream is = ffmpeg.getErrorStream();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			copy(is, os);
			ffmpeg.waitFor();

			String str = new String(os.toByteArray());
			return str;
		} catch (IOException e) {
			return null;
		} catch (InterruptedException e) {
			return null;
		}		
	}
	
	private static final int IO_BUFFER_SIZE = 4 * 1024;  
	
	private static void copy(InputStream in, OutputStream out) throws IOException {  
	byte[] b = new byte[IO_BUFFER_SIZE];  
	int read;  
	while ((read = in.read(b)) != -1) {  
	out.write(b, 0, read);  
	}  
	} 
	
	public Object getModule(int moduleType) {
		if (moduleType == StreamBabyModule.STREAMBABY_MODULE_VIDEO)
			return this;
		else
			return null;
	}


}

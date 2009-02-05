package com.unwiredappeal.tivo.vm.ffjava;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;


import net.sf.ffmpeg_java.AVUtilLibrary;
import net.sf.ffmpeg_java.FFmpegMgr;
import net.sf.ffmpeg_java.SWScaleLibrary;
import net.sf.ffmpeg_java.FFMPEGLibrary;
import net.sf.ffmpeg_java.FFMPEGLibrary.AVRational;

import net.sf.ffmpeg_java.v52.AVCodecLibrary;
import net.sf.ffmpeg_java.v52.AVFormatLibrary;
import net.sf.ffmpeg_java.v52.AVCodecLibrary.AVCodec;
import net.sf.ffmpeg_java.v52.AVCodecLibrary.AVCodecContext;
import net.sf.ffmpeg_java.v52.AVCodecLibrary.AVFrame;
import net.sf.ffmpeg_java.v52.AVFormatLibrary.AVFormatContext;
import net.sf.ffmpeg_java.v52.AVFormatLibrary.AVPacket;
import net.sf.ffmpeg_java.v52.AVFormatLibrary.AVStream;



import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.unwiredappeal.mediastreams.PreviewGenerator;
import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.mediastreams.mpeg.StreamableMpeg;
import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.modules.VideoFormats;
import com.unwiredappeal.tivo.modules.VideoHandlerModule;
import com.unwiredappeal.tivo.modules.VideoFormats.AllowableFormats;
import com.unwiredappeal.tivo.vm.ffjava.FFmpegJavaConfig;
import com.unwiredappeal.tivo.vm.ffmpeg.BaseFFmpegVideoModule;


public class FFmpegJavaVideoModule extends BaseFFmpegVideoModule implements StreamBabyModule {

	AVFormatLibrary avFormat;
	AVCodecLibrary avCodec;
	AVUtilLibrary avUtil;
	SWScaleLibrary swScale;
	
//	/*
//	@Override
//	public AllowableFormats getStreamableFormats() {
//		return streamableFormats;
//	}
//	*/
	
	public static AllowableFormats previewableFormats; /* = new AllowableFormats(
			// allowed
			VideoFormats.createFormatList(containerMap.values(), VideoFormats.wildCardList, VideoFormats.wildCardList),
			// disallowed
			null
		);
	*/
	public static ConfigEntry cfgUseInternalSeek = new ConfigEntry(
			"ffmpegjava.mpegseek",
			"false",
			"use internal mpeg seeking code when previewing instead of ffmpegs"
			);
	
	public static ConfigEntry cfgExactSeek= new ConfigEntry(
			"ffmpegjava.mpegexactseek",
			"false",
			"when previewing mpeg files, look for the exact frame to preview (can be slow)"
			);
	
	public static ConfigEntry cfgPreviewableFormats = new ConfigEntry(
			"ffmpegjava.previewformats",
			"*,*,*",
			"list of formats the ffmpegjava module should attempt to preview"
			);
	
	public static ConfigEntry cfgNotPreviewableFormats = new ConfigEntry(
			"ffmpegjava.previewformats.disallow",
			"mpeg,*,*;*,none,*;mpeges,*,*;tivo,*,*;raw,*,*",
			"list of formats ffmpegjava should not attempt to preview"
			);
	

	public FFmpegJavaVideoModule() {
	}
	
	@Override
	public boolean initialize(StreamBabyModule parentMod) {
		super.initialize(parentMod);
		boolean b = FFmpegJavaConfig.inst.useFFmpegLibrary();
		if (b) {
			avFormat = AVFormatLibrary.INSTANCE;
			avCodec = AVCodecLibrary.INSTANCE;
			avUtil = AVUtilLibrary.INSTANCE;
			if (FFmpegMgr.hasSwScale)
				swScale = SWScaleLibrary.INSTANCE;
			Log.info("FFmpeg-java Module: loaded");
			previewableFormats = configFormats(cfgPreviewableFormats, cfgNotPreviewableFormats, previewableFormats);

		} else {
			Log.info("FFmpeg-java Module: NOT LOADED! error loading libraries");
		}
		return b;
 	}
	
	/*
	@Override
	public AllowableFormats getStreamableFormats() {
		return streamableFormats;
	}
	*/

	public boolean canPreview(URI uri, VideoInformation vidinfo, boolean realtime) {
		if (!realtime)
			return false;
		return VideoFormats.isAllowed(previewableFormats, vidinfo);
	}

	public PreviewGenerator getPreviewHandler(URI uri, VideoInformation vi, boolean realtime) {
		if (!realtime)
			return null;
		return new FFmpegLibPreviewer();
	}
	public boolean canPreview(boolean realtime) {
		return realtime == true;
	}
	
	@Override
	public boolean fillVideoInformation(URI uri, VideoInformation vidinfo) {
		if (!Utils.isFile(uri))
			return false;
		String filename = new File(uri).getAbsolutePath();
		boolean res = false;
		AVFormatContext formatCtx = null;
		//AVCodecContext codecCtx = null;
		try {
			final PointerByReference ppFormatCtx = new PointerByReference();
			int or ;
			or = avFormat.av_open_input_file(ppFormatCtx, filename, null, 0, null);
			if (or  != 0)
			    return false;
			
			
			formatCtx = new AVFormatContext(ppFormatCtx.getValue());
			avFormat.av_find_stream_info(formatCtx);
			if (StreamBabyConfig.inst._DEBUG)
				avFormat.dump_format(formatCtx, 0, filename, 0);
		
			
		    // Find the first video stream
		     AVStream audioStream = null;
		     AVStream videoStream = null;
		    for (int i=0; i<formatCtx.nb_streams; i++)
		    {   
		    	AVStream stream = new AVStream(formatCtx.getStreams()[i]);
		    	AVCodecContext codec= new AVCodecContext(stream.codec);
		    	//System.out.println("codecCtx " + i + ": " + codec);
		    	if (videoStream ==null && codec.codec_type == AVCodecLibrary.CODEC_TYPE_VIDEO)
		        {
				    videoStream = stream;
		        } else if (audioStream == null && codec.codec_type == AVCodecLibrary.CODEC_TYPE_AUDIO) {
		        	audioStream = stream;
		        }
		    }
		    
		    AVFormatLibrary.AVInputFormat fc = new AVFormatLibrary.AVInputFormat(formatCtx.iformat);
		    setContainerFormat(fc.name, vidinfo);

		    if (formatCtx.duration != AVCodecLibrary.AV_NOPTS_VALUE) {
		    	long dur = formatCtx.duration / (AVCodecLibrary.AV_TIME_BASE/1000);
		    	vidinfo.setDuration(dur);
		    	Log.debug("vidDur:" + dur);
		    }
		    if (formatCtx.start_time != AVCodecLibrary.AV_NOPTS_VALUE) 
		    	vidinfo.setStartPosition((long)(((formatCtx.start_time/(double)AVCodecLibrary.AV_TIME_BASE) * 1000L)));
		    
		    if (formatCtx.bit_rate > 0) {
		    	vidinfo.setBitRate(formatCtx.bit_rate/1000);
		    }
		    
		    
		    //vidinfo.setVideoCodec(getCodecString(videoCodec));
		    //vidinfo.setAudioCodec(getCodecString(audioCodec));
		    
		    if (audioStream != null) {
		    	AVCodecContext codecCtx= new AVCodecContext(audioStream.codec);
			    String codecName = getCodecById(codecCtx.codec_id);
			    if (codecName != null)
			    	vidinfo.setAudioCodec(codecName);
			    else {
				    final AVCodec codec = avCodec.avcodec_find_decoder(codecCtx.codec_id);
				    if (codec != null)
				    	vidinfo.setAudioCodec(translateCodec(codec.name));
				    else if (codecCtx.codec_name[0] != 0)
				    	vidinfo.setAudioCodec(translateCodec(new String(codecCtx.codec_name)));
				    else {
				    	String aviTag = aviTagDecode(codecCtx.codec_tag);
				    	if (aviTag != null)
				    		vidinfo.setAudioCodec(translateCodec(aviTag));
				    	else if (vidinfo.getContainerFormat() != null && vidinfo.getContainerFormat().compareTo(VideoFormats.CONTAINER_MPEGPS) == 0) {
				    		// This is a hack-- Ubuntu ffmpeg (and probably debian) does not support AC3 so doesn't report it
				    		// So assume if we can't figure out the audio format, and it is a MPEGPS it is AC3
				    		vidinfo.setAudioCodec(VideoFormats.AUDIO_CODEC_AC3);
				    	}
				    }
			    }
			    vidinfo.setAudioChannels(codecCtx.channels);
		    	vidinfo.setAudioBps(codecCtx.sample_rate);
		    } else
		    	vidinfo.setAudioCodec(VideoFormats.AUDIO_CODEC_NONE);
		    if (videoStream != null) {
		    	AVCodecContext codecCtx= new AVCodecContext(videoStream.codec);
		    	AVRational avAspect = codecCtx.sample_aspect_ratio;
		    	if (avAspect.den != 0 && avAspect.num != 0) {
		    		vidinfo.setPixelAspect((float)avAspect.num / (float)avAspect.den);
		    	} else {
		    		try {
			    		Field f = videoStream.getClass().getField("sample_aspect_ratio");
			    		if (f != null) {
			    			AVRational r = (AVRational)f.get(videoStream);
			    			if (r != null && r.den != 0 && r.num != 0) {
			    				vidinfo.setPixelAspect((float)r.num / (float)r.den);
			    			}
			    		}
		    		} catch(Exception e) { }
		    	}
			    String codecName = getCodecById(codecCtx.codec_id);
			    if (codecName != null)
			    	vidinfo.setVideoCodec(codecName);
			    else {
				    final AVCodec codec = avCodec.avcodec_find_decoder(codecCtx.codec_id);
				    if (codec != null)
				    	vidinfo.setVideoCodec(translateCodec(codec.name));
				    else if (codecCtx.codec_name[0] != 0)
				    	vidinfo.setVideoCodec(translateCodec(new String(codecCtx.codec_name)));		
				    else {
				    	String aviTag = aviTagDecode(codecCtx.codec_tag);
				    	if (aviTag != null)
				    		vidinfo.setVideoCodec(translateCodec(aviTag));
				    }
			    }
			    double fps = 0;
			    if (videoStream.r_frame_rate.den != 0 && videoStream.r_frame_rate.num != 0)
			    	fps = av_q2d(videoStream.r_frame_rate);
			    /*else if (videoStream.time_base.den != 0 && videoStream.time_base.num != 0) 
			    	fps = 1 / av_q2d(videoStream.time_base); */
			    else {
			    	fps = 1 / av_q2d(codecCtx.time_base);
			    }
			    vidinfo.setFps(fps);
			    vidinfo.setWidth(codecCtx.width);
			    vidinfo.setHeight(codecCtx.height);
		    } else
		    	vidinfo.setVideoCodec(VideoFormats.VIDEO_CODEC_NONE);		    
		    res = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			/*
		    // Close the codec
		    if (codecCtx != null)
		    	FFmpegHelper.avCodec.avcodec_close(codecCtx);
		    codecCtx = null;
			*/
		    // Close the video file
		    if (formatCtx != null)
			    avFormat.av_close_input_file(formatCtx);
		    formatCtx = null;
		}
		Log.debug("VideoInfo:\n" + vidinfo.toString());
		return res;
	}

	/*
	public VideoInputStream openStreamableVideo(URI uri, VideoInformation vi, long startPosition) throws IOException {
		if (!Utils.isFile(uri))
			return null;
		String filename = new File(uri).getAbsolutePath();
		float secs = startPosition / 1000.0f;
		final AVPacket packet = new AVPacket();
		boolean gotFrame = false;
		long pos = -1;
		AVFormatContext formatCtx = null;

	    try {
			// Open video file
			final PointerByReference ppFormatCtx = new PointerByReference();
			if (avFormat.av_open_input_file(ppFormatCtx, filename, null, 0, null) != 0)
			    return null;
			
			
			formatCtx = new AVFormatContext(ppFormatCtx.getValue());
			avFormat.av_find_stream_info(formatCtx);

			long timestamp = (long)(secs * AVCodecLibrary.AV_TIME_BASE + formatCtx.start_time);
			avFormat.av_seek_frame(formatCtx, -1, timestamp, AVFormatLibrary.AVSEEK_FLAG_BACKWARD);
			//FFmpegHelper.avFormat.av_seek_frame(formatCtx, videoStream, timestamp, AVFormatLibrary.AVSEEK_FLAG_BACKWARD);
	
	
			while (!gotFrame && avFormat.av_read_frame(formatCtx, packet) >= 0)
		    {
				pos = packet.pos;
				gotFrame = true;
		        if (packet.destruct != null)
		        	packet.destruct.callback(packet);
	
		    }
	    } finally {
		    // Close the video file
		    if (formatCtx != null)
			    avFormat.av_close_input_file(formatCtx);
		    formatCtx = null;	    	
	    }
	

	    if (pos == -1)
	    	return null;
		long dur = vi.getDuration() - startPosition;
		FileInputStream fis = new FileInputStream(new File(uri));
		fis.skip(pos);
		
		return new VideoInputStreamWrapper(dur, fis, vi, "video/mpeg");
	}
	*/
	
	private String getCodecById(int codec_id) {
		switch(codec_id) {
		case AVCodecLibrary.CODEC_ID_MPEG1VIDEO:
			return VideoFormats.VIDEO_CODEC_MPEG1;
		case AVCodecLibrary.CODEC_ID_MPEG2VIDEO:
			return VideoFormats.VIDEO_CODEC_MPEG2;
		case AVCodecLibrary.CODEC_ID_H264:
			return VideoFormats.VIDEO_CODEC_H264;
		case AVCodecLibrary.CODEC_ID_VC1:
			return VideoFormats.VIDEO_CODEC_VC1;
		case AVCodecLibrary.CODEC_ID_AAC:
			return VideoFormats.AUDIO_CODEC_AAC;
		case AVCodecLibrary.CODEC_ID_AC3:
			return VideoFormats.AUDIO_CODEC_AC3;
		case AVCodecLibrary.CODEC_ID_WMAV2:
			return VideoFormats.AUDIO_CODEC_WMA2;
		case AVCodecLibrary.CODEC_ID_MP2:
			return VideoFormats.AUDIO_CODEC_MP2;
		case AVCodecLibrary.CODEC_ID_MP3:
			return VideoFormats.AUDIO_CODEC_MP3;
		}
		return null;
	}

	public  class FFmpegLibPreviewer implements PreviewGenerator {
		AVFormatContext formatCtx = null;
		AVCodecContext codecCtx = null;
		Pointer bufferToFree = null;
		AVFrame frame  = null;
		AVFrame frameRGB = null;
		AVStream stream;
		int videoStream;
		VideoInformation vidInfo;
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		public  float quality = (StreamBabyConfig.cfgPreviewQuality.getInt() / 100.0f);
		public int bandOffsets[] = { 0, 1, 2 };
		private StreamableMpeg smpeg;
		int sw, sh;

		ImageWriter jpgWriter = null;
		public FFmpegLibPreviewer() {
	        // Find a jpeg writer
//	        ImageWriter writer = null;
	        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
	        if (iter.hasNext()) {
	        	jpgWriter= iter.next();
	        }


		}
		
		public boolean open(URI uri, VideoInformation vi, int sw, int sh) {
			if (!FFmpegJavaConfig.inst.useFFmpegLibrary())
				return false;

			if (!Utils.isFile(uri))
				return false;
			String filename = new File(uri).getAbsolutePath();
			if (jpgWriter == null) {
				return false;
			}
			this.vidInfo = vi;
			this.sw = sw;
			this.sh = sh;
		    boolean success = false;
		    try {
				// Open video file
				final PointerByReference ppFormatCtx = new PointerByReference();
				if (avFormat.av_open_input_file(ppFormatCtx, filename, null, 0, null) != 0)
				    return false;
				
				
				formatCtx = new AVFormatContext(ppFormatCtx.getValue());
				avFormat.av_find_stream_info(formatCtx);
				
				if (StreamBabyConfig.inst._DEBUG)
					avFormat.dump_format(formatCtx, 0, filename, 0);
			
				
			    // Find the first video stream
			     videoStream=-1;
			    for (int i=0; i<formatCtx.nb_streams; i++)
			    {   AVStream stream = new AVStream(formatCtx.getStreams()[i]);
			    	AVCodecContext codecCtx = new AVCodecContext(stream.codec);
			    	//System.out.println("codecCtx " + i + ": " + codecCtx);
			    	if (codecCtx.codec_type == AVCodecLibrary.CODEC_TYPE_VIDEO)
			        {
			            videoStream=i;
			            break;
			        } else {
				    	//FFmpegHelper.avCodec.avcodec_close(codecCtx);
					    codecCtx = null;	        	
			        }
			    }
			    
			    if (videoStream==-1)
			        return false;
			    
			    //System.out.println("Video stream index: " + videoStream);
			    
			    // Get a pointer to the codec context for the video stream
			    stream = new AVStream(formatCtx.getStreams()[videoStream]);
			    final Pointer pCodecCtx = stream.codec;
			    codecCtx = new AVCodecContext(pCodecCtx);
			    
			    //System.out.println("Codec id: " + codecCtx.codec_id);
			    
			    if (codecCtx.codec_id == 0)
			    	return false;
			    
			    // Find the decoder for the video stream
			    final AVCodec codec = avCodec.avcodec_find_decoder(codecCtx.codec_id);
			    if (codec == null)
			        return false; // Codec not found
			    
			    // Open codec
			    if (avCodec.avcodec_open(codecCtx, codec) < 0)
			    	 return false; // Could not open codec
			    
			    // Allocate video frame
			    frame = avCodec.avcodec_alloc_frame();
			    if (frame == null)
			    	return false;
			     
			    // Allocate an AVFrame structure
			    frameRGB = avCodec.avcodec_alloc_frame();
			    if (frameRGB == null)
			    	return false;

			    int fw = codecCtx.width;
			    int fh = codecCtx.height;
			    if (useSwScale()) {
			    	fw = sw;
			    	fh = sh;
			    }
			    
			    // Determine required buffer size and allocate buffer
			    final int numBytes = avCodec.avpicture_get_size(AVCodecLibrary.PIX_FMT_RGB24, fw, fh);
			    bufferToFree = avUtil.av_malloc(numBytes);
			    
			    // Assign appropriate parts of buffer to image planes in pFrameRGB
			    avCodec.avpicture_fill(frameRGB, bufferToFree, AVCodecLibrary.PIX_FMT_RGB24, fw, fh);

			    
			    if (cfgUseInternalSeek.getBool() && vi.getContainerFormat() == VideoFormats.CONTAINER_MPEGPS) {
			    	try {
						smpeg = new StreamableMpeg(new File(uri), 0);
					} catch (IOException e) {
					}
			    }
			    
			    success = true;
		    } finally {
		    	if (!success)
		    		close();
		    }
			return success;
		}
		
		public void close() {
		    if (frameRGB != null)
			// Free the RGB image
		    	avUtil.av_free(frameRGB.getPointer());
		    frameRGB = null;
		    if (bufferToFree != null)
		    	avUtil.av_free(bufferToFree);
		    bufferToFree = null;
		    // Free the YUV frame
		    if (frame != null)
		    	avUtil.av_free(frame.getPointer());
		    frame = null;

		    // Close the codec
		    if (codecCtx != null)
		    	avCodec.avcodec_close(codecCtx);
		    codecCtx = null;

		    // Close the video file
		    if (formatCtx != null)
			    avFormat.av_close_input_file(formatCtx);
		    formatCtx = null;
		    
		    if (jpgWriter != null)
		    	jpgWriter.dispose();
		    jpgWriter = null;
		    
		    if (smpeg != null)
				try {
					smpeg.close();
				} catch (IOException e) {

				}
		    smpeg = null;
		}

		/*
		public double av_q2d(FFMPEGLibrary.AVRational a) {
			return a.num / (double) a.den;
		}
		*/
		

		
		public long convert_timebase(long ts, FFMPEGLibrary.AVRational src, FFMPEGLibrary.AVRational dst) {
			double sd = (double)src.num / (double)src.den;
			double dd = (double)dst.num / (double)dst.den;
			return (long)(ts * (sd/dd));
		}
		
		public boolean useSwScale() {
			return swScale != null;
		}
		
		public BufferedImage frameToImage(AVFrame inFrame, int inPixFmt, 
				int width, int height, int dw, int dh) {
			AVFrame outFrame = frameRGB;
			int outPixFmt = AVCodecLibrary.PIX_FMT_RGB24;
			int outWidth;
			int outHeight;
			if (!useSwScale()) {
				// swscale not available, img_convert should be(?)
				outWidth = width;
				outHeight = height;
				FFmpegMgr.helper.img_convert(outFrame, outPixFmt, inFrame, inPixFmt, width, height);
	            
			} else {
				outWidth = dw;
				outHeight = dh;
				Pointer ctx = swScale.sws_getContext(width, height, inPixFmt, dw, dh, outPixFmt, SWScaleLibrary.SWS_BICUBIC, null, null, null);
				Pointer[] id = new Pointer[] { inFrame.data0, inFrame.data1, inFrame.data2, inFrame.data3 };
				Pointer[] od = new Pointer[] { outFrame.data0, outFrame.data1, outFrame.data2, outFrame.data3 };

				swScale.sws_scale(ctx, id, inFrame.linesize, 0, height, od, outFrame.linesize);
				if (ctx != null) {
					swScale.sws_freeContext(ctx);
				}
			}

            final int len = outHeight * outFrame.linesize[0];
    		final byte[] data = outFrame.data0.getByteArray(0, len);
    		BufferedImage img = new BufferedImage(outWidth, outHeight, BufferedImage.TYPE_3BYTE_BGR);
    		//SampleModel m = new MultiPixelPackedSampleModel(DataBuffer.TYPE_BYTE, codecCtx.width, codecCtx.height, 8);
    		DataBuffer dataBuffer = new DataBufferByte(data, len, 0);
    		SampleModel m = new ComponentSampleModel(DataBuffer.TYPE_BYTE, outWidth, outHeight, 3, 3 * outWidth, bandOffsets);
    		Raster r = Raster.createRaster(m, dataBuffer, new Point(0,0));
    		//Raster r = Raster.createPackedRast(dataBuffer, codecCtx.width, codecCtx.height, 8, null);	        		
    		img.setData(r);
            //BufferedImage img = FrameDataToImage.createImage(data, codecCtx.width, codecCtx.height)
    		BufferedImage scaledImage;
    		if (img.getWidth() != dw || img.getHeight() != dh) {
                // Scale it
                scaledImage = new BufferedImage(dw, 
                	      dh, BufferedImage.TYPE_INT_RGB);
        	    Graphics2D graphics2D = scaledImage.createGraphics();
        	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        	      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        	    graphics2D.drawImage(img, 0, 0, dw, dh, null);
        	    graphics2D.dispose();
    		} else {
    			scaledImage = img;
    		}
    		
    		return scaledImage;
    		

		}
			public byte[] getFrameImageData(int secs) {
			    // Read frames and save first five frames to disk
				byte[] result = null;
				final AVPacket packet = new AVPacket();
				boolean gotFrame = false;
		
				/*
				FFMPEGLibrary.AVRational timebase;
				timebase = stream.time_base;
				// timebase = new FFMPEGLibrary.AVRational();  timebase.num = 1; timebase.den = AVCodecLibrary.AV_TIME_BASE;
				FFMPEGLibrary.AVRational srcTimebase = new FFMPEGLibrary.AVRational();
				srcTimebase.num = 1;
				srcTimebase.den = 1;
				long timestamp = convert_timebase(secs, srcTimebase, timebase);
				*/
				long timestamp = (long)secs * (long)AVCodecLibrary.AV_TIME_BASE + formatCtx.start_time;
				int seekType = AVFormatLibrary.AVSEEK_FLAG_BACKWARD;
				long seekPos = timestamp;
				if (smpeg != null) {
					try {
						StreamableMpeg.MpegPosInfo pi = smpeg.binarySeek(secs * 1000L);
						if (pi != null) {
							seekType = AVFormatLibrary.AVSEEK_FLAG_BYTE;
							seekPos = pi.filePos;
						}
					} catch (IOException e) {
					}
					
				}
				avFormat.av_seek_frame(formatCtx, -1, seekPos, seekType);
				//FFmpegHelper.avFormat.av_seek_frame(formatCtx, videoStream, timestamp, AVFormatLibrary.AVSEEK_FLAG_BACKWARD);
			    
				while (!gotFrame && avFormat.av_read_frame(formatCtx, packet) >= 0)
			    {
			    	
			    	// Is this a packet from the video stream?
			        if (packet.stream_index == videoStream)
			        {
			        	final IntByReference frameFinished = new IntByReference();
			            // Decode video frame
			        	avCodec.avcodec_decode_video(codecCtx, frame, frameFinished, packet.data, packet.size);
			        	if (frame.key_frame == 0)
			        		continue;
			        	long myPts;
			        	// This logic (reading to will hit a timestamp greater than what we are looking for
			        	// WOrks much better for mpeg than mp4, so only do in mpeg container case
			        	if (vidInfo.getContainerFormat().equals(VideoFormats.CONTAINER_MPEGPS) && cfgExactSeek.getBool() )
			        		myPts = (long)(packet.pts / packet.duration * AVCodecLibrary.AV_TIME_BASE / av_q2d( stream.r_frame_rate));
			            else
			            	myPts = timestamp;
			            // Did we get a video frame?
			            if (myPts >= timestamp && frameFinished.getValue() != 0)
			            {
			            	gotFrame = true;
			                // Convert the image from its native format to RGB
			            	BufferedImage scaledImage = frameToImage(frame, codecCtx.pix_fmt, codecCtx.width, 
				                    codecCtx.height, sw, sh); 
			                
			                bs.reset();
			                ImageOutputStream ios;
							try {
								ios = ImageIO.createImageOutputStream(bs);
				                jpgWriter.setOutput(ios);
				                // Set the compression quality
				                ImageWriteParam iwparam = new MyImageWriteParam();
				                iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
				                iwparam.setCompressionQuality(quality);
				        
				                // Write the image
				                jpgWriter.write(null, new IIOImage(scaledImage, null, null), iwparam);
				        
				                // Cleanup
				                ios.flush();		                
				                ios.close();
				                
				                result = bs.toByteArray();
							} catch (IOException e) {
								e.printStackTrace();
							}
		
		
			                /*
			                // Save the frame to disk
			                if(++i<=5)
			                    SaveFrame(frameRGB, codecCtx.width, codecCtx.height, 
			                        i);
			                        */
			            }
			        }
		
			        // Free the packet that was allocated by av_read_frame
			        // AVFORMAT.av_free_packet(packet.getPointer()) - cannot be called because it is an inlined function.
			        // so we'll just do the JNA equivalent of the inline:
			        if (packet.destruct != null)
			        	packet.destruct.callback(packet);
		
			    }
			    return result;
			}

		// This class overrides the setCompressionQuality() method to workaround
	    // a problem in compressing JPEG images using the javax.imageio package.
	    public class MyImageWriteParam extends JPEGImageWriteParam {
	        public MyImageWriteParam() {
	            super(Locale.getDefault());
	        }
	    
	        /* This doesn't seem needed in newer versions of java, and in fact causes problems */
	        
	        /*
	        // This method accepts quality levels between 0 (lowest) and 1 (highest) and simply converts
	        // it to a range between 0 and 256; this is not a correct conversion algorithm.
	        // However, a proper alternative is a lot more complicated.
	        // This should do until the bug is fixed.
	        public void setCompressionQuality(float quality) {
	            if (quality < 0.0F || quality > 1.0F) {
	                throw new IllegalArgumentException("Quality out-of-bounds!");
	            }
	            this.compressionQuality = 256 - (quality * 256);
	        }
	        */
	    }

		public boolean isRealtime() {
			return true;
		}

		public void prepare(int secs, int delta) {
			// TODO Auto-generated method stub
			
		}		
	}

	public Object getModule(int moduleType) {
		if (moduleType != StreamBabyModule.STREAMBABY_MODULE_VIDEO)
			return null;
		
		if (!FFmpegJavaConfig.inst.useFFmpegLibrary()) {
			Log.warn("Unable to load FFmpeg native libraries");
			return null;
		}
		VideoHandlerModule intf = null;
		try {
			intf = FFmpegMgr.fixupFFmpegClass(FFmpegJavaVideoModule.class);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return intf;
	}
}

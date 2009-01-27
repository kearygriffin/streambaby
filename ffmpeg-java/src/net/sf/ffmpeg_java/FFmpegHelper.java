package net.sf.ffmpeg_java;

import com.sun.jna.Structure;

//Note: Many of these functions take generic Structure instead of the correct superclass.  This is 
//so we can pass V51/V52 structures to the functions.  Probably a better way to handle this, but...

public interface FFmpegHelper {
	public int img_convert(Structure dst, int dst_pix_fmt,
			Structure src, int pix_fmt,
            int width, int height);
}
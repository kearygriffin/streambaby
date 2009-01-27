package net.sf.ffmpeg_java;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

// Note: Many of these functions take generic Structure instead of the correct superclass.  This is 
// so we can pass V51/V52 structures to the functions.  Probably a better way to handle this, but...
public class FFmpegHelperImpl implements FFmpegHelper {
	

	/* Image convert helpers */
	private int sws_img_convert(Structure dst, int dst_pix_fmt,
				Structure src, int pix_fmt,
	            int width, int height) {
			if (FFmpegMgr.hasSwScale) {
				Pointer ctx = SWScaleLibrary.INSTANCE.sws_getContext(width, height, pix_fmt, width, height, dst_pix_fmt, SWScaleLibrary.SWS_BICUBIC, null, null, null);
				
				Pointer[] id = src.getPointer().getPointerArray(0, 4); // new Pointer[] { psrc.data0, psrc.data1, psrc.data2, psrc.data3 };
				Pointer[] od = dst.getPointer().getPointerArray(0, 4);//new Pointer[] { pdst.data0, pdst.data1, pdst.data2, pdst.data3 };
				int[] src_linesize = src.getPointer().getIntArray(4*Pointer.SIZE, 4);
				int[] dst_linesize = dst.getPointer().getIntArray(4*Pointer.SIZE, 4);
				int ret = SWScaleLibrary.INSTANCE.sws_scale(ctx, id, src_linesize, 0, height, od, dst_linesize);
				if (ctx != null) {
					SWScaleLibrary.INSTANCE.sws_freeContext(ctx);
				}
				return ret;
			} 
			return -1;
		}
		private int call_img_convert(Structure dst, int dst_pix_fmt, Structure src, int pix_fmt, int width, int height) {
			Function img_convert_func = FFmpegMgr.avCodecNative.getFunction("img_convert");
			Object[] args = new Object[] {
				dst, dst_pix_fmt, src, pix_fmt, width, height	
			};
			return img_convert_func.invokeInt(args);
			//return AVCodecLibrary.INSTANCE.img_convert(dst, dst_pix_fmt, src, pix_fmt, width, height);
			
		}
		public int img_convert(Structure dst, int dst_pix_fmt,
				Structure src, int pix_fmt,
	            int width, int height) {
			if (FFmpegMgr.hasSwScale)
				return sws_img_convert((Structure)dst, dst_pix_fmt, (Structure)src, pix_fmt, width, height);
			else {
				return call_img_convert((Structure)dst, dst_pix_fmt, (Structure)src, pix_fmt, width, height);
			}
		}
}

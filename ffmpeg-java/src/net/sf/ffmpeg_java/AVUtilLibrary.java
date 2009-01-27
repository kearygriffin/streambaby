package net.sf.ffmpeg_java;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

/**
 * Based on FFMPEG Aug 12 2007.  
 * From mem.h
 * @author Ken Larson
 *
 */
public interface AVUtilLibrary extends FFMPEGLibrary 
{
	// Make sure the library is inited BEFORE we set the INSTANCE variable
	public static final int avCodecLibVer = FFmpegMgr.getAvCodecVersion();
    public static final AVUtilLibrary INSTANCE = (AVUtilLibrary) Native.loadLibrary(
    		System.getProperty("avutil.lib",
    		System.getProperty("os.name").startsWith("Windows") ? "avutil-49" : "avutil"), 
    		AVUtilLibrary.class);

//------------------------------------------------------------------------------------------------------------------------
// mem.h
    
//    void *av_malloc(unsigned int size);
//    void *av_realloc(void *ptr, unsigned int size);
//    void av_free(void *ptr);
//    void *av_mallocz(unsigned int size);
//    char *av_strdup(const char *s);
//    void av_freep(void *ptr);
    public Pointer av_malloc(int size);
    public Pointer av_realloc(Pointer ptr, int size);
    public void av_free(Pointer ptr);
    public Pointer av_mallocz(int size);
    public Pointer av_strdup(Pointer s);
    public void av_freep(PointerByReference ptr);
    
// end mem.h
//------------------------------------------------------------------------------------------------------------------------
	
   public void av_log_set_level(int l); 
   
   /* fifo.h */

   public static class AVFifoBuffer extends Structure {
       public Pointer buffer;
       public Pointer rptr;
       public Pointer wptr; 
       public Pointer end;
   } 

   /**
    * Initializes an AVFifoBuffer.
    * @param *f AVFifoBuffer to initialize
    * @param size of FIFO
    * @return <0 for failure >=0 otherwise
    */
   int av_fifo_init(AVFifoBuffer f, int size);

   /**
    * Frees an AVFifoBuffer.
    * @param *f AVFifoBuffer to free
    */
   void av_fifo_free(AVFifoBuffer f);

   /**
    * Returns the amount of data in bytes in the AVFifoBuffer, that is the
    * amount of data you can read from it.
    * @param *f AVFifoBuffer to read from
    * @return size
    */
   int av_fifo_size(AVFifoBuffer f);

   /**
    * Reads data from an AVFifoBuffer.
    * @param *f AVFifoBuffer to read from
    * @param *buf data destination
    * @param buf_size number of bytes to read
    */
   int av_fifo_read(AVFifoBuffer f, Pointer buf, int buf_size);

   /**
    * Feeds data from an AVFifoBuffer to a user supplied callback.
    * @param *f AVFifoBuffer to read from
    * @param buf_size number of bytes to read
    * @param *func generic read function
    * @param *dest data destination
    */
   int av_fifo_generic_read(AVFifoBuffer f, int buf_size, Pointer func, Pointer dest);

   /**
    * Writes data into an AVFifoBuffer.
    * @param *f AVFifoBuffer to write to
    * @param *buf data source
    * @param size data size
    */
   void av_fifo_write(AVFifoBuffer f, Pointer buf, int size);

   /**
    * Resizes an AVFifoBuffer.
    * @param *f AVFifoBuffer to resize
    * @param size new AVFifoBuffer size in bytes
    */
   void av_fifo_realloc(AVFifoBuffer f, int size);

   /**
    * Reads and discards the specified amount of data from an AVFifoBuffer.
    * @param *f AVFifoBuffer to read from
    * @param size amount of data to read in bytes
    */
   void av_fifo_drain(AVFifoBuffer f, int size);   
   
   /**
    * rescale a 64bit integer with rounding to nearest.
    * a simple a*b/c isn't possible as it can overflow
    */
   long av_rescale(long  a, long  b, long  c);

   /**
    * rescale a 64bit integer with specified rounding.
    * a simple a*b/c isn't possible as it can overflow
    */
   //long av_rescale_rnd(long a, long b, long c, enum AVRounding);

   /**
    * rescale a 64bit integer by 2 rational numbers.
    */
   long av_rescale_q(long a, AVRational bq, AVRational cq);

}

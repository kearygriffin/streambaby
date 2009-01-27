package net.sf.ffmpeg_java.custom_protocol;

//import net.sf.ffmpeg_java.v52.AVFormatLibrary.URLContext;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * A Java version ffmpeg URLProtocol, bug specific to a single stream.
 * Subclasses should be constructed once for each stream, with the information needed to connect to the
 * stream provided in the constructor.
 * @author Ken Larson
 *
 */
public interface CallbackURLProtocolHandler
{	
	public static final int URL_RDONLY =0;
	public static final int URL_WRONLY =1;
	public static final int URL_RDWR   =2;
	public static final int AVSEEK_SIZE = 0x10000;


	/** Flags may be a combination of: AVFormatLibrary.URL_RDONLY, AVFormatLibrary.URL_WRONLY, AVFormatLibrary.URL_RDWR. */
    int open(Structure h, String filename, int flags);
	int read(Structure h, Pointer buf, int size);
	int write(Structure h, Pointer buf, int size);
	// values for whence
	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;
	/** Implementations need to be very careful about returning -1.  Basically, they should support full seek capability,
	 * or not at all, because much code in ffmpeg never checks the return value of a seek. */
	long seek(Structure h, long pos, int whence);
    int close(Structure h);

    
}

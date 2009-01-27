package net.sf.ffmpeg_java.custom_protocol;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;


public class PipeURLProtocolHandler implements CallbackURLProtocolHandler  {

	private PipedInputStream is;
	private PipedOutputStream os;
	
	public PipeURLProtocolHandler(PipedInputStream is, PipedOutputStream os) {
		this.is = is;
		this.os = os;
	}
	
	public PipeURLProtocolHandler(PipedInputStream is) {
		this(is, null);
	}
	
	public PipeURLProtocolHandler(PipedOutputStream os) {
		this(null, os);
	}
	public int close(Structure h) {
		boolean err = false;
		if (is != null)
			try {
				is.close();
			} catch (IOException e) {
				err = true;
			}
		is = null;
		if (os != null)
			try {
				os.close();
			} catch (IOException e) {
				err = true;
			}
		os = null;
		return err ? -1 : 0;
	}

	public int open(Structure h, String filename, int flags) {
		if ((flags & URL_RDWR) != 0 && (is == null || os == null))
			return -1;
		else if ((flags & URL_WRONLY) != 0 && os == null)
			return -1;
		else if (is == null)
			return -1;
		h.writeField("is_streamed", 1);
		return 0;

	}

	public int read(Structure h, Pointer buf, int size) {
		final byte[] ba = new byte[size]; //buf.getByteArray(0, size);
		
		try
		{
			final int ret = is.read(ba, 0, size);
			buf.write(0, ba, 0, size);
			
			return ret;
		} catch (IOException e)
		{
			return -1;
		}

	}

	public long seek(Structure h, long pos, int whence) {
		return -1;
	}

	public int write(Structure  h, Pointer buf, int size) {
		final byte[] ba = buf.getByteArray(0, size);

		int ret = -1;
		if (os != null) {
			try {
				os.write(ba);
				ret = ba.length;
			} catch (IOException e) {
			}
		}
		return ret;
	}

}

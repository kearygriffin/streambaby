package net.sf.ffmpeg_java.custom_protocol;


import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public  class URLProtocol extends Structure {
    public String name;
    public URLOpen url_open;
    public URLRead url_read;
    public URLWrite url_write;
    public URLSeek url_seek;
    public URLClose url_close;
    public Pointer next;

	public interface URLOpen extends Callback 
    {
        int callback(Structure h, String filename, int flags);
    }
	public interface URLRead extends Callback 
    {
    	int callback(Structure h, Pointer buf, int size);
    }
	public interface URLWrite extends Callback 
    {
    	int callback(Structure h, Pointer buf, int size);
    }
	public interface URLSeek extends Callback 
    {
        long callback(Structure h, long pos, int whence);
    }
	public interface URLClose extends Callback 
    {
        int callback(Structure h);
    }
	

}

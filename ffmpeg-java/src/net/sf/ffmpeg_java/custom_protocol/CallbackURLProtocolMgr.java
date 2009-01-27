package net.sf.ffmpeg_java.custom_protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.ffmpeg_java.FFmpegMgr;
import net.sf.ffmpeg_java.custom_protocol.URLProtocol.URLClose;
import net.sf.ffmpeg_java.custom_protocol.URLProtocol.URLOpen;
import net.sf.ffmpeg_java.custom_protocol.URLProtocol.URLRead;
import net.sf.ffmpeg_java.custom_protocol.URLProtocol.URLSeek;
import net.sf.ffmpeg_java.custom_protocol.URLProtocol.URLWrite;

//import net.sf.ffmpeg_java.v52.AVFormatLibrary;
//import net.sf.ffmpeg_java.v52.AVFormatLibrary.URLContext;
//import net.sf.ffmpeg_java.v52.AVFormatLibrary.URLProtocol;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Provides a single ffmpeg URLProtocol, using the prefix callback:, which calls back to Java code to access the data.
 * 
 * A particular handler for a particular stream is set with addCallbackURLProtocolHandler, using a generated URL.
 * 
 * Before using, the CallbackURLProtocolMgr needs to be registered using register.
 *
 * Then, to register a new stream, and open it with ffmpeg:
 * 
 * 		final String callbackURL = CallbackURLProtocolMgr.addCallbackURLProtocolHandler(new FileCallbackURLProtocolHandler(new File(filePath)));
 *
 *		final PointerByReference ppFormatCtx = new PointerByReference();
 *		
 *		if (AVFORMAT.av_open_input_file(ppFormatCtx, callbackURL, null, 0, null) != 0)
 *		    throw new RuntimeException("Couldn't open file"); // Couldn't open file
 *			
 * @author Ken Larson
 *
 */
public class CallbackURLProtocolMgr
{
	private static URLProtocol instance = init();

	public static URLProtocol getURLProtocol()
	{	return instance;
	}
	
	public static final String URL_PREFIX = "callback";
	
	
	private static boolean registered = false;
	
	public static synchronized void register()
	{
		if (registered)
			return;
		FFmpegMgr.LibDefaults def = FFmpegMgr.initLibrary();
		if (def != null) {
			Function f= FFmpegMgr.avFormatNative.getFunction("register_protocol");
			int ret = ((Integer)(f.invoke(int.class, new Object[] { CallbackURLProtocolMgr.getURLProtocol() } ))).intValue();
			if (ret == 0) {
				registered = true;

				return;
			}
		}
		// Only end up here on error.
		throw new RuntimeException("Failed to register callback protocol");	// register_protocol never returns nonzero, so this should never happen
		
	}
	
	private static long index = 0;
	/**
	 * Generate a unique URL with our special prefix.
	 */
	private static final synchronized String generateURL()
	{
		return URL_PREFIX + ":" + index++;
	}
	
	private static URLProtocol init()
	{
		URLProtocol callbackProtocol = new URLProtocol();
		callbackProtocol.name = URL_PREFIX;
		callbackProtocol.url_open = new URLOpen() {
			public int callback(Structure h, String filename, int flags)
			{
				final CallbackURLProtocolHandler handler;
				synchronized (HANDLERS_SYNC_OBJ)
				{
					handler = get(filename);
					if (handler == null)
					{	System.err.println("Could not find handler for " + filename);
						return -1;
					}
					// store it by pointer so we can look it up later:
					handlersByURLContext.put(h, handler);
				}
				
				// we are also able to set h.priv_data to whatever we want... but
				// this is not really needed since we use handlersByURLContext.
				
				return handler.open(h, filename, flags);
			}
		};
		callbackProtocol.url_read = new URLRead() {
			public int callback(Structure h, Pointer buf, int size)
			{
				final CallbackURLProtocolHandler handler = get(h);
				if (handler == null)
				{	System.err.println("Could not find handler for " + h);
					return -1;
				}
				return handler.read(h, buf, size);
			}
		};
		callbackProtocol.url_write = new URLWrite() {
			public int callback(Structure h, Pointer buf, int size)
			{
				final CallbackURLProtocolHandler handler = get(h);
				if (handler == null)
				{	System.err.println("Could not find handler for " + h);
					return -1;
				}
				return handler.write(h, buf, size);
			}
		};
		callbackProtocol.url_seek = new URLSeek() {
			public long callback(Structure h, long pos, int whence)
			{
				final CallbackURLProtocolHandler handler = get(h);
				if (handler == null)
				{	System.err.println("Could not find handler for " + h);
					return -1;
				}
				return handler.seek(h, pos, whence);
			}
		};
		callbackProtocol.url_close = new URLClose() {
			public int callback(Structure h)
			{
				final CallbackURLProtocolHandler handler = get(h);
				if (handler == null)
				{	System.err.println("Could not find handler for " + h);
					return -1;
				}
				return handler.close(h);
			}
		};
		
		return callbackProtocol;
	}
	
	private static final Object HANDLERS_SYNC_OBJ = new Boolean(true);
	
	// sync on handlersByFilename for either of these:
	private static final Map<String, CallbackURLProtocolHandler> handlersByFilename = new HashMap<String, CallbackURLProtocolHandler>();
	private static final Map<Structure, CallbackURLProtocolHandler> handlersByURLContext = new HashMap<Structure, CallbackURLProtocolHandler>();
	
	private static CallbackURLProtocolHandler get(String filename)
	{
		synchronized (HANDLERS_SYNC_OBJ)
		{
			return handlersByFilename.get(filename);
		}
	}
	
	private static CallbackURLProtocolHandler get(Structure filename)
	{
		synchronized (HANDLERS_SYNC_OBJ)
		{
			return handlersByURLContext.get(filename);
		}
	}
	
	public static String addCallbackURLProtocolHandler(/*String filename, */CallbackURLProtocolHandler h)
	{
		final String s = generateURL();
		
		synchronized (HANDLERS_SYNC_OBJ)
		{
			handlersByFilename.put(s, h);
		}
		return s;
	}
	
	public static void removeCallbackURLProtocolHandler(CallbackURLProtocolHandler h)
	{
		synchronized (HANDLERS_SYNC_OBJ)
		{
			// remove from handlersByFilename
			{
				final Set<String> toRemove = new HashSet<String>();
				for (Entry<String, CallbackURLProtocolHandler> e : handlersByFilename.entrySet())
				{	if (e.getValue() == h)
						toRemove.add(e.getKey());
				}
				for (String p : toRemove)
					handlersByFilename.remove(p);
				
			}
			
			// remove from handlersByURLContext
			{
				final Set<Structure> toRemove = new HashSet<Structure>();
				for (Entry<Structure, CallbackURLProtocolHandler> e : handlersByURLContext.entrySet())
				{	if (e.getValue() == h)
						toRemove.add(e.getKey());
				}
				for (Structure p : toRemove)
					handlersByURLContext.remove(p);
				
			}
		}
	}

	
}

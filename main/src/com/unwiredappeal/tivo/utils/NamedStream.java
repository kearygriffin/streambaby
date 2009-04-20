package com.unwiredappeal.tivo.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import java.util.UUID;


public class NamedStream /* extends FilterInputStream */ {

	private static Map<String, NamedStream> streamMap = Collections.synchronizedMap(new HashMap<String, NamedStream>()); 
	//public InputStream is = null;
	boolean isClosed = false;
	private long dur = -1;
	public String name;
	public static final String NAMED_STREAM_EXT = ".stream";
	private String contentType;
	InputStream is;

	public class WrappedStream extends FilterInputStream {

		protected WrappedStream(InputStream in) {
			super(in);
		}
		
		@Override
		public void close() throws IOException {
			closeNamedStream();
			super.close();
		}
		
	}
	public NamedStream(InputStream is) {
		this(is, -1);
	}
	
	public NamedStream(InputStream is, long dur) {
		this(is, UUID.randomUUID().toString(), dur);
	}

	public String getStreamName() {
		return name + NAMED_STREAM_EXT;
	}

	public NamedStream(InputStream is, String name, long dur) {
		this(is, name);
		setDuration(dur);
	}
	
	protected NamedStream(String name) {
		this.name = name;
		registerStream(this, getStreamName());		
	}
	protected NamedStream() {
		this(UUID.randomUUID().toString());
	}
	
	
	public NamedStream(InputStream is, String name) {
		this(name);
		// Wrap the inputstream to close this named stream when we close.
		//super(is);
		this.is = new WrappedStream(is);
		//this.is = is;
	}

	public void setDuration(long d) {
		dur = d;
	}
	
	public boolean isClosed() {
		return isClosed;
	}

	public void closeNamedStream() {
		if (!isClosed()) {
			isClosed = true;
			deregisterStream(getStreamName());
		}
	}
	
	public InputStream open() throws IOException {
		return this.is;
	}
	
	public static NamedStream getNamedStream(String streamName) {
		Log.debug("Getting mapped stream for named stream: " + streamName);
		return streamMap.get(streamName);
	}
	
	public static InputStream openNamedStream(String streamName) throws IOException {
		NamedStream ns = getNamedStream(streamName);
		if (ns == null)
			return null;
		return ns.open();
	}


	private static void deregisterStream(String streamName) {
		Log.debug("DeRegistering named stream: " + streamName);
		streamMap.remove(streamName);
	}

	private static void registerStream(NamedStream namedStream, String streamName) {
		Log.debug("Registering named stream: " + streamName);
		streamMap.put(streamName, namedStream);
	}

	public long getDuration() {
		return dur;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
}

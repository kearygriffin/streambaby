package com.unwiredappeal.mediastreams;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;

public class VideoInputStreamWrapper extends FilterInputStream implements VideoInputStream {

	public static int BUFFER_SIZE = 64 * 1024;
	public VideoInformation vidinfo;
	public String mime;
	public String contentType;
	public long dur;
	public boolean canRandomPosition = true;
	public VideoInputStreamWrapper(long dur, InputStream is, VideoInformation vi, String mime) {
		super(new BufferedInputStream(is, BUFFER_SIZE));
		this.dur = dur;
		this.vidinfo = vi;
		this.mime = mime;
		this.contentType = mime;
	}
	public InputStream getInputStream() {
		return this;
	}

	public String getMimeType() {
		return mime;
	}

	public VideoInformation getVideoInformation() {
		return vidinfo;
	}
	public long getSubDuration() {
		return dur;
	}
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String c) {
		contentType = c;
	}

	public boolean canPosition() {
		return canRandomPosition;
	}

}

package com.unwiredappeal.mediastreams;

import java.io.InputStream;


public interface VideoInputStream {
	public  String getMimeType();
	public String getContentType();
	public  VideoInformation getVideoInformation();
	public InputStream getInputStream();
	public long getSubDuration();
	public boolean canPosition();
	public long getLength();
}

package com.unwiredappeal.mediastreams;

import java.io.IOException;
import java.io.InputStream;
import com.unwiredappeal.tivo.utils.SocketProcessInputStream;
import com.unwiredappeal.tivo.utils.AvailableSocket.SocketNotAvailable;

public class SocketProcessVideoInputStream extends SocketProcessInputStream implements VideoInputStream {

	boolean hasError = false;
	String mime;
	VideoInformation vidinfo;
	long startPos;
	public SocketProcessVideoInputStream(VideoInformation vidinfo, long startPos, String mime) throws SocketNotAvailable, IOException  {
		this.mime = mime;
		this.vidinfo = vidinfo;
		this.startPos = startPos;
	}
	public InputStream getInputStream() {
		return this;
	}

	public String getMimeType() {
		return mime;
	}

	public long getSubDuration() {
		return vidinfo.getDuration() - startPos;
	}

	public VideoInformation getVideoInformation() {
		return vidinfo;
	}
	public String getContentType() {
		return mime;
	}
	
	public boolean canPosition() {
		return true;
	}
	
}

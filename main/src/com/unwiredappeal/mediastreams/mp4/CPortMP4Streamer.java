package com.unwiredappeal.mediastreams.mp4;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CPortMP4Streamer extends MP4Streamer {

	
	StreamableMP4 mp4;
	public CPortMP4Streamer(File f, long startPos, boolean reinterleave) throws IOException {
		super(new StreamableMP4(f, startPos, reinterleave));
		mp4 = (StreamableMP4)this.in;
	}
	@Override
	public List<String> getFormats() {
		return mp4.getFormats();
	}

	@Override
	public int getHeight() {
		return mp4.getHeight();
	}

	@Override
	public int getProfile() {
		return mp4.getProfile();
	}

	@Override
	public int getProfileLevel() {
		return mp4.getProfileLevel();
	}

	@Override
	public long getSubDuration() {
		return mp4.getSubDuration();
	}

	@Override
	public int getWidth() {
		return mp4.getWidth();
	}

}

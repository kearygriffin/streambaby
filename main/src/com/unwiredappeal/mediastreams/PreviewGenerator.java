package com.unwiredappeal.mediastreams;

import java.net.URI;

public interface PreviewGenerator {
	public boolean isRealtime();
	public boolean open(URI uri, VideoInformation vinfo, int sw, int sh);
	public void close();
	public byte[] getFrameImageData(int secs);
	public void prepare(int secs, int delta);
	}

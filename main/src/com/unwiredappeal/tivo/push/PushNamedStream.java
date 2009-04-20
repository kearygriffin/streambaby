package com.unwiredappeal.tivo.push;

import java.io.IOException;
import java.io.InputStream;

import com.unwiredappeal.mediastreams.VideoInputStream;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.modules.VideoModuleHelper;
import com.unwiredappeal.tivo.utils.NamedStream;

public class PushNamedStream extends NamedStream {
	DirEntry de;
	int qual;
	public PushNamedStream(DirEntry de, int qual) {
		this.de = de;
		this.qual = qual;
	}
	
	public InputStream open() throws IOException {
		VideoInputStream vis = VideoModuleHelper.inst.openVideo(de, 0L, qual);
		if (vis == null)
			throw new IOException("Can't open video: " + de.getName());
		return vis.getInputStream();
			
	}
}

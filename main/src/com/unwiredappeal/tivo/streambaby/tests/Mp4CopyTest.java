package com.unwiredappeal.tivo.streambaby.tests;

import com.unwiredappeal.mediastreams.MP4StreamingModule;

public class Mp4CopyTest implements Tests.Test {

	public void runTest(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: --test mp4copy src.mp4 dst.mp4 <start_in_milliseconds>");
			return;
		}
		try {
			MP4StreamingModule.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

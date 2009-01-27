package com.unwiredappeal.tivo.streambaby.tests;

import com.unwiredappeal.mediastreams.MpegStreamingModule;

public class MpegCopyTest implements Tests.Test {

	public void runTest(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: --test mpegcopy src.mpg dst.mpg <start_in_milliseconds>");
			return;
		}
		try {
			MpegStreamingModule.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

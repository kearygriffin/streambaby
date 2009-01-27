package com.unwiredappeal.tivo.streambaby.tests;

import net.sf.ffmpeg_java.example.PlayerExample;

public class FFPlayTest implements Tests.Test {

	public void runTest(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: --test ffplay <filetoplay>");
		}
		try {
			PlayerExample.main(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

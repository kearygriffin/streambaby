package com.unwiredappeal.tivo.streambaby.tests;

import java.util.HashMap;
import java.util.Map;

import com.tivo.hme.host.util.ArgumentList;

public class Tests {
	public static interface Test {
		public void runTest(String[] args);
	}
	private static Map<String, String> testMap = new HashMap<String, String>();
	
	static {
		testMap.put("test", SimpleTest.class.getCanonicalName());
		testMap.put("mp4copy", Mp4CopyTest.class.getCanonicalName());
		testMap.put("mpegcopy", MpegCopyTest.class.getCanonicalName());
		testMap.put("ffplay", FFPlayTest.class.getCanonicalName());

	}
	public void performTest(ArgumentList al) {
		String tname = al.shift();
		if (tname != null) {
			String testClass = testMap.get(tname);
			if (testClass == null) {
				System.err.println("Unknown test: " + tname);
				return;
			}

			String[] args = al.getRemainingArgs();
			Test test;
			try {
				System.err.println("Running test: " + tname + ", Class: " + testClass);
				test = (Test)(Class.forName(testClass).newInstance());
			} catch (Exception e) {
				System.err.println("Failed to run test");
				return;
			}
			test.runTest(args);
		} else {
			System.err.println("You must provide a test name");
		}
	}
}

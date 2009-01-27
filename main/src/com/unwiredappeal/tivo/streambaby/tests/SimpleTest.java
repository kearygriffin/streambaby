package com.unwiredappeal.tivo.streambaby.tests;

public class SimpleTest implements Tests.Test {

	public void runTest(String[] args) {
		for (int i=0;i<args.length;i++) {
			System.err.println("arg(" + i + "): " + args[i]);
		}
		
	}

}

package com.unwiredappeal.tivo.utils;

import java.io.ByteArrayOutputStream;

public class AccessibleByteArrayOutputStream extends ByteArrayOutputStream {

	public byte[] getByteBuffer() {
		return this.buf;
	}
	
	public int getByteBufferCount() {
		return this.count;
	}
}

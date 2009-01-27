package com.unwiredappeal.mediastreams.mpeg;

import java.io.IOException;
import com.unwiredappeal.tivo.utils.BitInputStream;
import com.unwiredappeal.tivo.utils.RandomAccessInputStream;

public class RandomAccessBitInputStream extends BitInputStream {

	private  RandomAccessInputStream in;
	public RandomAccessBitInputStream(RandomAccessInputStream in) {
		super(in);
		this.in = in;
	}
	
	public RandomAccessBitInputStream(RandomAccessInputStream in, boolean reverse) {
		super(in, reverse);
		this.in = in;
	}
	public long getFilePointer() throws IOException {
		return in.getFilePointer();
	}
	
	public void seek(long pos) throws IOException {
		in.seek(pos);
		resetBitPositions();
	}
	
	public long length() throws IOException {
		return in.length();
	}
	
	public int readByte() throws IOException {
		resetBitPositions();
		return in.read();
	}


}

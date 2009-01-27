package com.unwiredappeal.virtmem;

public interface MemChunk {
	public void free();
	void set(int offset, int val);
	int get(int offset);
	void write(int offset, byte[] buf, int bufOffset, int len);
	void write(int offset, byte[] buf);
	void read(int offset, byte[] buf, int bufOffset, int len);
	void read(int offset, byte[] buf);
	public int getSize();
}

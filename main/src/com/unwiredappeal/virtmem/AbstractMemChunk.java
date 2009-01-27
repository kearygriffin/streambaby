package com.unwiredappeal.virtmem;


public abstract class AbstractMemChunk implements MemChunk {

	AbstractMemoryManager memManager;
	MemoryArea memArea;
	int size;
	
	protected AbstractMemChunk(AbstractMemoryManager memManger, MemoryArea memArea, int size) {
		this.memManager = memManger;
		this.memArea = memArea;
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
	public void free() {
		synchronized(memManager) {
			memManager.allocMethod.mergeMemoryArea(memManager.freeMemory, memArea);
		}
		
	}

	public void read(int offset, byte[] buf, int bufOffset, int len) {
		for (int i=0;i<len;i++) {
			buf[i+bufOffset] = (byte)(get(offset+i)&0xff);
		}
	}

	public void read(int offset, byte[] buf) {
		read(offset, buf, 0, buf.length);
	}

	public void write(int offset, byte[] buf, int bufOffset, int len) {
		for (int i=0;i<len;i++) {
			set(offset+i, buf[i+bufOffset]);
		}
	}

	public void write(int offset, byte[] buf) {
		write(offset, buf, 0, buf.length);
	}

}

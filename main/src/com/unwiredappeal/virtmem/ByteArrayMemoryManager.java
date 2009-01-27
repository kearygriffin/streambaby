package com.unwiredappeal.virtmem;


public class ByteArrayMemoryManager extends SegmentedMemoryManager {

	public static int MAX_BUFFERS = 128;
	public static int BUFFER_SIZE = 64 * 1024;
	
	public static ByteArrayMemoryManager manager = new ByteArrayMemoryManager();
	
	protected ByteArrayMemoryManager() {
		super(MAX_BUFFERS);
	}
	
	@Override
	public int getConstantSegmentSize() {
		return BUFFER_SIZE;
	}

	
	private static class ByteArraySegment extends AbstractSegment {

		byte[] b;
		public ByteArraySegment(int offset, int size) {
			super(offset, size);
			b = new byte[size];
		}
		public int get(int offset) {
			return b[offset - getOffset()];
		}

		public void set(int offset, int val) {
			b[offset - getOffset()] = (byte)(val&0xff);
		}
		
	}
	
	public Segment requestNewSegment(int offset, int size) {
		return new ByteArraySegment(offset, BUFFER_SIZE);
	}

	public static void fill(MemChunk[] chunks, int siz) {
		boolean hasMoreMemory = true;
		int cnt = 0;
		while(hasMoreMemory) {
			MemChunk chunk = ByteArrayMemoryManager.manager.alloc(siz);
			if (chunk == null) {
				System.err.println("Alloc failed.");
				hasMoreMemory = false;
			} else {
				chunks[cnt] = chunk;
				cnt++;
				System.err.println("Alloc #" + cnt + " succeeded.");
			}
		}		
	}
	public static void main(String[] argv) throws Exception {
		MemChunk[] chunks = new MemChunk[1024];
		fill(chunks, 5000);
		chunks[7].free();
		chunks[12].free();
		chunks[13].free();
		fill(chunks, 10000);
		fill(chunks, 5000);

	}


}

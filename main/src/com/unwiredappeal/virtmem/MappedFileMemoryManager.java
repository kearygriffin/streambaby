package com.unwiredappeal.virtmem;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;


public class MappedFileMemoryManager extends SegmentedMemoryManager {

	public static final int ONEK = 1024;
	public static final int ONEMEG = 1024 * ONEK;
	public static final int ONEGIG = 1024 * ONEMEG;
	
	public static int TOTAL_BUFFER_LENGTH = 512 * ONEMEG;
	public static int BUFFER_SIZE = 16 * ONEMEG;		// 16 megabytes
	public static int MAX_BUFFERS = TOTAL_BUFFER_LENGTH/BUFFER_SIZE;
	
	public static MappedFileMemoryManager manager = new MappedFileMemoryManager();
	
	public static interface TempCreator {
		public File createTempFile(String prefix, String suffix) throws IOException;
	}
	
	public static TempCreator tmpCreator = new TempCreator() {
		public File createTempFile(String prefix, String suffix) throws IOException {
			return File.createTempFile(prefix, suffix);
		}
	};
	
	protected MappedFileMemoryManager() {
		super(MAX_BUFFERS);
	}

	@Override
	public int getConstantSegmentSize() {
		return BUFFER_SIZE;
	}

	private static class MappedFileMemorySegment extends AbstractSegment {
		File f;
		RandomAccessFile fp;
		MappedByteBuffer bb;

		public MappedFileMemorySegment(int offset, int siz) throws IOException {
			super(offset, siz);
			//f = File.createTempFile("sbs", null);
			f = tmpCreator.createTempFile("sbs", null);
			f.deleteOnExit();
			fp = new RandomAccessFile(f, "rw");
			bb = fp.getChannel().map(MapMode.READ_WRITE, 0, siz);
		}

		public int get(int index) {
			return bb.get(index-getOffset());
		}

		public void set(int index, int v) {
			bb.put(index - getOffset(), (byte) (v & 0xff));
		}
		
		public void read(int offset, byte[] buf, int bufOffset, int len) {
			bb.position(offset - getOffset());
			bb.get(buf, bufOffset, len);
		}

		public void write(int offset, byte[] buf, int bufOffset, int len) {
			bb.position(offset - getOffset());
			bb.put(buf, bufOffset, len);
		}
		
	}
	
	public Segment requestNewSegment(int offset, int size) {
		try {
			return new MappedFileMemorySegment(offset, BUFFER_SIZE);
		} catch (IOException e) {
			return null;
		}
	}

	public static void fill(MemChunk[] chunks, int siz) {
		boolean hasMoreMemory = true;
		int cnt = 0;
		while(hasMoreMemory) {
			MemChunk chunk = MappedFileMemoryManager.manager.alloc(siz);
			if (chunk == null) {
				System.err.println("Alloc failed.");
				hasMoreMemory = false;
			} else {
				if (cnt < chunks.length)
					chunks[cnt] = chunk;
				cnt++;
				System.err.println("Alloc #" + cnt + " succeeded.");
			}
		}		
	}
	public static void main(String[] argv) throws Exception {
		MemChunk[] chunks = new MemChunk[4096];
		fill(chunks, 250 * 1000);
		chunks[7].free();
		chunks[12].free();
		chunks[13].free();
		fill(chunks, 250 * 1000 * 2);
		fill(chunks, 250 * 1000);

	}


}

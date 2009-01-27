package com.unwiredappeal.virtmem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class SegmentedMemoryManager extends AbstractMemoryManager {

	int nextOffset = 0;
	int maxSegments;
	
	List<Segment> segments = new ArrayList<Segment>();
	public static interface Segment {
			public int getSize();
			public int getOffset();
			void set(int offset, int val);
			int get(int offset);
			void write(int offset, byte[] buf, int bufOffset, int len);
			void write(int offset, byte[] buf);
			void read(int offset, byte[] buf, int bufOffset, int len);
			void read(int offset, byte[] buf);
	}
	public static abstract class AbstractSegment implements Segment {
		int segOffset;
		int size;
		public AbstractSegment(int offset, int size) {
			this.size = size;
			this.segOffset = offset;
		}
		public AbstractSegment(int size) {
			this(size, 0);
		}
		public int getOffset() {
			return segOffset;
		}
		public void setOffset(int offset) {
			this.segOffset = offset;
		}
		public int getSize() {
			return size;
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
	
	public int getConstantSegmentSize() {
		return 0;
	}
	public class SegmentMemChunk extends AbstractMemChunk {

		protected SegmentMemChunk(AbstractMemoryManager manager, MemoryArea memArea, int size) {
			super(manager, memArea, size);
		}

		private Segment findSegment(int offset) {
			int segSize = getConstantSegmentSize();
			if (segSize > 0) {
				int p = offset / segSize;
				if (p < segments.size())
					return segments.get(p);
				else
					return null;
			}
			Iterator<Segment> it = segments.iterator();
			while(it.hasNext()) {
				Segment seg = it.next();
				if (offset >= seg.getOffset() && offset < (seg.getOffset()+seg.getSize()))
					return seg;
			}
			return null;
		}

		public int get(int offset) {
			offset += this.memArea.getStartPos();
			Segment seg = findSegment(offset);
			if (seg == null)
				return -1;
			else
				return seg.get(offset);
		}

		public void set(int offset, int val) {
			offset += this.memArea.getStartPos();

			Segment seg = findSegment(offset);
			if (seg == null)
				return;
			else
				seg.set(offset, val);
		}

		public void write(int offset, byte[] buf, int bufOffset, int len) {
			offset += this.memArea.getStartPos();

			int p = bufOffset;
			while(len > 0) {
				Segment seg = findSegment(offset);
				int segLeft = (seg.getOffset() + seg.getSize()) - offset;
				if (seg == null)
					return;
				int wl = Math.min(len, segLeft);
				seg.write(offset, buf, p, wl);
				len -= wl;
				offset += wl;
				p += wl;
			}
		}
		@Override
		public void read(int offset, byte[] buf, int bufOffset, int len) {
			offset += this.memArea.getStartPos();

			int p = bufOffset;
			while(len > 0) {
				Segment seg = findSegment(offset);
				int segLeft = (seg.getOffset() + seg.getSize()) - offset;
				if (seg == null)
					return;
				int wl = Math.min(len, segLeft);
				seg.read(offset, buf, p, wl);
				len -= wl;
				offset += wl;
				p += wl;
			}
			
		}
		
	}
	protected SegmentedMemoryManager(int maxSegments) {
		this.maxSegments = maxSegments;
	}
	protected SegmentedMemoryManager() {this.maxSegments = -1; } 

	@Override
	public MemChunk createMemChunk(MemoryArea area) {
		return new SegmentMemChunk(this, area, area.getSize());
	}

	@Override
	public MemoryArea requestMoreMemory(int size) {
		if (segments.size() == maxSegments)
			return null;
		Segment seg = requestNewSegment(nextOffset, size);
		if (seg == null)
			return null;
		else {
			MemoryArea memArea = new MemoryArea(nextOffset, seg.getSize());
			nextOffset += seg.getSize();
			segments.add(seg);
			return memArea;
		}
		
	}
	
	public abstract Segment requestNewSegment(int offset, int size);

}

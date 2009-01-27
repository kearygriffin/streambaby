package com.unwiredappeal.mediastreams.mpeg;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import com.unwiredappeal.tivo.utils.RandomAccessFileInputStream;
import com.unwiredappeal.tivo.utils.RandomAccessInputStream;

public class StreamableMpeg extends FilterInputStream {
	public int height;
	public int width;
	public float aspect;
	public float frameRate;
	boolean hasStartEndPos;
	private long subDuration;
	boolean pictureInfoSet = false;
	private RandomAccessInputStream ris;
	
	public static Logger logger;
	public static class MpegPosInfo implements Comparable<MpegPosInfo> {
		public MpegPosInfo(float timestamp, long filePos) {
			this.timestamp = timestamp;
			this.filePos = filePos;
		}
		public float	timestamp;	// seconds
		public long 	filePos;	// filePos
		public String toString() {
			return "Timestamp: " + timestamp + ", filePos: " + filePos;
		}

		public int compareTo(MpegPosInfo info) {
			return Float.compare(timestamp, info.timestamp);
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof MpegPosInfo))
				return false;
			
			return this.filePos == ((MpegPosInfo)o).timestamp;
		}
	}
	
	private TreeSet<MpegPosInfo> cachedPositions =  new TreeSet<MpegPosInfo>();
	
	private MpegPosInfo mpegStartPos;
	private MpegPosInfo mpegLastPos;

	public static float closeEnough = 0.4f; // if we seek within this position, we are considered good enough
	//private float startPosSecs;
	private int maxSearchBytes = 32768;
	float MAX_SEARCH_PERCENT = 0.05f;
	
	public static int FIND_END_CHUNK_SIZ = 1024;
	public StreamableMpeg(File file, long pos) throws IOException {
		this(file, pos, -1);
	}

	public StreamableMpeg(File f, long startPosition, long duration) throws IOException {
		this(new RandomAccessFileInputStream(f), startPosition, duration);
	}
	public StreamableMpeg(RandomAccessInputStream iris, long startPosition, long duration) throws IOException {
		super(iris);
		ris = iris;
		//startPosSecs = startPosition / 1000.0f;
		maxSearchBytes = (int)(ris.length() * MAX_SEARCH_PERCENT);
		//seek(0L);
		//setPictureInfo();
		findStartEnd();
		long newDuration = calcDuration(mpegStartPos, mpegLastPos);
		if (newDuration >= 0)
			duration = newDuration;
		if (startPosition == 0) {
			this.ris.seek(0);
			subDuration = duration;
			return;
		}	
		
		MpegPosInfo seekPos = binarySeek(startPosition);
		long fileSeekPos;
		if (seekPos == null) { // If we are very confused, just assume CBR and guess
			if (duration <= 0) {
				log("MPEG Seek error-- No duration set so can't approx.  Seeking to beginning.");
				// don't even know the subduration....

				subDuration = -1;
				fileSeekPos = 0;
			} else {
				log("MPEG Seek error, approximating assuming CBR");				
				long fileLength = ris.length();
				int bytesPerSecond = (int)(fileLength / ((duration / 1000.0)));
				fileSeekPos  = (long)((startPosition/1000.0) * bytesPerSecond);
				subDuration = duration - startPosition;
			}
		} else {
			log("Seeking for ts " + ((startPosition/1000.0)+mpegStartPos.timestamp) + " to timestamp: " + seekPos.timestamp + ", filePos: " + seekPos.filePos);
			subDuration = calcDuration(seekPos, mpegLastPos);
			fileSeekPos = seekPos.filePos;
		}
		ris.seek(fileSeekPos);
	}
	
	private long calcDuration(MpegPosInfo s, MpegPosInfo d) {
		if (s == null || d == null)
			return -1;
		if (s.timestamp > d.timestamp)
			return -1;
		return (long)((d.timestamp - s.timestamp) * 1000);
	}
	

	private void findStartEnd() throws IOException {
		if (hasStartEndPos)
			return;
		hasStartEndPos = true;
		RandomAccessBitInputStream mpegbs = new RandomAccessBitInputStream(ris, true);
		mpegStartPos = getStartPosition(mpegbs);
		if (mpegStartPos == null)
			return;
		mpegLastPos = getEndPosition(mpegbs);
		if (mpegLastPos == null)
			return;
		log("MpegStartTS:" + mpegStartPos);
		log("MpegEndTS:" + mpegLastPos);		
	}
	public  MpegPosInfo binarySeek(long startPosition) throws IOException {
		findStartEnd();
		if (mpegStartPos == null || mpegLastPos == null)
			return null;
		RandomAccessBitInputStream mpegbs = new RandomAccessBitInputStream(this.ris, true);
		if (mpegStartPos.timestamp >= mpegLastPos.timestamp)
			return null;

		float startPosSecs = startPosition / 1000.0f;;
		float findPosSecs = startPosSecs + mpegStartPos.timestamp;
		MpegPosInfo closestPos = mpegStartPos;
		MpegPosInfo low = mpegStartPos;
		MpegPosInfo high = mpegLastPos;
		// See if we can do better
		MpegPosInfo searchPosInfo = new MpegPosInfo(findPosSecs, 0);
		if (cachedPositions != null) {
			SortedSet<MpegPosInfo> s = cachedPositions.tailSet(searchPosInfo);
		
			if (s != null && !s.isEmpty())
				high = s.first();
			s = cachedPositions.headSet(searchPosInfo);
			if (s != null && !s.isEmpty())
				low = s.last();
		}
		//int cnt = 0;
		boolean forceBinary = false;
		while(closestPos.timestamp > findPosSecs || (findPosSecs - closestPos.timestamp > closeEnough)) {

			//System.err.println("Binary search (" + cnt++ + " between: " + low.timestamp + ":" + high.timestamp);
			// get the middle, we will use this as a default if we can't do better
			long middleNewPos = (low.filePos + high.filePos) / 2;
			long newPos = middleNewPos;
			boolean usedBinary = true;
			if (!forceBinary) {
				// so we haev the middle. but let's try to do better
				// by assuming CBR between the two points and seeking based on that....
				long lowToHighDur = calcDuration(low, high);
				// Shouldn't happen
				if (lowToHighDur < 0)
					return closestPos;
				// Try to guess where we should be
				int bytesPerSecond = (int)((high.filePos-low.filePos) / ((lowToHighDur / 1000.0)));
				//System.err.println("  local bps: " + bytesPerSecond);
				float guessPosSecs = findPosSecs - (closeEnough/2);
				if (guessPosSecs > 0) {
					long guessNewPos = low.filePos + (long)((guessPosSecs - low.timestamp) * bytesPerSecond);
					//System.err.println("  guess newpos: " + guessNewPos);
					if (guessNewPos > low.filePos && guessNewPos < high.filePos) {
						usedBinary = false;
						newPos = guessNewPos;
					}
				}
			}
			//System.err.println("  Using newpos: " + newPos);
			mpegbs.seek(newPos);
			MpegPosInfo pinfo = findNextPts(mpegbs, maxSearchBytes);
			if (pinfo == null)
				return closestPos;
			if (pinfo.filePos == high.filePos && usedBinary)
				return closestPos;
			forceBinary = false;
			if (pinfo.timestamp < findPosSecs && (findPosSecs - pinfo.timestamp) < (findPosSecs - closestPos.timestamp)) {
				closestPos = pinfo;
			}
			if (findPosSecs > pinfo.timestamp) {
				// look higher
				if (low.filePos == pinfo.filePos)
					forceBinary = true;
				low = pinfo;
			} else {
				if (high.filePos == pinfo.filePos)
					forceBinary = true;				
				high = pinfo;
			}
			if (low.timestamp == high.timestamp || low.timestamp > high.timestamp || low.filePos >= high.filePos)
				return closestPos;
		}
		
		return closestPos;
	}

	private MpegPosInfo getStartPosition(RandomAccessBitInputStream bs) throws IOException {
		bs.seek(0);
		MpegPosInfo pinfo = findNextPts(bs, maxSearchBytes);
		return pinfo;
	}
	
	private MpegPosInfo getEndPosition(RandomAccessBitInputStream bs) throws IOException {
		int search_size = FIND_END_CHUNK_SIZ;
		long fileOffset = ris.length() - search_size;
		if (fileOffset < 0)
			fileOffset = 0;
		MpegPosInfo pinfo = null;
		int total = 0;
		while(pinfo == null && fileOffset > 0 && total < maxSearchBytes) {
			bs.seek(fileOffset);
			pinfo = findNextPts(bs, search_size);
			if (pinfo != null) {
				MpegPosInfo lastPinfo = pinfo;
				while(pinfo != null) {
					bs.seek(pinfo.filePos+4);
					pinfo = findNextPts(bs, (int)(search_size-(pinfo.filePos-fileOffset)));
					if (pinfo != null)
						lastPinfo = pinfo;
				}
				return lastPinfo;
			}
			total += search_size;
			fileOffset -= 1024;
			search_size = FIND_END_CHUNK_SIZ + 3; // 3 bytes higher than before, because we need to reinclude the first 3 bytes (mark is on boundarY)
		}
		return pinfo;
	}


	public static class SimpBackByteBuffer {
		int[] q;
		int pos;
		int maxSize;
		public SimpBackByteBuffer(int maxSize) {
			this.maxSize = maxSize;
			q = new int[maxSize];
			for (int i=0;i<maxSize;i++)
				q[i] = -1;
			pos = 0;
		}
		
		void add(int val) {
			q[pos++] = val;
			if (pos >= maxSize)
				pos = 0;
		}
		int get(int p) {
			int qpos = pos + p;
			if (qpos < 0)
				qpos += maxSize;
			return q[qpos];
		}
	}
	
	public void setPictureInfo() {
		if (pictureInfoSet)
			return;
		pictureInfoSet = true;
		int searchSize = maxSearchBytes;
		SimpBackByteBuffer back = new SimpBackByteBuffer(3);
		int pos = 0;
		//long filePos = bs.getFilePointer();
		try {
			this.ris.seek(0L);
			RandomAccessBitInputStream bs = new RandomAccessBitInputStream(this.ris, true);

			while(pos < searchSize) {
					int b = bs.readByte();
					pos++;
					if (b == 0xB3) {
						if (back.get(-1) == 0x01 && back.get(-2) == 0 && back.get(-3) == 0) {
							int mwidth = readbits(bs, 12);
							int mheight = readbits(bs, 12);
							int maspect = readbits(bs, 4);
							int mframerate = readbits(bs, 4);
							this.width = mwidth;
							this.height = mheight;
							switch(maspect) {
							case 1:
								this.aspect = 0;
								break;
							case 2:
								this.aspect = 4/3.0f;
								break;
							case 3:
								this.aspect = 16/9.0f;
								break;
							}
							switch(mframerate) {
							case 1:
								frameRate = 23.976f;
								break;
							case 2:
								frameRate = 24;
								break;
							case 3:
								frameRate = 25;
								break;
							case 4:
								frameRate = 29.97f;
								break;
							case 5:
								frameRate = 30;
								break;
							case 6:
								frameRate = 50;
								break;
							case 7:
								frameRate = 59.94f;
								break;
							case 8:
								frameRate = 60;
								break;
							}
							return;
						}
					}
					back.add(b);
			}
		} catch (IOException e) {
		}			
	}
	/*
	private MpegPosInfo findNextScr(RandomAccessBitInputStream bs, int searchSize) throws IOException {
		SimpBackByteBuffer back = new SimpBackByteBuffer(3);
		int pos = 0;
		long filePos = bs.getFilePointer();
		try {
			while(pos < searchSize) {
					int b = bs.readByte();
					pos++;
					if (b == 0XBA) {
						if (back.get(-1) == 0x01 && back.get(-2) == 0 && back.get(-3) == 0) {
							// found it???....
							bs.mark(16);
							if (bs.readBit() != 0 || bs.readBit() != 1 ) {
								bs.reset();
								continue;
							}
							long sysClock = 0;
							for (int i=32;i>=30;i--) {
								sysClock += (bs.readBit() << i);
							}
							if (bs.readBit() != 1) {
								bs.reset();
								continue;
							}
							for (int i=29;i>=15;i--) {
								sysClock += (bs.readBit() << i);
							}
							if (bs.readBit() != 1) {
								bs.reset();
								continue;
							}
							for (int i=14;i>=0;i--) {
								sysClock += (bs.readBit() << i);
							}
							if (bs.readBit() != 1) {
								bs.reset();
								continue;
							}
							// Got the sysclock....
							float timestamp = sysClock / 90000.0f;
							MpegPosInfo pi = new MpegPosInfo(timestamp, filePos+(pos-4));
							cachedPositions.add(pi);
							return pi;
						}
					}
					back.add(b);
			}
		} catch (IOException e) {
			return null;
		}			
		return null;
	}
	*/
	private MpegPosInfo findNextPts(RandomAccessBitInputStream bs, int searchSize) throws IOException {
		SimpBackByteBuffer back = new SimpBackByteBuffer(3);
		int pos = 0;
		long filePos = bs.getFilePointer();
		try {
			while(pos < searchSize) {
					int b = bs.readByte();
					pos++;
					if (b >= 0xe0 && b <= 0xef) {
						if (back.get(-1) == 0x01 && back.get(-2) == 0 && back.get(-3) == 0) {
							// found it???....
							bs.mark(16);
							bs.readByte();
							bs.readByte();
							if (bs.readBit() != 1 || bs.readBit() != 0 ) {
								bs.reset();
								continue;
							}
							readbits(bs, 6);
							int flags = readbits(bs, 2);
							if (flags == 0) {
								bs.reset();
								continue;
							}
								
							readbits(bs, 6);
							bs.readByte();
							readbits(bs, 4);
							long sysClock = 0;
							for (int i=32;i>=30;i--) {
								sysClock += (bs.readBit() << i);
							}
							if (bs.readBit() != 1) {
								bs.reset();
								continue;
							}
							for (int i=29;i>=15;i--) {
								sysClock += (bs.readBit() << i);
							}
							if (bs.readBit() != 1) {
								bs.reset();
								continue;
							}
							for (int i=14;i>=0;i--) {
								sysClock += (bs.readBit() << i);
							}
							if (bs.readBit() != 1) {
								bs.reset();
								continue;
							}
							// Got the sysclock....
							float timestamp = sysClock / 90000.0f;
							MpegPosInfo pi = new MpegPosInfo(timestamp, filePos+(pos-4));
							if (cachedPositions != null && !cachedPositions.contains(pi)) {
								SortedSet<MpegPosInfo> s = cachedPositions.headSet(pi);
								
								float delta = 999999f;
								if (s != null && !s.isEmpty()) {
									MpegPosInfo p = s.last();
									delta = pi.timestamp-p.timestamp; 
								}
								s = cachedPositions.tailSet(pi);
								if (s != null && !s.isEmpty()) {
									MpegPosInfo p = s.first();
									float dd = p.timestamp-pi.timestamp;
									if (dd < delta)
											delta = dd;
								}
								if (delta > 1.0f)
									cachedPositions.add(pi);
							}
							return pi;
						}
					}
					back.add(b);
			}
		} catch (IOException e) {
			return null;
		}			
		return null;
	}
	
	int readbits(RandomAccessBitInputStream bs, int bits) throws IOException {
		int ret = 0;
		for (int i=bits-1;i>=0;i--) {
			ret += (bs.readBit() << i);
		}
		return ret;

	}
	public long getSubDuration() {
		return subDuration;
	}
		
	public static void log(String s) {
		if (logger != null)
			logger.log(s);
		else
			System.err.println(s);
	}
	public static interface Logger {
		public void log(String str);
	}	

}

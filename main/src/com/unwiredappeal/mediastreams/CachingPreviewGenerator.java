package com.unwiredappeal.mediastreams;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.unwiredappeal.virtmem.MappedFileMemoryManager;
import com.unwiredappeal.virtmem.MemChunk;

public class CachingPreviewGenerator implements PreviewGenerator {

	private PreviewGenerator gen;
	VideoInformation vidinfo;
	public CachingThread cacheThread = null;
	int frameCnt;
	static class ImageIndex {
		public ImageIndex(MemChunk chunk, int len) {
			this.chunk = chunk;
			this.len = len;
		}
		MemChunk chunk;
		int len;
	}
	protected Map<Integer, ImageIndex> imageMap = Collections.synchronizedMap(new TreeMap<Integer, ImageIndex>());
	public CachingPreviewGenerator(PreviewGenerator gen, VideoInformation vidinfo, boolean threaded, boolean predictive) {
		frameCnt = (int)(vidinfo.getDuration()/1000)+1;
		this.gen = gen;
		this.vidinfo = vidinfo;
		if (threaded) {
			cacheThread = new CachingThread(predictive);
			cacheThread.start();
		}
	}

	public synchronized void close() {
		if (cacheThread != null) {
			cacheThread.shutdown();
			try {
				cacheThread.join();
			} catch (InterruptedException e) {
			}
			cacheThread = null;
		}

		gen.close();
		Iterator<ImageIndex> it = imageMap.values().iterator();
		while(it.hasNext()) {
			ImageIndex idx = it.next();
			if (idx != null) {
				if (idx.chunk != null)
					idx.chunk.free();
			}
		}
		imageMap.clear();
	}

	public synchronized byte[] getFrameImageData(int secs) {
		return getFrameImageData(secs, false);
	}

	public synchronized void putChunk(int secs, byte[] b) {
		Integer secInt = new Integer(secs);
		MemChunk chunk = MappedFileMemoryManager.manager.alloc(b.length);
		chunk.write(0, b);
		ImageIndex idx = new ImageIndex(chunk, b.length);
		imageMap.put(secInt, idx);
	}
	public synchronized byte[] getFrameImageData(int secs, boolean idxOnly) {
		Integer secInt = new Integer(secs);
		ImageIndex idx = imageMap.get(secInt);
		if (idx == null) {
			if (idxOnly)
				return null;
			byte[] b = gen.getFrameImageData(secs);
			if (b != null) {
				putChunk(secs, b);
			} else {
				idx = new ImageIndex(null, 0);
				imageMap.put(secInt, idx);
			}
			return b;
		} else {
			if (idx.len == 0)
				return null;
			byte[] b= new byte[idx.len];
			idx.chunk.read(0, b);
			return b;
		}
	}

	
	public boolean isRealtime() {
		return gen.isRealtime();
	}

	public boolean open(URI uri, VideoInformation vinfo, int sw, int sh) {
		return gen.open(uri, vinfo, sw, sh);
	}
	public void prepare(int secs, int delta) {
		if (cacheThread != null)
			cacheThread.setNextFrame(secs, delta);
	}
	
	public class CachingThread extends Thread {

		private int nextFrame = -1;
		private boolean done = false;
		private int delta = 0;
		private int lastFrame = -1;
		boolean predictive;
		public CachingThread(boolean predictive) {
			this.predictive = predictive;
		}
		@Override
		public void run() {
			while(!isDone()) {
				int frame = getNextFrame();
				if (frame == -1) {
					if (predictive && lastFrame != -1 && delta != 0) {
						predictFrame();
						continue;
					}
					synchronized(this) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							return;
						}
					}
				} else {
					generateNextFrame();
				}
			}
		}

		public synchronized void predictFrame() {
			if (getNextFrame() != -1 || lastFrame == -1)
				return;
			long nextPos = (lastFrame * 1000L) + (delta*1000);
			if (nextPos >= 0  && nextPos < vidinfo.getDuration()) {
				int predict = (int)(nextPos/1000);
				//Log.debug("Predicting: " + predict);
				this.nextFrame = predict;
			}
		}
		public void generateNextFrame() {
			int f;
			synchronized(this) {
				f = getNextFrame();
				if (f != -1) {
					nextFrame = -1;
					lastFrame = f;
				}
			}
			if (f != -1) {
				getFrameImageData(f);				
			}
		}
		public synchronized void shutdown() {
			nextFrame = -1;
			done = true;
			this.notify();
		}
		
		public synchronized boolean isDone() {
			return done;
		}
		
		public synchronized void setNextFrame(int f, int delta) {
			//Log.debug("Setting next frame: " + f);
			nextFrame = f;
			this.delta = delta;
			this.notify();
		}

		public synchronized void setNextFrame(int f) {
			setNextFrame(f, delta);
		}

		public synchronized int getNextFrame() {
			return nextFrame;
		}
		
	}

}

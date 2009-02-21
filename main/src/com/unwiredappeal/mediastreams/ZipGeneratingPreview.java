package com.unwiredappeal.mediastreams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.utils.Log;

public class ZipGeneratingPreview extends CachingPreviewGenerator  {

	public static class GeneratorException extends Exception {

		public GeneratorException(String string) {
			super(string);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	private int instanceCount = 1;
	URI uri;
	GenThread gt = null;
	public ZipGeneratingPreview(URI uri, PreviewGenerator gen, VideoInformation vidinfo, int sw, int sh) throws GeneratorException {
		super(gen, vidinfo, false, false);
		this.uri = uri;
		boolean opened = this.open(uri, vidinfo, sw, sh);
		if (!opened)
			throw new GeneratorException("Can't open generator");
		gt = new GenThread();
		gt.setPriority(Thread.NORM_PRIORITY-2);
		if (!gt.begin(new File(uri), vidinfo)) {
			throw new GeneratorException("Failed to launch pvwZip thread");
		}
		gt.start();
	}
	
	public boolean isRunning() {
		if (gt == null)
			return false;
		return gt.isAlive();
	}

	@Override
	public void close() {
		boolean doClose = false;
		synchronized(AutoPreviewGenerationManager.inst) {
			instanceCount--;
			if (instanceCount == 0) {
				AutoPreviewGenerationManager.inst.offer(this);
				if (instanceCount == 0) {
					AutoPreviewGenerationManager.inst.remove(uri);
					doClose = true;
				}
			}
		}
		if (doClose) {
			forceClose();
		}
	}
	
	public void forceClose() {
		if (gt != null) {
			gt.shutdown();
			try {
				gt.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.close();
	}

	@Override
	public  byte[] getFrameImageData(int secs) {
		Integer secInt = new Integer(secs);
		ImageIndex idx = imageMap.get(secInt);
		if (idx == null)
			return null;
		if (idx.len == 0)
			return null;
		byte[] b= new byte[idx.len];
		idx.chunk.read(0, b);
		return b;
	}

	@Override
	public boolean isRealtime() {
		return true;
	}


	@Override
	public void prepare(int secs, int delta) {
		
	}

	public void addInstance() {
		synchronized(AutoPreviewGenerationManager.inst) {
			instanceCount++;
		}
	}

	public  class GenThread extends Thread {
		File f;
		long len;
		OutputStream os = null;
		ZipOutputStream zos = null;
		File pvwFile ;
		boolean shutdownFlag = false;
		
		public synchronized boolean getShutdown() {
			return shutdownFlag;
		}
		public synchronized void shutdown() {
			shutdownFlag = true;
		}
		@Override
		public void run() {
			try {
				int start = 0;
				File partial = ZipPreviewer.getCacheFile(f, ".partial");
				if (partial.exists()) {
					Log.debug("Using partial pvw: " + partial.getAbsolutePath());
					byte[] b = new byte[4096];
					ByteArrayOutputStream bs = new ByteArrayOutputStream();
					ZipFile zis = null;
					InputStream is = null;
					try {
						zis = new ZipFile(partial);
						Enumeration<? extends ZipEntry> enm = zis.entries();
						while(enm.hasMoreElements()) {
							ZipEntry  e = enm.nextElement();
							is = zis.getInputStream(e);
							bs.reset();
							int read;
							while ((read = is.read(b)) != -1) {
								bs.write(b, 0, read);
							}				
							byte[] ar = bs.toByteArray();
							int pos = Integer.parseInt(e.getName().substring(4, 9));
							if ((pos+1) > start) {
								start = pos+1;
							}
							putChunk(pos, ar);
							zos.putNextEntry(e);							
							zos.write(ar);
							zos.closeEntry();
							is.close();
							is  = null;
						}
						Log.debug("partial pvw copy completed: " + partial.getAbsolutePath());

					} catch(IOException e) {
					} finally {
						if (zis != null)
							zis.close();
						if (is != null)
							is.close();
					}
					partial.delete();
					Log.debug("partial pvw deleted: " + partial.getAbsolutePath());

				}
				for (int i=start;i<len;i++) {
					if (getShutdown())  {
						zos.close();
						os.close();
						Log.debug("Saving partial preview: " + partial);
						partial.delete();
						pvwFile.renameTo(partial);
						return;
					}
						
					byte[] b = getFrameImageData(i, false);
					if (b == null) {
						if (getShutdown())
							continue;
						if ((len-i) > 10) {
							Log.error("Generation Error creating zip pvw: " + pvwFile.getAbsolutePath());
							return;
						}
						continue;
					}
					zos.putNextEntry(new ZipEntry(ZipPreviewer.getEntryName(i)));
					zos.write(b);
					zos.closeEntry();
				}
			} catch(IOException e) {
				Log.error("IOError creating zip file: " + pvwFile.getAbsolutePath());
			}
			try {
				zos.close();
				os.close();				
			} catch(IOException e) { }
			File dstFile = ZipPreviewer.getCacheFile(f, "");
			pvwFile.renameTo(dstFile);
			Log.debug("Finished generating preview: " + dstFile.getAbsolutePath());
		}
		public boolean begin(File f, VideoInformation vi) {
			this.f = f;
			if (!f.exists() || !f.isFile()) {
				return false;
			}
			File cacheDirFile = new File(StreamBabyConfig.cacheDir);
			cacheDirFile.mkdir();
			pvwFile = ZipPreviewer.getCacheFile(f, ".tmp");
			try {
				pvwFile.createNewFile();
				os = new FileOutputStream(pvwFile);
				zos = new ZipOutputStream(os);
				len = vi.getDuration()/1000;
			} catch (IOException e) {
				if (zos != null)
					try {
						zos.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				if (os != null)
					try {
						os.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				pvwFile.delete();
				return false;
			}
			return true;
	
		}
	
	}

}

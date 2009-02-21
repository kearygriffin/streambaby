package com.unwiredappeal.tivo.streambaby;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.unwiredappeal.mediastreams.ZipPreviewer;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.Utils;

public class PreviewCacheUtils {

	private static int MAX_LEVEL = 100;
	
	public static boolean delTemp = false;
	public static final Boolean bt = new Boolean(true);
	public static Set<String> removeSet;
	public static Set<String> alreadyRecursed;
	public static void markPvwUsed(DirEntry de) {
		URI uri = de.getUri();
		File f= new File(uri);
		if (f.isFile() && f.exists()) {
			removeSet.remove(ZipPreviewer.getCacheFile(f, "").getName());
			removeSet.remove(ZipPreviewer.getCacheFile(f, ".partial").getName());
			if (!delTemp)
				removeSet.remove(ZipPreviewer.getCacheFile(f, ".tmp").getName());			
			removeSet.remove(f.getName() + ".pvw");

		}
	}
	public static interface UriCallback {
		public void callback(DirEntry de);
	}
	public synchronized static void recursiveFill(DirEntry de, int level, UriCallback callback) {
		if (level > MAX_LEVEL)
			return;
		if (level == 0) {
			alreadyRecursed = new HashSet<String>();
		}
		// Handle symlinks.  If we have already dealt with this directory, don't do it again!
		if (de.isFolder && de.getUri() != null && Utils.isFileScheme(de.getUri())) {
			try {
				String can = new File(de.getUri()).getCanonicalPath();
				if (alreadyRecursed.contains(can))
					return;
				alreadyRecursed.add(can);
			} catch (IOException e) {
				return;
			}
			return;
		}
		List<DirEntry> entriesList;
		entriesList = de.getEntryList("", true);
		Iterator<DirEntry> it = entriesList.iterator();
		while(it.hasNext()) {
		  DirEntry thisEntry = it.next();
		  if (thisEntry.isFolder) {
			  URI dir = thisEntry.getUri();
			  File newDir = new File(dir);
			  if (newDir.exists()) {
				  FileFilter filter = new FileFilter() {
				     public boolean accept(File f) {
				    	 /*
				        if (f.isDirectory()) {
					           return true;
					        }
				    	 // Video file
				    	 if (StreamBabyConfig.inst.extList.length == 0)
				    		return true;
				        for (int i=0; i<StreamBabyConfig.inst.extList.length; i++) {
				           if (f.getName().toLowerCase().endsWith("." + StreamBabyConfig.inst.extList[i].toLowerCase())) {
				              return true;
				           }
				        }
				        return false;
				        */
				    	 if (f.getName().startsWith("."))
				    		 return false;

				    	 return true;
				     }
				  };
			      File[] files = newDir.listFiles(filter);
			      if (files != null) {
					      for (int i=0;i<files.length;i++) {
					    	  File f = files[i];
					    	  DirEntry newDirEntry = new DirEntry(f.toURI());
					    	  if (newDirEntry.isFolder) {
						    	  thisEntry.addEntry(newDirEntry);
					    	  }
					    	  else
					    		  callback.callback(newDirEntry);
		
					      }
			      }
			      recursiveFill(thisEntry, level+1, callback);
			  }
		  } else {
			  callback.callback(thisEntry);
		  }
		}
	}

	// Make this synchronized because it is still a pretty brain-dead implementation.  Should be non-static
	public static synchronized void cleanup(boolean delTemp) {
		PreviewCacheUtils.delTemp = delTemp;
		removeSet = new HashSet<String>();
		File[] files = new File(StreamBabyConfig.cacheDir).listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				boolean acc = name.endsWith(".pvw") || name.endsWith(".pvw.partial");
				if (PreviewCacheUtils.delTemp)
					acc = acc || name.endsWith(".pvw.tmp");
				return acc;
			}
		}
				
			);
		if (files != null) {
			for (File sf : files)
				removeSet.add(sf.getName());
		} else {
			// No files, nothing to do
			return;
		}
		DirEntry de = StreamBabyConfig.inst.buildRootDirEntry();
		recursiveFill(de, 0, new UriCallback() {

			public void callback(DirEntry de) {
				markPvwUsed(de);
			}
			
		}
				);
		Iterator<String> it = removeSet.iterator();
		while(it.hasNext()) {
			String k = it.next();
			File df = new File(StreamBabyConfig.cacheDir, k);
			df.delete();
		}
		Log.info("Cache Clean complete");
		
	}
	/*
	public static void generate(ArgumentList al) {
		String filename = al.shift();
		if (filename != null && filename.length() > 0) {
			File f = new File(filename);
			if (!f.exists() || !f.isFile()) {
				System.err.println("Error: " + f + " doesn't exist.");
			}
			URI uri = f.toURI();
			VideoInformation vi = new VideoInformation(uri);
			VideoModuleHelper.inst.fillVideoInformation(uri, vi);
			PreviewGenerator gen = VideoModuleHelper.inst.getPreviewHandler(uri, vi, false);
			if (gen == null || gen.open(uri, vi, PreviewWindow.small_PREVIEW_WIDTH, PreviewWindow.small_PREVIEW_HEIGHT) == false) {
				System.err.println("Error finding preview generator for: " + f);
			}
			File cacheDirFile = new File(StreamBabyConfig.cacheDir);
			cacheDirFile.mkdir();
			File pvwFile = ZipPreviewer.getCacheFile(f, ".tmp");
			OutputStream os = null;
			ZipOutputStream zos = null;
			int lastPercent = -100;
			try {
				pvwFile.createNewFile();
				os = new FileOutputStream(pvwFile);
				zos = new ZipOutputStream(os);
				long len = vi.getDuration()/1000;
				for (int i=0;i<len;i++) {
					byte[] b = gen.getFrameImageData(i);
					if (b == null && (len-i) > 10) {
						throw new IOException();
					}
					zos.putNextEntry(new ZipEntry(ZipPreviewer.getEntryName(i)));
					zos.write(b);
					zos.closeEntry();
					int percent = (int)(((float)i / len) * 100);
					if (percent >= 100 || percent - lastPercent >= 5) {
						System.err.println("Percent complete: " + percent + "%");
						lastPercent = percent;
					}
					//System.err.println("Frame #" + i + ": " + (b == null ? "null" : b.length));
				}
				zos.close();
				os.close();
				gen.close();
				pvwFile.renameTo(ZipPreviewer.getCacheFile(f, ""));
				System.err.println("Done.");
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
				System.err.println("Error generating preview file: " + pvwFile);
			}

		}
	}
	*/
}

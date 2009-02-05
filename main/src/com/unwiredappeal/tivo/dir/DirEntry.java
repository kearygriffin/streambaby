package com.unwiredappeal.tivo.dir;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.metadata.MetaData;
import com.unwiredappeal.tivo.modules.VideoModuleHelper;

public class DirEntry implements Comparable<DirEntry> {
	//public String userPassword;
	public boolean entriesLoaded = false;
	public boolean isFolder;
	public boolean isRoot = false;
	public String fileName;
	public String name = null;
	public DirEntry parent;
	public URI uri;
	//public long videoLength;
	public boolean isFile = true;
	public String mimeType;
	public List<String> passwords = new ArrayList<String>();
	private  VideoInformation vinfo = null;
	private boolean hasMeta = false;
	private boolean cachedMeta = false;
	private MetaData meta = new MetaData();
	public List<DirEntry> entryList = new ArrayList<DirEntry>();
	
	public DirEntry() { }
	
	public DirEntry(URI uri) {
		this();

		this.uri = uri;
		this.isFolder = false;
		this.fileName = uri.toString();
		if (Utils.isFile(uri)) {
			File f = new File(uri);
			this.isFolder = f.isDirectory() || !f.exists();
			this.fileName = f.getName();
		}
		//this.parent = parent;
	}
	
	public boolean isFolder() {
		return isFolder;
	}
	
	public boolean isFile() {
		return !isFolder() && isFile;
	}
	
	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}
	
	public String getStrippedFilename() {
		   String name = getFilename();
		  	  if (isFile && StreamBabyConfig.cfgTrimExtensions.getBool())
				  name = trimExtension(name);
			  return name;
	}
	
	public void setName(String n) {
		this.name = n;
	}
	
	public String getName() {
		if (name != null)
			return name;
		if (!StreamBabyConfig.cfgUseTitle.getBool())
			return getStrippedFilename();
		if (!cachedMeta) {
			MetaData m = new MetaData();
			getMetadata(m);
		}
		if (hasMeta && meta.hasTitle() && meta.getTitle().length() > 0) {
			return meta.getTitle();
		} else {
			return getStrippedFilename();
		}
	}
	
	public String getFilename() {
		return fileName;
	}
	public DirEntry getParent() {
		return parent;
	}
	public void setParent(DirEntry parent) {
		this.parent = parent;
	}
	public URI getUri() {
		return uri;
	}
	
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	public List<DirEntry> getEntryList(String pw) {
		return getEntryList(pw, false);
	}
	
	public List<DirEntry> getEntryList(String pw, boolean ignorepw) {
		if (!entriesLoaded) {
			loadEntries(pw, ignorepw);
		}
		List<DirEntry> newList = new ArrayList<DirEntry>();
		Iterator<DirEntry> it = entryList.iterator();
		while(it.hasNext()) {
			DirEntry de = it.next();
			if (ignorepw || de.passwords.isEmpty() || de.passwords.contains(pw))
				newList.add(de);
		}
		//Collections.sort(newList);
		return newList;
	}
	
	public void loadEntries(String pw, boolean ignorepw) {
		if (!entriesLoaded) {
			fillEntryList(ignorepw);
			entriesLoaded = true;
		}
	}
	
	private static String trimExtension(String s) {
		int p = s.lastIndexOf('.');
		if (p > 0) {
			return s.substring(0, p);
		}
		return s;
	}

	public void fillEntryList(boolean forceAll) {
		DirEntry thisEntry = this;
		  if (thisEntry.isFolder) {
			  URI dir = thisEntry.getUri();
			  if (dir == null || !Utils.isFile(dir))
				  return;
			  File newDir = new File(dir);
		    	//Log.debug("fillEntryList for: " + newDir.getAbsolutePath() + ", exists:" + newDir.exists() + ", files: " + newDir.list());
			  if (newDir.exists()) {
				  FileFilter filter = new FileFilter() {
				     public boolean accept(File f) {
				    	//Log.debug("Accept?: " + f.getAbsolutePath());
				    	 if (StreamBabyConfig.cfgIgnoreDotFiles.getBool() && f.getName().startsWith("."))
				    		 return false;
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
				     }
				  };
			      File[] files = newDir.listFiles(filter);
			      for (int i=0;i<files.length;i++) {
			    	  File f = files[i];
			    	  DirEntry newDirEntry = new DirEntry(f.toURI());
			    	  /*
			    	  if (newDirEntry.isFile && StreamBabyConfig.cfgTrimExtensions.getBool())
			    		  newDirEntry.setName(trimExtension(newDirEntry.getName()));
			    	  */
			    	  if (true || forceAll || newDirEntry.isFolder || newDirEntry.isStreamableVideo()) {
				    	  thisEntry.addEntry(newDirEntry);
			    	  }

			      }
			  }
		    }
		
	}
	/*
	public void recursiveFill(int level, String pw, int maxLevel, boolean ignorepw) {
		DirEntry de = this;
		de.fillEntryList(ignorepw);
		if (level >= maxLevel)
			return;
		List<DirEntry> entriesList;
		entriesList = de.getEntryList(pw, ignorepw);
		Iterator<DirEntry> it = entriesList.iterator();
		while(it.hasNext()) {
			DirEntry thisEntry = it.next();
		    thisEntry.recursiveFill(level+1, pw, maxLevel, ignorepw);
		}
	}
	*/
	
	public void setEntryList(List<DirEntry> entryList) {
		this.entryList = entryList;
	}
	
	public void addEntry(DirEntry de) {
		if (this.entryList == null)
			this.entryList = new ArrayList<DirEntry>();
		de.setParent(this);
		entryList.add(de);
	}
	
	public boolean isStreamableVideo() {
		VideoInformation vinfo = getVideoInformation();
		if (!vinfo.isValid())
			return false;
		if (VideoModuleHelper.inst.canStreamOrTranscodeVideo(getUri(), vinfo))
			return true;
		return false;
	}
	
	public boolean getMetadata(MetaData meta) {
		if (cachedMeta) {
			this.meta.copy(meta);
			return hasMeta;
		}
		this.hasMeta = VideoModuleHelper.inst.setMetadata(meta, this, getVideoInformation());
		meta.copy(this.meta);
		cachedMeta = true;
		return hasMeta;
	}
	
	public VideoInformation getVideoInformation() {
		if (vinfo == null)
			vinfo = VideoInformation.getVideoInformation(uri);
		return vinfo;
	}

	/*
	public long getVideoLength() {
		VideoInformation info = getVideoInformation();
		if (vinfo != null)
				return  info.getVideoLength();
		return -1;
	}

	public void setVideoLength(long videoLength) {
		this.videoLength = videoLength;
	}
	*/
	
	public String toString() {
		return getName();
	}


	public void setPasswords(String pws) {
		passwords.clear();
		if (pws == null || pws.length() == 0)
			return;
		String[] pwArray = pws.split(",");
		for (int i=0;i<pwArray.length;i++) {
			String tpw = pwArray[i].trim();
			if (tpw.length() > 0)
				passwords.add(pwArray[i]);
		}
	}

	public int compareTo(DirEntry de) {
		if (de.isFolder() != isFolder()) {
			return de.isFolder() ? 1 : -1;
		}

		return this.toString().compareToIgnoreCase(de.toString());
		//return 0;
	}

	public boolean isRoot() {
		return isRoot;
	}
	
	public void setIsRoot(boolean b) {
		isRoot = b;
	}

	/*
	public void setUserPassword(String password) {
		userPassword = password;
	}
	*/

}

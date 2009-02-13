package com.unwiredappeal.tivo.dir;

import java.io.File;
import java.io.Serializable;
import java.net.URI;

import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.metadata.MetaData;
import com.unwiredappeal.tivo.streambaby.Cleanupable;
import com.unwiredappeal.tivo.streambaby.ShutdownHook;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.PersistentHashTable;
import com.unwiredappeal.tivo.utils.Utils;

public class TitleCacher implements Cleanupable {

	PersistentHashTable hash = null;
	public static class TitleEntry implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String title;
		public String filename;
		public String metaRefFilename;
		public long modDate;
		public TitleEntry(String filename, String title, String metaRefFilename, long modDate) {
			this.title = title;
			this.filename = filename;
			this.metaRefFilename = metaRefFilename;
			this.modDate = modDate;
		}
	}
	
	public static String cacheName = StreamBabyConfig.convertRelativePath("meta.cache", StreamBabyConfig.cacheDir);
	
	protected static TitleCacher inst;
	
	
	protected TitleCacher() { }

	public static TitleCacher getInstance() {
		if (inst != null)
			return inst;
		return allocInstance();
	}
	
	protected static synchronized TitleCacher allocInstance() {
		if (inst != null)
			return inst;
		
		File f = new File(cacheName);
		f.getParentFile().mkdirs();
		inst = new TitleCacher();
		try {
			inst.hash = PersistentHashTable.createInstance(f, false);
		} catch(InternalError e) {
			try {
				inst.hash = PersistentHashTable.createInstance(f, true);
			} catch(InternalError e1) {
				Log.error("Unable to open meta.cache file-- Caching disabled");
			}
		}
		ShutdownHook.getShutdownHook().addCleanupRequired(inst);		
		return inst;
	}
	public  void setCachedTitle(URI uri, MetaData m) {
		if (!Utils.isFile(uri))
			return;
		if (m.getTitle() == null || m.getTitle().length() == 0)
			return;
		File f = new File(uri);
		TitleEntry t = new TitleEntry(f.getAbsolutePath(), m.getTitle(), m.refFile, m.refModDate);
		store(t);
	}

	public  String getCachedTitle(URI uri) {
		if (!Utils.isFile(uri))
			return null;
		File f = new File(uri);
		TitleEntry t = get(f.getAbsolutePath());
		if (t != null) {
			if (t.modDate > 0 && t.metaRefFilename != null) {
				// Check and see if we are still ok.
				File rf = new File(t.metaRefFilename);
				if (rf.lastModified() > t.modDate)
					return null;
				else
					return t.title;
			} else 
				return t.title;
		}
		return null;
	}
	
	private void  store(TitleEntry t) {
		if (hash == null)
			return;
		hash.put(t.filename, t);
	}
	
	private TitleEntry get(String filename) {
		if (hash == null)
			return null;
		return (TitleEntry)hash.get(filename);
	}
	
	public void cleanup() {
		if (hash != null)
			hash.shutdown();
	}

}

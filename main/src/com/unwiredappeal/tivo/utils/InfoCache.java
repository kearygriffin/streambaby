package com.unwiredappeal.tivo.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.unwiredappeal.tivo.streambaby.GLOBAL;

public class InfoCache  {
	private static InfoCache cache = null;
	//private static Timer timer;
	private Map<String, Map<String, Cacheable>> allHashes = new HashMap<String, Map<String, Cacheable>>();
	public static InfoCache getInstance() {
		if (null == cache) {
			initCache();
		}
		return cache;
	}
	
	private synchronized static void initCache() {
		if (cache == null)
			cache = new InfoCache();
	}
	
	
	public synchronized void removeEntry(String hashName, String key) {
		Map<String, Cacheable> map = allHashes.get(hashName);
		if (map != null)
			map.remove(key);
		
	}
	
	public synchronized void putEntry(String hashName, String key, Cacheable val) {
		Map<String, Cacheable> map = allHashes.get(hashName);
		if (map == null) {
			map = new HashMap<String, Cacheable>();
			allHashes.put(hashName, map);
		}
		map.put(key, val);
	}
	
	public synchronized Cacheable getEntry(String hashName, String key) {
		Map<String, Cacheable> map = allHashes.get(hashName);
		if (map == null)
			return null;
		return map.get(key);
	}
	
	public synchronized void prune(String cacheName) {
		Map<String, Cacheable> cache = allHashes.get(cacheName);
		if (cache == null)
			return;
		Iterator<String> it = cache.keySet().iterator();
		while(it.hasNext()) {
			String k = it.next();
			Cacheable c = cache.get(k);
			if (c.isExpired()) {
				c.expire();
				it.remove();
			}
		}
	}
	
	public synchronized void pruneAll() {
		Set<String> caches = allHashes.keySet();
		Iterator<String> it = caches.iterator();
		while(it.hasNext()) {
			prune(it.next());
		}
	}
	
	
	public static class Cacheable {
		public Cacheable() { }
		public boolean isExpired() {
			return false;
		}
		public void expire() {
			
		}
	}

	public void schedule(Timer t) {
		t.schedule(new PruneTask(t), GLOBAL.CACHE_PRUNE_DELAY);
	}

	public class PruneTask extends TimerTask {
		public Timer timer;
		public PruneTask(Timer t) {
			timer = t;
		}
		@Override
		public void run() {
			Log.debug("Pruning cache...");
			pruneAll();		
			Log.debug("Pruning complete.");
			schedule(timer);
		}
		
	}
}

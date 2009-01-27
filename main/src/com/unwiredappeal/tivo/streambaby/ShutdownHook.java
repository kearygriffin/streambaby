package com.unwiredappeal.tivo.streambaby;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.unwiredappeal.tivo.utils.Log;

public class ShutdownHook extends Thread {

	protected static ShutdownHook inst;
	private List<Cleanupable> cleanupRequired = Collections.synchronizedList(new LinkedList<Cleanupable>());

	protected ShutdownHook() { }
	public static ShutdownHook getShutdownHook() {
		if (inst != null)
			return inst;
		else {
			return allocInstance();
		}
	}
	
	protected static synchronized ShutdownHook allocInstance() {
		if (inst != null)
			return inst;
		inst = new ShutdownHook();
		return inst;
	}
	public void run() {
		Log.info("Cleaning up...");
		cleanup();
	}
	
	public void addCleanupRequired(Cleanupable c) {
		synchronized(cleanupRequired) {
			if (!cleanupRequired.contains(c))
				cleanupRequired.add(c);
		}
	}
	
	public void removeCleanupRequire(Cleanupable c) {
		synchronized(cleanupRequired) {
			if (cleanupRequired.contains(c))
					cleanupRequired.remove(c);
		}
	}
	
	public void cleanup() {
		synchronized(cleanupRequired) {
			while(cleanupRequired.size() > 0) {
				Cleanupable c = cleanupRequired.get(cleanupRequired.size()-1);
				c.cleanup();
				cleanupRequired.remove(c);
			}
		}

	}

}

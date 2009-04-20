package com.unwiredappeal.tivo.push;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.push.pytivo.pyTivo;

public class Push {

	public static class NullPushHandler implements PushHandler {

		private static List<Tivo> tivos = new ArrayList<Tivo>();
		public boolean canPush(DirEntry de, int qual) {
			return false;
		}

		public List<Tivo> getTivos() {
			return tivos;
		}

		public boolean pushVideo(URL baseUri, DirEntry de, Tivo tivo, int qual) {
			return false;
		}
		
	}
	private static PushHandler nullInstance = new NullPushHandler();
	private static InternalPush intPush = InternalPush.getInstance();
	
	public static void pushSetup() {
		if (StreamBabyConfig.cfgPytivoIp.getValue().length() > 0)
			pyTivo.setup();
	}
	
	public static PushHandler getInstance() {
		if (intPush != null && intPush.getTivos().size() > 0)
			return intPush;
		
		if (pyTivo.py == null) {
			return nullInstance;
		}
		return pyTivo.py;
	}

}

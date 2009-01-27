package com.unwiredappeal.tivo.config;

import java.io.File;

import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.Log;

public class RootDirEntry extends ConfigurableObject {
	public DirEntry _de;
	public static ConfigEntry cfgDirs = new ConfigEntry(
			"dir",
			new dirEntryHandler(),
			"Configuration directories to scan.  Optional: dir.x.name=Sets dir name, dir.x.password=Sets password for dir"
			);	


	public RootDirEntry() {
		//super(new DirEntry(), true);
		//_de = (DirEntry)this.getInitObject();
		_de = new DirEntry();
		_de.setName("Top Level");
		_de.isFolder = true;
		//Log.debug("Populating root dir entries");
		populateConfig();
		if (_de.entryList.isEmpty()) {
			File f = new File(StreamBabyConfig.DEFAULT_VIDEO_DIR);
			if (f.exists() && f.isDirectory()) {
				DirEntry nde = new DirEntry(f.toURI());
				_de.addEntry(nde);
			}
		}

	}
	public DirEntry getDirEntry() {
		return _de;
	}
	
	private static class dirEntryHandler implements ConfigurationManager.propertyHandler {

		public dirEntryHandler() {
		}

		public void process(ConfigurableObject te, String key, String value) {
			if (value != null && value.length() > 2) {
				if (value.charAt(0) == '\"' && value.charAt(value.length()-1) == '\"') {
					value = value.substring(1, value.length()-1);
				}
			}
			DirEntry newde = new DirEntry(new File(value).toURI());
			Log.debug("dirEntry: " + newde.getUri());
			((RootDirEntry)te)._de.addEntry(newde);
			String name = ConfigurationManager.inst.getStringProperty(key + ".name", null);
			if (name != null) {
				newde.setName(name);
			}
			String pw = ConfigurationManager.inst.getStringProperty(key + ".password", null);
			if (pw != null) {
				newde.setPasswords(pw);
			} else {
				newde.setPasswords(StreamBabyConfig.cfgPassword.value);
			}
		}

	}
}

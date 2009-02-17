package com.unwiredappeal.tivo.dir;

import com.unwiredappeal.tivo.config.StreamBabyConfig;

public class FolderFileType extends BaseFileType {

	public String getIcon() {
		return StreamBabyConfig.cfgFolderIcon.getValue();
	}
	
	public boolean isFolder() {
		return true;
	}
}

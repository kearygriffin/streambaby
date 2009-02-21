package com.unwiredappeal.tivo.dir;

import java.util.Iterator;
import java.util.List;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BScreen;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.metadata.MetadataModule;
import com.unwiredappeal.tivo.modules.VideoHandlerModule;
import com.unwiredappeal.tivo.streambaby.PlayScreen;
import com.unwiredappeal.tivo.streambaby.ViewScreen;

public class BaseFileType implements MediaFileType {

	public String getIcon() {
		return StreamBabyConfig.cfgMovieIcon.getValue();
	}

	public boolean isFolder() {
		return false;
	}
	
	public BScreen createPlayScreen(BApplicationPlus app, DirEntry entry) {
		return new PlayScreen(app, entry);
	}

	public BScreen createViewerScreen(BApplicationPlus app,
			List<DirEntry> elist, String name, int kbps) {
		return new ViewScreen(app, elist, name, kbps);
	}

	public Iterator<MetadataModule> filterMetadataModulesIterator(
			List<MetadataModule> sortedList) {
		return sortedList.iterator();
	}

	public Iterator<VideoHandlerModule> filterVideoModulesIterator(
			List<VideoHandlerModule> sortedList) {
		return sortedList.iterator();
	}

}

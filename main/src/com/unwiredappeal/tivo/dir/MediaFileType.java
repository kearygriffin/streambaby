package com.unwiredappeal.tivo.dir;

import java.util.Iterator;
import java.util.List;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BScreen;
import com.unwiredappeal.tivo.metadata.MetadataModule;
import com.unwiredappeal.tivo.modules.VideoHandlerModule;

public interface MediaFileType {
	public String getIcon();
	public boolean isFolder();
	public BScreen createPlayScreen(BApplicationPlus app, DirEntry entry);
	public BScreen createViewerScreen(BApplicationPlus app, List<DirEntry> elist, String name,
			int kbps);
	public Iterator<MetadataModule> filterMetadataModulesIterator(
			List<MetadataModule> sortedList);
	public Iterator<VideoHandlerModule> filterVideoModulesIterator(
			List<VideoHandlerModule> sortedList);
}

package com.unwiredappeal.tivo.dir;

import java.util.List;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BScreen;

public interface MediaFileType {
	public String getIcon();
	public boolean isFolder();
	public BScreen createPlayScreen(BApplicationPlus app, DirEntry entry);
	public BScreen createViewerScreen(BApplicationPlus app, List<DirEntry> elist, String name,
			int kbps);
}

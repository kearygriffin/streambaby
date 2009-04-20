package com.unwiredappeal.tivo.push;

import java.net.URI;
import java.net.URL;
import java.util.List;

import com.unwiredappeal.tivo.dir.DirEntry;

public interface PushHandler {

	List<Tivo> getTivos();

	boolean pushVideo(URL url, DirEntry de, Tivo tivo, int qual);

	boolean canPush(DirEntry de, int qual);

}

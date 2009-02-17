package com.unwiredappeal.tivo.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

public class Utils {

	public static boolean isFolder(URI uri) {
		if (isFileScheme(uri)) {
			File f = new File(uri);
			return f.isDirectory();
		} else
			return false;
	}

	public static boolean isFile(URI uri) {
		if (isFileScheme(uri)) {
			File f = new File(uri);
			return f.isFile();
		} else
			return false;
	}
	public static boolean isFileScheme(URI uri) {
		try {
			if (uri.toURL().getProtocol().compareToIgnoreCase("file") == 0)
				return true;
			return false;
		} catch (MalformedURLException e) {
			return false;
		}
		
	}

}

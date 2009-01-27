package com.unwiredappeal.tivo.utils;

import java.net.MalformedURLException;
import java.net.URI;

public class Utils {
	public static boolean isFile(URI uri) {
		try {
			if (uri.toURL().getProtocol().compareToIgnoreCase("file") == 0)
				return true;
			return false;
		} catch (MalformedURLException e) {
			return false;
		}
		
	}

}

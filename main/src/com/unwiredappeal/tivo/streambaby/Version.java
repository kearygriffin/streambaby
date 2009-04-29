package com.unwiredappeal.tivo.streambaby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Version {
	public static String appName = "StreamBaby";
	public static String version = "UNKNOWN-DEVEL";
	static {
		InputStream is = Version.class.getResourceAsStream("/version.txt");
		if (is != null) {
			BufferedReader r= new BufferedReader(new InputStreamReader(is));
			try {
				String s = r.readLine();
				if (s != null) {
					String app = null;
					String ver = null;
					String strs[] = s.split("\\|");
					if (strs.length > 0)
						app = strs[0];
					if (strs.length > 1)
						ver = strs[1];
					if (app != null)
						appName = app;
					if (ver != null)
						version = ver;
				}
			} catch (IOException e) {
			}
		}
	}
	public String getVersionString() {
		String v = "v";
		if (version.length() > 0 && !Character.isDigit(version.charAt(0)))
			v = "";
		return appName + " " + v + version;
	}
}

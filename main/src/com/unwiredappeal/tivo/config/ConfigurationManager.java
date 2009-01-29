package com.unwiredappeal.tivo.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConfigurationManager {
	public class SlashTranslatorStream extends InputStream {

		private InputStream in;
		private int lastChar;
		public  SlashTranslatorStream(InputStream in) {
			this.in = in;
		}

		@Override
		public int read() throws IOException {
			int ret;
			if (lastChar == '\\') {
				lastChar = 0;
				ret = '\\';
			} else {
				lastChar = in.read();
				ret = lastChar;
			}
			return ret;
		}

	}

	private Properties configProps = new Properties();

	public Map<String, ConfigEntry> configMap = new LinkedHashMap<String, ConfigEntry>();
	public static ConfigurationManager inst = new ConfigurationManager();
	protected ConfigurationManager() { }
	
	public boolean attemptLoadProps(URL url) {
		if (url == null)
			return false;
		try {
			InputStream is = url.openStream();
			if (true || isWindows())
				is = new SlashTranslatorStream(is);
			configProps.load(is);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean attemptLoadProps(String filename) {
		if (filename == null || filename.length() == 0)
			return false;
		try {
			return attemptLoadProps(new File(filename).toURI().toURL());
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
	public void setConfigProperties(Map<String, String> map) {
		configProps.clear();
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> e = it.next();
			configProps.setProperty(e.getKey(), e.getValue());
		}
	}

	public boolean getBooleanProperty(String string, boolean b) {
		String def = b ? "true" : "false";
		String val = configProps.getProperty(string, def).trim().toLowerCase();
		return !(val.startsWith("f") || val.compareTo("0") == 0);
	}

	public String getStringProperty(String key, String defaultValue) {
		return configProps.getProperty(key, defaultValue);
	}

	public int getIntProperty(String key, int defaultValue) {
		String defString = Integer.toString(defaultValue);
		String prop = configProps.getProperty(key, defString);
		int retValue = defaultValue;
		try {
			retValue = Integer.parseInt(prop);
		} catch (NumberFormatException e) {
		}

		return retValue;
	}

	public static int parseInt(String s) {
		int p = -1;
		try {
			p = Integer.parseInt(s);
		} catch(NumberFormatException e) { 
			boolean asBool = parseBool(s);
			return asBool ? 1 : 0;
		}
		return p;
	}
	
	public static boolean parseBool(String val) {
		return !(val.startsWith("f") || val.compareTo("0") == 0);
	}

	public void addConfigEntry(ConfigEntry e) {
		configMap.put(e.name, e);
	}
	
	public ConfigEntry getConfigEntry(String name) {
		return configMap.get(name);
	}
	
	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}
	
	public String getHelpString() {
		String help = "";
		String newLine = System.getProperty("line.separator");

		Iterator<String> it= configMap.keySet().iterator();
		while(it.hasNext()) {
			String cfgName = it.next();
			ConfigEntry e = configMap.get(cfgName);
			String def = e.defaultValue;
			if (def == null)
				def = "none";
			help = help + cfgName + "=  (default:" + def + ")" + newLine;
			String helpMsg = e.helpMsg;
			if (helpMsg != null && helpMsg.length() > 0) {
				help = help + "   " + helpMsg + newLine;
			}
		}
		return help;
	}
	
	public static interface propertyHandler {
		void process(ConfigurableObject te, String key, String value);
	}

	public void matchProperties(ConfigurableObject te, String pattern, propertyHandler h) {
		Pattern p = Pattern.compile(pattern);
		// Process the keys in a particular order
		SortedSet<Object> keys = new TreeSet<Object>();
		keys.addAll(configProps.keySet());
		Iterator<Object> it = keys.iterator();
		while (it.hasNext()) {
			String key = it.next().toString().toLowerCase();
			Matcher m = p.matcher(key);
			if (m.matches()) {
				h.process(te, key, configProps.getProperty(key));				
			}
		}

	}

	public Properties getProperties() {
		return configProps;
	}


}

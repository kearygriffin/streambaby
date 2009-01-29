package com.unwiredappeal.tivo.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyReplacer {
	Pattern propPattern = Pattern.compile("\\$\\{(.*?)\\}");

	Map<String, String> propMap = new HashMap<String, String>();
	public void set(String prop, String val) {
		propMap.put(prop, val);		
	}

	public String parseProperties(String str) {
		Matcher m = propPattern.matcher(str);
		StringBuffer buf = new StringBuffer();
		while(m.find()) {
			String key = m.group(1);
			String replacement = propMap.get(key);
			if (replacement != null)
				m.appendReplacement(buf, replacement);
		}
		m.appendTail(buf);
		return buf.toString();
	}

	public void set(String prop, int n) {
		set(prop, Integer.toString(n));
	}

}

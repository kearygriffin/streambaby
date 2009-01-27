package com.unwiredappeal.tivo.config;

import java.lang.reflect.Field;

public class ConfigurableObject {
	//protected Object initObject;
	public ConfigurableObject() {
		/*
		initObject = obj;
		if (process)
			processConfigEntrys();
			*/
	}
	/*
	protected ConfigurableObject() {
		//this(null, true);
	}
	*/
	
	/*
	protected ConfigurableObject(boolean b) {
		this(null, b);
	}
	*/
	
	/*
	protected Object getInitObject() {
		return initObject;
	}
	*/
	protected void populateConfig() {
		Field[] fields = this.getClass().getFields();
		for (int i=0;i<fields.length;i++) {
			Field field = fields[i];
			if (field.getType() == ConfigEntry.class) {
				ConfigEntry e;
				try {
					e = (ConfigEntry)field.get(this);
					e.process(this);

				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}

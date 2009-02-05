package com.unwiredappeal.tivo.modules;

public interface StreamBabyModule {
	public static final int STREAMBABY_MODULE_VIDEO = 1;
	public static final int STREAMBABY_MODULE_METADATA = 2;
	
	public static int MAX_PRIORITY = 100;
	public static int MIN_PRIORITY = 0;
	public static int DEFAULT_PRIORITY = 50;

	public int getSimplePriority();
	public Object getModule(int moduleType);
}

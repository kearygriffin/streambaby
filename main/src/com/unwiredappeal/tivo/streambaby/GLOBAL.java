// $Id: GLOBAL.java 11 2008-11-28 18:10:07Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.streambaby;

import java.awt.Color;

public class GLOBAL {
	//public static final String film_background = "film_strip_blue.jpg";
	public static final String film_background = null; /// "film_section.jpg";

	public static final float SELECT_STRETCH = 1.15f;

	// public static String sortOrder = "alphanumeric";
	/*
	public static String BACKGROUND_PICTURE = "blue.jpg";
	*/
	// public static String[] extList = {".mpg", ".vob", ".mpeg2", ".mp2",
	// ".mp4", ".mpeg4"};

	// public static String FOLDER_PREFIX = "__folder__";
	public static String FONT_SIZE = "medium";
	// public static String bookmarkFile = "bookmarks";

	// Colors
	public static Color title_COLOR = Color.yellow;
	public static Color text_COLOR = Color.white;
	public static Color text_SHADOW = Color.black;

	// Playback times
	public static int timeout_status_bar = 5;
	public static int timeout_info = 10;
	public static int skip_back = 8;
	public static int skip_forwards = 30;
	public static float slow_speed = (float) 0.125;

	// list text relative positions
	public static int text_X = 25;
	public static int text_Y = 4;

	// folder icon
	/*
	public static String folder_icon = "folder.png";
	public static String movie_icon = "movie.png";
	public static String icon_icon = "icon.png";
	*/
	public static int icon_X = 0;
	public static int icon_Y = 4;
	public static int icon_W = 24;

	// status bar
	public static int statusBG_W = 540; // 376+53;
	public static int statusBG_H = 15;
	public static int statusBG_X = (640 - statusBG_W) / 2;
	public static int statusBG_Y = 375;
	public static Color statusBG_COLOR = Color.black;
	public static String status_FONT = "default-18.font";
	public static Color statusBAR_COLOR = Color.green;

	// prune cache every 30 minutes
	public static long CACHE_PRUNE_DELAY = 30 * 60 * 1000L;
	//public static String DEFAULT_FFMPEG_PATH = "ffmpeg";
	//public static String DEFAULT_WIN_FFMPEG_PATH = "native/ffmpeg";

}

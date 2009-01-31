package com.unwiredappeal.tivo.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import com.unwiredappeal.mediastreams.MP4StreamingModule;
import com.unwiredappeal.mediastreams.MpegStreamingModule;
import com.unwiredappeal.mediastreams.RawStreamingModule;
import com.unwiredappeal.mediastreams.TivoStreamingModule;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.streambaby.PreviewWindow;
import com.unwiredappeal.tivo.streambaby.host.Listener;
import com.unwiredappeal.tivo.utils.AvailableSocket;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.RandomAccessFileInputStream;
import com.unwiredappeal.tivo.utils.TempFileManager;
import com.unwiredappeal.tivo.videomodule.VideoFormats;
import com.unwiredappeal.tivo.videomodule.VideoModuleHelper;
import com.unwiredappeal.virtmem.MappedFileMemoryManager;

public class StreamBabyConfig extends ConfigurableObject {

	public static String workingDir;
	public static String streamBabyDir;
	public static String cacheDir;
	public static String nativeDir;
	
	static {
		setupDefaultDirectories();
	}
	
	public static boolean isWindows = ConfigurationManager.isWindows();
	//public static String streamBabyDir = getJarDirectory();
	public static String DEFAULT_VIDEO_DIR = "videos";
	public static String DEFAULT_TITLE = "Stream, Baby, Stream";
	public static String CONFIG_FILE = "streambaby.ini";
	public static String CONFIG_DEFAULT_EXTS = "mp4,mpeg,vob,mpg,mpeg2,mp2,avi,wmv,asf,mkv,tivo,m4v,m4a,raw";

	
	public static ConfigEntry cfgSocketStart = new ConfigEntry(
			"sockets.start",
			"8500",
			"Socket start for internal socket use"
			);
	public static ConfigEntry cfgSocketsCount= new ConfigEntry(
				"sockets.count",  
				"500",	
				"Number of sockets to use for internal socket use"
				); 
	public static ConfigEntry cfgTrimExtensions = new ConfigEntry( 
		"trimextensions",
		"false",
		"Trim extensions from filenames when displaying"
		);
	
	public static ConfigEntry cfgExtensions = new ConfigEntry(
			"extensions",
			CONFIG_DEFAULT_EXTS,
			"Extensions to scan for when listing files"
			);
	public static ConfigEntry cfgPort = new ConfigEntry(
			"port",
			"7288",
			"HME port to attach to"
			);
	
	public static ConfigEntry cfgIp = new ConfigEntry(
			"ip",
			(String)null,
			"IP address to bine HME to"
			);
	
	
	public static ConfigEntry cfgPassword = new ConfigEntry(
			"password",
			(String)null,
			"default password list for directories (comma seperated)"
			);
	
	public static ConfigEntry cfgMp4ModuleDisable = new ConfigEntry(
			"mp4module.disable",
			"false",
			"Disable built-in mp4 streaming module"
			);
	
	public static ConfigEntry cfgMpegModuleDisable = new ConfigEntry(
			"mpegmodule.disable",
			"false",
			"Disable built-in mpeg streaming module"
			);
	
	public static ConfigEntry cfgTitle = new ConfigEntry(
			"title",
			DEFAULT_TITLE,
			"Title to use for HME"
			);
	
	public static ConfigEntry cfgPreviewQuality = new ConfigEntry(
			"preview.quality",
			"25",
			"jpeg quality for preview (1-100)"
			);
	
	public static ConfigEntry cfgPreviewBig = new ConfigEntry(
			"preview.big",
			"false",
			"use full screen for preview (otherwise it is thumbnailed)"
			);
	
	public static ConfigEntry cfgDisableMdns  = new ConfigEntry(
			"mdns.disable",
			"false",
			"Disable mDNS"
			);
	
	public static ConfigEntry cfgMp4Interleave = new ConfigEntry(
			"mp4module.interleave",
			"true",
			"enable re-interleaving of mp4s"
		);
	
	public static ConfigEntry _cfgNativePath = new ConfigEntry(
			"lib.native",
			"",
			"path to look for native libraries"
			);
	
	public static ConfigEntry cfgStreamBabyDir = new ConfigEntry(
			"streambaby.dir",
			"",
			"main path for streambaby"
			);
	public static ConfigEntry cfgDisableTranscode = new ConfigEntry(
			"transcode.disable",
			"false",
			"disable transcoding for incompatible video streams"
			);

	public static ConfigEntry cfgTmpPath= new ConfigEntry(
			"tmp.path",
			System.getProperty("java.io.tmpdir"),
			"Temporary files path"
			);

	public static ConfigEntry cfgDisablePreview = new ConfigEntry(
		"preview.disable",
		"false",
		"Disable jpg images for preview"
		);
	
	public static ConfigEntry cfgPreviewThreaded= new ConfigEntry(
			"preview.threaded",
			"true",
			"Use threads to generate preview frames"
			);

	public static ConfigEntry cfgPreviewPredictive= new ConfigEntry(
			"preview.predictive",
			"false",
			"Attempt to predict which frame will be previewed next, and pre-generate it"
			);
	
	public static ConfigEntry cfgPreviewDisplayTimeAlways = new ConfigEntry(
			"preview.displaytime",
			"true",
			"Display the time over the preview image when ffwding/rwding"
			);
	
	public static ConfigEntry cfgCacheDirectory = new ConfigEntry(
			"preview.cache",
			"cache",
			"Directory to look for (and possibly store) static pvw files"
			);
	
	public static ConfigEntry cfgAutoGeneratePreview= new ConfigEntry(
			"preview.autogenerate",
			"true",
			"Autogenerate static pvw files when file is played for first time"
			);
	
	public static ConfigEntry cfgContinueGenerate= new ConfigEntry(
			"autogenerate.continue",
			"true",
			"continue autogeneration of previews, even when movie stopped"
			);
	
	public static ConfigEntry cfgBackgroundGenerate = new ConfigEntry(
			"autogenerate.background",
			"0",
			"minutes to check for files to autogenerate previews in the background 0=disabled"
			);

	public static ConfigEntry cfgDeletePreviews = new ConfigEntry(
			"autogenerate.delete",
			"false",
			"deletes preview files when the original file can no longer be found"
			);

	public static ConfigEntry cfgUsePleaseWait = new ConfigEntry(
			"player.pleasewait",
			"true",
			"Shows pleaseWait graphic when seeking.  If disabled shows text"
			);
	
	public static ConfigEntry cfgReadBufferSize = new ConfigEntry(
			"readbuffer.size",
			"0",
			"read-ahead buffer size.  0 = use built-in default"
			);

	public static ConfigEntry cfgIconIcon = new ConfigEntry(
			"icon.icon",
			"icon.png",
			"Icon to use for Music, photos, and more screen"
			);

	public static ConfigEntry cfgFolderIcon = new ConfigEntry(
			"icon.folder",
			"folder.png",
			"Icon to use for folder"
			);

	public static ConfigEntry cfgMovieIcon = new ConfigEntry(
			"icon.movie",
			"movie.png",
			"Icon to use for movie"
			);
	
	public static ConfigEntry cfgBackgroundImage = new ConfigEntry(
			"background.image",
			"blue.jpg",
			"background image to use"
			);
	
	public static ConfigEntry cfgSavePassword = new ConfigEntry(
			"remember.password",
			"true",
			"set to false to not remember passwords on a tivo/tivo basis"
			);

	public static ConfigEntry cfgIgnoreDotFiles= new ConfigEntry(
			"ignore.dotfiles",
			"true",
			"set to false to not include files/folders beginning with a dot in file listings"
			);
	
	public static ConfigEntry cfgCutStartOffset = new ConfigEntry(
			"cut.startoffset",
			"0",
			"set to the number of milliseconds to wait after seeing a cut point before jumping"
			);

	public static ConfigEntry cfgCutEndOffset = new ConfigEntry(
			"cut.endoffset",
			"0",
			"set to the number of milliseconds to jump to before the end of a cutpoint"
			);
	
	public static ConfigEntry cfgQualityHighestVbr = new ConfigEntry(
			"quality.highestvbr",
			"5000",
			"video kbps for highest quality setting"
			);
	
	public static ConfigEntry cfgQualityLowestVbr = new ConfigEntry(
			"quality.lowestvbr",
			"512",
			"video kbps for lowest quality setting"
			);

	public static ConfigEntry cfgQualityHighestAbr = new ConfigEntry(
			"quality.highestabr",
			"192",
			"audio kbps for highest quality setting"
			);
	
	public static ConfigEntry cfgQualityLowestAbr = new ConfigEntry(
			"quality.lowestvbr",
			"128",
			"audio kbps for lowest quality setting"
			);

	public static ConfigEntry cfgQualityHighestRes= new ConfigEntry(
			"quality.highres",
			"720",
			"Highest allowable height resolution for quality settings"
			);

	public static ConfigEntry cfgQualityLowestRes= new ConfigEntry(
			"quality.lowres",
			"400",
			"lowest allowable height resolution for quality settings"
			);
	
	public static ConfigEntry cfgTwoChannel = new ConfigEntry(
			"quality.2channel",
			"0",
			"below or equal to this quality level, force drop to 2 channels"
			);

	public static ConfigEntry cfgDefaultQuality = new ConfigEntry(
			"quality.default",
			"same",
			"default quality to use.  Can be auto/same, 1-7 (lowest=1,highest=7), or kbps"
			);
	
	public static ConfigEntry cfgAutoQuality = new ConfigEntry(
			"quality.auto",
			"false",
			"Turn on auto-quality.  Will check bandwidth at startup and base quality on bandwidth"
			);
	
	public static ConfigEntry cfgAutoQualityPercent = new ConfigEntry(
			"quality.auto.percent",
			"70",
			"Use this percentage of bandwidth for stream transfer"
			);

	public static ConfigEntry cfgQualitySelection = new ConfigEntry(
			"quality.select",
			"false",
			"Enable user to select quality from 'play' screen"
			);

	public static ConfigEntry cfgTopLevelSkip = new ConfigEntry(
			"toplevel.skip",
			"false",
			"Set to true to skip top-level folders if there is only one"
			);

	
	// This always be last
	public static ConfigEntry cfgModules = new ConfigEntry(
			"module",
			new moduleEntryHandler(),
			"select which video modules to load"
			);
	
	

	
	
	/*
	public static ConfigEntry cfgIs16x9 = new ConfigEntry(
			"aspect16x9",
			"true",
			"set to true if your tivo is set to 16/9"
			);
	*/
	//public final String CFG_ENTRY_DIRS_PATTERN = "dir\\.\\d+"; // dir.1=xxxx
	//public final String CFG_ENTRY_MODULES_PATTERN = "module\\.\\d+"; // module.1=xxxx

	public String[] extList;

	public final String[] defaultVideoModules = new String[] {
			"com.unwiredappeal.tivo.vm.ffjava.FFmpegJavaVideoModule",
			"com.unwiredappeal.tivo.vm.ffexe.FFmpegExeVideoModule"
	};
	
	public Boolean _DEBUG = false;
	// public List<String> baseDirs = new ArrayList<String>();
	// public DirEntry _rootDirEntry;
	
	public boolean configRead = false;
	public static StreamBabyConfig inst = new StreamBabyConfig();
	static
	{
		init();
	}

	protected StreamBabyConfig() 
	{
		//super(false);
	}
	
	public static void init() {
		// Set DEBUG variable if environment variable is set
		if (System.getenv("DEBUG") != null
				|| System.getProperty("streambaby.debug", null) != null) {
			inst.setDebug(true);
		} else
			inst.setDebug(false);
	}

	public void setDebug(boolean b) {
		_DEBUG = b;
		Listener.DEBUG = b;
	}




	/*
	private void processDirectories(DirEntry de) {
		// = new DirEntry();
		de.setName("Top Level");
		de.isFolder = true;

		matchProperties(CFG_ENTRY_DIRS_PATTERN, new dirEntryHandler(de));
		if (de.entryList.isEmpty()) {
			File f = new File(DEFAULT_VIDEO_DIR);
			if (f.exists() && f.isDirectory()) {
				de.addEntry(new DirEntry(f.toURI()));
			}
		}
	}
	*/
	
	private void trimStrings(String[] sa) {
		for (int i = 0; i < sa.length; i++) {
			sa[i] = sa[i].trim();
		}

	}
	
	private static class moduleEntryHandler implements ConfigurationManager.propertyHandler {
		public void process(ConfigurableObject te, String key, String value) {
			VideoModuleHelper.inst.addModule(value);
		}

	}

	public void processModules() {
		//matchProperties(CFG_ENTRY_MODULES_PATTERN, new moduleEntryHandler());
		int cnt = VideoModuleHelper.inst.getModuleCount();
		if (cfgMp4ModuleDisable.getBool() != true)
			VideoModuleHelper.inst.addModule(MP4StreamingModule.class.getCanonicalName());
		if (cfgMpegModuleDisable.getBool() != true)
			VideoModuleHelper.inst.addModule(MpegStreamingModule.class.getCanonicalName());			
		VideoModuleHelper.inst.addModule(TivoStreamingModule.class.getCanonicalName());
		VideoModuleHelper.inst.addModule(RawStreamingModule.class.getCanonicalName());		
		if (cnt == 0) {
			for (int i=0;i<defaultVideoModules.length;i++) {
				boolean loadmod = ConfigurationManager.inst.getBooleanProperty(defaultVideoModules[i], true);
				if (loadmod)
					VideoModuleHelper.inst.addModule(defaultVideoModules[i]);
			}
		}
	}

	public static String convertRelativePath(String path, String dir) {
		if (!(path.charAt(0) == File.separatorChar || (path.length() > 1 && path.charAt(1) == ':'))) 
			path = dir + File.separator + path;
		return path;
	}
	
	public static String convertRelativePath(String path) {
		return convertRelativePath(path, streamBabyDir);
	}

	private void processConfiguration() {

		/*
		Properties props = ConfigurationManager.inst.getProperties();
		Iterator<Entry<Object, Object>> it = props.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Object, Object> e = it.next();
			Log.debug("key: ("+e.getKey()+"), value: (" + e.getValue() + ")");
		}
		*/
		populateConfig();
		
		String sbd = cfgStreamBabyDir.getValue();
		if (sbd.length() > 0)
			streamBabyDir = sbd;
		
		if (_cfgNativePath.getValue().length() > 0)
			nativeDir = convertRelativePath(_cfgNativePath.getValue());
		else
			nativeDir = streamBabyDir + File.separator + "native";

		
		Log.debug("WorkingDir: " + workingDir);
		Log.debug("StreamBabyDir: " + streamBabyDir);
		Log.debug("CurDir: " + getCurDir());
		
		String extstr = cfgExtensions.value;
		extList = extstr.split(",");
		trimStrings(extList);
		//trimExtensions = this.cfgTrimExtensions.getBool();
		
		AvailableSocket.setStartSocket(cfgSocketStart.getInt());
		AvailableSocket.setMaxSockets(cfgSocketsCount.getInt());

		
		String tmpPath = cfgTmpPath.getValue();
		if (tmpPath != null && tmpPath.length() > 0) {
			tmpPath = convertRelativePath(tmpPath);
			File td = new File(tmpPath);
			if (td == null || !td.exists() || !td.isDirectory())
				tmpPath = System.getProperty("java.io.tmpdir");
			TempFileManager.tmpDirName = tmpPath;
			File f = new File(tmpPath);
			if (!f.exists()) {
				f.mkdir();
			}
			TempFileManager.INSTANCE_TMP_PREFIX = "sbsp" + cfgPort.getValue() + ".";
			TempFileManager.cleanupPreviousInstance();
		}
		
		// use our new temp file manager for virtual memory
		MappedFileMemoryManager.tmpCreator = new MappedFileMemoryManager.TempCreator() {
			public File createTempFile(String prefix, String suffix) throws IOException {
				return TempFileManager.createTempFile(prefix, suffix);
			}
		};


		PreviewWindow.threadedCacher = cfgPreviewThreaded.getBool();
		PreviewWindow.defaultPredictive = cfgPreviewPredictive.getBool();
		PreviewWindow.displayTimeAlways = cfgPreviewDisplayTimeAlways.getBool();
		
		if (StreamBabyConfig.cfgCacheDirectory.getValue().length() == 0)
			cacheDir = null;
		else 
			cacheDir = convertRelativePath(cfgCacheDirectory.getValue());
		
		// Delete tmp files in cache directory
		File cache = new File(cacheDir);
		File[] tmpFiles = cache.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().endsWith(".tmp");
			}
		});
		if (tmpFiles != null) {
			for (File f : tmpFiles)
				f.delete();
		}
		
		int bsize = cfgReadBufferSize.getInt();
		if (bsize > 0) {
			RandomAccessFileInputStream.BUFFER_SIZE= bsize;
			Listener.READ_BUFFER_SIZE = bsize;
		}

		processModules();
	}

	public DirEntry buildRootDirEntry() {
		RootDirEntry de = new RootDirEntry();

		//processDirectories(de);
		return de.getDirEntry();
	}

	private boolean _readConfiguration(String configFile) {
		// first, if a config is specified try it

		configRead = true;
		if (configFile != null) {
			if (ConfigurationManager.inst.attemptLoadProps(configFile))
				return true;
			return false;
		}
		// Next, try user dir
		if (ConfigurationManager.inst.attemptLoadProps(System.getProperty("user.dir")
				+ File.separator + CONFIG_FILE))
			return true;
		

		// and the current directory
		if (ConfigurationManager.inst.attemptLoadProps(CONFIG_FILE))
			return true;

		// then 
		if (ConfigurationManager.inst.attemptLoadProps(streamBabyDir + File.separator + CONFIG_FILE))
			return true;
		
		return false;


	}

	public boolean readConfiguration(String configFile) {
		if (configRead)
			return true;
		if (_readConfiguration(configFile) == true) {
			processConfiguration();
			return true;
		}
		processConfiguration();
		return false;
	}
	
	public boolean readConfiguration(Map<String, String> props) {
		if (configRead)
			return true;
		ConfigurationManager.inst.setConfigProperties(props);
		processConfiguration();
		return true;
	}

	public String getConfigHelp() {
		return ConfigurationManager.inst.getHelpString();
	}
	
	private static String getCurDir() {
		String curDir = new File(".").getAbsolutePath();
		while(curDir != null && curDir.length() > 0 && curDir.lastIndexOf('.') == curDir.length()-1 || curDir.lastIndexOf('/') == curDir.length()-1 || curDir.lastIndexOf('\\') == curDir.length()-1) {
			curDir = curDir.substring(0, curDir.length()-1);
		}
		return curDir;
	}
	private static void setupDefaultDirectories() {

		workingDir = getCurDir();
		String launchDir = System.getenv("LAUNCHDIR");
		if (launchDir != null && launchDir.length() > 0)
			workingDir  = launchDir;
		
		String dir = System.getProperty("streambaby.dir");
		if (dir == null) {
			dir = workingDir;
			try {
				Class<ConfigurationManager> c = ConfigurationManager.class;
				if (c != null) {
					URL url = c.getResource(c.getSimpleName() + ".class");
					String urlString = url.toString();
					if (urlString != null) {
						int i;
						i = urlString.lastIndexOf("/jbin/");
	
						if (i < 0) {
							i = urlString.lastIndexOf("/main/bin/");
						}
						if (i >= 0) {
							urlString = urlString.substring(0, i);
						}
						if (urlString.startsWith("jar:")) {
							urlString = urlString.substring(4);
						}
						dir = new File(new URL(urlString).toURI()).getCanonicalPath();
					} else
						dir = workingDir;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//debug.print("CurDir: " + dir);

		streamBabyDir = dir;
	}

	
	public int getDefaultQuality() {
		String qualString = cfgDefaultQuality.getValue().toLowerCase();
		if (qualString.compareTo("auto") == 0)
			return VideoFormats.QUALITY_AUTO;
		else if (qualString.compareTo("same") == 0)
			return VideoFormats.QUALITY_SAME;
		else {
			int q = cfgDefaultQuality.getInt();
			if (q >= VideoFormats.QUALITY_LOWEST && q <= VideoFormats.QUALITY_HIGHEST)
				return q;
		}
		return VideoFormats.QUALITY_SAME;
	}
	
	public int getAudioChannels(int qual) {
		if (cfgTwoChannel.getInt() < VideoFormats.QUALITY_LOWEST)
			return 0;
		
		if (qual > VideoFormats.LAST_QUALITY) {
			int thisAudioBitrate = getAudioBr(qual);
			int twobitrate = getAudioBr(cfgTwoChannel.getInt());
			if (thisAudioBitrate <= twobitrate)
				return 2;
			else
				return 0;
		} else {
			if (qual <= cfgTwoChannel.getInt())
				return 2;
			else
				return 0;
		}
	}

	public int getVideoBr(int qual) {
		if (qual > VideoFormats.LAST_QUALITY) {
			return qual - getAudioBr(qual);
		}
		int range = cfgQualityHighestVbr.getInt() - cfgQualityLowestVbr.getInt();
		int qrange = VideoFormats.QUALITY_HIGHEST - VideoFormats.QUALITY_LOWEST;
		qual = qual - VideoFormats.QUALITY_LOWEST;
		int delta = (int)((qual / (float)qrange) * range);
		return cfgQualityLowestVbr.getInt() + delta;
		
	}

	public int getAudioBr(int qual) {
		int delta;
		if (qual > VideoFormats.LAST_QUALITY) {
			if (qual <= cfgQualityLowestVbr.getInt())
				return cfgQualityLowestAbr.getInt();
			if (qual >= cfgQualityHighestVbr.getInt())
				return cfgQualityHighestAbr.getInt();
			
			int range = cfgQualityHighestVbr.getInt() - cfgQualityLowestVbr.getInt();
			int arange = cfgQualityHighestAbr.getInt() - cfgQualityLowestAbr.getInt();

			int q = qual - cfgQualityLowestVbr.getInt();
			delta = (int)((q / (float)range) * arange);
			
		} else {
			int range = cfgQualityHighestAbr.getInt() - cfgQualityLowestAbr.getInt();
			int qrange = VideoFormats.QUALITY_HIGHEST - VideoFormats.QUALITY_LOWEST;
			qual = qual - VideoFormats.QUALITY_LOWEST;
			delta = (int)((qual / (float)qrange) * range);
		}
		return cfgQualityLowestAbr.getInt() + delta;

	}
	
	public int getYRes(int qual) {
		int delta;
		if (qual > VideoFormats.LAST_QUALITY) {
			if (qual <= cfgQualityLowestVbr.getInt())
				return cfgQualityLowestRes.getInt();
			if (qual >= cfgQualityHighestVbr.getInt())
				return cfgQualityHighestRes.getInt();
			
			int range = cfgQualityHighestVbr.getInt() - cfgQualityLowestVbr.getInt();
			int yrange = cfgQualityHighestRes.getInt() - cfgQualityLowestRes.getInt();

			int q = qual - cfgQualityLowestRes.getInt();
			delta = (int)((q / (float)range) * yrange);
			
		} else {
			int range = cfgQualityHighestRes.getInt() - cfgQualityLowestRes.getInt();
			int qrange = VideoFormats.QUALITY_HIGHEST - VideoFormats.QUALITY_LOWEST;
			qual = qual - VideoFormats.QUALITY_LOWEST;
			delta = (int)((qual / (float)qrange) * range);
		}
		return cfgQualityLowestRes.getInt() + delta;

	}

}
 
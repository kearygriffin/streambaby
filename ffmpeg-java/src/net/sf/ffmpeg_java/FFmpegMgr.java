package net.sf.ffmpeg_java;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.ffmpeg_java.bcel.FFmpegClassLoader;


import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

public class FFmpegMgr {
	private static boolean inited = false;
	public static Logger logger = null; // new DefaultLogger();
	public static class LibDefaults {
		public LibDefaults(int cver, int fver, String avUtilName, String avCodecName, String avFormatName, String swScaleName) {
			this.codecVer = cver;
			this.formatVer = fver;			
			this.avUtilName = avUtilName;
			this.avCodecName = avCodecName;
			this.avFormatName = avFormatName;
			this.swScaleName = swScaleName;			
		}
		public int codecVer;
		public int formatVer;
		public int logLevel = 0;
		public String avUtilName;
		public String avCodecName;
		public String avFormatName;
		public String swScaleName;
	}
	private static LibDefaults default51 = new LibDefaults(51, 51,
    		System.getProperty("avutil.lib", System.getProperty("os.name").startsWith("Windows") ? "avutil-49" : "avutil"),
    	    System.getProperty("avcodec.lib",
    	    	    		System.getProperty("os.name").startsWith("Windows") ? "avcodec-51" : "avcodec"), 
    	    System.getProperty("avformat.lib",
    	    	    	    		System.getProperty("os.name").startsWith("Windows") ? "avformat-51" : "avformat"), 
    		System.getProperty("swscale.lib",
    	    		System.getProperty("os.name").startsWith("Windows") ? "swscale-0" : "swscale") 
    	    		);
	private static LibDefaults default52 = new LibDefaults(52, 52,
    		System.getProperty("avutil.lib", System.getProperty("os.name").startsWith("Windows") ? "avutil-49" : "avutil"),
    	    System.getProperty("avcodec.lib",
    	    	    		System.getProperty("os.name").startsWith("Windows") ? "avcodec-52" : "avcodec"), 
    	    System.getProperty("avformat.lib",
    	    	    	    		System.getProperty("os.name").startsWith("Windows") ? "avformat-52" : "avformat"), 
    		System.getProperty("swscale.lib",
    	    		System.getProperty("os.name").startsWith("Windows") ? "swscale-0" : "swscale") 

		);
    private static LibDefaults defaultLib = default52;

    /*
	public static net.sf.ffmpeg_java.v51.AVCodecLibrary avCodec_51;
	public static net.sf.ffmpeg_java.v51.AVFormatLibrary avFormat_51;
	public static net.sf.ffmpeg_java.v52.AVCodecLibrary avCodec_52;
	public static net.sf.ffmpeg_java.v52.AVFormatLibrary avFormat_52;
	public static AVUtilLibrary avUtil;
	public  static SWScaleLibrary swScale = null;
	*/

	public static boolean hasSwScale; // assume true until we fail
	public static int _avCodecVersion = -1;
	public static int _avCodecMajor = -1;
	public static int _avFormatVersion = -1 ;
	public static int _avFormatMajor = -1;
	
	public static NativeLibrary avUtilNative;
	public static NativeLibrary avCodecNative;
	public static NativeLibrary avFormatNative;
	public static NativeLibrary swScaleNative;
	public static FFmpegHelper helper;
	
	public static LibDefaults loadedVersion = null;
	
	public static LibDefaults initLibrary() {
		return initLibrary(defaultLib, null);
	}
	public static LibDefaults initLibrary(LibDefaults def) {
		return initLibrary(def, null);
	}
	public static LibDefaults initLibrary(LibDefaults def, String nativePath) {
		if (!inited)
			return _initLibrary(def, nativePath);
		else
			return loadedVersion;
	}

	public synchronized static LibDefaults _initLibrary(LibDefaults def, String nativePath) {
		if (inited)
			return loadedVersion;
		if (def == null) 
			def = defaultLib;
		
		String defCodecName;
		String defFormatName;
		if (def.codecVer == 51) 
			defCodecName = default51.avCodecName;
		else
			defCodecName = default52.avCodecName;
		if (def.formatVer == 51)
			defFormatName = default51.avFormatName;
		else
			defFormatName = default52.avFormatName;
		
		String avUtilName = def.avUtilName == null ? defaultLib.avUtilName : def.avUtilName;
		String avCodecName = def.avCodecName == null ? defCodecName : def.avCodecName;
		String avFormatName = def.avFormatName == null ? defFormatName : def.avFormatName ;
		String swScaleName = def.swScaleName == null ? defaultLib.swScaleName : def.swScaleName;

		if (nativePath != null) {
			NativeLibrary.addSearchPath(avUtilName, nativePath);
			NativeLibrary.addSearchPath(avCodecName, nativePath);
			NativeLibrary.addSearchPath(avFormatName, nativePath);
			NativeLibrary.addSearchPath(swScaleName, nativePath);

		}

		String swsName = null;
		try {
			/* Order is important here to make sure correct (from native/) ffmpeg libraries are loaded */
			/* Otherwise, for instance, avFormat will try to load the system avcodec library instead of the one in native */
			avUtilNative = getLibraryInstance(avUtilName, "avutil");
			log("Loaded: " + avUtilNative.getFile().getAbsolutePath());

			//avUtil = AVUtilLibrary.INSTANCE;
			
			avCodecNative  = getLibraryInstance(avCodecName, "avcodec");
			log("Loaded: " + avCodecNative.getFile().getAbsolutePath());
			Function f= avCodecNative.getFunction("avcodec_version");
			_avCodecVersion = ((Integer)(f.invoke(int.class, new Object[0] ))).intValue();
			_avCodecMajor = _avCodecVersion >>> 16;
			log("Loaded avCodec version: " + _avCodecMajor + " (" + _avCodecVersion + ")");
			avFormatNative = getLibraryInstance(avFormatName, "avformat");
			log("Loaded: " + avFormatNative.getFile().getAbsolutePath());
			f = null;
			try {
				f= avFormatNative.getFunction("avformat_version");
			} catch(UnsatisfiedLinkError le) { }
			if (f == null) {
				log("avformat_version function not available, will attempt to guess version");
				int avCodecMinor = (_avCodecVersion >>> 8) & 0xff;
				if (avCodecMinor < 48) {
					_avFormatMajor = 51;	
				} else {
					_avFormatMajor = 52;
				}
				_avFormatVersion = _avFormatMajor << 16;
			} else {
				_avFormatVersion = ((Integer)(f.invoke(int.class, new Object[0] ))).intValue();
				_avFormatMajor = _avFormatVersion >>> 16;
			}
			log("Loaded avFormat version: " + _avFormatMajor + " (" + _avFormatVersion + ")");
			
			hasSwScale = false;
			try {
				if (swScaleName != null) {
					swScaleNative = getLibraryInstance(swScaleName, "swscale");
					swsName = swScaleNative.getFile().getAbsolutePath();
					log("Loaded: " +  swScaleNative.getFile().getAbsolutePath());
					log("Marking libswscale as available.");
					hasSwScale = true;
				}
				//swScale = SWScaleLibrary.INSTANCE;
			} catch(Throwable e) {
				log("Error loading swscale: " + e.getMessage());
				log("Marking as not available (this may be normal)");
			}
			
			f = avFormatNative.getFunction("av_register_all");
			f.invokeVoid(new Object[0]);
			f = avCodecNative.getFunction("avcodec_register_all");
			f.invokeVoid(new Object[0]);

			
			// default to standard ffmpeg output
			f = avUtilNative.getFunction("av_log_set_level");
			f.invokeVoid(new Integer[] { def.logLevel });

			/* Must set this to true before calling fixup */
			inited = true;
			
			helper = fixupFFmpegClass(FFmpegHelperImpl.class);

		} catch(Throwable e) {
			//e.printStackTrace();
			log("Error attempting toload ffmpeg libraries: " + e.getMessage());
			return null;
		}
		LibDefaults ret = new LibDefaults(_avCodecMajor, _avFormatMajor, avUtilNative.getFile().getAbsolutePath(),
				avCodecNative.getFile().getAbsolutePath(), avFormatNative.getFile().getAbsolutePath(), swsName);
		return ret;
		
	}
	
	public static int getAvCodecVersion() {
		initLibrary();
		return _avCodecMajor;
	}

	public static int getAvFormatVersion() {
		initLibrary();
		return _avFormatMajor;
	}

	@SuppressWarnings("unchecked")
	public static <T> T fixupFFmpegClass(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		FFmpegMgr.initLibrary();
		ClassLoader cl = new FFmpegClassLoader(className);
		return (T) cl.loadClass(className).newInstance();
	}

	@SuppressWarnings("unchecked")
	public static <T> T fixupFFmpegClass(Class<T> clazz) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String className = clazz.getCanonicalName();
		return (T) fixupFFmpegClass(className);
	}
	

	public static NativeLibrary getLibraryInstance(String lib, String propBase)  {
		String val = lib;
		NativeLibrary inst = null;
		log("Loading " + propBase + ", default: " + lib);
		try {
			inst = NativeLibrary.getInstance(val);
		} catch(UnsatisfiedLinkError e) {
			if (Platform.isLinux()) {
	                String archPath = new Integer(Pointer.SIZE * 8).toString();
	            String[] paths = {
	                "/usr/lib" + archPath,
	                "/lib" + archPath,
	                "/usr/lib",
	                "/lib",
	            };
	            // Linux 64-bit does not use /lib or /usr/lib
	            if (Platform.isLinux() && Pointer.SIZE == 8) {
	                paths = new String[] {
	                    "/usr/lib" + archPath,
	                    "/lib" + archPath,
	                };
	            }
				
				// We are using a default value, so lets try a little more
	            String libMatch = matchLibrary(val, Arrays.asList(paths));
	            if (libMatch != null) {
					inst = NativeLibrary.getInstance(libMatch);
					//cfg.defaultValue = libMatch;
					e = null;
	            }
			}
			if (e != null)
				throw e;
		}
		String propName = propBase + ".lib";
		System.getProperties().setProperty(propName, inst.getFile().getAbsolutePath());
		return inst;
	}
	
    /**
     * matchLibrary() is very Linux specific.  It is here to deal with the case
     * where /usr/lib/libc.so does not exist, or it is not a valid symlink to
     * a versioned file (e.g. /lib/libc.so.6).
     */
    public static String matchLibrary(final String libName, List<String> searchPath) {
    	File lib = new File(libName);
        if (lib.isAbsolute()) {
            searchPath = Arrays.asList(new String[] { lib.getParent() });
        }
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return (filename.startsWith("lib" + libName + ".so")
                        || (filename.startsWith(libName + ".so")
                            && libName.startsWith("lib")))
                    && isVersionedName(filename);
            }
        };

        List<File> matches = new LinkedList<File>();
        for (Iterator<String> it = searchPath.iterator(); it.hasNext(); ) {
            File[] files = new File((String) it.next()).listFiles(filter);
            if (files != null && files.length > 0) {
                matches.addAll(Arrays.asList(files));
            }
        }

        //
        // Search through the results and return the highest numbered version
        // i.e. libc.so.6 is preferred over libc.so.5
        double bestVersion = -1;
        String bestMatch = null;
        for (Iterator<File> it = matches.iterator(); it.hasNext(); ) {
            String path = ((File) it.next()).getAbsolutePath();
            String ver = path.substring(path.lastIndexOf(".so.") + 4);
            double version = parseVersion(ver);
            if (version > bestVersion) {
                bestVersion = version;
                bestMatch = path;
            }
        }
        return bestMatch;
    }

	
    public static double parseVersion(String ver) {
    	double v = 0;
    	double divisor = 1;
    	int dot = ver.indexOf(".");
    	while (ver != null) {
    		String num;
    		if (dot != -1) {
    			num = ver.substring(0, dot);
    			ver = ver.substring(dot + 1);
        		dot = ver.indexOf(".");
    		}
    		else {
    			num = ver;
    			ver = null;
    		}
    		try {
    			v += Integer.parseInt(num, 36) / divisor;
    		}
    		catch(NumberFormatException e) {
    			return 0;
    		}
    		divisor *= 100;
    	}

    	return v;
    }
    
    public static boolean isVersionedName(String name) {
        if (name.startsWith("lib")) {
            int so = name.lastIndexOf(".so.");
            if (so != -1 && so + 4 < name.length()) {
                for (int i=so+4;i < name.length();i++) {
                    char ch = name.charAt(i);
                    if (!Character.isLetterOrDigit(ch) && ch != '.') {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
	public static void log(String s) {
		if (logger != null)
			logger.log(s);
		else
			System.err.println(s);		
	}
	public static interface Logger {
		public void log(String str);
	}

	public static class DefaultLogger implements Logger {
		public void log(String str) {
			System.err.println(str);
		}
	}
    /*
	public static Object getFFmpegInterface(Class clazz) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		initLibrary();
		Class objClass =  FFmpegClassGenerator.inst.processFFmpegClass(clazz.getCanonicalName());
		return objClass.newInstance();
	}
	*/
}

package com.unwiredappeal.tivo.streambaby;

//import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.interfaces.IApplication;
import com.tivo.hme.interfaces.IArgumentList;
import com.tivo.hme.interfaces.IHttpRequest;
import com.tivo.hme.sdk.FactoryPlus;
import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.ImageResource;
import com.tivo.hme.sdk.Resolution;
import com.tivo.hme.sdk.ResolutionInfo;
import com.unwiredappeal.mediastreams.AutoPreviewGenerationManager;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.InfoCache;
import com.unwiredappeal.tivo.utils.NamedStream;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.modules.VideoFormats;

public class StreamBabyStream extends BApplicationPlus implements Cleanupable {
	
    
	private BView currentScreen;
    public boolean simulator = false;
    public String pw;
    private DirEntry builtRoot;
    public int autoQuality = VideoFormats.QUALITY_SAME;
	Map<String, String> persistentCache = Collections.synchronizedMap(new HashMap<String, String>());
	public Vector<Object> lastEntry = new Vector<Object> ();
	public ImageResource filmResource;
	public List<Cleanupable> cleanupRequired = Collections.synchronizedList(new LinkedList<Cleanupable>());
 
	private Map<String, Object >appObjectMap = Collections.synchronizedMap(new HashMap<String, Object>());
	private Map<String, FontResource > fontMap = new HashMap<String, FontResource>();
	/*
	public void init(IContext context) throws Exception {
		
		super.init(context);
	    //DIR = topDir.firstElement();
		curDir = EntryManager.getRootDirEntry();
	    screen = new SelectionScreen(this, curDir);
	    push(screen, TRANSITION_NONE);

	}
	*/
	
	public void init() {
		super.init();

		builtRoot = null;
		if (GLOBAL.film_background != null && GLOBAL.film_background.length() > 0)
			filmResource = this.createImage(GLOBAL.film_background);

		Resolution x = this.getResolutionInfo().getCurrentResolution();
		Log.debug("width: " + x.getWidth());
		Log.debug("height: " + x.getHeight());
		Log.debug("aspect: " + x.getPixelAspectNumerator() + "/" + x.getPixelAspectDenominator());

		first();
	}
	

	
	
	public static int MAX_LEVEL = 100;
	private void buildEntries() {
		builtRoot = StreamBabyConfig.inst.buildRootDirEntry();
		builtRoot.setIsRoot(true);
		//builtRoot.setUserPassword(getPassword());
		//recursiveFill(builtRoot, 0, getPassword(), MAX_LEVEL);
	}
	
	
	public DirEntry getRootEntry() {
		if (builtRoot == null)
			buildEntries();

		return builtRoot;
		
	}
	public String getPassword() {
		return (pw == null) ? "" : pw;
	}
	
	public void setPassword(String pw) {
		this.pw = (pw == null) ? "" : pw;
		if (StreamBabyConfig.cfgSavePassword.getBool())
			getContext().setPersistentData("pw", this.pw);		
	}
	public void first() {
		if (StreamBabyConfig.cfgSavePassword.getBool())
			pw = getContext().getPersistentData("pw");
		else
			pw = null;
	    if (pw == null)
	    	pw = "";
//	    if (false && GLOBAL.Password != null && GLOBAL.Password.length() > 0 && GLOBAL.Password.compareToIgnoreCase(pw) == 0) {
//			DirEntry curDir = EntryManager.getRootDirEntry();
//		    SelectionScreen screen = new SelectionScreen(this, curDir);
//		    push(screen, TRANSITION_NONE);
//	    } else {
	    	PasswordScreen ps = new PasswordScreen(this);
	    	push(ps, TRANSITION_NONE, pw);
//	    }

	}
	
	public void  setCurrentScreen(BView sc) {
		currentScreen = sc;
	}
	
	public boolean appIdle() {
		if (currentScreen instanceof IdleHandler) {
			IdleHandler h = (IdleHandler)currentScreen;
			return h.isIdle();
		}
		// Assume we are idle
		return true;
	}
	public boolean handleIdle(boolean isIdle) {
	    if (isIdle && !appIdle()) {
	        getApp().acknowledgeIdle(true);
	    }

	    return true;
	}
	
	public void popTop() {
		while (getStackDepth() != 1) {
			pop();
		}
	}
	
	public int getAutoQuality() {
		return autoQuality;
	}
	


   public static class StreamBabyStreamFactory extends FactoryPlus {
	   	  private Timer timer = new Timer();
	   	  public StreamBabyStreamFactory() {
	   		  super();
	   	  }
	   	  
	   	 
	   	  @Override
	   	  protected void addHeaders(IHttpRequest http, String uri) {
		         if (uri.endsWith(NamedStream.NAMED_STREAM_EXT)) {
		        	 NamedStream stream = NamedStream.getNamedStream(uri);
		        	 if (stream != null) {
		        		 try {
			        		 long dur = stream.getDuration();
			        		 if (dur > 0)
									http.addHeader("X-TiVo-Accurate-Duration", Long.toString(dur));
							String ctype = stream.getContentType();
							if (ctype != null && ctype.length() > 0) {
								http.addHeader("Content-Type", ctype);
							}
		        		 } catch(IOException e) {
		        			 e.printStackTrace();
		        		 }
		        	 }
		         }
	   		  
	   	  }
	      @Override
	      public InputStream getStream(String uri) throws IOException {
	         Log.debug("uri=" + uri);
	         
	         
	         //boolean isStreamFile = false;
	         /*
	         for (int i=0; i<GLOBAL.extList.length; i++) {
	            if (uri.toLowerCase().endsWith(GLOBAL.extList[i].toLowerCase())) {
	               isStreamFile = true;
	            }
	         }
	         */
	         if (uri.endsWith(NamedStream.NAMED_STREAM_EXT)) {
	        	 NamedStream stream = NamedStream.getNamedStream(uri);
	        	 if (stream == null)
	        		 throw new IOException("Stream Not Found");
	        	 
	        	 return stream;
	         } else {
	        	 File f = new File(StreamBabyConfig.streamBabyDir + "/assets/" + uri);
	        	 if (f.exists() && f.isFile())
	        		 return f.toURL().openStream();
		         return super.getStream(uri);
	        	 
	         }
	      }
	      @Override
	      public void removeApplication(IApplication app) {
	    	  if (app instanceof Cleanupable) {
	    		  Cleanupable myApp = (Cleanupable)app;
	    		  myApp.cleanup();
	    	  }
	    	  super.removeApplication(app);
	      }
	      @Override
	      public void initFactory(String appClassName, ClassLoader loader, IArgumentList args) {
	    	  super.initFactory(appClassName, loader, args);
	    	  
	    	  String configFile = args.getValue("config", null);
	    	  StreamBabyConfig.inst.readConfiguration(configFile);
	    	  this.title = StreamBabyConfig.cfgTitle.value;
	    	  scheduleTasks();
	    	  //fillVidInfoCache();
	      }
	      
	      /*
	      public static void fillVidInfoCache() {
	    	  // Cheap way to do this...  Just load up all the directories, and call getVideoInformation();
	  		DirEntry root = StreamBabyConfig.inst.buildRootDirEntry();
	  		// a null password always returns all entries..
	  		Log.info("Caching video information...");
	  		root.recursiveFill(0, null, MAX_LEVEL, true);
			Log.info("Video caching complete.");
	      }
	      */
	      private void scheduleTasks() {
	    	  InfoCache.getInstance().schedule(timer);
	    	  AutoPreviewGenerationManager.inst.scheduleFirst(timer);
	    	  
	      }
	   }	

   @Override
   public void rootBoundsChanged(Rectangle rootBounds) {
       super.rootBoundsChanged(rootBounds);
       //setBackground();
   }
   
   /*
   protected void setBackground() {
       //
       // If we are running in the simulator display jpg.
       //
       if (simulator) {
           getRoot().setResource(new Color(0, 0, 55));
           
           int offset = (getCurrentResolution().getHeight() == 720) ? 54 : 40;
           
           int safeTitleH = getSafeTitleHorizontal();
           int safeTitleV = getSafeTitleVertical();
           if (innerSimView == null) {
               innerSimView = new BView(getBelow(), safeTitleH, safeTitleV+offset, 
                       getWidth()-safeTitleH, getHeight()-2*safeTitleV-offset);
           } else {
               innerSimView.setBounds(safeTitleH, safeTitleV+offset, 
                       getWidth()-safeTitleH, getHeight()-2*safeTitleV-offset);
           }
           innerSimView.setResource(new Color(0, 0, 33));
       } else {
           getBelow().setResource("blue.mpg");
       }
   }
	*/

   /**
    * Every screen handle the "pop" action in the same way, so we will handle
    * this in the application.
    */
   public boolean handleAction(BView view, Object action) 
   {
	   /*
       if (action.equals("pop")) {
           pop();
           return true;
       } 
       */       
       return super.handleAction(view, action);
   }
   @Override
   protected Resolution getPreferredResolution(ResolutionInfo resInfo) {
       Resolution r= new Resolution("640x480-PAR=1/1");

       int y = StreamBabyConfig.cfgHmeRes.getInt();
       if (y > 0) {
    	   Iterator<Resolution> it = resInfo.getSupportedResolutions().iterator();
    	   while(it.hasNext()) {
    		   Resolution ir = it.next();
    		   if (ir.getHeight() == y && ir.getPixelAspectDenominator() == ir.getPixelAspectNumerator()) {
		       GLOBAL.y_res = y;
		       return ir;
		   }
    	   }
       }
       GLOBAL.y_res = 480;

       return r;
   }

   
   public void handleSimulatorEvent() {
	   com.tivo.hme.host.sample.Listener.DEBUG  = StreamBabyConfig.inst._DEBUG;
   }
   /**
    * This event handler looks for device info in order to substitute a jpeg
    * image as a background instead of the mpeg. Without this the root view in
    * the simulator the root would be left with a black background.
    *TRANSITION_LEFT
    * This code and extra jpeg image are meant to be removed from a shipping
    * application!
    *
    * REMIND: This is only needed by developers while using the simulator!
    */
   public boolean handleEvent(HmeEvent event)
   {
       switch (event.getOpCode()) {
         case EVT_DEVICE_INFO:
           HmeEvent.DeviceInfo info = (HmeEvent.DeviceInfo)event;
           simulator =((String)info.getMap().get("platform")).startsWith("sim-");
           if (simulator)
        	   handleSimulatorEvent();
           //setBackground();
           break;
       }
       return super.handleEvent(event);
   }
   
   public boolean isSimulator() {
	   return simulator;
   }
   
	      
	   public boolean handleApplicationError(int code, String message) {
	      Log.debug("code=" + code + " message=" + message);
	      if (code != 3 && code != 4) {
	         System.out.println("ERROR code=" + code + " Message: " + message);
	      }
	      return false;
	   }


	public void flushPersistentCacheObject(String name, boolean remove) {
		String val = persistentCache.get(name);
		if (remove)
			persistentCache.remove(name);
		if (val != null) {
			getContext().setPersistentData(name, val);
		}
	}
	
	public void flushPersistentCache() {
		Set<String> keys = persistentCache.keySet();
		Iterator<String> it = keys.iterator();
		while(!persistentCache.isEmpty()) {
			flushPersistentCacheObject(it.next(), false);
			it.remove();
		}
	}
	public void cachePersistentData(String name, String obj,
			boolean saveCache) {
		persistentCache.put(name, obj);
		if (saveCache) {
			flushPersistentCacheObject(name, true);
		}
		
	}
	
	public void addCleanupRequired(Cleanupable c) {
		synchronized(cleanupRequired) {
			if (!cleanupRequired.contains(c))
				cleanupRequired.add(c);
		}
	}
	
	public void removeCleanupRequire(Cleanupable c) {
		synchronized(cleanupRequired) {
			if (cleanupRequired.contains(c))
					cleanupRequired.remove(c);
		}
	}
	
	public void cleanup() {
		synchronized(cleanupRequired) {
			while(cleanupRequired.size() > 0) {
				Cleanupable c = cleanupRequired.get(cleanupRequired.size()-1);
				c.cleanup();
				cleanupRequired.remove(c);
			}
		}
		
		flushPersistentCache();

	}


	public Object getApplicationObject(String str) {
		return appObjectMap.get(str);
	}
	
	public void setApplicationObject(String str, Object o) {
		appObjectMap.put(str, o);
	}


	public synchronized FontResource getFont(String fontName, int fontFace, int fontSize) {
		String id = fontName + "-" + fontFace + "-" + fontSize;
		FontResource r = fontMap.get(id);
		if (r != null)
			return r;
		r = (FontResource)createFont(fontName, fontFace, fontSize, FONT_METRICS_BASIC|FONT_METRICS_GLYPH);
		fontMap.put(id, r);
		return r;
	}
	
}

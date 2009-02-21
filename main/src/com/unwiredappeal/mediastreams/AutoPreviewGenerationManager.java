package com.unwiredappeal.mediastreams;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.unwiredappeal.mediastreams.ZipGeneratingPreview.GeneratorException;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.streambaby.Cleanupable;
import com.unwiredappeal.tivo.streambaby.PreviewCacheUtils;
import com.unwiredappeal.tivo.streambaby.PreviewWindow;
import com.unwiredappeal.tivo.streambaby.ShutdownHook;
import com.unwiredappeal.tivo.streambaby.PreviewCacheUtils.UriCallback;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.modules.VideoModuleHelper;

public class AutoPreviewGenerationManager implements Cleanupable {

	public static long lastDelete = 0;
	public boolean isShuttingDown = false;
	public static AutoPreviewGenerationManager inst = new AutoPreviewGenerationManager();
	private HashMap<URI, ZipGeneratingPreview>  genMap = new HashMap<URI, ZipGeneratingPreview>();
	private ZipGeneratingPreview runningGenerator = null;
	
	protected AutoPreviewGenerationManager() {
		ShutdownHook.getShutdownHook().addCleanupRequired(this);
	}
	public synchronized boolean hasRunning() {
		if (runningGenerator != null && runningGenerator.isRunning())
			return true;
		if (runningGenerator != null)
			runningGenerator.close();
		runningGenerator = null;
		return false;
	}
	public synchronized void offer(ZipGeneratingPreview zpw) {
		if (isShuttingDown)
			return;
		if ((StreamBabyConfig.cfgContinueGenerate.getBool() ||StreamBabyConfig.cfgBackgroundGenerate.getBool()) && zpw.isRunning() && (runningGenerator == null || !runningGenerator.isRunning())) {
			if (runningGenerator != null) {
				runningGenerator.close();
			}
			zpw.addInstance();
			runningGenerator = zpw;
		}
	}
	public synchronized PreviewGenerator getPreviewer(DirEntry de, int sw, int sh, boolean tryGenerate) {
	  URI uri = de.getUri();
	  VideoInformation vinfo = de.getVideoInformation();
  	  ZipPreviewer generator = new ZipPreviewer();
  	  if (generator.open(uri, vinfo, sw, sh))
  		  return generator;
  	  if (genMap.containsKey(uri)) {
  		  ZipGeneratingPreview zgp = genMap.get(uri);
  		  zgp.addInstance();
  		  return zgp;
  	  }
  	  
  	PreviewGenerator gen = null;
  	  if (tryGenerate && (StreamBabyConfig.cfgAutoGeneratePreview.getBool() || StreamBabyConfig.cfgBackgroundGenerate.getBool())) {
  		  try {
  			  if (runningGenerator != null && runningGenerator.isRunning()) {
  				  runningGenerator.close();
  				  runningGenerator = null;
  			  }
  			  gen = VideoModuleHelper.inst.getPreviewHandler(de, false);
  			  if (gen == null)
  				  return null;
  			  ZipGeneratingPreview zpw = new ZipGeneratingPreview(uri, gen, vinfo, sh, sh);
  			  genMap.put(uri, zpw);
  			  return zpw;
  		  } catch (GeneratorException e) {
  			  if (gen != null)
  				  gen.close();
		}
  	  }
	return null;
	}
	public synchronized void remove(URI uri) {
		genMap.remove(uri);
	}
	
	public void schedule(Timer t) {
		if (StreamBabyConfig.cfgBackgroundGenerate.getInt() != 0)
			t.schedule(new PreviewTask(t), StreamBabyConfig.cfgBackgroundGenerate.getInt()*60*1000L);
	}
	public void scheduleFirst(Timer t) {
		if (StreamBabyConfig.cfgBackgroundGenerate.getInt() != 0)
			t.schedule(new PreviewTask(t), 20000);
	}
	
	public static void possiblyGenerate(DirEntry de) {
		if (AutoPreviewGenerationManager.inst.hasRunning())
			return;
		boolean canRealtimePreview = VideoModuleHelper.inst.canPreview(de, true);
		if (!canRealtimePreview) {
			if (!Utils.isFile(de.getUri()))
				return;
			File f= new File(de.getUri());
			if (new File(StreamBabyConfig.cacheDir, f.getName() + ".pvw").exists())
				return;
			if (new File(StreamBabyConfig.cacheDir, ZipPreviewer.getCacheFile(f, "").getName()).exists())
				return;
			//if (new File(StreamBabyConfig.cacheDir, ZipPreviewer.getCacheFile(f, "partial").getName()).exists())
				//return;			
			Log.info("Autogenerating preview for file: " + f.getAbsolutePath());
			PreviewGenerator gen = AutoPreviewGenerationManager.inst.getPreviewer(de, PreviewWindow.small_PREVIEW_WIDTH, PreviewWindow.small_PREVIEW_HEIGHT, true);
			// We can close it immediately, as it will offer itself up to keep running
			if (gen != null)
				gen.close();
		}
	}

	public class PreviewTask extends TimerTask {
		public Timer timer;
		public PreviewTask(Timer t) {
			timer = t;
		}
		
		@Override
		public void run() {
			if (StreamBabyConfig.cfgDeletePreviews.getBool() && (System.currentTimeMillis()-lastDelete > 10*60*1000L))
					PreviewCacheUtils.cleanup(false);
			if (AutoPreviewGenerationManager.inst.hasRunning() == false) {
				DirEntry de = StreamBabyConfig.inst.buildRootDirEntry();
				PreviewCacheUtils.recursiveFill(de, 0, new UriCallback() {
					public void callback(DirEntry de) {
						possiblyGenerate(de);
					}
					
				});

			}
			schedule(timer);
		}
		
	}

	public synchronized void cleanup() {
		isShuttingDown = true;
		if (runningGenerator != null && runningGenerator.isRunning()) {
			  runningGenerator.close();
			  runningGenerator = null;
		}
		Iterator<ZipGeneratingPreview> it = genMap.values().iterator();
		while(it.hasNext()) {
			ZipGeneratingPreview pvw = it.next();
			pvw.forceClose();
		}
	}
}
	



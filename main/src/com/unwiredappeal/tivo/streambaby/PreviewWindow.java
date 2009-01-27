// $Id: StatusBar.java 10 2008-09-16 08:13:42Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.streambaby;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;


import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BViewPlus;
import com.tivo.hme.sdk.Resolution;
import com.tivo.hme.sdk.Resource;
import com.unwiredappeal.mediastreams.CachingPreviewGenerator;
import com.unwiredappeal.mediastreams.PreviewGenerator;
import com.unwiredappeal.mediastreams.AutoPreviewGenerationManager;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.videomodule.VideoModuleHelper;

public class PreviewWindow extends BViewPlus {

   public static int small_PREVIEW_WIDTH = 352;
   public static int small_PREVIEW_HEIGHT = 288;
   public static final int small_width = small_PREVIEW_WIDTH + 20;
   public static final int small_height = small_PREVIEW_HEIGHT + 20;
   public static final int small_x = (640-small_width)/2;
   public static final int small_y = ((480-small_height)/2) - 50;
   
   public static int big_PREVIEW_WIDTH = 640;
   public static int big_PREVIEW_HEIGHT = 480;
   public static final int big_width = big_PREVIEW_WIDTH;
   public static final int big_height = big_PREVIEW_HEIGHT;
   public static final int big_x = (640-big_width)/2;
   public static final int big_y = ((480-big_height)/2);

   public Boolean visible = false;
   public int currentDisplayFrame = -1; 
   private BText timeText = null;
   private long pos;
   private long nextFrame;
   
   public static boolean threadedCacher = true;
	public static boolean defaultPredictive = false;
	public static boolean displayTimeAlways = true;

 
   Resource img = null;
   PreviewGenerator generator = null;
   private static int 	  disablePreview = StreamBabyConfig.cfgDisablePreview.getInt();
   
   protected PreviewWindow(BView parent, DirEntry de, PreviewGenerator ge, int x, int y, int width, int height) {
	  super(parent, x, y, width, height);
	  this.setVisible(false);
	  visible = false;
	  Log.debug("pWidth: " + parent.getWidth());
      Log.debug("preview parent=" + parent);
      setResource(Color.DARK_GRAY);

      this.generator = ge;
	  if (disablePreview < 2) {
          timeText = new BText(this, 0, 0, width, height);
          timeText.setColor(GLOBAL.text_COLOR);
          timeText.setShadow(GLOBAL.text_SHADOW,3);
          String font = String.format("default-%d.font", 50);
          timeText.setFont(font);
	  }
	  hideText();
 

   }
   
   public static PreviewWindow getPreviewWindow(BView parent, DirEntry de) {
	   PreviewGenerator generator = null;
	   boolean useBig = StreamBabyConfig.cfgPreviewBig.getBool();
	   int PREVIEW_WIDTH = useBig ? big_PREVIEW_WIDTH : small_PREVIEW_WIDTH;
	   int PREVIEW_HEIGHT = useBig ? big_PREVIEW_HEIGHT : small_PREVIEW_HEIGHT;
	   int width = useBig ? big_width : small_width;
	   int height = useBig ? big_height : small_height;
	   int x = useBig ? big_x : small_x;
	   int y = useBig ? big_y : small_y;

	   boolean is16x9 = true;
		List<Resolution> reses = ((StreamBabyStream)parent.getBApp()).getResolutionInfo().getSupportedResolutions();
		Iterator<Resolution> it = reses.iterator();
		while(it.hasNext()) {
			Resolution r = it.next();
			if (r.getWidth() == 1280 && r.getHeight() == 720 && (r.getPixelAspectDenominator() != r.getPixelAspectNumerator())) {
				is16x9 = false;
				break;
			}
		}

	   float aspect = de.getVideoInformation().getAspect();
	   if (is16x9) {
		   aspect = aspect / 1.33f;
	   }
	   int sh = PREVIEW_HEIGHT;
	   int sw = (int)(aspect*sh);

	   if (sw > PREVIEW_WIDTH) {
		   sw = PREVIEW_WIDTH;
		   sh = (int)((1/aspect)*sw);
	   }
	   
	if (disablePreview == 0) {
	   	if (generator == null) {
	   		  generator = AutoPreviewGenerationManager.inst.getPreviewer(de.getUri(), de.getVideoInformation(), sw, sh, false);
	    	  //generator = new ZipPreviewer();
	    	  //if (!generator.open(de.getUri(), de.getVideoInformation(), sw, sh))
	    		//  generator = null;
	      }
	      if (generator == null) {
	    	  generator = VideoModuleHelper.inst.getPreviewHandler(de.getUri(), de.getVideoInformation(), true);
	    	  if (generator != null) {
	    		  generator = new CachingPreviewGenerator(generator, de.getVideoInformation(), threadedCacher, defaultPredictive);
	    		  if (!generator.open(de.getUri(), de.getVideoInformation(), sw, sh))
	    			  generator = null;
	    	  }
	      }
	      if (generator == null) {
	    	  // try zip file again, this time allow auto-generation
	   		  generator = AutoPreviewGenerationManager.inst.getPreviewer(de.getUri(), de.getVideoInformation(), sw, sh, true);
	      }
	      
	}

      // For no generator, use the small window
      if (generator == null) {
   	   PREVIEW_WIDTH = small_PREVIEW_WIDTH;
	   PREVIEW_HEIGHT = small_PREVIEW_HEIGHT;
	   width = small_width;
	   height = small_height;
	   x = small_x;
	   y = small_y;
    	  
      }
      
      return new PreviewWindow(parent, de, generator, x, y, width, height);
   
   }

   public void setTimeText(int posSecs) {
	   int p = posSecs;
       int hours; 
       int mins; 
       int secs;
       hours = (int)p/3600;
       mins = (int)p/60 - (hours*60);
       secs = (int)p % 60;
       timeText.setValue(String.format("%02d:%02d:%02d",hours,mins,secs));	   
   }
   
   public boolean setPreviewFrame(int posSecs) {
	   int frame = posSecs;


	   Resource oldImg = img;
	   boolean foundImage = false;
	   
	   img = null;
	   byte[] data = generator.getFrameImageData(frame);
	   if (data != null)
		   img = this.createImage(data, 0, data.length);
	   if (img != null) {
		   foundImage = true;
		   setResource(img);
	   } else { 
		   setResource(Color.black);
	   }
	   if (oldImg != null) {
		   oldImg.remove();
	   }
	   return foundImage;
   }
   
   public synchronized void setPosition(long p) {
	   pos = p;
	   int f = (int)(p/1000L);
	   if (currentDisplayFrame == f)
		   return;
	   currentDisplayFrame = f;
	   boolean foundImage = false;
	   if (generator != null) 
		   foundImage = setPreviewFrame(f);	   
	   if (timeText != null)
		   setTimeText(f);
	   if (!foundImage)
		   showText();
	   else
		   hideText();
   }
   
   public void showText() {
	   if (timeText != null)
		   timeText.setVisible(true);
   }

   public void hideText() {
	   if (displayTimeAlways)
		   return;
	   if (timeText != null)
		   timeText.setVisible(false);
   }

   public synchronized long getPosition() {
	   return pos;
   }
   
   public void makeVisible(Boolean state) {
	   if (disablePreview < 2) {
		   setVisible(state);
		   visible = state;
	   }
   }
      
   public void remove() {
	   if (generator != null)
		   generator.close();
	  generator = null;
      if (timeText != null)
    	  timeText.remove();
      timeText = null;
      if (img != null)
    	  img.remove();
      img = null;
      super.remove();
      /*
	   bar.remove();
	   main.remove();
	   textDUR.setValue(null);
	   textDUR.clearResource();
	   textDUR.remove();
	   textPOS.setValue(null);
	   textPOS.clearResource();
	   textPOS.remove();
	   textPOSBG.clearResource();
	   textPOSBG.remove();
	   */
   }

	public synchronized long getNextFrame() {
		return nextFrame;
	}
	
	public synchronized void setNextFrame(long p, int delta) {
		if (generator != null)
			generator.prepare((int)(p/1000), delta/1000);
		nextFrame = p;
	}
}

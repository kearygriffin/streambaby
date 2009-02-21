// $Id: StatusBar.java 10 2008-09-16 08:13:42Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.streambaby;

import static com.tivo.hme.bananas.IBananasPlus.H_SHUTTLEBAR_MODE_PREFIX;
import static com.tivo.hme.bananas.IBananasPlus.P_PAD_H;
import static com.tivo.hme.bananas.IBananasPlus.P_PAD_V;

import com.tivo.hme.bananas.BShuttleBarPlus;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BSkin.Element;
import com.tivo.hme.sdk.Resource;
import com.unwiredappeal.tivo.utils.Log;

public class NewStatusBar extends BShuttleBarPlus {

   public Boolean visible = false;
   int progressX;
   int bufferStart;
   int bufferEnd;
   
   public NewStatusBar(BView parent) {
       super(parent,  (parent.getWidth() - (parent.getWidth() * 4 / 5)) / 2,
	     parent.getHeight() - GLOBAL.statusBG_Y_from_bottom,
	     (parent.getWidth() * 4 / 5), false);
       progressX = progress.getX() ;      
   }
   
   public void makeVisible(Boolean state) {
	   Log.debug("state=" + state);
	   setVisible(state);
	   visible = state;
   }

   public void Update(long pos) {
	   Update(pos, -1, -1, -1);
   }
   public void Update(long pos, long dur) {
	   Update(pos, dur, -1, -1);
   }
   
   

   public void Update(long pos, long dur, long bs, long be) {
      //debug.print("pos=" + pos + " dur=" + dur);
	   boolean doRefresh = false;
	   int ipos = (int)(pos / 1000);
	   int idur = (int)((dur+999) / 1000);
	   int ibs= (int)(bs/1000);
	   int ibe = (int)(be/1000);
		int oldValue = this.value;
		int oldShuttleValue = this.shuttleValue;
		if (pos != -1)
			this.value = Math.min(Math.max(ipos, minValue), maxValue);
		if (isShuttleSnapToValue()) {
			this.shuttleValue = this.value;
		}
		if (oldShuttleValue != this.shuttleValue || oldValue != this.value)
			doRefresh = true;
	   
	   if (dur != -1) {
		    int oldMin = this.minValue;
		    int oldMax = this.maxValue;
			this.minValue = 0;
			this.maxValue = idur;
	        if (format != null) {
	            minText.setValue(format.format(minValue));
	            maxText.setValue(format.format(maxValue));
	        }
	        if (oldMin != this.minValue || oldMax != this.maxValue)
	        	doRefresh = true;
	   }
	   if (bs != -1 && be != -1) {
		   int oldBs = bufferStart;
		   int oldBe = bufferEnd;
		   this.bufferEnd = ibe;
		   this.bufferStart = ibs;
		   if (oldBs != this.bufferStart || oldBe != this.bufferEnd)
			   doRefresh = true;
	   }
	   
	   if (doRefresh)
		   refresh();
   }
   
	protected void refresh() {
		setPainting(false);
		try {
			Resource anim = (animation != null) ? getResource(animation) : null;
			
			//set the progress meter
			float range = maxValue-minValue;
			float progressRange = bufferEnd - bufferStart;
			float progressPercent = progressRange / range;
			float offsetPercent = (float)bufferStart / range;
			progress.setLocation(progressX + (int)(progressWidth*offsetPercent), progress.getY(), anim);
			progress.setSize((int)(progressWidth*progressPercent), progress.getHeight(), anim);

			float percent = value/range;
			
			//set the shuttle location
			percent = shuttleValue/range;
			int offset = progressX - shuttle.getWidth()/2;
			shuttle.setLocation(offset + (int)(progressWidth*percent), shuttle.getY(), anim);
			
			//set the tick spacing
			percent = tickSpacing/range;
			int tickTileWidth=(int)(progressWidth*percent);
			if (ticks.getTileWidth() != tickTileWidth) {
				//resize the tick marks
				ticks.setTileSize(tickTileWidth, ticks.getTileHeight());
			}
			
			//set the mode
			Element e = null;
			if (mode != null) {
				try {
					e = getBApp().getSkin().get(H_SHUTTLEBAR_MODE_PREFIX + mode);
				} catch (Exception t) {
					//the mode doesn't exist
				}
			}
			if (e != null) {
				Resource modeRes = e.getResource();
				if (modeRes != shuttleMode.getResource()) {
					//the mode has changed
					int padH = e.getInt(P_PAD_H, Integer.MIN_VALUE);
					int padV = e.getInt(P_PAD_V, Integer.MIN_VALUE);
					if (padH == Integer.MIN_VALUE) {
						padH = (shuttle.getWidth() - e.getWidth())/2;
					}
					if (padV == Integer.MIN_VALUE) {
						padV = (shuttle.getHeight() - e.getHeight())/2;
					}
					shuttleMode.setBounds(padH, padV, e.getWidth(), e.getHeight());
					shuttleMode.setResource(modeRes);
				
					if (!shuttleMode.getVisible()) {
						shuttleMode.setVisible(true);
					}
				}
			} else {
				if (shuttleMode.getVisible()) {
					shuttleMode.setVisible(false);
				}
			}
			
			//set the text value
            if (format != null) {
                minText.setValue(format.format(minValue));
                maxText.setValue(format.format(maxValue));
                valueText.setValue(format.format(value));
            } else {
                minText.setValue(null);
                maxText.setValue(null);
                valueText.setValue(null);
            }
			
		} finally {
			setPainting(true);
		}
	}

      
   public void remove() {
      Log.debug("");
      super.remove();
   }
}

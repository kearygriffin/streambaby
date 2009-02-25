// $Id: ScreenTemplate.java 9 2008-08-30 17:34:11Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.streambaby;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BScreenPlus;
import com.tivo.hme.bananas.BText;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.views.VFont;

public class ScreenTemplate extends BScreenPlus {

	BText titleText;
   public ScreenTemplate(BApplicationPlus app) {
       super(app);
       resetBackground();
       //
       // set the title of the screen
       //
       float leftmul = 1.75f;
       float rightmul = 1f;
       
       int safeTitleH = (int)(getBApp().getSafeTitleHorizontal()*leftmul);
       int width = (int)((getWidth()-safeTitleH) - getBApp().getSafeTitleHorizontal()*rightmul);
       int safeTitleV = getBApp().getSafeTitleVertical();
       int fs = VFont.getFontSize(this, "medium");
       BText title = new BText(getNormal(), safeTitleH, safeTitleV, width, fs*2+(fs/2));
       title.setValue(this.toString());
       title.setColor(getTitleColor());
       title.setShadow(getTitleShadowColor(), getTitleShadowOffset());
       title.setFlags(RSRC_VALIGN_TOP|RSRC_HALIGN_CENTER|RSRC_TEXT_WRAP);
       
       title.setFont("default-" + fs + ".font");
       titleText = title;
       /*
      titleText = new VText(
    	         getNormal(), SAFE_TITLE_H, SAFE_TITLE_V-20, 1, "title"
    	      );
    	      //title.setValue(t);
      titleText.setColor(GLOBAL.title_COLOR);
      titleText.setFlags(RSRC_VALIGN_TOP | RSRC_HALIGN_CENTER);
	*/

   }
   
   public void resetBackground() {
  	  getBelow().setResource(StreamBabyConfig.getBackgroundImage(this.getBApp()));
	      if (this.toString() != null) {
	          // Set background and title
	          //getBelow().setResource(GLOBAL.BACKGROUND_PICTURE); 
	          setTitle(this.toString());
	       }
   }
    
   // Set the title of the window in a safe area of the screen.
   
   public void setTitle(String t) {

      if (titleText != null)
    	  titleText.setValue(t);
   }
  
   // Pass key events up to parent class
   public boolean handleKeyPress(int code, long rawcode) {  
      return super.handleKeyPress(code, rawcode);
   }      
   
   @Override
   protected void viewRemoveNotify() {
       super.viewRemoveNotify();
   }
   
   public void play(String snd) {
	   getBApp().play(snd);
   }
}

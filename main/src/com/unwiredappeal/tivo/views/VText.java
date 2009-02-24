// $Id: VText.java 10 2008-09-16 08:13:42Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.views;

import com.tivo.hme.bananas.BApplication;
import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.bananas.BTextPlus;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.Resource.FontResource;
import com.unwiredappeal.tivo.streambaby.GLOBAL;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.streambaby.StreamBabyStream;

public class VText extends BTextPlus<String> {
   public boolean visible = true;
   public VText(BView view, int x, int y, int width, int height, String type) {
	   super(view, x, y, width, height, true);
	   FontSizeInfo info = getFontSize(this, type);
	   setupText(this, type, info.fontSize);

   }

   public VText(BView view, int x, int y, int h_multiplier, String type) {
	 super(view, x, y, view.getScreen().getWidth()-(BScreen.SAFE_TITLE_H*2), getFontSize(view, type).h*h_multiplier+(getFontSize(view, type).h), true);
      setupText(this, type, getFontSize(this, type).fontSize);
   }

   public static class FontSizeInfo {
	   int h;
	   int fontSize;
	   public FontSizeInfo(int h, int fontSize) {
		   this.h = h;
		   this.fontSize = fontSize;
	   }
   }
   public static FontSizeInfo getFontSize(BView view, String type) {
       //text = new BText(view, x, y, w, h*h_multiplier);
       int fontSize = VFont.getFontSize(view, type);
       return new FontSizeInfo(fontSize, fontSize);
   }
   public static FontResource setupText(BTextPlus<String> t, String type, int fontSize) {
	      t.setColor(GLOBAL.text_COLOR);
	      t.setShadow(GLOBAL.text_SHADOW,3);
	      //String font = String.format("default-%d.font", fontSize);
	      FontResource font = getFontResource(t, "default.ttf", FONT_PLAIN, fontSize);
	      t.setFont(font);
	      return font;
	   
   }
   
   public static FontResource getFontResource(BView v, String fontName, int style, int size) {
	      FontResource font = ((StreamBabyStream)v.getBApp()).getFont(fontName, style, size);
	      return font;
   }
   
   public void setVisible(boolean visible) {
      this.visible = visible;
      super.setVisible(visible);
   }
   
   public boolean isVisibile() {
	   return this.visible;
   }
   
   public void remove() {
	  clearResource();
	  super.remove();

   }   
}
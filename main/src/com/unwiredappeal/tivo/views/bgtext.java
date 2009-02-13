package com.unwiredappeal.tivo.views;

import java.awt.Color;

import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.HmeEvent.FontInfo;
import com.tivo.hme.sdk.HmeEvent.FontInfo.GlyphInfo;
import com.tivo.hme.sdk.Resource.FontResource;
import com.unwiredappeal.tivo.streambaby.StreamBabyStream;

public class bgtext {
   FontInfo fm = null;
   BText text;
   BView bg;
   public int x, y, w, h, fontSize;

   public bgtext(BView view, int fontSize, String value) {
	   this.fontSize = fontSize;
	   h = fontSize + 2;
	   /*
      fontSize = 20;
      h = 22;
      if (size.equals("large") ) {
         fontSize = 25;
         h = 27;
      }
      if (size.equals("medium") ) {
         fontSize = 20;
         h = 22;
      }
      if (size.equals("small") ) {
         fontSize = 15;
         h = 17;
      }
      */
	   FontResource font = ((StreamBabyStream)view.getBApp()).getFont("default.ttf", BView.FONT_PLAIN, fontSize);
	   fm = font.getFontInfo();
	   if (fm != null)
		   h = (int)(fm.getHeight()+0.99)+1;

      w = stringLength(value);
      //w = value.length()*fontSize/2;
      x = (view.getScreen().getWidth() - w)/2;
      y = view.getHeight() - (BScreen.SAFE_ACTION_V);
      
      // Text background
      bg = new BView(view, x, y, w, h);
      bg.setResource(Color.black);
      bg.setTransparency((float)0.25);

      // Text
      text = new BText(bg, 0, 0, w, h);
      text.setColor(Color.white);
      //String fontName = String.format("default-%d.font", fontSize);
      text.setFont(font);
      text.setValue(value);
   }
   
   public void setVisible(Boolean visible) {
      text.setVisible(visible);
      bg.setVisible(visible);
   }
   
	public int stringLength(String str) {
		if (fm == null)
			return str.length()*fontSize/2;
		double width = 0;
		for (char c : str.toCharArray()) {
			GlyphInfo gi = fm.getGlyphInfo(c);
			width += gi.getAdvance();
		}
		return (int)(width+0.99);
	}
   
   public void setLocation(int x, int y) {
      text.setLocation(x, y);
      bg.setLocation(x, y);
   }
   
   public void setBounds(int x, int y, int w, int h) {
      bg.setBounds(x, y, w, h);
   }
   
   public void setValue(String value) {
      text.setValue(value);
   }
   
   public Object getValue() {
      return text.getValue();
   }
   
   public void setColor(Object object) {
      text.setColor(object);
   }
   
   public void setFlags(int flags) {
      text.setFlags(flags);
   }
   
   public void clearResource() {
      text.clearResource();
      bg.clearResource();
   }
   
   public void remove() {
      text.setValue(null);
      clearResource();
      text.remove();
      bg.remove();
   }
}
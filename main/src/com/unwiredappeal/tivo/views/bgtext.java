package com.unwiredappeal.tivo.views;

import java.awt.Color;
import java.lang.reflect.Field;

import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.HmeEvent.FontInfo;
import com.tivo.hme.sdk.Resource.FontResource;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.streambaby.StreamBabyStream;

public class bgtext {
   FontInfo fm = null;
   TivoCharacters tc = null;
   BText text;
   BView bg;
   public int x, y, w, h, fontSize;
   Boolean bgon = true;

    public bgtext(BView view, String fontSize, String value) {
	this(view, VFont.getFontSize(view, fontSize), value);
    }

   public bgtext(BView view, int fontSize, String value) {
	   this.fontSize = fontSize;
	   h = fontSize + 2;
	   if( StreamBabyConfig.cfgCCBackground.getInt() == 0 )
		   bgon = false;
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
	   if (fm != null) {
		   tc = new TivoCharacters(fm);
		   value = tc.stripInvalidChars(value);
		   h = (int)(fm.getHeight()+0.99)+1;		   
	   }

      w = stringLength(value);
      //w = value.length()*fontSize/2;
      x = (view.getScreen().getWidth() - w)/2;
      y = view.getHeight() - StreamBabyConfig.cfgCCYOffset.getInt();
      
      // Text background
      if (bgon) {
	      bg = new BView(view, x, y, w, h);
	      bg.setResource(getColor(StreamBabyConfig.cfgCCBackgroundColor.getValue()));
	      if (StreamBabyConfig.cfgCCBackgroundTransparency.getFloat() != 0.0)
	    	  bg.setTransparency(StreamBabyConfig.cfgCCBackgroundTransparency.getFloat());
      }

      // Text
      if (bgon)
    	  text = new BText(bg, 0, 0, w, h);
      else
    	  text = new BText(view, x, y, w, h);
      text.setColor(getColor(StreamBabyConfig.cfgCCTextColor.getValue()));
      //String fontName = String.format("default-%d.font", fontSize);
      text.setFont(font);
      if (value != null)
    	  text.setValue(value);
   }
   
   // Returns a Color based on 'colorName' which must be one
   // of the predefined colors in java.awt.Color.
   // Returns null if colorName is not valid.
   private Color getColor(String colorName) {
	   try {
		   // Find the field and value of colorName
		   Field field = Class.forName("java.awt.Color").getField(colorName);
		   return (Color)field.get(null);
	   } catch (Exception e) {
		   return null;
	   }
   }
   
   public void setVisible(Boolean visible) {
      text.setVisible(visible);
      if (bgon)
    	  bg.setVisible(visible);
   }

	public int stringLength(String str) {
		if (tc== null)
			return str.length()*fontSize/2;
		return tc.stringLength(str);
	}
   
   public void setLocation(int x, int y) {
      text.setLocation(x, y);
      if (bgon)
    	  bg.setLocation(x, y);
   }
   
   public void setBounds(int x, int y, int w, int h) {
	   if (bgon)
		   bg.setBounds(x, y, w, h);
	   else
		   text.setBounds(x, y, w, h);
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
      if (bgon)
    	  bg.clearResource();
   }
   
   public void remove() {
      text.setValue(null);
      clearResource();
      text.remove();
      if (bgon)
    	  bg.remove();
   }
}
// $Id: VText.java 10 2008-09-16 08:13:42Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.streambaby;

import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BScreen;

public class VText extends BText {
   BText text;
   public int h;
   public boolean visible = false;
   public VText(BView view, int x, int y, int h_multiplier, String type) {
	   super(view, x, y, 1, 1, false);
	  text = this;
      int fontSize = 30;
      h = 30;
      int w = view.getScreen().getWidth()-(BScreen.SAFE_TITLE_H*2);
      if (GLOBAL.FONT_SIZE.equals("large") ) {
         fontSize = 35;
         h = 35;
      }
      if (GLOBAL.FONT_SIZE.equals("medium") ) {
         fontSize = 30;
         h = 30;
      }
      if (GLOBAL.FONT_SIZE.equals("small") ) {
         fontSize = 25;
         h = 27;
      }
      if (type.equals("title")) {
         fontSize += 18;
         h += 18;
      }
      if (type.equals("small")) {
         fontSize -= 10;
         h -= 10;
      }
            
      //text = new BText(view, x, y, w, h*h_multiplier);
      text.setBounds(x, y, w, h*h_multiplier+(h_multiplier));
      text.setVisible(true);
      text.setColor(GLOBAL.text_COLOR);
      text.setShadow(GLOBAL.text_SHADOW,3);
      String font = String.format("default-%d.font", fontSize);
      text.setFont(font);
   }
   
   public void setVisible(Boolean visible) {
      this.visible = visible;
      text.setVisible(visible);
   }
   
   public boolean isVisibile() {
	   return this.visible;
   }
   
   public void remove() {
	  clearResource();
	  super.remove();

   }   
}
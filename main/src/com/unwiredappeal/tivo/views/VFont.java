// $Id: VText.java 10 2008-09-16 08:13:42Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.views;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BView;
import com.unwiredappeal.tivo.streambaby.GLOBAL;

public class VFont {
    public static int getFontSize(BView view, String size) {
    int yres = ((BApplicationPlus)view.getBApp()).getCurrentResolution().getHeight();
	int fontSize = 30;
	try {
	    fontSize = Integer.parseInt(size);
	} catch (NumberFormatException e) {
	    if (GLOBAL.FONT_SIZE.equals("large") ) {
		fontSize = 35;
	    }
	    if (GLOBAL.FONT_SIZE.equals("medium") ) {
		fontSize = 30;
	    }
	    if (GLOBAL.FONT_SIZE.equals("small") ) {
		fontSize = 25;
	    }
	    if (yres < 720) {
		if (size.equals("title")) {
		    fontSize += 18;
		}
		if (size.equals("small")) {
		    fontSize -= 10;
		}
		if (size.equals("tiny")) {
		    fontSize -= 15;
		}
	    } else {
		if (size.equals("title")) {
		    fontSize += 18;
		}
		if (size.equals("medium")) {
		    fontSize += 10;
		}
		if (size.equals("small")) {
		    fontSize -= 0;
		}
		if (size.equals("tiny")) {
		    fontSize -= 3;
		}
	    }
	}
	return fontSize;
    }

    public static int getFontSize(int size) {
	return size;
    }
}
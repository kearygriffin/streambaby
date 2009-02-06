package com.unwiredappeal.tivo.utils;

import java.util.ArrayList;
import java.util.List;


public class WordWrap {

	
    //FontMetrics fm;
    int width;
    int maxWidth;
    String txt;
    int pos;
    StringLength sl;

    public static interface StringLength {
    	public int stringLength(String str);
    }
    public WordWrap (StringLength sl, String txt, int width) {
	
	//this.fm = fm;
	this.txt = txt;
	this.width = width;
	this.sl = sl;
    }
    

    /** returns -1 if no text is left */

    public int next () {
	
	int i = pos;
	int len = txt.length ();

	if (pos >= len) return -1;
	
	int start = pos;
	
	while (true) {
	    while (i < len && txt.charAt (i) > ' ')
	    	i++;

	    int w = sl.stringLength(txt.substring (start, i));
	    if (w > width && pos == start) {
	    	i = start;
	    	while(i < len && sl.stringLength(txt.substring (start, i)) < width)
	    		i++;
	    	i--;
	    	w = sl.stringLength(txt.substring(start, i));
	    }

	    if (pos == start  || w <= width) {
	    	if (w > maxWidth) maxWidth = w;
	    		pos = i;
	    }

	    if (w > width || i >= len || txt.charAt(i) == '\n') break;
	    i++;
	}

	return pos >= len ? pos : ++pos;
    }


    public int getMaxWidth () {
	return maxWidth;
    }
    
    public String[] split() {
    	List<String> slist = new ArrayList<String>();
    	int index;
    	int start = pos;
    	while((index = next()) >= 0) {
    		String str = txt.substring(start, index);
    		slist.add(str);
    		start = index;
    	}
    	return slist.toArray(new String[0]);
    }
    public static void main(String[] argv) {
    	StringLength sl = new StringLength() {

			public int stringLength(String str) {
				return str.length() * 5;
			}
    		
    	};
    	String txt = "Hello dude, this is a test string to see exactly aaaaaaaaaaabbbbbbbbbbbbbbbbbbbcccccccccccccccccccddddddddddddddddddddd how and when it will split, doodaaa, doodaaa";
    	WordWrap w = new WordWrap(sl, txt, 100);
    	String[] sa = w.split();
    	for (String str : sa) {
    		System.err.println(str);
    	}
    }
    
}
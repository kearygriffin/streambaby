package com.unwiredappeal.tivo.views;

import com.tivo.hme.sdk.HmeEvent.FontInfo;
import com.tivo.hme.sdk.HmeEvent.FontInfo.GlyphInfo;

public class TivoCharacters {
	FontInfo fm;
	
	public TivoCharacters(FontInfo fm) {
		this.fm = fm;
	
	}
	

	public String stripInvalidChars(String str) {
		StringBuffer sb = new StringBuffer();
		for (char c : str.toCharArray()) {
			if (fm.getGlyphInfo(c) == null)
				c = 127;
			if (fm.getGlyphInfo(c) != null)
				sb.append(c);
		}		
		return sb.toString();
	}
	
	public int stringLength(String str) {
		double width = 0;
		for (char c : str.toCharArray()) {
			GlyphInfo gi = fm.getGlyphInfo(c);
			if (gi == null)
				continue;
			width += gi.getAdvance();
		}
		return (int)(width+0.99);
	}
	
}

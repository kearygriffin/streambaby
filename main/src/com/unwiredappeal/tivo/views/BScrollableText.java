package com.unwiredappeal.tivo.views;

import java.util.ArrayList;
import java.util.List;

import com.tivo.hme.bananas.BView;
import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.HmeEvent.FontInfo;
import com.tivo.hme.sdk.Resource.FontResource;
import com.unwiredappeal.tivo.streambaby.StreamBabyStream;
import com.unwiredappeal.tivo.utils.WordWrap;
import com.unwiredappeal.tivo.views.VText.FontSizeInfo;

public class BScrollableText extends SBScrollPanePlus {
	FontInfo fm;
	TivoCharacters tc;
	public String text;
	public String size;
	int height;
	
	List<BView> views = new ArrayList<BView>();
	public BScrollableText(BView parent, int x, int y, int width, int height,
			String size) {
		super(parent, x, y, width, height);
		this.height = height;
		this.size = size;
	}
	
	public  boolean handleEvent(HmeEvent event) {
		if (fm == null && event.getOpCode() == EVT_FONT_INFO) {
                fm = (HmeEvent.FontInfo) event;
                invalidate();
                refresh();
                return true;
		}
		else
			return super.handleEvent(event);
	}
	
	protected void validate() {
		if (!validated) {
			for (BView v : views) {
				v.clearResource();
				v.remove();
			}
		   FontSizeInfo info = VText.getFontSize(size);
		   FontResource font = ((StreamBabyStream)this.getBApp()).getFont("default.ttf", FONT_PLAIN, info.fontSize);
		   fm = font.getFontInfo();
		   if (fm != null)
			   tc = new TivoCharacters(fm);
		   if (fm == null) {
			  font.addHandler(this) ;
			  VText vt = new VText(this, 0, 0, this.getWidth(), this.getHeight(), size);
			  views.add(vt);
			  vt.setFlags(VText.RSRC_HALIGN_LEFT|VText.RSRC_VALIGN_TOP|VText.RSRC_TEXT_WRAP);
			  vt.setValue(text);
		   } else {
			   createTextViews();
		   }			
			super.validate();			
		}
	}
	
	public void setValue(Object o) {
		text = (String)o;
		text = text.trim();
		invalidate();
		refresh();
	}

	public class TivoStringLength implements WordWrap.StringLength {

		
		public int stringLength(String str) {
			return tc.stringLength(str);
		}
	}
	
    public void setLineHeightNoRefresh(int lineHeight) {
        this.lineHeight = lineHeight;
        this.pageHeight = getHeight() - lineHeight;
    }

	public void createTextViews() {
		int lineHeight = (int)(fm.getHeight()+0.99)+1;
		int newheight = (height / lineHeight) * lineHeight;
		if (newheight != this.getHeight()) {
			this.setBounds(this.getX(), this.getY(), this.getWidth(), newheight);
		}
		
		setLineHeightNoRefresh(lineHeight);
		String strippedText = tc.stripInvalidChars(text);
    	WordWrap w = new WordWrap(new TivoStringLength(), strippedText, this.getWidth());
    	String[] lines = w.split();
    	int yoff = 0;
    	for (String l : lines) {
    		VText vt = new VText(this, 0, yoff, this.getWidth(), lineHeight, size);
  		  	vt.setFlags(VText.RSRC_HALIGN_LEFT|VText.RSRC_VALIGN_TOP);
    		vt.setValue(l);
    		views.add(vt);
    		yoff += lineHeight;
    	}
	}
}

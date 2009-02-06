package com.unwiredappeal.tivo.views;

import java.awt.Rectangle;

import com.tivo.hme.bananas.BHighlight;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BViewPlus;
import com.tivo.hme.bananas.ViewUtils;
import com.tivo.hme.bananas.layout.Layout;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.View;

	public class SBScrollPanePlus extends BViewPlus {
		public static final String ACTION_RESIZE = "resize";
	    public final static String ANIM = "*100";
	    
		protected int lineHeight;
		protected int pageHeight;
		protected int offset;
		protected int totalHeight;
		protected boolean validated;
		protected boolean animate = true;

		public SBScrollPanePlus(BView parent, int x, int y, int width, int height) {
			this(parent, x, y, width, height, 25, true);
		}

		public SBScrollPanePlus(BView parent, int x, int y, int width, int height, int lineHeight) {
			this(parent, x, y, width, height, lineHeight, true);
		}
		
		public SBScrollPanePlus(BView parent, int x, int y, int width, int height, int lineHeight, boolean visible) {
			this(parent, ViewUtils.getBounds(parent, x, y, width, height), lineHeight, true);
		}

	    public SBScrollPanePlus(BView parent, Layout layout) {
	        this(parent, layout, 25, true);
	    }

	    public SBScrollPanePlus(BView parent, Layout layout, int lineHeight, boolean visible) {
	        this(parent, layout.getBounds(), lineHeight, visible);
	    }
		
		protected SBScrollPanePlus(BView parent, Rectangle bounds, int lineHeight, boolean visible) {
			super(parent, bounds.x, bounds.y, bounds.width, bounds.height, visible);
			this.lineHeight = lineHeight;
			this.pageHeight = bounds.height - lineHeight;
		}
		
	    protected void validate() {
	        if (!validated) {
	                validated = true;
	                totalHeight = 0;
	                int count = getChildCount();
	                for (int i=0; i < count; i++) {
	                        View view = getChild(i);
	        if (view.getVisible()) {
	            totalHeight = Math.max(view.getY() + view.getHeight(), totalHeight);
	        }
	                }
	    //getHighlights().setPageHint(H_PAGEUP,   A_RIGHT+13, A_TOP - 25);
	    //getHighlights().setPageHint(H_PAGEDOWN, A_RIGHT+13, A_BOTTOM + 30);
	    getHighlights().setPageHint(H_PAGEUP,   A_RIGHT, A_TOP);
	    getHighlights().setPageHint(H_PAGEDOWN, A_RIGHT, A_BOTTOM);

	                refresh();
	        }
	    }
		
		public void invalidate() {
			validated = false;
		}
		
		public void reset() {
			offset = 0;
			setTranslation(0, offset);
		}
		
		public void pageUp() {
			validate();
			if (offset < 0) {
		        Resource anim = animate ? getAnimationResource() : null;
				offset = Math.min(0, offset+pageHeight);
				setTranslation(0, offset, anim);
				refresh();
				getBApp().play("pageup.snd");
			}
		}
		
		protected Resource getAnimationResource() {
			return getResource(ANIM);
		}
		
		public void pageDown() {
			validate();
			if (offset > getHeight()-totalHeight) {
		        Resource anim = animate ? getAnimationResource() : null;
				offset = Math.max(pageHeight-totalHeight, offset-pageHeight);
				offset = Math.min(0, offset);
				setTranslation(0, offset, anim);
				refresh();
				getBApp().play("pagedown.snd");
			}
		}
		
		public void lineUp() {
			validate();
			if (offset < 0) {
		        Resource anim = animate ? getAnimationResource() : null;
				offset = Math.min(0, offset+lineHeight);
				setTranslation(0, offset, anim);
				refresh();
				getBApp().play("updown.snd");
			}
		}
		
		public void lineDown() {
			validate();
			if (offset > getHeight()-totalHeight) {
		        Resource anim = animate ? getAnimationResource() : null;
				offset = Math.max(pageHeight-totalHeight, offset-lineHeight);
				offset = Math.min(0, offset);
				setTranslation(0, offset, anim);
				refresh();
				getBApp().play("updown.snd");
			}
		}

		public void refresh() {
			validate();
			refreshHighlights();
		}
		
		protected void refreshHighlights() {
			BHighlight up = getHighlights().get(H_PAGEUP);
			if (up != null) {
				up.setVisible((offset < 0) ? H_VIS_TRUE : H_VIS_FALSE);
			}
			
			BHighlight down = getHighlights().get(H_PAGEDOWN);
			if (down != null) {
				down.setVisible((offset > getHeight()-totalHeight) ? H_VIS_TRUE : H_VIS_FALSE);
			}
			getHighlights().refresh();
		}

		@Override
		public boolean handleAction(BView view, Object action) {
			if (action.equals(ACTION_RESIZE)) {
				invalidate();
				refresh();
			}
			return super.handleAction(view, action);
		}

		@Override
		public boolean handleKeyPress(int code, long rawcode) {

	        switch (code) {
	        case KEY_UP:
	            lineUp();
	            return true;
	        case KEY_DOWN:
	            lineDown();
	            return true;
	        case KEY_CHANNELUP:
	            pageUp();
	            return true;
	        case KEY_CHANNELDOWN:
	            pageDown();
	            return true;
	        }

	        return super.handleKeyPress(code, rawcode);
		}

		@Override
		public boolean handleKeyRepeat(int code, long rawcode) {
			return handleKeyPress(code, rawcode);
		}

		public boolean isAnimate() {
			return animate;
		}

		public void setAnimate(boolean animate) {
			this.animate = animate;
		}

	    public int getLineHeight() {
	        return lineHeight;
	    }

	    public void setLineHeight(int lineHeight) {
	        this.lineHeight = lineHeight;
	        this.pageHeight = getHeight() - lineHeight;
	        refresh();
	    }
		

    @Override
    public void setVisible(boolean b) {
    	super.setVisible(b);
    	invalidate();
    	refresh();
    }

}

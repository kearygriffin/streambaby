package com.unwiredappeal.tivo.streambaby;

import static com.tivo.hme.bananas.IBananasPlus.H_BAR_FONT;
import static com.tivo.hme.bananas.IBananasPlus.H_BAR_TEXT_COLOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BButtonPlus;
import com.tivo.hme.bananas.BRect;
import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.bananas.BSkin;
import com.tivo.hme.bananas.BTextPlus;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BViewPlus;
import com.tivo.hme.bananas.IBananasPlus;
import com.tivo.hme.bananas.ViewUtils;
import com.tivo.hme.bananas.BSkin.Element;
import com.tivo.hme.bananas.layout.Layout;
import com.tivo.hme.bananas.layout.LayoutManager;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.util.Ticker;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.metadata.MetaData;
import com.unwiredappeal.tivo.modules.VideoFormats;
import com.unwiredappeal.tivo.modules.VideoModuleHelper;
import com.unwiredappeal.tivo.push.Push;
import com.unwiredappeal.tivo.push.Tivo;

public abstract class ButtonScreen extends ScreenTemplate implements Ticker.Client {

	int buttonHeight;
	//int buttonY;
	Layout layout;
	int totalButtons;
	int curButton;
	boolean isReturn;

	protected BViewPlus pleaseWait;
	protected BApplicationPlus app;
	
	public abstract class ButtonHandler {

		@SuppressWarnings("unchecked")
		public BView parentView;
		public List<BView> childViews = new ArrayList<BView>();
		public String toString() {
			return null;
		}

		public abstract boolean left();

		public abstract boolean right();

		public abstract boolean select();
		
		public void gainFocus() { };
	}

	BButtonPlus<ButtonHandler> curFocusButton = null;
	List<BButtonPlus<ButtonHandler>> buttonsList = new ArrayList<BButtonPlus<ButtonHandler>>();

	public ButtonScreen(BApplicationPlus app) {
		super(app);
		this.app = app;

	      LayoutManager lm = new LayoutManager(getNormal());
	      layout = lm.safeTitle(this);

	      Element e = getBApp().getSkin().get(IBananasPlus.H_PLEASE_WAIT);
	      layout = lm.size(layout, e.getWidth(), e.getHeight());
	      layout = lm.align(layout, A_CENTER, A_CENTER);

	      pleaseWait = new BViewPlus(this, layout);
	      pleaseWait.setResource(e.getResource());
	      pleaseWait.setVisible(true);
	      
		  //title = de.getStrippedFilename();
		  resetTitle();

	}
	
	public synchronized long tick(long tm, Object arg) {		
		timerRender();
		flush();
		return 0;
	}

	public abstract void render();
	protected abstract int setupButtons(boolean isReturn);
	
	private void timerRender() {
		this.setPainting(false);
		render();
		curButton = -1;
		setButtonFocus();
		setFocus(null);
		for (int i = 0; i < buttonsList.size(); i++) {
			BButtonPlus<ButtonHandler> b = buttonsList.get(i);
			for (int j=0;j<b.getValue().childViews.size();j++)
				b.getValue().childViews.get(j).remove(null);
			b.remove(null);
			b.getValue().parentView.remove(null);
		}
		buttonsList.clear();
		
		getBApp().getRoot().setPainting(false);

		//totalButtons = pos > 0 ? 3 : 2;
		curButton = 0;
		layout = calcListLayout(1);
		buttonHeight = (int)(ViewUtils.getHeight(this, H_BAR) * .75f);
		//buttonY = layout.getBounds().y;

		curButton = setupButtons(isReturn);
		
		finishButtons();
		getBApp().getRoot().setPainting(true);
		setButtonFocus();

		resetTitle();

		pleaseWait.setVisible(false);
		this.setPainting(true);		
	}
	public Layout calcListLayout(int rows) {

		LayoutManager lm = new LayoutManager(getNormal());
		Layout safeTitle = lm.safeTitle(this);
		Layout layout = safeTitle;

	      float stretchy = .98f;
	      int safeY = 25;
	      if (this.getBApp().getCurrentResolution().getHeight() == 720) {
	    	  stretchy = 1.04f;
	    	  safeY = 35;
	      }

		layout = lm.relativeY(layout, false);
		// layout = lm.stretchWidth(layout, 0.9f);
		layout = lm.safeAction(layout, this, -20, safeY);
		layout = lm.stretchWidth(layout, GLOBAL.SELECT_STRETCH);
		layout = lm.stretchHeight(layout, stretchy);
		layout = lm.indentY(layout, 65);
		int height =  Math.min(45, ViewUtils.getHeight(this, H_BAR));
		//layout = lm.valign(layout, ((int)(.62 * getHeight())) - (rows * height));
		int y = (int)(.62 * getHeight());
		y = (int)(y * stretchy);
		layout = lm.valign(layout, y - (rows * height));
		return layout;
	}

	public boolean handleEnter(Object arg, boolean isReturn) {
		((StreamBabyStream)getBApp()).setCurrentScreen(this);
		this.isReturn = isReturn;
		if (isReturn)
			timerRender();
		else
	      Ticker.master.add(this, System.currentTimeMillis()+200, null);
		return true;
	}

	public Resource getDefaultTextColor() {
		BSkin skin = getBApp().getSkin();
		BSkin.Element e = skin.get(H_BAR_TEXT_COLOR);
		if (e != null) {
			return e.getResource();
		} else {
			return null;
		}
	}

	public Resource getDefaultFont() {
		BSkin skin = getBApp().getSkin();
		BSkin.Element e = skin.get(H_BAR_FONT);
		if (e != null) {
			return e.getResource();
		} else {
			return null;
		}
	}

	protected BButtonPlus<ButtonHandler> addSimpleTextButton(String text, ButtonHandler h, boolean lastButton) {
		BView v = new BView(this, layout.getBounds().x, layout.getBounds().y, layout.getBounds().width, buttonHeight);
		BButtonPlus<ButtonHandler> b = new BButtonPlus<ButtonHandler>(v,
				0, 0, v.getWidth(), v.getHeight());
//		buttonY += buttonHeight;

		BTextPlus<String> bt = new BTextPlus<String>(b, 10, 0, b
				.getWidth() - 50, b.getHeight());
		bt.setFlags(RSRC_HALIGN_LEFT);
		bt.setShadow(true);
		bt.setValue(text);
		bt.setColor(getDefaultTextColor());
		bt.setFont(getDefaultFont());
		h.childViews.add(bt);
		b.setValue(h);
		b.setBarAndArrows(BAR_HANG, BAR_DEFAULT, IBananasPlus.FLAG_VIS_TRUE,
				"pop", H_RIGHT, null, null, true);
		b.getHighlights().get(H_BAR).getView().setTransparency(1.0f);
		int safeTitleH = ((BApplicationPlus) getBApp())
				.getSafeTitleHorizontal();
		BRect rect = b.getHighlightBounds();

		int originx = -rect.x;
		BSkin skin = getBApp().getSkin();
		BSkin.Element up = skin.get(H_UP);
		BSkin.Element down = skin.get(H_DOWN);

		int whi_up = originx + safeTitleH - up.getWidth();
		int whi_down = originx + safeTitleH - down.getWidth();
		String upAction = curButton == 0 ? null : H_UP;
		String downAction = lastButton ? null : H_DOWN;
		if (upAction != null)
			b.getHighlights().setWhisperingArrow(H_UP, A_LEFT + whi_up,
					A_TOP - up.getHeight(), upAction);
		if (downAction != null)
			b.getHighlights().setWhisperingArrow(H_DOWN, A_LEFT + whi_down,
					A_BOTTOM + down.getHeight(), downAction);
		
		h.parentView = v;		
		curButton++;
		buttonsList.add(b);
		// b.setBarAndArrows(null, "pop", true);
		return b;

	}

	
	void setButtonFocus() {
		getBApp().getRoot().setPainting(false);
		if (curFocusButton != null)
			curFocusButton.getHighlights().get(H_BAR).getView()
					.setTransparency(1.0f);
		if (curButton >= 0) {
			BButtonPlus<ButtonHandler> b = buttonsList.get(curButton);
			b.getHighlights().get(H_BAR).getView().setTransparency(0.0f);
			curFocusButton = b;
			setFocus(b);
			b.getValue().gainFocus();

		} else
			curFocusButton = null;
		getBApp().getRoot().setPainting(true);		
	}
	
	protected void finishButtons() {
		totalButtons = buttonsList.size();
		for (int i=0;i<totalButtons;i++) {
			Layout layout = calcListLayout(totalButtons-i);
			BView v = buttonsList.get(i).getValue().parentView;
			v.setBounds(v.getBounds().x, layout.getBounds().y, v.getBounds().width, v.getBounds().height);
		}
	}


	public boolean moveCursor(int dir) {
		curButton += dir;
		if (curButton < 0) {
			curButton = 0;
			play("bonk.snd");
			return true;
		} else if (curButton >= totalButtons) {
			curButton = totalButtons - 1;
			play("bonk.snd");
			return true;
		}
		setButtonFocus();
		return true;
	}

	public boolean handleAction(BView view, Object action) {
		Log.debug("action=" + action);
		// if (action.equals("push")) {
		if (action.equals("right")) {
			BButtonPlus<ButtonHandler> b = buttonsList.get(curButton);
			return b.getValue().right();
		}
		if (action.equals("pop") || action.equals("left")) {
			BButtonPlus<ButtonHandler> b = buttonsList.get(curButton);
			return b.getValue().left();
		}
		if (action.equals("up"))
			return moveCursor(-1);
		else if (action.equals("down"))
			return moveCursor(1);
		return super.handleAction(view, action);
	}

	public boolean popBack() {
		getBApp().pop();
		return true;
	}

	public boolean handleKeyPress(int code, long rawcode) {
		Log.debug("code=" + code + " rawcode=" + rawcode);
		switch (code) {
		case KEY_SELECT:
			BButtonPlus<ButtonHandler> b = buttonsList.get(curButton);
			return b.getValue().select();
			
		// case KEY_LEFT:
		// moveLeft();
		// break;
		}

		return super.handleKeyPress(code, rawcode);
	}

	public void resetTitle() {
		setTitle(this.toString());
	}


}

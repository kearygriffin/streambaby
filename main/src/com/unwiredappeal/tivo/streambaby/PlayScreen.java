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
import com.tivo.hme.bananas.IBananasPlus;
import com.tivo.hme.bananas.ViewUtils;
import com.tivo.hme.bananas.layout.Layout;
import com.tivo.hme.bananas.layout.LayoutManager;
import com.tivo.hme.sdk.Resource;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.metadata.MetaData;
import com.unwiredappeal.tivo.modules.VideoFormats;
import com.unwiredappeal.tivo.modules.VideoModuleHelper;

public class PlayScreen extends ScreenTemplate {

	private DirEntry de;
	int buttonHeight;
	//int buttonY;
	Layout layout;
	int totalButtons;
	int curButton;
	int qual;
	boolean canStream;
	boolean canTranscode;

	BView mview = null;
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

	public PlayScreen(BApplicationPlus app, DirEntry de) {
		super(app);
		this.de = de;
		qual = StreamBabyConfig.inst.getDefaultQuality();
		MetaDataViewer mv = new MetaDataViewer();
		MetaData meta = new MetaData();
		boolean hasMeta = de.getMetadata(meta);
		if (hasMeta) {
			LayoutManager lm = new LayoutManager(getNormal());
			Layout safeTitle = lm.safeTitle(this);
			Layout layout = safeTitle;

			layout = lm.relativeY(layout, false);		
			
			layout = lm.safeAction(layout, this, 0, 25);
			layout = lm.indentX(layout, -20);
			layout = lm.stretchWidth(layout, GLOBAL.SELECT_STRETCH);
			layout = lm.indentY(layout, 60);

			int bottom = calcListLayout(4).getBounds().y;
			int height = bottom - layout.getBounds().y;
			mview = mv.getView(meta, this.getNormal(), layout.getBounds().x, layout.getBounds().y, layout.getBounds().width+50, height);
		}

		/*
		 * LayoutManager lm = new LayoutManager(getNormal()); Layout safeTitle =
		 * lm.safeTitle(this); Layout layout = safeTitle;
		 * 
		 * list = new StandardList(getNormal(), layout);
		 * 
		 * //setupList();
		 * 
		 * setFocusDefault(list);
		 */
		resetTitle();
	}

	public Layout calcListLayout(int rows) {

		LayoutManager lm = new LayoutManager(getNormal());
		Layout safeTitle = lm.safeTitle(this);
		Layout layout = safeTitle;

		layout = lm.relativeY(layout, false);
		// layout = lm.stretchWidth(layout, 0.9f);
		layout = lm.safeAction(layout, this, 0, 25);
		layout = lm.stretchWidth(layout, GLOBAL.SELECT_STRETCH);
		layout = lm.indentY(layout, 65);
		int height = ViewUtils.getHeight(this, H_BAR);
		layout = lm.valign(layout, ((int)(.62 * getHeight())) - (rows * height));
		return layout;
	}

	public boolean handleEnter(Object arg, boolean isReturn) {
		((StreamBabyStream)getBApp()).setCurrentScreen(this);
		setupList(isReturn);
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

	private BButtonPlus<ButtonHandler> addSimpleTextButton(String text, ButtonHandler h, boolean lastButton) {
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

	private class QualityButtonHandler extends ButtonHandler {
		private static final String KBPS = " kb/s";
		int bitrate;
		BButtonPlus<?> button;
		private class QEntry {
			String text;
			int qual;
			public QEntry(String txt, int q) {
				text = txt;
				qual = q;
			}
		}

		int cur = 0;
		List<QEntry> entries = new ArrayList<QEntry>();
		
		private void addQEntry(int q, String txt) {
			// if we can stream the current video at a bitrate that is less than this quality, don't bother showing it.
			int newbitrate = StreamBabyConfig.inst.getVideoBr(q) + StreamBabyConfig.inst.getAudioBr(q);
			if (bitrate > 0 && canStream && bitrate < newbitrate)
				return;
			int kbps = StreamBabyConfig.inst.getVideoBr(q);
			QEntry qe = new QEntry(txt + " - " + kbps + KBPS, q);
			entries.add(qe);
		}
		public QualityButtonHandler() {
			bitrate = de.getVideoInformation().getBitRate();

			if (qual > VideoFormats.LAST_QUALITY) {
				entries.add(new QEntry("Default - " + qual + KBPS, qual));
			}
			String sameTxt = "Same";
			if (canStream && bitrate > 0) {
				sameTxt += " - " + bitrate + KBPS;
			}
			entries.add(new QEntry(sameTxt, VideoFormats.QUALITY_SAME));
			if (StreamBabyConfig.cfgAutoQuality.getBool())
				addQEntry(VideoFormats.QUALITY_AUTO, "Auto");
			addQEntry(VideoFormats.QUALITY_HIGHEST, "Highest");
			addQEntry(VideoFormats.QUALITY_HIGH, "High");
			addQEntry(VideoFormats.QUALITY_MEDIUMHIGH, "Medium High");
			addQEntry(VideoFormats.QUALITY_MEDIUM, "Medium");
			addQEntry(VideoFormats.QUALITY_MEDIUMLOW, "Medium Low");
			addQEntry(VideoFormats.QUALITY_LOW, "Low");
			addQEntry(VideoFormats.QUALITY_LOWEST, "Lowest");

		}
		public boolean left() {
			if (cur == 0)
				play("bonk.snd");
			else {
				cur--;
				setValue();
				play("updown.snd");
			}
			return true;
		}
		public boolean right() {
			if (cur == (entries.size()-1))
				play("bonk.snd");
			else {
				cur++;
				setValue();
				play("updown.snd");
			}
			return true;
		}
		public boolean select() {
			curButton = 0;
			setButtonFocus();
			return true;
		}
		
		public void setDefault() {
			cur = 0;
			for (int i=0;i<entries.size();i++) {
				if (entries.get(i).qual == qual) {
					cur = i;
					break;
				}
			}
			setValue();
		}
		public void setValue() {
			bt.setValue(entries.get(cur).text);
			qual = entries.get(cur).qual;
			setArrows();
		}
		public void setArrows() {
			float leftTransparent = 0.0f;
			float rightTransparent = 0.0f;
			if (cur == 0)
				leftTransparent = 1.0f;
			if (cur == (entries.size()-1))
				rightTransparent = 1.0f;
			BView view = button.getHighlights().get(H_LEFT).getView();
			if (view != null)
				view.setTransparency(leftTransparent);
			 view = button.getHighlights().get(H_RIGHT).getView();
			if (view != null)
				view.setTransparency(rightTransparent);
		}
		public void gainFocus() {
			setArrows();
		}
		BTextPlus<String> bt;
	}
	private BButtonPlus<ButtonHandler> addQualityButton(boolean lastButton) {
		QualityButtonHandler h = new QualityButtonHandler();
		BView v = new BView(this, layout.getBounds().x, layout.getBounds().y, layout.getBounds().width, buttonHeight);
		//int barWidth = v.getWidth();
		int qualityWidth = v.getWidth()/5;
		BTextPlus<String> bt = new BTextPlus<String>(v, 10, 0, qualityWidth, v.getHeight());
		bt.setFlags(RSRC_HALIGN_LEFT);
		bt.setShadow(true);
		bt.setValue("Quality: ");
		bt.setColor(getDefaultTextColor());
		bt.setFont(getDefaultFont());
		h.childViews.add(bt);
		BButtonPlus<ButtonHandler> b = new BButtonPlus<ButtonHandler>(v,
				qualityWidth, 0, (v.getWidth()-qualityWidth)-50, v.getHeight());
//		buttonY += buttonHeight;

		h.button = b;
		BSkin skin = getBApp().getSkin();
		BSkin.Element le = skin.get(H_LEFT);

		bt = new BTextPlus<String>(b, 2*le.getWidth(), 0, b
				.getWidth() - 50, b.getHeight());
		bt.setFlags(RSRC_HALIGN_LEFT);
		bt.setShadow(true);
		bt.setColor(getDefaultTextColor());
		bt.setFont(getDefaultFont());
		h.bt = bt;
		h.childViews.add(bt);
		b.setValue(h);
		b.setBarAndArrows(BAR_DEFAULT, BAR_DEFAULT, IBananasPlus.FLAG_VIS_TRUE,
				H_LEFT, H_RIGHT, null, null, true);
		b.getHighlights().get(H_BAR).getView().setTransparency(1.0f);
		int safeTitleH = ((BApplicationPlus) getBApp())
				.getSafeTitleHorizontal();
		BRect rect = b.getHighlightBounds();

		int originx = -rect.x;
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
		h.setDefault();

		curButton++;
		buttonsList.add(b);
		// b.setBarAndArrows(null, "pop", true);
		return b;

	}

	
	private void setButtonFocus() {
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
	
	private void finishButtons() {
		totalButtons = buttonsList.size();
		for (int i=0;i<totalButtons;i++) {
			Layout layout = calcListLayout(totalButtons-i);
			BView v = buttonsList.get(i).getValue().parentView;
			v.setBounds(v.getBounds().x, layout.getBounds().y, v.getBounds().width, v.getBounds().height);
		}
	}

	private void setupList(boolean isReturn) {
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
		long pos = ViewScreen.getResetSavedPosition(
				(StreamBabyStream) getBApp(), de.getUri(), de
						.getVideoInformation().getDuration());
		
		getBApp().getRoot().setPainting(false);

		//totalButtons = pos > 0 ? 3 : 2;
		curButton = 0;
		layout = calcListLayout(1);
		buttonHeight = ViewUtils.getHeight(this, H_BAR);
		//buttonY = layout.getBounds().y;


		if (pos != 0) {
			addSimpleTextButton("Resume playing"
					, new ButtonHandler() {
				public boolean left() {
					popBack();
					return true;
				}
				public boolean right() {
					return beginPlay(false);
				}
				public boolean select() {
					return beginPlay(false);
				}
			}, false);
			addSimpleTextButton("Play from beginning", new ButtonHandler() { 
				public boolean left() {
					popBack();
					return true;
				}
				public boolean right() {
					return beginPlay(true);
				}
				public boolean select() {
					return beginPlay(true);
				}
				
			}, false);
		} else {
			addSimpleTextButton("Play", new ButtonHandler() { 
				public boolean left() {
					popBack();
					return true;
				}
				public boolean right() {
					return beginPlay(true);
				}
				public boolean select() {
					return beginPlay(true);
				}
				
			}, false);
		}
		// We can only adjust quality if we can transcode this puppy.
		canTranscode = VideoModuleHelper.inst.canTranscode(de);
		canStream = VideoModuleHelper.inst.canStream(de);
		if (StreamBabyConfig.cfgQualitySelection.getBool() && canTranscode)
			addQualityButton(false);
		addSimpleTextButton("Go back", new ButtonHandler() { 
			public boolean left() {
				popBack();
				return true;
			}
			public boolean right() {
				return popBack();
			}
			public boolean select() {
				return popBack();
			}			
		}, true);
		
		curButton = 0;
		finishButtons();
		getBApp().getRoot().setPainting(true);
		setButtonFocus();
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

	private boolean beginPlay(boolean reset) {
		if (reset == true) {
			((StreamBabyStream) getBApp()).cachePersistentData(ViewScreen
					.persistKey(de.getUri()), "0", true);
		}
		BScreen newScreen = de.getFileType().createViewerScreen(getBApp(), Arrays.asList(new DirEntry[] { de }), null, qual);

		//ViewScreen newScreen = new ViewScreen(getBApp(), de,
				//qual);
		getBApp().push(newScreen, TRANSITION_LEFT);
		return true;
	}

	public boolean popBack() {
		getBApp().pop();
		return true;
	}

	public boolean handleKeyPress(int code, long rawcode) {
		Log.debug("code=" + code + " rawcode=" + rawcode);
		switch (code) {
		case KEY_CHANNELUP:
		case KEY_CHANNELDOWN:
			if (mview != null)
				return mview.handleKeyPress(code, rawcode);
			break;
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

	public String toString() {
		if (de != null)
			return de.getStrippedFilename();
		else
			return "Play";
	}

}

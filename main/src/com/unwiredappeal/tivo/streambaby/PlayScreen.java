package com.unwiredappeal.tivo.streambaby;

import static com.tivo.hme.bananas.IBananasPlus.H_BAR_FONT;
import static com.tivo.hme.bananas.IBananasPlus.H_BAR_TEXT_COLOR;

import java.net.MalformedURLException;
import java.net.URL;
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
import com.unwiredappeal.tivo.streambaby.DeleteScreen;

public class PlayScreen extends ButtonScreen implements Ticker.Client {

	private DirEntry de;
	int qual;
	boolean canStream;
	boolean isMore = false;
	boolean canTranscode;
	//boolean isReturn;

	BView mview = null;

	public PlayScreen(BApplicationPlus app, DirEntry de) {
		super(app);
		this.de = de;

	}
	

	public void render() {
		if (!isReturn) {
			qual = StreamBabyConfig.inst.getDefaultQuality();
			MetaDataViewer mv = new MetaDataViewer();
			MetaData meta = new MetaData();
			boolean hasMeta = de.getMetadata(meta);
			if (hasMeta) {
			      float stretchy = 1f;
			      int safeY = 25;
			      if (this.getBApp().getCurrentResolution().getHeight() == 720) {
			    	  stretchy = 1.05f;
			    	  safeY = 35;
			      }
	
				LayoutManager lm = new LayoutManager(getNormal());
				Layout safeTitle = lm.safeTitle(this);
				Layout layout = safeTitle;
	
				layout = lm.relativeY(layout, false);		
				
				layout = lm.safeAction(layout, this, 0, safeY);
				layout = lm.indentX(layout, -20);
				layout = lm.stretchWidth(layout, GLOBAL.SELECT_STRETCH);
				layout = lm.stretchHeight(layout, stretchy);
				layout = lm.indentY(layout, 60);
	
				int bottom = calcListLayout(5).getBounds().y;
				int height = bottom - layout.getBounds().y;
				height -= 20;
				mview = mv.getView(meta, this.getNormal(), layout.getBounds().x, layout.getBounds().y, layout.getBounds().width+50, height);
			}
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

		setupButtons(isReturn);

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

	private class pushTivoButtonHandler extends ButtonHandler {
		BButtonPlus<?> button;

		int cur = 0;
		List<Tivo> entries = new ArrayList<Tivo>();
		
		public pushTivoButtonHandler() {
			for (Tivo tivo : Push.getInstance().getTivos()) {
				Tivo nt = new Tivo(tivo);
				/*
				if (tivo.getAuto() && tivo.getTsn().compareTo(app.getContext().getReceiverGUID()) == 0) {
					nt.setName("This Tivo");
				}
				*/
				entries.add(nt);
			}
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
			/*
			boolean success = Push.getInstance().pushVideo(app.getContext().getBaseURI(), de, entries.get(cur), qual);
			if (success) {
				Log.info("Push succeeded: " + de.getName() + "->" + entries.get(cur).getName());
			} else {
				//StreamBabyConfig.py.handleErrors();
			}
			curButton = 0;
			setButtonFocus();
			*/
			final URL baseUri;
			Tivo tivo = entries.get(cur);
			if (tivo.getIsExternal()) {
				String ext = StreamBabyConfig.cfgExternalUrl.getValue();
				if (ext == null) {
					baseUri = app.getContext().getBaseURI();
				} else {
					try {
						baseUri = new URL(ext);
					} catch(MalformedURLException e) {
						Log.error("Malformed url: " + ext);
						return true;
					}
				}
			}
			else {
				baseUri = app.getContext().getBaseURI();
			}
			SingleActionScreen.Action action = new SingleActionScreen.Action() {

				public final URL pbaseUri = baseUri;
				public final DirEntry pushDe = de;
				public final Tivo tivo = entries.get(cur);
				public final int pqual = qual;
				public String go() {
					boolean success = Push.getInstance().pushVideo(pbaseUri, pushDe, tivo, pqual);
					if (success) {
						Log.info("Push succeeded: " + pushDe.getName() + "->" + tivo.getName());
						return "Push Succeeded.";
					} else {
						Log.info("Push Failed: " + pushDe.getName() + "->" + tivo.getName());						
						return "Push Failed!";
					}

				}
				
			};
			
			getBApp().push(new SingleActionScreen(app, action, de.getStrippedFilename()), TRANSITION_LEFT);
			return true;
		}
		
		public void setDefault() {
			cur = 0;
			setValue();
		}
		public void setValue() {
			bt.setValue(entries.get(cur));
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
	
	private BButtonPlus<ButtonHandler> addPushTivoButton() {
		boolean lastButton = false;
		pushTivoButtonHandler h = new pushTivoButtonHandler();
		BView v = new BView(this, layout.getBounds().x, layout.getBounds().y, layout.getBounds().width, buttonHeight);
		//int barWidth = v.getWidth();
		int qualityWidth = v.getWidth()/5;
		BTextPlus<String> bt = new BTextPlus<String>(v, 10, 0, qualityWidth, v.getHeight());
		bt.setFlags(RSRC_HALIGN_LEFT);
		bt.setShadow(true);
		bt.setValue("Push video: ");
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
	

	protected int setupButtons(boolean isReturn) {
		long pos = ViewScreen.getResetSavedPosition(
				(StreamBabyStream) getBApp(), de.getUri(), de
						.getVideoInformation().getDuration());

		String playText = "Play";
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
			playText = "Play from beginning";
		}
		addSimpleTextButton(playText, new ButtonHandler() { 
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
		// We can only adjust quality if we can transcode this puppy.
		canTranscode = VideoModuleHelper.inst.canTranscode(de);
		canStream = VideoModuleHelper.inst.canStream(de);
		if (StreamBabyConfig.cfgQualitySelection.getBool() && canTranscode)
			addQualityButton(false);
		
		// If pyTivo running and this video found then add pyTivo push button
		if (Push.getInstance().canPush(de, VideoFormats.QUALITY_SAME))
			addPushTivoButton();

		isMore = false;

		if (StreamBabyConfig.cfgShowDelete.getBool() && de.isFile()){
			isMore = true;
		}

		/*
		if (StreamBabyConfig.cfgShowDelete.getBool() && de.isFile()){
			addSimpleTextButton("Delete now", new ButtonHandler() { 
				public boolean left() {
					popBack();
					return true;
				}
				public boolean right() {
					doDelete();
					return true;
					
				}
				public boolean select() {
					doDelete();
					return true;
				}			
			}, false);
		}
		*/
		
		BButtonPlus<ButtonHandler > gbButton = addSimpleTextButton("Go back", new ButtonHandler() { 
			public boolean left() {
				popBack();
				return true;
			}
			public boolean right() {
				if (!isMore)
					return popBack();
				else
				{
			    	MoreScreen moreScreen = new MoreScreen(getBApp(), de);
			        getBApp().push(moreScreen, TRANSITION_LEFT);					
					return true;
				}
			}
			public boolean select() {
				return popBack();
			}			
		}, true);
		

		if (isMore) {
			BTextPlus<String> bt = new BTextPlus<String>(gbButton, 10, 0, gbButton
					.getWidth() - 50, gbButton.getHeight());
			bt.setFlags(RSRC_HALIGN_RIGHT);
			bt.setShadow(true);
			bt.setValue("More        ");
			bt.setColor(getDefaultTextColor());
			bt.setFont(getDefaultFont());
			ButtonHandler h = gbButton.getValue();
			h.childViews.add(bt);
		}

		return 0;
		
	}

	private void doDelete() {
    	DeleteScreen delScreen = new DeleteScreen(getBApp(), de, 2);
        getBApp().push(delScreen, TRANSITION_LEFT);					
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


	public boolean handleKeyPress(int code, long rawcode) {
		if (super.handleKeyPress(code, rawcode))
			return true;
		Log.debug("code=" + code + " rawcode=" + rawcode);
		switch (code) {
		case KEY_CLEAR:
			if (StreamBabyConfig.cfgShowDelete.getBool() && de.isFile()){
				play("right.snd");				
				doDelete();
				return true;
			}			
		case KEY_CHANNELUP:
		case KEY_CHANNELDOWN:
			if (mview != null)
				return mview.handleKeyPress(code, rawcode);
			break;
		// case KEY_LEFT:
		// moveLeft();
		// break;
		}

		return super.handleKeyPress(code, rawcode);
	}

	public String toString() {
		if (de != null)
			return de.getStrippedFilename();
		else
			return "Play";
	}

}

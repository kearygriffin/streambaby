// $Id: ViewScreen.java 11 2008-11-f28 18:10:07Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.streambaby;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import org.arabidopsis.interval.Interval;
import org.arabidopsis.interval.IntervalTree;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BShuttleBarPlus;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BViewPlus;
import com.tivo.hme.bananas.IBananasPlus;
import com.tivo.hme.bananas.BSkin.Element;
import com.tivo.hme.bananas.layout.Layout;
import com.tivo.hme.bananas.layout.LayoutManager;
import com.tivo.hme.interfaces.IContext;
import com.tivo.hme.sdk.HmeEvent;
import com.tivo.hme.sdk.ImageResource;
import com.tivo.hme.sdk.Resource;
import com.tivo.hme.sdk.StreamResource;
import com.tivo.hme.sdk.util.Ticker;
import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.mediastreams.VideoInputStream;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.NamedStream;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.Utils;
import com.unwiredappeal.tivo.metadata.MetaData;
import com.unwiredappeal.tivo.modules.VideoFormats;
import com.unwiredappeal.tivo.modules.VideoModuleHelper;
import com.unwiredappeal.tivo.views.BRoundedPanel;
import com.unwiredappeal.tivo.views.VText;

public class ViewScreen extends ScreenTemplate implements Ticker.Client,
		Cleanupable, IdleHandler {

	/* Guesses at constants */
	public static final int RSRC_STATUS_END = 11;
	public static final int RSRC_EOB = 12;
	private static final long PREVIEW_INTERVAL = 334L;
	private static final long BEGIN_PREVIEW_WINDOW = 3;
	private static final long END_SEEK_PROTECTION = 10 * 1000L;
	private static final long BEGIN_RWD_WINDOW = 1500L;
	private static final long MAX_DUR_TIME = 1500L;

	public boolean ignoreCutList = false;

	IntervalTree cutPosTree = null;
	private BViewPlus pleaseWait;
	BView waitView = null;

	long subDuration = 0;
	boolean isMp3 = false;
	private boolean readyHandled = false;
	long lastPreviewMillis;
	boolean played;
	long lastPosition;
	long gotoPos = -1;
	private boolean isClosed = true;
	private String startMimeType = null;
	public boolean queuedToPlay;
	public NamedStream namedStream = null;
	StreamResource stream = null;
	public DirEntry de;
	public SelectionScreen initialScreen;
	public float _lastSpeed = 0;
	public boolean _isPreviewing = false;
	BRoundedPanel infoView;
	VText errorText;
	VText keypadText;
	VText waitText;
	NewStatusBar _statusBar;
	public boolean canSeek = true;
	Stack<Integer> keypad = new Stack<Integer>();
	StreamBabyStream sapp;

	int SPEEDS[] = { -60, -18, -3, 1, 3, 18, 60 };
	int stream_speed = 3;

	public static long CLOSE_TO_END_RESTART = 5000;
	long lastPositionUpdate = 0;
	long position = 0;
	long duration = 0;
	long startPosition = 0;
	boolean isComplete = false;

	long timeout_info = -1;
	long timeout_icon = -1;
	long timeout_status = -1;
	long timeout_keypad = -1;
	long timeout_idle = -1;
	int reallyClose = 0;
	long maxDuration = 0;
	long maxDurationTime = 0;
	URI deUri;
	ImageResource filmResource;
	BViewPlus icon;

	VideoInformation vinfo;
	int curVideoNum = 0;
	List<DirEntry> videoList;
	private String folderName;
	boolean playingMultiple = false;
	int quality;
	public PreviewWindow preview = null;
	BView metaview;

	public static int INFO_VIEW_HEIGHT = 300;
	public static float INFO_VIEW_TRANSPARENCY = 0.20f;
	public static int INFO_VIEW_ARC = 50;
	public static float INFO_VIEW_STROKE = 12.0f;
	public static Color INFO_VIEW_COLOR = Color.blue;
	public static String INFO_FONT_SIZE_STANDALONE = "small";
	public static String INFO_FONT_SIZE = "tiny";
	public static int INFO_SPACE = 0;
	
	public static int PLAY_ICON_X = 570;
	public static int PLAY_ICON_Y = 30;
	
	cctext cc;
	boolean CC = true;
	
	public ViewScreen(BApplicationPlus app, List<DirEntry> elist, String name,
			int quality) {
		super(app);
		this.quality = quality;
		this.folderName = name;
		this.videoList = elist;
		if (elist.size() > 1)
			playingMultiple = true;
		curVideoNum = 0;
		sapp = (StreamBabyStream) app;
		// this.de = videoList.get(0);
		// ImageResource r = this.createImage(GLOBAL.film_background);
		filmResource = sapp.filmResource;
		setDefaultBackground();
		/*
		 * if (StreamBabyConfig.inst.DEBUG) tivoCmd = new TivoCmd(app);
		 */
		sapp.addCleanupRequired(this);
	}

	/*
	// TivoCmd tivoCmd;
	public ViewScreen(BApplicationPlus app, DirEntry e, int kbps) {
		this(app, Arrays.asList(new DirEntry[] { e }), null, kbps);
	}
	*/
	
	public void updateInfoView() {
		String desc = de.getStrippedFilename();
		if (videoList.size() > 1) {
			desc = desc + " - " + (curVideoNum + 1) + "/" + videoList.size();
			if (folderName != null)
				desc = folderName + ": " + desc;
		}
		infoView.clearInnerView();
		MetaData meta = new MetaData();
		boolean hasMeta = de.getMetadata(meta);
		String siz = INFO_FONT_SIZE_STANDALONE;
		int rows = 4;
		if (hasMeta) {
			rows = 1;
			siz = INFO_FONT_SIZE;
		}
		
		Log.debug("Desc: " + desc);
		VText _infoText = new VText(infoView, infoView.getInnerBounds().x, infoView.getInnerBounds().y, 
				rows, siz);		
		_infoText.setFlags(RSRC_HALIGN_CENTER | RSRC_VALIGN_TOP
				| RSRC_TEXT_WRAP);
		_infoText.setValue(desc);
		infoView.addInner(_infoText);
		if (hasMeta) {
			int y = infoView.getInnerBounds().y + _infoText.getHeight() + INFO_SPACE;
			int h = infoView.getInnerBounds().height - (_infoText.getHeight() + INFO_SPACE);
			MetaDataViewer viewer = new MetaDataViewer();
			metaview = viewer.getView(meta, infoView, infoView.getInnerBounds().x, y, infoView.getInnerBounds().width, h);
			metaview.setVisible(infoView.getVisible());
			if (metaview != null)
				infoView.addInner(metaview);
		}
	}


	public void setDefaultBackground() {
		if (filmResource != null)
			getBelow().setResource(filmResource);
		else
			getBelow().setResource(Color.darkGray);
	}

	public void startStream() {

		// getBelow().setResource(Color.black);

		infoView = new BRoundedPanel(getNormal(), sapp
				.getSafeActionHorizontal(), sapp.getSafeActionVertical(), this
				.getWidth()
				- (sapp.getSafeActionHorizontal() * 2), INFO_VIEW_HEIGHT,
				INFO_VIEW_ARC, INFO_VIEW_STROKE, INFO_VIEW_COLOR,
				INFO_VIEW_TRANSPARENCY);
		infoView.setVisible(false);

		// keypad text (when numbers pressed)
		keypadText = new VText(getNormal(), SAFE_ACTION_H,
				GLOBAL.statusBG_Y + 35, 1, "");
		keypadText.setVisible(false);

		// Error screen text (for error reporting)
		errorText = new VText(getNormal(), SAFE_ACTION_H, getHeight()
				- getHeight() / 3, 6, "small");
		// errorText.setFlags(RSRC_HALIGN_CENTER | RSRC_TEXT_WRAP);
		errorText.setFlags(RSRC_HALIGN_CENTER | RSRC_TEXT_WRAP);
		errorText.setColor(Color.red);
		errorText.setVisible(false);

		waitText = new VText(getBelow(), SAFE_ACTION_H, getHeight() / 2, 2,
				"small");
		waitText.setFlags(RSRC_HALIGN_CENTER | RSRC_TEXT_WRAP);

		LayoutManager lm = new LayoutManager(getNormal());
		Layout safeTitle = lm.safeTitle(this);
		Layout layout = safeTitle;
		Element e = getBApp().getSkin().get(IBananasPlus.H_PLEASE_WAIT);
		layout = lm.size(layout, e.getWidth(), e.getHeight());
		layout = lm.align(layout, A_CENTER, A_CENTER);

		icon = new BViewPlus(getAbove(), PLAY_ICON_X, PLAY_ICON_Y, 32, 32, false);
		pleaseWait = new BViewPlus(this, layout, false);
		pleaseWait.setResource(e.getResource());
		pleaseWait.setVisible(false);

		playNextVideo(1);

	}

	public void playNextVideo(int dir) {
		startPosition = 0;
		duration = 0;

		errorText.setVisible(false);
		boolean ok = false;
		while (curVideoNum >= 0 && curVideoNum < videoList.size() && !ok) {
			this.de = videoList.get(curVideoNum);
			this.vinfo = de.getVideoInformation();
			deUri = de.getUri();

			if (this.vinfo == null
					|| !VideoModuleHelper.inst.canStreamOrTranscodeVideo(deUri,
							de.getVideoInformation())) {
				curVideoNum += dir;
			} else
				ok = true;
		}

		updateInfoView();
		if (preview != null) {
			preview.remove();
			preview = null;
		}
		preview = PreviewWindow.getPreviewWindow(getBelow(), de); // new
																	// PreviewWindow(getBelow(),
																	// de);

		long gotoPos = 0;
		if (!playingMultiple)
			gotoPos = getResetSavedPosition(sapp, deUri, de
					.getVideoInformation().getDuration());
		_isPreviewing = false;
		_lastSpeed = 1;
		stream_speed = 3;

		// force a reload
		duration = -1;

		if (playingMultiple) {
			timeout_info = new Date().getTime() + 1000 * 5;
			infoView.setVisible(true);
		}

		VideoInformation vinfo = de.getVideoInformation();
		if (vinfo == null
				|| !VideoModuleHelper.inst.canStreamOrTranscodeVideo(deUri, de
						.getVideoInformation())) {
			displayError("Incompatible video stream");
		} else {
			loadCutList();
			closeCC();
			cc = new cctext(getNormal(), StreamBabyConfig.cfgCCFontSize.getInt(), deUri.getPath());
			if (cc == null || !cc.exists())
				closeCC();
			if (cc != null)
				displayCCIcon();
			gotoPosition(gotoPos, null);
		}
		// gotoPosition(gotoPos, "Starting");

	}

	private void loadCutList() {
		cutPosTree = null;
		if (Utils.isFile(de.getUri())) {
			File f = new File(de.getUri().getPath() + ".edl");
			if (f.exists() && f.isFile()) {
				BufferedReader br = null;
				try {
					br = new BufferedReader(new InputStreamReader(f.toURL()
							.openStream()));
					if (br != null) {
						cutPosTree = new IntervalTree();
						String str;
						while ((str = br.readLine()) != null) {
							StringTokenizer st = new StringTokenizer(str);
							if (st.countTokens() < 3)
								continue;
							String start = st.nextToken();
							String end = st.nextToken();
							String remove = st.nextToken();
							try {
								double startd = Double.parseDouble(start);
								double endd = Double.parseDouble(end);
								startd += (StreamBabyConfig.cfgCutStartOffset
										.getInt() / 1000.0);
								endd -= (StreamBabyConfig.cfgCutEndOffset
										.getInt() / 1000.0);
								int rem = Integer.parseInt(remove);
								if (rem == 0 && startd > 0 && endd > 0
										&& endd > startd) {
									Interval i = new Interval(startd, endd);
									cutPosTree.insert(i);
								}

							} catch (NumberFormatException ne) {
							}
						}
					}
				} catch (IOException e) {
					cutPosTree = null;
				} finally {
					if (br != null)
						try {
							br.close();
						} catch (IOException e) {
						}
				}
			}
		}

	}

	private static long getSavedPosition(IContext context, URI uri) {
		long pos = 0;
		String stringPos = context.getPersistentData(persistKey(uri));
		if (stringPos != null && stringPos.length() > 0) {
			try {
				pos = Long.parseLong(stringPos);
			} catch (Exception e) {
			}
		}
		return pos;
	}

	public static long getResetSavedPosition(StreamBabyStream app, URI uri,
			long dur) {
		long saved = getSavedPosition(app.getContext(), uri);
		if (saved < 0)
			saved = 0;
		long left = dur - saved;
		if (left < CLOSE_TO_END_RESTART) {
			saved = 0;
			app.cachePersistentData(persistKey(uri), "0", true);
		}
		return saved;
	}

	public boolean handleEnter(Object arg, boolean isReturn) {
		sapp.setCurrentScreen(this);
		startStream();
		return true;
	}

	private void gotoPosition(long pos) {
		if (isPreviewing())
			gotoPreviewPosition(pos);
		else
			gotoPosition(pos, null);
	}

	public long getVideoLength() {
		if (vinfo != null) {
			long len = vinfo.getVideoLength();
			if (len <= 0)
				return duration;
			else
				return len;
		} else
			return duration;
	}

	public long getRealVideoLength() {
		if (vinfo != null)
			return vinfo.getVideoLength();
		return 0;
	}

	private void gotoPosition(long pos, String ssString) {
		Log.debug("goto position: " + pos + ", vidlen: " + getVideoLength());
		if (getVideoLength() > 0 && pos >= getVideoLength()) {
			pos = getVideoLength();
			return;
		}
		if (pos < 0)
			pos = 0;

		// String fileName = de.getFilename();
		// Let's make sure we can open the stream...
		// Start the stream
		if (stream != null && pos >= startPosition
				&& pos <= (startPosition + duration)) {
			Log.debug("Position in buffer, seeking");
			stream.setPosition(pos - startPosition);
			stream_speed = 3;
			changeSpeed(1);

			if (stream.isPaused())
				stream.play();
			return;
		}

		// if we can't seek, don't try!
		// if dur < 0, it means we haven't started playing yet...
		if (duration >= 0 && !canSeek) {
			play("bonk.snd");
			pos = startPosition + duration;
		}

		Log.debug("Position not in buffer, starting new stream");
		// if (stream != null)
		// getBelow().remove(stream);

		// Don't seek too close to the end
		//
		if (getVideoLength() - pos < END_SEEK_PROTECTION) {
			pos = getVideoLength() - END_SEEK_PROTECTION;
			if (pos < 0)
				pos = 0;
		}
		displayStatusBar(false);
		if (waitView != null)
			waitView.setVisible(false);
		waitView = null;
		if (ssString == null
				&& StreamBabyConfig.cfgUsePleaseWait.getBool() == false)
			ssString = "Seeking...";
		if (ssString != null) {
			// ssString = "Seeking";
			waitText.setValue(ssString);
			waitView = waitText;
		} else {
			waitView = pleaseWait;
		}
		waitView.setVisible(true);
		gotoPos = pos;
		if (_statusBar != null)
			setStatusBarMode(BShuttleBarPlus.MODE_PLAY);
		stream_speed = 3;
		setLastSpeed(1);
		// getBelow().setResource(Color.black);
		if (stream != null) {
			if (!stream.isPaused())
				stream.pause();
		}
		closeStream();
		Ticker.master.add(this, System.currentTimeMillis() + 250, null);
		// finishGoto(gotoPos);
	}

	public void finishGoto(long pos) {
		gotoPos = -1;
		long startSec = (pos / 1000);
		startPosition = (startSec * 1000);
		maxDuration = 0;
		if (!VideoModuleHelper.inst.canStreamOrTranscodeVideo(deUri, de
				.getVideoInformation()))
			displayError("Incompatible video stream");
		else {
			VideoInputStream is = null;
			Log.debug("Openening stream at position: " + startPosition + "("
					+ startPosition / 1000 + " secs)");
			if (quality == VideoFormats.QUALITY_AUTO)
				quality = sapp.getAutoQuality();
			is = VideoModuleHelper.inst.openVideo(deUri, de
					.getVideoInformation(), startPosition, quality);
			// URL streamUrl = new URL(fileName + "?start=" + startSec);
			// debug.print("Creating name stream: " + streamUrl.toString());

			if (is == null) {
				displayError("Failed to open stream");
				// getBelow().clearResource();
				// getBelow().setResource(Color.black);
				// getBelow().setResource(filmResource);
				setDefaultBackground();

			} else {
				canSeek = is.canPosition();
				if (!canSeek)
					startPosition = 0;				
				this.vinfo = is.getVideoInformation();
				subDuration = is.getSubDuration();
				if (startPosition != 0)
					startPosition = getVideoLength() - subDuration;
				startMimeType = is.getMimeType();
				namedStream = new NamedStream(is.getInputStream(), subDuration);
				namedStream.setContentType(is.getContentType());
				queuedToPlay = true;
				played = false;
				possiblyStartNext();
			}
		}
	}

	public void possiblyStartNext() {
		if (queuedToPlay && isClosed) {
			stream = createVideoStream(getContext().getBaseURI().toString()
					+ namedStream.getStreamName(), startMimeType, true);
			if (startMimeType.compareTo("audio/mp3") == 0)
				isMp3 = true;
			else
				isMp3 = false;
			readyHandled = false;
			isComplete = false;
			stream.addHandler(this);
			Resource r = stream;
			if (StreamBabyConfig.inst._DEBUG && sapp.isSimulator()) {
				r = createImage(StreamBabyConfig.cfgBackgroundImage.getValue());
			}
			getBelow().setResource(r);
			queuedToPlay = false;
		}

	}

	public void changeSpeed(float speed) {
		Log.debug("speed=" + speed);
		if (isPreviewing() && speed == 1) {
			speed = 0;
		}
		setLastSpeed(speed);
		if (!isPreviewing())
			stream.setSpeed(speed);
		if (speed == 0) {
			setStatusBarMode(BShuttleBarPlus.MODE_PAUSE);
		} else if (speed == GLOBAL.slow_speed) {
			setStatusBarMode(BShuttleBarPlus.MODE_SLOW);
		} else {
			String mode = BShuttleBarPlus.MODE_PLAY;
			switch (stream_speed) {
			case 0:
				mode = BShuttleBarPlus.MODE_REVERSE3;
				break;
			case 1:
				mode = BShuttleBarPlus.MODE_REVERSE2;
				break;
			case 2:
				mode = BShuttleBarPlus.MODE_REVERSE1;
				break;
			case 3:
				mode = BShuttleBarPlus.MODE_PLAY;
				break;
			case 4:
				mode = BShuttleBarPlus.MODE_FORWARD1;
				break;
			case 5:
				mode = BShuttleBarPlus.MODE_FORWARD2;
				break;
			case 6:
				mode = BShuttleBarPlus.MODE_FORWARD3;
				break;

			}
			setStatusBarMode(mode);
		}
		displayStatusBar(true);
	}

	public synchronized void displayStatusBar(Boolean state) {
		if (_statusBar == null) {
			return;
		}
		if (state == true && (waitView == null)) {
			timeout_status = new Date().getTime() + 1000
					* GLOBAL.timeout_status_bar;
			_statusBar.makeVisible(true);
		} else {
			timeout_status = -1;
			_statusBar.makeVisible(false);
		}
	}

	public void handleReady() {
		if (readyHandled)
			return;
		// Clear out Starting text and display status bar
		// waitText.setVisible(false);
		if (waitView != null) {
			waitView.setVisible(false);
			waitView = null;
		}
		if (_statusBar == null) {
			_statusBar = new NewStatusBar(getBelow());
			setStatusBarMode(BShuttleBarPlus.MODE_PLAY);

		}
		// changeSpeed(lastSpeed);
		displayStatusBar(true);
		readyHandled = true;
	}

	public void error() {
		closeStream();
		displayStatusBar(false);
		displayError("Failed to open stream");
		if (waitView != null)
			waitView.setVisible(false);
		waitView = null;

	}

	public synchronized boolean isStatusBarVisible() {
		return _statusBar.visible;
	}

	public synchronized void updateStatusBar(long p, long d, long bs, long be) {
		_statusBar.Update(p, d, bs, be);
	}

	public synchronized void updateStatusBar(long p) {
		_statusBar.Update(p);
	}

	public synchronized void setStatusBarMode(String mode) {
		_statusBar.setMode(mode);
	}

	public void displayError(String error) {
		Log.debug("error=" + error);
		errorText.setValue(error);
		errorText.setVisible(true);
		if (waitView != null) {
			waitView.setVisible(false);
			waitView = null;
		}
	}

	public synchronized boolean handleEvent(HmeEvent event) {
		Log.verbose("ReceivedEvent: " + event.toString());
		if (stream == null) {
			return super.handleEvent(event);
		}
		// Log.verbose("event=" + event);
		// Clear text if timeouts reached
		long date = new Date().getTime();
		if (timeout_info > 0 && date >= timeout_info) {
			infoView.setVisible(false);
			timeout_info = -1;
		}
		if (timeout_status > 0 && date >= timeout_status
				&& stream.getSpeed() == 1 && isStatusBarVisible()) {
			displayStatusBar(false);
			timeout_status = -1;
		}

		if (timeout_keypad > 0 && date >= timeout_keypad) {
			keypadText.setVisible(false);
			timeout_keypad = -1;
			keypad.removeAllElements();
		}
		if (timeout_icon > 0 && date >= timeout_icon) {
			icon.clearResource();
			icon.setVisible(false);
			timeout_icon = -1;
		}

		// Update stream position and duration information
		if (event instanceof HmeEvent.ResourceInfo) {
			HmeEvent.ResourceInfo info = (HmeEvent.ResourceInfo) event;
			if (info != null && info.getMap().get("position") != null) {
				position = Long.parseLong(info.getMap().get("position")
						.toString());
				duration = Long.parseLong(info.getMap().get("duration")
						.toString());
				lastPositionUpdate = System.currentTimeMillis();
			}
			if (isMp3)
				duration = getVideoLength();
			if (isComplete && duration < subDuration) {
				duration = subDuration;
			}
			Resource r = info.getResource();
			int status = r.status;
			if (stream == null || r.getID() != stream.getID()) {
				Log.debug("received message for old stream, ignoring...");
				return super.handleEvent(event);
			}
			if (duration > maxDuration) {
				maxDuration = duration;
				maxDurationTime = date;
			}
			//if (ccEnabled())
				//cc.display(position+startPosition);
			// int status = stream.getStatus();
			// debug.print("ResourceStatus(" + r.toString() + "): " + status
			// + ", pos: " + position + ", dur: " + duration);
			if (status == RSRC_EOB) {
				if (position == 0 && duration == 0) {
					//error();
				} else {
					boolean rebuffered = possibleRebuffer(date);
					if (!rebuffered
							&& getRealVideoLength() > 0
							&& getRealVideoLength()
									- (startPosition + duration) < 20000L)
						endOfVideo();
					else if (!rebuffered) {
						// if (stream_speed != 3) {
						stream.pause();
						changeSpeed(0);
						displayStatusBar(true); // keep status bar displayed
						timeout_status = -1;
						// }
					}
				}

			} else if (status == RSRC_STATUS_ERROR) {
				// Display error associated with starting a stream
				displayError("ERROR: " + info.getMap().get("error.text") + "\n"
						+ de.getFilename());
			} else if (status == RSRC_STATUS_END) {
				Log.debug("END: ");
				boolean rebuffered = possibleRebuffer(date);
				if (!rebuffered
						&& getRealVideoLength() > 0
						&& getRealVideoLength() - (startPosition + duration) < 20000L)
					endOfVideo();
			} else if (status == RSRC_STATUS_COMPLETE) {
				Log.debug("COMPLETE: ");
				isComplete = true;
				// boolean rebuffered = possibleRebuffer(date);
				// if (!rebuffered)
				// endOfVideo();
			} else if (status == RSRC_STATUS_READY) {
				Log.verbose("RSSRC_STATUS_READY");
				handleReady();
			} else if (status == RSRC_STATUS_CONNECTING) {
			} else if (status == RSRC_STATUS_CLOSED) {
				isClosed = true;
				possiblyStartNext();
			} else if (status == RSRC_STATUS_PAUSED) {
				handleReady();
				if (position == 0 && duration == 0 && !played) {
					error();
				}
			} else if (status == RSRC_STATUS_PLAYING) {
				handleReady();
				played = true;
				if (stream_speed > 3
						&& (duration - position) < BEGIN_PREVIEW_WINDOW
								* SPEEDS[stream_speed] * 1000L) {
					beginPreviewMode();
				} else {
					if (!ignoreCutList && stream_speed == 3) {
						long cutpos = getEndCutPos(position + startPosition);
						if (cutpos > 0
								&& cutpos < ((startPosition + duration) - 8000)) {
							Log.debug("Jumping to cutpos: " + cutpos);
							gotoPosition(cutpos);
						}
					}
				}
			}
			// if (status >= RSRC_STATUS_PLAYING && statusBar != null &&
			// statusBar.visible) {
			if (status >= RSRC_STATUS_PLAYING && _statusBar != null) {
				if (isPreviewing()) {
					// previewTick();
				}
				lastPosition = position;
				// Update status bar even when not being displayed
				// statusBar.Update(position, duration);
				if (!isPreviewing())
					updateStatusBar(startPosition + position, getVideoLength(),
							startPosition, startPosition + duration);
				else
					updateStatusBar(-1, getVideoLength(), startPosition,
							startPosition + duration);
				// statusBar.Update();
				if (startPosition > BEGIN_RWD_WINDOW && stream_speed != 3
						&& position < BEGIN_RWD_WINDOW)
					beginPreviewMode();
				else if (position == 0 && stream_speed != 3) {
					// stream_speed = 3;
					// changeSpeed(0);
					beginPreviewMode();

				}
				if (stream_speed == 3)
					savePosition(false);
			}

		}

		return super.handleEvent(event);
	}

	@SuppressWarnings("unchecked")
	private long getEndCutPos(long pos) {
		double secs = pos / 1000.0;
		if (cutPosTree == null)
			return -1;
		List<Interval> cutList = (List<Interval>) cutPosTree
				.searchAll(new Interval(secs, secs));
		if (cutList != null && !cutList.isEmpty()) {
			double maxsecs = Double.MIN_VALUE;
			Iterator<Interval> it = cutList.iterator();
			while (it.hasNext()) {
				Interval i = it.next();
				if (i.getHigh() > maxsecs)
					maxsecs = i.getHigh();
			}
			long retpos = (long) (maxsecs * 1000);
			if (Math.abs(retpos - pos) < 1000)
				return -1;
			return retpos;
		} else
			return -1;
	}
	
   public Boolean ccEnabled() {
	      boolean enabled = true;
	      if (!CC) enabled = false;
	      if (cc == null) enabled = false;
	      if (stream == null) enabled = false;
	      if (stream != null && stream.getSpeed() != 1) enabled = false;
	      if (_statusBar != null && _statusBar.visible) enabled = false;

	      if (cc != null ) {
	         if (enabled)
	            cc.on = true;
	         else {
	        	if (cc.on)
	        		cc.setVisible(false);
	            cc.on = false;
	         }
	      }
	      return enabled;
	   }


	private boolean possibleRebuffer(long date) {
		if (!canSeek)
			return false;
		long maxDelta = date - maxDurationTime;
		long vidlen = getRealVideoLength();
		if (vidlen == 0)
			return false;
		long endDelta = vidlen - (startPosition + duration);
		if (maxDurationTime > 0 && maxDelta > MAX_DUR_TIME && stream_speed == 3
				&& endDelta > 5000 && (duration) > 300000L) {
			// We are at the end of the 1.1G buffer, reload
			// pretend we are a closed stream
			gotoPosition(startPosition + duration + 1,
					"Rebuffering... Please wait...");
			return true;
		}
		return false;
	}

	private StreamResource createVideoStream(String uri, String mime,
			boolean play) {
		StreamResource s = createStream(uri, mime, play);
		s.addHandler(this);
		return s;
	}

	public boolean handleKeyPress(int code, long rawcode) {
		Log.debug("code=" + code + " rawcode=" + rawcode);
		if ((stream == null || _statusBar == null) && code != KEY_LEFT
				&& code != KEY_CHANNELUP && code != KEY_CHANNELDOWN) {
			return false;
		}
		/*
		 * if (stream == null) { // Directory mode switch (code) { case
		 * KEY_LEFT: // pop back to calling screen getBApp().pop();
		 * this.remove(); return true; } } else {
		 */
		int mins;
		boolean handled = false;
		if (isPreviewing()) {
			handled = handlePreviewKey(code, rawcode);
		}
		// Streaming video mode
		if (!handled) {
			switch (code) {

			case KEY_LEFT:
				/*
				 * // Close and remove stream if (stream.getStatus() >=
				 * RSRC_STATUS_PLAYING ) { stream.pause(); stream.close();
				 * stream.remove(); stream = null; try { namedStream.close();
				 * namedStream = null; } catch (IOException e) {
				 * e.printStackTrace(); System.out.println("Had trouble closing
				 * cstream"); } }
				 * 
				 */
				endPlaying();

				return true;
			case KEY_PAUSE:
				// Toggle between pause and play modes
				if (stream.isPaused()) {
					stream.play();
					stream_speed = 3;
					changeSpeed(1);
					displayStatusBar(true);
				} else {
					stream.pause();
					changeSpeed(0);
					displayStatusBar(true); // keep status bar displayed
					timeout_status = -1;
				}
				return true;
			case KEY_PLAY:
				if (isStatusBarVisible() && stream.getSpeed() == 1) {
					displayStatusBar(false);
				} else {
					stream_speed = 3;
					changeSpeed(1);

				}
				return true;
			case KEY_REVERSE:
				if (getLastSpeed() == 0) {
					stream_speed = 3;
				}
				stream_speed -= 1;
				if (stream_speed > -1) {
					changeSpeed(SPEEDS[stream_speed]);
				} else {
					stream_speed = 3;
					changeSpeed(1);
				}
				if (stream_speed < 0)
					stream_speed = 0;
				if (stream_speed == 2 || stream_speed == 4)
					play("speedup1.snd");
				if (stream_speed == 1 || stream_speed == 5)
					play("speedup2.snd");
				if (stream_speed == 0 || stream_speed == 6)
					play("speedup3.snd");
				if (stream_speed == 3)
					play("slowdown1.snd");
				return true;
			case KEY_FORWARD:
				if (getLastSpeed() == 0) {
					stream_speed = 3;
				}
				stream_speed += 1;
				if (stream_speed < 7) {
					changeSpeed(SPEEDS[stream_speed]);
				} else {
					stream_speed = 3;
					changeSpeed(1);
					stream_speed = 3;
				}
				if (stream_speed > 6)
					stream_speed = 6;
				if (stream_speed == 2 || stream_speed == 4)
					play("speedup1.snd");
				if (stream_speed == 1 || stream_speed == 5)
					play("speedup2.snd");
				if (stream_speed == 0 || stream_speed == 6)
					play("speedup3.snd");
				if (stream_speed == 3)
					play("slowdown1.snd");
				return true;
			case KEY_REPLAY:
				play("left.snd");
				mins = getMinutes();
				if (mins > 0) {
					// Jump n minutes back
					gotoPosition((startPosition + position) - mins * 60000);
					// stream.setPosition(position - mins*60000);
					keypadText.setVisible(false);
				} else {
					// Instant Replay
					long skip = 1000 * GLOBAL.skip_back;
					if (getLastSpeed() != 0 && stream_speed < 3) {
						skip = (15 * 60) * 1000L;
					} else if (getLastSpeed() != 0 && stream_speed > 3) {
						skip = (-15 * 60) * 1000L;
					}
					movePosition(-1 * skip);
				}
				// Clear keypad numbers
				keypadText.setVisible(false);
				timeout_keypad = -1;
				keypad.removeAllElements();
				// Display status bar
				displayStatusBar(true);
				return true;
			case KEY_ADVANCE:
				play("right.snd");
				mins = getMinutes();
				if (mins > 0) {
					// Jump n minutes forward
					gotoPosition((startPosition + position) + mins * 60000);
					// stream.setPosition(position + mins*60000);
					keypadText.setVisible(false);
				} else {
					// Skip forwards
					long skip = 1000 * GLOBAL.skip_forwards;
					if (getLastSpeed() != 0 && stream_speed < 3) {
						skip = (-15 * 60) * 1000L;
					} else if (getLastSpeed() != 0 && stream_speed > 3) {
						skip = (15 * 60) * 1000L;
					}
					movePosition(skip);
				}
				// Clear keypad numbers
				keypadText.setVisible(false);
				timeout_keypad = -1;
				keypad.removeAllElements();
				// Display status bar
				displayStatusBar(true);
				return true;
			case KEY_THUMBSDOWN:
				ignoreCutList = !ignoreCutList;
				/*
				 * if (tivoCmd != null) { mins = getMinutes(); int cmd = mins;
				 * int arg = -1; if (mins >= 0) { if (mins > 100) { arg = mins /
				 * 100; cmd = mins = 100; } Log.debug("Sending cmd: " + cmd + ",
				 * arg: " + arg); tivoCmd.sendCmd(stream.getID(), cmd, arg);
				 * keypadText.setVisible(false); timeout_keypad = -1;
				 * keypad.removeAllElements(); } } else {
				 *  }
				 */
				break;
			case KEY_ENTER:
				play("right.snd");
				mins = getMinutes();
				Log.debug("enter: goto position: " + mins + " mins");
				if (mins >= 0) {
					// play("select.snd");
					gotoPosition(mins * 60000);
				}
				keypadText.setVisible(false);
				timeout_keypad = -1;
				keypad.removeAllElements();
				// Display status bar
				displayStatusBar(true);
				return true;
			case KEY_SLOW:
				changeSpeed(GLOBAL.slow_speed);
				return true;
			case KEY_CLEAR:
				// Clear text and status bar
				// Really want to leave error on screen if we are not playing
				// multiple files
				clearDisplay();
				return true;
				/*
				 * case KEY_INFO: // Display file name temporarily if
				 * (timeout_info == -1) { timeout_info = new Date().getTime() +
				 * 1000*GLOBAL.timeout_info; infoText.setVisible(true); } else {
				 * infoText.setVisible(false); timeout_info = -1; }
				 * play("select.snd"); return true;
				 */
			case KEY_INFO:
				// KJM added
				// Display file name temporarily
				if (timeout_info == -1) {
					timeout_info = new Date().getTime() + 1000
							* GLOBAL.timeout_info;
					infoView.setVisible(true);
				} else {
					infoView.setVisible(false);
					timeout_info = -1;
				}
				// play("select.snd");
				return true;
			case KEY_OPT_ASPECT:
				CC = !CC;
				// make it hide/show immediately?
				//if (ccEnabled())
					//cc.display(position+startPosition);
				handleCC();
				if (cc == null)
					play("bonk.snd");
				else
					play("updown.snd");
				displayCCIcon();
				break;
			case KEY_NUM0:
				addKey(0);
				play("updown.snd");
				return true;
			case KEY_NUM1:
				addKey(1);
				play("updown.snd");
				return true;
			case KEY_NUM2:
				addKey(2);
				play("updown.snd");
				return true;
			case KEY_NUM3:
				addKey(3);
				play("updown.snd");
				return true;
			case KEY_NUM4:
				addKey(4);
				play("updown.snd");
				return true;
			case KEY_NUM5:
				addKey(5);
				play("updown.snd");
				return true;
			case KEY_NUM6:
				addKey(6);
				play("updown.snd");
				return true;
			case KEY_NUM7:
				addKey(7);
				play("updown.snd");
				return true;
			case KEY_NUM8:
				addKey(8);
				play("updown.snd");
				return true;
			case KEY_NUM9:
				addKey(9);
				play("updown.snd");
				return true;
			case KEY_CHANNELUP:
			case KEY_CHANNELDOWN:
				if (infoView != null && infoView.getVisible() && metaview != null) {
					// reset timer
					timeout_info = new Date().getTime() + 1000 * GLOBAL.timeout_info;
					return metaview.handleKeyPress(code, rawcode);
				}
				int dir = (code == KEY_CHANNELUP ? 1 : -1);
				int newVidNum = curVideoNum + dir;
				if (newVidNum < 0 || newVidNum >= videoList.size()) {
					play("bonk.snd");
					return true;
				}
				curVideoNum = newVidNum;
				playNextVideo(dir);
				return true;

			} // switch
		}
		return super.handleKeyPress(code, rawcode);
	}
	
	private void displayCCIcon() {
		if (icon == null)
			return;
		timeout_icon = new Date().getTime() + 1000
		* GLOBAL.timeout_icon;
		icon.clearResource();
		if (cc == null || !CC) {
			icon.setResource("cc-off.png");
		}
		else {
			icon.setResource("cc.png");
		}
		icon.setVisible(true);
	}

	private void movePosition(long l) {
		if (!isPreviewing()) {
			long np = position + l;
			if (Math.abs(l) > (60 * 1000L)) {
				if (np < 0 || np > duration) {
					beginPreviewMode(l);
					return;
				}
			}
			stream.setPosition(position + l);
		} else {
			movePreviewPosition(l);
		}

	}
	
	private void clearDisplay() {
		if (playingMultiple)
			errorText.setVisible(false);
		infoView.setVisible(false);
		displayStatusBar(false);
		// Clear keypad numbers
		keypadText.setVisible(false);
		timeout_keypad = -1;
		keypad.removeAllElements();
		timeout_info = -1;
		icon.clearResource();
		icon.setVisible(true);
		timeout_icon = -1;
	}

	private void beginPreviewMode() {
		beginPreviewMode(0L);
	}

	private void beginPreviewMode(long offset) {
		if (isPreviewing())
			return;
		infoView.setVisible(false);
		lastPreviewMillis = System.currentTimeMillis();
		stream.pause();

		displayStatusBar(true);
		timeout_status = -1;
		setIsPreviewing(true);
		long newPos = (startPosition + position) + offset;
		long vidlen = getVideoLength();
		if (newPos < 0)
			newPos = 0;
		else if (newPos > vidlen) {
			newPos = vidlen;
		}
		preview.setPosition(newPos);
		preview.setNextFrame(newPos, 0);
		preview.makeVisible(true);
		// Start up the timer
		//Ticker.master.add(this, System.currentTimeMillis(), null);
	}

	public synchronized void setIsPreviewing(boolean b) {
		_isPreviewing = b;
	}

	public synchronized boolean isPreviewing() {
		return _isPreviewing;
	}

	public synchronized float getLastSpeed() {
		return _lastSpeed;
	}

	public synchronized void setLastSpeed(float sp) {
		_lastSpeed = sp;
	}
	
	public synchronized boolean handleCC() {
		if (ccEnabled()) {
			long delta = System.currentTimeMillis() - lastPositionUpdate;
			cc.display(position+startPosition+delta);
			return true;
		} else
			return false;
	}

	public synchronized long tick(long tm, Object arg) {
		
		if (getBApp().isApplicationClosing())
			return -1;
		// Check to see if the application has closed down
		// and return -1 so the ticker doesn't re-register us.
		if (gotoPos != -1) {
			finishGoto(gotoPos);
			gotoPos = -1;
		}
		
		long ctime = System.currentTimeMillis();		
		long nextPreview = previewTick();
		
		long nextCC;
		boolean ccret = handleCC();
		if (ccret)
			nextCC = ctime + 100;
		else
			nextCC = ctime + 500;

		flush();
		
		long nxt = (nextPreview == -1) ? nextCC : Math.min(nextPreview, nextCC);
		return nxt;
	}

	public long previewTick() {
		if (!isPreviewing() || getBApp().isApplicationClosing()) {
			return -1;

		}

		long curTime = System.currentTimeMillis();

		long p = preview.getNextFrame();
		preview.setPosition(p);
		updateStatusBar(p);

		p = preview.getPosition();
		// long delta = curTime - lastPreviewMillis;
		long delta = PREVIEW_INTERVAL;
		// if (delta < PREVIEW_INTERVAL)
		// return 0;
		lastPreviewMillis = curTime;
		int secDelta = (int) ((float) delta * getLastSpeed());
		p = p + (long) ((float) delta * getLastSpeed());
		long vidlen = getVideoLength();
		if (p < 0) {
			p = 0;
			setLastSpeed(0);
			setStatusBarMode(BShuttleBarPlus.MODE_PAUSE);
		} else if (p > vidlen) {
			setLastSpeed(0);
			p = vidlen;
			setStatusBarMode(BShuttleBarPlus.MODE_PAUSE);
		}
		preview.setNextFrame(p, secDelta);
		return curTime + PREVIEW_INTERVAL;
	}

	private void movePreviewPosition(long l) {
		long np = preview.getPosition() + l;
		if (np < 0)
			np = 0;
		else if (np > getVideoLength())
			np = getVideoLength();
		preview.setNextFrame(np, 0);
	}

	private void gotoPreviewPosition(long pos) {
		preview.setNextFrame(pos, 0);
	}

	private boolean handlePreviewKey(int code, long rawcode) {
		switch (code) {
		case KEY_PLAY:
			startFromPreview();
			return true;
		case KEY_PAUSE:
			if (getLastSpeed() == 0) {
				// we are paused, so resume playing
				startFromPreview();
			} else {
				stream_speed = 3;
				changeSpeed(0);
			}

			return true;
		case KEY_CLEAR:
			// exit preview, then proceed to regular clear
			exitPreview();
			stream_speed = 3;
			changeSpeed(0);
			displayStatusBar(true);
			return true;
		}
		return false;
	}

	public void startFromPreview() {
		exitPreview();
		play("right.snd");
		// stream_speed = 3;
		// changeSpeed(1);
		long pos = preview.getPosition();
		gotoPosition(pos);
	}

	public void exitPreview() {
		setIsPreviewing(false);
		preview.makeVisible(false);
		updateStatusBar(lastPosition + startPosition);
	}

	public void addKey(int num) {
		Log.debug("num=" + num);
		timeout_keypad = new Date().getTime() + 5000;
		if (keypad.size() < 3) {
			keypad.push(num);
		}
		String val = "";
		for (int i = 0; i < keypad.size(); ++i) {
			val = String.format("%s%d", val, keypad.get(i));
		}
		keypadText.setValue(val);
		keypadText.setVisible(true);
		displayStatusBar(true);
	}

	public int getMinutes() {
		int mins = -1;
		if (timeout_keypad != -1 && keypad.size() > 0) {
			mins = 0;
			int multiplier = 1;
			for (int i = keypad.size() - 1; i >= 0; i--) {
				mins += keypad.get(i) * multiplier;
				multiplier *= 10;
			}
		}
		return mins;
	}

	private void savePosition(boolean saveCache) {
		if (deUri == null)
			return;
		if (playingMultiple)
			return;
		// save position
		String stringPos = "0";
		long curpos = startPosition + position;
		if (curpos < 0)
			curpos = 0;
		if ((getVideoLength() - curpos) > 60000L) {
			// debug.print("Iime till end: " + (getVideoLength()-curpos));
			stringPos = Long.toString(curpos);
		} else {
			// debug.print("Resetting start time, at end");
		}
		sapp.cachePersistentData(persistKey(deUri), stringPos, saveCache);
	}

	public static String persistKey(URI uri) {
		return "mpos:" + uri.toString();
	}

	private void endOfVideo() {
		startPosition = 0;
		position = 0;
		setIsPreviewing(false);
		savePosition(true);
		if (playingMultiple) {
			curVideoNum++;
			if (curVideoNum < videoList.size()) {
				playNextVideo(1);
				return;
			}
		}
		endPlaying();
	}

	private void endPlaying() {
		setIsPreviewing(false);
		savePosition(true);
		play("left.snd");

		// Pop back to file browser mode
		getBApp().pop();
		this.remove();
		// play("left.snd");

		// Re-refresh directory listing (re-build ViewScreen list)
		// initialScreen.updateFileList(de.getParent());
		// initialScreen.focusOn(de.getName());
		// initialScreen.resetBackground();
	}

	private void closeStream() {
		if (stream != null) {
			// if (stream.getStatus() >= RSRC_STATUS_PLAYING) {
			if (!stream.isPaused())
				stream.pause();
			stream.close();
			// }
			stream.remove();
		}
		stream = null;
	}

	public void closeCC() {
		if (cc != null)
			cc.remove();
		cc = null;
	}
	public void remove() {
		closeCC();
		sapp.removeCleanupRequire(this);
		closeStream();
		if (_statusBar != null)
			_statusBar.remove();
		if (waitText != null) {
			waitText.setValue(null);
			waitText.clearResource();
			waitText.remove();
			waitText = null;
		}
		if (pleaseWait != null) {
			pleaseWait.clearResource();
			pleaseWait.remove();
			pleaseWait = null;
		}
		if (infoView != null) {
			infoView.clearInnerView();
			infoView.clearResource();
			infoView.remove(null);
		}
		infoView = null;
		if (errorText != null) {
			errorText.setValue(null);
			errorText.clearResource();
			errorText.remove();
			errorText = null;
		}
		if (keypadText != null) {
			keypadText.setValue(null);
			keypadText.clearResource();
			keypadText.remove();
			keypadText = null;
		}
		if (icon != null) {
			icon.clearResource();
			icon.remove();
			icon = null;
		}

		if (preview != null) {
			preview.remove();
			preview = null;
		}

		super.remove();
	}

	public void cleanup() {
		remove();
	}

	public void finalize() {
		this.remove();
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		if (de != null)
			return de.getStrippedFilename();
		return null;
	}

	public boolean isIdle() {
		if (stream == null)
			return true;
		if (errorText.isVisibile())
			return true;
		return false;
	}

}

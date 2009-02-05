package com.unwiredappeal.tivo.html.cobra;

import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.gui.HtmlBlockPanel;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.util.gui.ColorFactory;

@SuppressWarnings("serial")
public class SBHtmlPanel extends HtmlPanel {
	/**
	 * Method invoked internally to create a {@link HtmlBlockPanel}.
	 * It is made available so it can be overridden.
	 */
	@Override
	protected HtmlBlockPanel createHtmlBlockPanel(UserAgentContext ucontext, HtmlRendererContext rcontext) {
		return new HtmlBlockPanel(ColorFactory.TRANSPARENT, false, ucontext, rcontext, this);
	}


}

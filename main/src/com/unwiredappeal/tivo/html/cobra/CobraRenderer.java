package com.unwiredappeal.tivo.html.cobra;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.style.RenderState;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.unwiredappeal.tivo.html.BaseHtmlRenderer;

public class CobraRenderer extends BaseHtmlRenderer {

	public BufferedImage getImage(int width, int height) {
		if (!isModified() && this.width == width && this.height == height)
			return bi;
		BufferedImage bi = null;
		File fileToDelete = null;
		Graphics graphics = null;
		
		try {
			if (bi != null) {
				setModified(false);
				this.width = width;
				this.height = height;
			}
			
			// Find the encoding of the text in the URL
			String encoding = null; // connection.getContentEncoding();
			if (encoding == null) {
			    encoding = "ISO-8859-1";
			}
	
			String rurl = this.url;
			if (!urlBased) {
				rurl = htmlToFileUrl();
				if (rurl == null)
					return null;
				fileToDelete = new File(rurl);
			}
	
			// Set up the HTML renderer
			final SBHtmlPanel panel = new SBHtmlPanel();
	
			// HTML component support
			SimpleUserAgentContext uContext = new SimpleUserAgentContext();
	
			// NoopHtmlRenderContext implements HtmlRendererContext 
			// but does nothing.
			HtmlRendererContext rContext = 
			    new SimpleHtmlRendererContext(panel, uContext);
	
			// Parse the HTML
			DocumentBuilderImpl builder = new DocumentBuilderImpl(
			    rContext.getUserAgentContext(),
			    rContext);
			Document doc;
			try {
				doc = builder.parse(rurl);
			} catch (SAXException e1) {
				return null;
			} catch (IOException e1) {
				return null;
			}
	
			// Since no layout manager is used setBounds() must be called.
			panel.setOpaque(false);
	        panel.setDefaultMarginInsets(new Insets(0,0,0,0));
			panel.setDefaultOverflowX(RenderState.OVERFLOW_HIDDEN);
			panel.setDefaultOverflowY(RenderState.OVERFLOW_HIDDEN);
			panel.setPreferredWidth(width);
			panel.setBounds(0, 0, width, height);
			panel.setBorder(null);
			panel.addNotify(); 
			panel.setDoubleBuffered(false);
			panel.setDocument(doc, rContext);
	
			doc.setDocumentURI(baseUrl);
			bi = 
			    new BufferedImage(width, 
			                      height, 
			                      BufferedImage.TYPE_4BYTE_ABGR);
			graphics = bi.getGraphics();
			final Graphics fgraphics = graphics;
	
			// Cobra insists that the rendering is 
			// done in the Swing UI thread
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
				        public void run() {
				            panel.print(fgraphics);
				        }
				});
			} catch (InterruptedException e) {
				return null;
			} catch (InvocationTargetException e) {
				return null;
			}
		} finally {
			if (graphics != null)
				graphics.dispose();
			if (fileToDelete != null)
				fileToDelete.delete();
			
		}
	
		return bi;

	}
	

}

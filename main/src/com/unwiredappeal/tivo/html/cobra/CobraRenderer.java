package com.unwiredappeal.tivo.html.cobra;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.domimpl.HTMLDocumentImpl;
import org.lobobrowser.html.domimpl.HTMLImageElementImpl;
import org.lobobrowser.html.domimpl.ImageEvent;
import org.lobobrowser.html.domimpl.ImageListener;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.style.RenderState;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.html2.HTMLCollection;
import org.xml.sax.SAXException;

import com.unwiredappeal.tivo.html.BaseHtmlRenderer;
import com.unwiredappeal.tivo.utils.Log;

public class CobraRenderer extends BaseHtmlRenderer {

	@SuppressWarnings({ "serial", "deprecation" })
	public BufferedImage[] getImages(final int width, final int height) {
		if (!isModified() && this.width == width && this.height == height)
			return bis;
		File fileToDelete = null;
		Graphics graphics = null;
		
		try {
			if (bis != null) {
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
			// Since no layout manager is used setBounds() must be called.
			panel.setOpaque(false);
	        panel.setDefaultMarginInsets(new Insets(0,0,0,0));
			panel.setDefaultOverflowX(RenderState.OVERFLOW_HIDDEN);
			panel.setDefaultOverflowY(RenderState.OVERFLOW_HIDDEN);
			panel.setPreferredWidth(width);
			//panel.setBounds(0, 0, width, height);
			panel.setBorder(null);
			panel.addNotify(); 
			panel.setDoubleBuffered(false);			
	
			// HTML component support
			SimpleUserAgentContext uContext = new SimpleUserAgentContext();
	
			// NoopHtmlRenderContext implements HtmlRendererContext 
			// but does nothing.
			final HtmlRendererContext rContext = 
			    new SimpleHtmlRendererContext(panel, uContext);
	
			// Parse the HTML
			DocumentBuilderImpl builder = new DocumentBuilderImpl(
			    rContext.getUserAgentContext(),
			    rContext);
			final Document doc;
			
			try {
				doc = builder.createDocument(new InputSourceImpl(new FileReader(new File(new URI(rurl))), baseUrl));
				//((HTMLDocumentImpl)doc).setBaseURI(baseUrl);
				((HTMLDocumentImpl)doc).load();
				//doc = builder.parse(rurl);
			} catch (SAXException e1) {
				return null;
			} catch (IOException e1) {
				return null;
			} catch (URISyntaxException e) {
				return null;
			}
	
			HTMLCollection col = ((HTMLDocumentImpl)doc).getImages();
			
			imageCount = 0;
			int localImageCount = 0;
			for (int i=0;i<col.getLength();i++) {
				Node node = col.item(i);
				deltaImages(1);
				localImageCount++;
				HTMLImageElementImpl imgNode = (HTMLImageElementImpl)node;
				imgNode.addImageListener(new ImageListener() {

					public void imageLoaded(ImageEvent event) {
						deltaImages(-1);
					} });
				
			}
			while(deltaImages(0) > 0) {
				sleep(50);
			}
			
			
			final JPanel parentPanel = new JPanel( new BorderLayout() )
	        {
	            private int CUSTOM_WIDTH = width;
	            public Dimension getPreferredSize()
	            {
	                Dimension d = super.getPreferredSize();
	                d.width = CUSTOM_WIDTH;
	                return d;
	            }
	            public Dimension getMinimumSize()
	            {
	                Dimension d = super.getMinimumSize();
	                d.width = CUSTOM_WIDTH;
	                return d;
	            }
	            public Dimension getMaximumSize()
	            {
	                Dimension d = super.getMaximumSize();
	                d.width = CUSTOM_WIDTH;
	                return d;
	            }
	        };

	        parentPanel.add(panel);			
	


			try {
				SwingUtilities.invokeAndWait(new Runnable() {
				        public void run() {
							panel.setDocument(doc, rContext);
					        parentPanel.setSize( parentPanel.getPreferredSize() );
					        parentPanel.doLayout();
					        panel.doLayout();
					        parentPanel.setSize( parentPanel.getPreferredSize());
					        panel.setSize(panel.getPreferredSize());
							Log.debug("ParentPanelPref:" + panel.getSize());
				        }
				});
			} catch (InterruptedException e) {
				return null;
			} catch (InvocationTargetException e) {
				return null;
			}

			// I can't figure out why I need to wait here
			if (localImageCount > 0) {
				sleep(200);
			}
			int images = ((int)parentPanel.getSize().getHeight() + (height-1))/height;
			int totalHeight = (int)parentPanel.getSize().getHeight();
			if (totalHeight < height)
				totalHeight = height;
			final int iheight = totalHeight;

			// Set up the HTML renderer
			final SBHtmlPanel npanel = new SBHtmlPanel();
			// Since no layout manager is used setBounds() must be called.
			npanel.setOpaque(false);
	        npanel.setDefaultMarginInsets(new Insets(0,0,0,0));
			npanel.setDefaultOverflowX(RenderState.OVERFLOW_HIDDEN);
			npanel.setDefaultOverflowY(RenderState.OVERFLOW_HIDDEN);
			npanel.setPreferredWidth(width);
			npanel.setBounds(0, 0, width, iheight);
			npanel.setBorder(null);
			npanel.addNotify(); 
			npanel.setDoubleBuffered(false);		
			
			BufferedImage bi = 
			    new BufferedImage(width, 
			                      iheight, 
			                      BufferedImage.TYPE_4BYTE_ABGR);
			graphics = bi.getGraphics();
			final Graphics fgraphics = graphics;

			// Cobra insists that the rendering is 
			// done in the Swing UI thread
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
				        public void run() {
							npanel.setDocument(doc, rContext);				        	
							Container c = new Container();
					        SwingUtilities.paintComponent(fgraphics, npanel, c, 0, 0, width, iheight);
					        
				        	//npanel.print(fgraphics);
				        }
				});
			} catch (InterruptedException e) {
				return null;
			} catch (InvocationTargetException e) {
				return null;
			}

			int heightLeft = iheight;
			bis = new BufferedImage[images];
			for (int i=0;i<images;i++) {
				bis[i] = bi.getSubimage(0, height*i, width, Math.min(height, heightLeft));
				heightLeft -= height;
			}

	
		} finally {
			if (graphics != null)
				graphics.dispose();
			if (fileToDelete != null)
				fileToDelete.delete();
			
		}
	
		return bis;

	}
	
	int imageCount = 0;
	public synchronized int deltaImages(int delta) {
		imageCount += delta;
		return imageCount;
	}
	
	public void sleep(int n) {
		try {
			Thread.sleep(n);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

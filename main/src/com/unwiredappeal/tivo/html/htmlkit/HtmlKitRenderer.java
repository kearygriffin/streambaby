package com.unwiredappeal.tivo.html.htmlkit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;



import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.html.BaseHtmlRenderer;
import com.unwiredappeal.tivo.utils.Log;


public class HtmlKitRenderer extends BaseHtmlRenderer {
	@SuppressWarnings("serial")
	public BufferedImage[] getImages(final int width, final int height) {
		if (!isModified() && this.width == width && this.height == height)
			return bis;
		BufferedImage bi = null;
		File fileToDelete = null;
		Graphics graphics = null;
		
		try {
			if (bi != null) {
				setModified(false);
				this.width = width;
				this.height = height;
			}
			
			String rurl = this.url;
			if (!urlBased) {
				rurl = htmlToFileUrl();
				if (rurl == null)
					return null;
				fileToDelete = new File(rurl);
			}

			final JEditorPane pane = new JEditorPane();
			JPanel parentPanel = new JPanel( new BorderLayout() )
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

	        parentPanel.add(pane);			
	        pane.setEditable(false);
	        pane.setMargin(new Insets(0,0,0,0));
	        SBHtmlEditorKit kit = new SBHtmlEditorKit(baseUrl);
			pane.setEditorKit(kit);
			//pane.setPreferredSize(new Dimension(width, 0));
			File css = new File(StreamBabyConfig.convertRelativePath(StreamBabyConfig.cfgDefaultCss.getValue(), StreamBabyConfig.streamBabyDir + File.separator));
			if (css.exists()) {
				try {
					kit.getStyleSheet().importStyleSheet(css.toURL());
				} catch (MalformedURLException e) {
				}
			}
			//pane.setBounds(0, 0, width, height);
			pane.setOpaque(false);
			pane.setDoubleBuffered(false);
			/*
	        final Object lock = new Object();
	        loaded = false;
	        pane.addPropertyChangeListener(new PropertyChangeListener() {
	            public void propertyChange(PropertyChangeEvent evt) {
	              if (evt.getPropertyName().equals("page")) {
	            	  synchronized(lock) {
	            		  loaded = true;
	            		  lock.notify();
	            	  }
	              }
	            }
	          });
			*/
			try {
				pane.setPage(rurl);
			} catch (IOException e1) {
				return null;
			}
			/*
	        synchronized(lock) {
	        	while(!loaded) {
	        		lock.wait();
	        	}
	        }
	        */

	        parentPanel.setSize( parentPanel.getPreferredSize() );
	        parentPanel.doLayout();
	        parentPanel.setSize( parentPanel.getPreferredSize() );
			Log.debug("ParentPanelPref:" + parentPanel.getSize());
			int images = ((int)parentPanel.getSize().getHeight() + (height-1))/height;
			int iheight = (int)parentPanel.getSize().getHeight();
			if (iheight < height)
				iheight = height;
			bi = 
			    new BufferedImage(width, 
			                      iheight, 
			                      BufferedImage.TYPE_4BYTE_ABGR);
			graphics = bi.getGraphics();
			final Graphics fgraphics = graphics;
			Container c = new Container();
	        SwingUtilities.paintComponent(fgraphics, pane, c, 0, 0, width, iheight);
			bis = new BufferedImage[images];
			int heightLeft = iheight;
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

}

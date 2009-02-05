package com.unwiredappeal.tivo.html.htmlkit;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;


import com.unwiredappeal.tivo.html.BaseHtmlRenderer;


public class HtmlKitRenderer extends BaseHtmlRenderer {
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
			
			String rurl = this.url;
			if (!urlBased) {
				rurl = htmlToFileUrl();
				if (rurl == null)
					return null;
				fileToDelete = new File(rurl);
			}

			final JEditorPane pane = new JEditorPane();
	        pane.setEditable(false);
	        pane.setMargin(new Insets(0,0,0,0));

			pane.setEditorKit(new SBHtmlEditorKit(baseUrl));
			pane.setBounds(0, 0, width, height);
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
			bi = 
			    new BufferedImage(width, 
			                      height, 
			                      BufferedImage.TYPE_4BYTE_ABGR);
			graphics = bi.getGraphics();
			final Graphics fgraphics = graphics;
			Container c = new Container();
	        SwingUtilities.paintComponent(fgraphics, pane, c, 0, 0, width, height);		
	      } finally {
			if (graphics != null)
				graphics.dispose();
			if (fileToDelete != null)
				fileToDelete.delete();
			
		}
	
		return bi;
	}

	

}

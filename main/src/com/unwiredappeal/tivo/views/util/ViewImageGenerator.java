package com.unwiredappeal.tivo.views.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.tivo.hme.bananas.BApplication;
import com.tivo.hme.bananas.BTilesPlus;import com.tivo.hme.sdk.ImageResource;

import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BViewPlus;
import com.unwiredappeal.tivo.streambaby.StreamBabyStream;

public class ViewImageGenerator {

	//private static Map<String, ImageResource> resourceMap = Collections.synchronizedMap(new HashMap<String, ImageResource>());
	//private static Map<String, Rectangle> rectangleMap = Collections.synchronizedMap(new HashMap<String, Rectangle>());

	private String imgId;
	Rectangle boundingRectangle;

	StreamBabyStream app;
	
	public ViewImageGenerator(BApplication bapp) { app = (StreamBabyStream)bapp; }
	public void createRoundedCorners(int width, int height, int arc,
			float strokeWidth, Color borderColor) {
		reset();
		imgId = "roundc" + "-" + width + "." + height + "." + arc + "." + strokeWidth + "." + borderColor.getRGB() + "-";
		if (isResourceCached())
			return;
		else
			generateRoundedCorners(width, height, arc, strokeWidth, borderColor);
	}
	
	public void generateRoundedCorners(int width, int height, int  arc, float strokeWidth, Color color) {
        int xarc = arc;
        int yarc = arc;
        int nw = xarc*2;
        int nh = yarc*2;

		BufferedImage bi = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
        BasicStroke stroke = new BasicStroke(strokeWidth);
       
        g2d.setStroke(stroke);
        g2d.setColor(color);

        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, nw, nh, xarc, yarc);
        int bx = (int)(strokeWidth+1);
        int by = 0;
        while(!roundedRectangle.contains(bx, by))
        	by++;
        boundingRectangle = new Rectangle(bx, by, width-2*bx, height-2*by);
        getRectangleMap().put(imgId + "rect", boundingRectangle);
        
        g2d.draw(roundedRectangle);
        /*
        FloodFill ff = new FloodFill(bi);
        ff.fill(0, 0, color);
        ff.fill(0, nh-1, color);
        ff.fill(nw-1, nh-1, color);
        ff.fill(nw-1, 0, color);
        bi = ff.getImage();
        */
        g2d.dispose();
        Map<String, ImageResource> rmap = getResourceMap();
        rmap.put(imgId + "tl", app.createImage(bi.getSubimage(0, 0, xarc, yarc)));
        rmap.put(imgId + "tr", app.createImage(bi.getSubimage(bi.getWidth()-xarc, 0, xarc, yarc)));
        rmap.put(imgId + "bl", app.createImage(bi.getSubimage(0, bi.getHeight()-yarc, xarc, yarc)));
        rmap.put(imgId + "br", app.createImage(bi.getSubimage(bi.getWidth()-xarc, bi.getHeight()-yarc, xarc, yarc)));
        rmap.put(imgId + "top", app.createImage(bi.getSubimage(xarc, 0, xarc/2, (int)strokeWidth)));
        rmap.put(imgId + "bottom", app.createImage(bi.getSubimage(xarc, bi.getHeight()-(int)strokeWidth, (xarc/2), (int)strokeWidth)));
        rmap.put(imgId + "left", app.createImage(bi.getSubimage(0, yarc, (int)strokeWidth, yarc/2)));
        rmap.put(imgId + "right", app.createImage(bi.getSubimage(bi.getWidth()-(int)strokeWidth, yarc, (int)strokeWidth, yarc/2)));
	}
	
	public ImageResource getImageResource(String t) {
		return getResourceMap().get(imgId + t);
	}
	
	public boolean isResourceCached() {
		boundingRectangle = getRectangleMap().get(imgId + "rect"); 
		return boundingRectangle != null;
	}
	
	public void reset() {
		imgId = null;
	}
	
	public void drawImages(BView parent) {
		// stick in the corners
		ImageResource rtl= this.getImageResource("tl");
		BViewPlus v = new BViewPlus(parent, 0, 0, rtl.getWidth(), rtl.getHeight(), true);
		v.setResource(rtl);
		ImageResource rtr = this.getImageResource("tr");
		v = new BViewPlus(parent, parent.getWidth()-rtr.getWidth(), 0, rtr.getWidth(), rtr.getHeight(), true);
		v.setResource(rtr);
		ImageResource rbl= this.getImageResource("bl");
		v = new BViewPlus(parent, 0, parent.getHeight()-rbl.getHeight(), rbl.getWidth(), rbl.getHeight(), true);
		v.setResource(rbl);
		ImageResource rbr= this.getImageResource("br");
		v = new BViewPlus(parent, parent.getWidth()-rbr.getWidth(), parent.getHeight()-rbr.getHeight(), rbr.getWidth(), rbr.getHeight(), true);
		v.setResource(rbr);
		
		ImageResource rtop= this.getImageResource("top");
		v = new BTilesPlus(parent, rtl.getWidth(), 0, parent.getWidth()-(rtl.getWidth()+rtr.getWidth()), rtop.getHeight(), rtop.getWidth(), rtop.getHeight());
		v.setResource(rtop);
		
		ImageResource rbot= this.getImageResource("bottom");
		v = new BTilesPlus(parent, rbr.getWidth(), parent.getHeight()-rbot.getHeight(), parent.getWidth()-(rtl.getWidth()+rtr.getWidth()), rbot.getHeight(), rbot.getWidth(), rbot.getHeight());
		v.setResource(rbot);
		
		ImageResource rleft= this.getImageResource("left");
		v = new BTilesPlus(parent, 0, rtr.getHeight(), rleft.getWidth(), parent.getHeight()-(rtl.getHeight()+rbl.getHeight()), rleft.getWidth(), rleft.getHeight());
		v.setResource(rleft);
		
		ImageResource rright = this.getImageResource("right");
		v = new BTilesPlus(parent, parent.getWidth()-rright.getWidth(), rtr.getHeight(), rright.getWidth(), parent.getHeight()-(rtr.getHeight()+rbr.getHeight()), rright.getWidth(), rright.getHeight());
		v.setResource(rright);
		
	}
	public Rectangle getOffsetRectangle(int xoff, int yoff) {
		Rectangle r = new Rectangle(boundingRectangle);
		r.grow(-xoff, -yoff);
		return r;
	}
	
	//private static Map<String, ImageResource> resourceMap = Collections.synchronizedMap(new HashMap<String, ImageResource>());
	//private static Map<String, Rectangle> rectangleMap = Collections.synchronizedMap(new HashMap<String, Rectangle>());

	@SuppressWarnings("unchecked")
	public synchronized Map<String, Rectangle> getRectangleMap() {
		Map<String, Rectangle> rmap = (Map<String, Rectangle>)(app.getApplicationObject(ViewImageGenerator.class.getCanonicalName()+ ".rectangleMap"));
		if (rmap != null)
			return rmap;
		else {
			rmap = Collections.synchronizedMap(new HashMap<String, Rectangle>());
			app.setApplicationObject(ViewImageGenerator.class.getCanonicalName() + ".rectangleMap", rmap);
			return rmap;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public synchronized Map<String, ImageResource> getResourceMap() {
		Map<String, ImageResource> rmap = (Map<String, ImageResource>)(app.getApplicationObject(ViewImageGenerator.class.getCanonicalName() + ".imageMap"));
		if (rmap != null)
			return rmap;
		else {
			rmap = Collections.synchronizedMap(new HashMap<String, ImageResource>());
			app.setApplicationObject(ViewImageGenerator.class.getCanonicalName() + ".imageMap", rmap);
			return rmap;
		}
		
	}

}

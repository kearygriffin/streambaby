package com.unwiredappeal.tivo.html.fs;

import java.awt.image.BufferedImage;

import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.ImageUtil;

public class SBFSJava2DRenderer extends Java2DRenderer {

	public SBFSJava2DRenderer(String url, String baseUrl, int width,
			int height) {
		super(url, baseUrl, width, height);
	}

	@Override
	protected BufferedImage createBufferedImage(int width, int height) {
		//BufferedImage image = ImageUtil.createCompatibleBufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		//ImageUtil.clearImage(image);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		/*		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = image.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
		Rectangle2D.Double rect = new Rectangle2D.Double(0,0,width,height); 
		g2d.fill(rect);
		g2d.setPaintMode();
		g2d.dispose();
		*/
		return image;
	}
}

package com.unwiredappeal.mediastreams;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.unwiredappeal.tivo.config.StreamBabyConfig;

public class ScalingUtils {
	int sw, sh;
	public  float quality = (StreamBabyConfig.cfgPreviewQuality.getInt() / 100.0f);
	
	private ImageWriter jpgWriter = null;
	private ImageReader jpgReader;
	public ScalingUtils(int sw, int sh) {
		this.sw = sw;
		this.sh = sh;
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
        if (iter.hasNext()) {
        	jpgWriter= iter.next();
        }
        Iterator<ImageReader> riter = ImageIO.getImageReadersByFormatName("jpg");
        if (riter.hasNext()) {
        	jpgReader= riter.next();
        }

	}

	byte[] scaleData(byte[] b) {
	   ImageInputStream iis;
	   ByteArrayInputStream is = new ByteArrayInputStream(b);
	   try {
		iis = ImageIO.createImageInputStream(is);
		   jpgReader.setInput(iis);
		   BufferedImage img = jpgReader.read(0, new ImageReadParam());
		   if (img.getWidth() == sw && img.getHeight() == sh)
			   return b;
           BufferedImage scaledImage = new BufferedImage(sw, 
         	      sh, BufferedImage.TYPE_INT_RGB);
 	    Graphics2D graphics2D = scaledImage.createGraphics();
 	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 	      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 	    graphics2D.drawImage(img, 0, 0, sw, sh, null);
 	    graphics2D.dispose();
 	    ByteArrayOutputStream bs = new ByteArrayOutputStream();
         ImageOutputStream ios;
			ios = ImageIO.createImageOutputStream(bs);
         jpgWriter.setOutput(ios);
         // Set the compression quality
         ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
         iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
         iwparam.setCompressionQuality(quality);
 
         // Write the image
         jpgWriter.write(null, new IIOImage(scaledImage, null, null), iwparam);
         // Cleanup
         ios.flush();		                
         ios.close();
         return bs.toByteArray();
	   } catch (IOException e) {
		   return b;
	   }
	}
}

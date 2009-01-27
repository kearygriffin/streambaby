package com.unwiredappeal.mediastreams;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import com.unwiredappeal.tivo.utils.Utils;

public class ZipPreviewer implements PreviewGenerator {
	ByteArrayOutputStream bs = new ByteArrayOutputStream();
	private ZipFile zip = null;
	int sw, sh;
	public  float quality = (StreamBabyConfig.cfgPreviewQuality.getInt() / 100.0f);
	int off = 0;
	
	private ImageWriter jpgWriter = null;
	private ImageReader jpgReader;
	
	public ZipPreviewer() {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
        if (iter.hasNext()) {
        	jpgWriter= iter.next();
        }
        Iterator<ImageReader> riter = ImageIO.getImageReadersByFormatName("jpg");
        if (riter.hasNext()) {
        	jpgReader= riter.next();
        }

	}
	public void close() {
		if (zip != null) {
			try {
				zip.close();
			} catch (IOException e) {
			}
			zip = null;
		}
		
	}

	private boolean attemptToOpen(String filename) {
  	  try {
			zip = new ZipFile(filename);
		} catch (Exception e) {
			//debug.print("Failed to open zip file: " + previewFilename);
			return false;
		}
		return true;
	}

	public static File getCacheFile(File file, String add) {
		if (add == null)
			add = "";
		File f = new File(StreamBabyConfig.cacheDir, file.getName() + "-" + file.length() + ".pvw" + add);
		return f;
	}
	private boolean _open(File file) {
		if (file == null)
			return false;
		String previewFilename = file.getAbsolutePath() + ".pvw";
		if (attemptToOpen(previewFilename))
			return true;
		if (StreamBabyConfig.cacheDir != null) {
			String name = file.getName();
			File f = new File(StreamBabyConfig.cacheDir, name + ".pvw");
			if (attemptToOpen(f.getAbsolutePath()))
				return true;
			f = getCacheFile(file, "");
			return attemptToOpen(f.getAbsolutePath());
			
		}
		return false;
	}
	public boolean open(URI uri, VideoInformation vidinfo, int sw, int sh) {
		if (uri == null || !Utils.isFile(uri))
			return false;
      this.sw = sw;
      this.sh = sh;
      File file = new File(uri);

		
	    if (_open(file)) {
	    	// do we start at 0 or 1?
	    	  ZipEntry e = zip.getEntry(getEntryName(0));
	    	  if (e != null)
	    		  return true;
	    	   e = zip.getEntry(getEntryName(1));
	    	  if (e != null)
	    		  off = 1;
	    	  return true;
	    }
	    // attempt in our cache
	    return false;
	}

	public static String getEntryName(int i) {
		return String.format("img-%05d.jpg", i);
	}
	public byte[] getFrameImageData(int secs) {
		   String iname = getEntryName(secs+off);
		   
		   ZipEntry e = zip.getEntry(iname);
		   try {
			   if (e != null) {
				   InputStream is = zip.getInputStream(e);
				   if (is != null) {
					   
					   //bs.reset();
					   //copy(is, bs);
					   //byte[] ba = bs.toByteArray();
					   //return ba;
					   try {
						   ImageInputStream iis;
						   iis = ImageIO.createImageInputStream(is);
						   jpgReader.setInput(iis);
						   BufferedImage img = jpgReader.read(0, new ImageReadParam());
			               BufferedImage scaledImage = new BufferedImage(sw, 
			                	      sh, BufferedImage.TYPE_INT_RGB);
			        	    Graphics2D graphics2D = scaledImage.createGraphics();
			        	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			        	      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			        	    graphics2D.drawImage(img, 0, 0, sw, sh, null);
			        	    graphics2D.dispose();
			                bs.reset();
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
			                
			                byte[] ba  = bs.toByteArray();
			                return ba;
						} catch (Exception e1) {
							e1.printStackTrace();
						}
		        	    
				   }
			   }
		   } catch(IOException ioe) { }

		   return null;
	}
	
	/*
	private static final int IO_BUFFER_SIZE = 4 * 1024;  
	private byte[] b_copy = new byte[IO_BUFFER_SIZE];  

 	private void copy(InputStream in, OutputStream out) throws IOException {  
		   
		   int read;  
		   while ((read = in.read(b_copy)) != -1) {  
		   out.write(b_copy, 0, read);  
		   }  
		   }
	 */
	public boolean isRealtime() {
		return true;
	}
	public void prepare(int secs, int delta) {
		// TODO Auto-generated method stub
		
	}  

}

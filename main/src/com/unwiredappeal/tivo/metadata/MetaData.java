package com.unwiredappeal.tivo.metadata;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.w3c.tidy.Tidy;

import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.utils.Log;

public class MetaData {
	

	public static final int METADATA_URL = 1;
	public static final int METADATA_STRING = 2;
	public static final int METADATA_HTML = 3;
	public static final int METADATA_IMAGE = 4;
	
	boolean isConverted = false;
	String data = null;
	BufferedImage bufferedImage = null;
	int dataType = -1;
	String urlStr = null;
	String title = null;
	
	public void copy(MetaData copy) {
		copy.data = data;
		copy.bufferedImage = bufferedImage;
		copy.dataType = dataType;
		copy.urlStr = urlStr;
		copy.isConverted = isConverted;
		copy.title = title;
	}
	
	public String getUrl() {
		return urlStr;
	}
	public void setUrl(String urlStr) {
		this.urlStr = urlStr;
		dataType = METADATA_URL;
	}

	public void setBaseUrl(String urlStr) {
		// just set the url to use for relative references.
		this.urlStr = urlStr;
	}
	
	public void convertUrl() {
		if (isConverted)
			return;
		isConverted = true;
		try {
			URL url = new URL(this.urlStr);
			URLConnection c = url.openConnection();
			c.connect();
			InputStream is = c.getInputStream();
			if (c.getContentType().startsWith("image/")) {
				setImage(is);
			}
			else
				set(is);
		} catch (MalformedURLException e) {
			Log.error("Malformed url for metadata: " + urlStr);
		} catch (IOException e) {
			Log.error("IOError reading metadata url: " + urlStr);
		}
	}
	
	public void setImage(InputStream is) {
		try {
			bufferedImage = ImageIO.read(is);
			dataType = METADATA_IMAGE;
		} catch (IOException e) {
			Log.error("Error reading image metadata");
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}
	
	public void setImage(File f) {
		try {
			InputStream is = new FileInputStream(f);
			setImage(is);
		} catch (FileNotFoundException e) {
			Log.error("Error open image file: " + f.getAbsolutePath());
		}
	}
	
	public void setImage(URL url) {
		try {
			setImage(url.openStream());
		} catch (IOException e) {
			Log.error("Error openining image URL: " + url);
		}
	}
	
	public void set(InputStream is) {
		Reader r = null;
		StringWriter w  = null;
		try {
			r = new BufferedReader(new InputStreamReader(is));
			w = new StringWriter();
			char[] buf = new char[4096];
			int len;
			while((len = r.read(buf)) > 0) {
				w.write(buf, 0, len);
			}
			setString(w.toString());
		} catch (IOException e) {
			Log.error("Can't read metadata stream");
		} finally {
			if (w != null)
				try {
					w.close();
				} catch (IOException e) {
				}
			if (r != null)
				try {
					r.close();
				} catch (IOException e) {
				}
		}
	}
	
	public void setString(String str) {
		str = str.trim();
		if (str.startsWith("<"))
			setHtml(str);
		else {
			data = str;
			dataType = METADATA_STRING;
		}
	}
	
	public class TidyLogger extends Writer {

		@Override
		public void close() throws IOException {
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
		}
		
	}
	public void setHtml(String html) {
		//Log.verbose("PreTidy: " + html);
		if (StreamBabyConfig.cfgForceTidy.getBool()) {
			Tidy t = new Tidy();
			t.setQuiet(true);
			t.setErrout(new PrintWriter(new TidyLogger()));
			t.setXHTML(StreamBabyConfig.cfgTidtXhtml.getBool());
			StringReader sr = new StringReader(html);
			StringWriter sw = new StringWriter();
			InputStream is = new ReaderInputStream(sr);
			OutputStream os = new WriterOutputStream(sw);
			t.parse(is, os);
			try {
				os.close();
			} catch (IOException e) {
			}
			try {
				is.close();
			} catch (IOException e) {
			}
			data = sw.toString();
			//Log.verbose("PostTidy: " + data);
		}
		else
			data = html;
		dataType = METADATA_HTML;
	}
	
	public boolean hasMetaData() {
		return dataType != -1;
	}
	
	public String getMetadata() {
		return data;
	}
	
	public int getMetadataType() {
		return dataType;
	}
	
	public String toString() {
		return data == null ? "" : data;
	}
	
	public class WriterOutputStream extends OutputStream {
		Writer w;
		public WriterOutputStream(Writer w)  {
			this.w = w;
		}
		@Override
		public void write(int b) throws IOException {
			w.write(b);
		}
		@Override
		public void close() throws IOException {
			super.close();
			w.close();
		}
		
	}
	
	public class ReaderInputStream extends InputStream {

		Reader r;
		public ReaderInputStream(Reader r) {
			this.r = r;
		}
		@Override
		public int read() throws IOException {
			return r.read();
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			r.close();
		}
	}

	public BufferedImage getImage() {
		return bufferedImage;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String t) {
		title = t;
	}
	
	public boolean hasTitle() {
		return title != null;
	}
}

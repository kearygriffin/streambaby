package com.unwiredappeal.tivo.metadata;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;


import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.FileRandomAccessDataSource;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoInputStream;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.RandomAccessDataSource;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxContainer;
import com.coremedia.iso.boxes.BoxInterface;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.apple.*;
import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.RandomAccessFileInputStream;
import com.unwiredappeal.tivo.utils.Utils;

public class MP4MetadataModule extends BaseMetadataModule {

	public static ConfigEntry cfgMp4Metadata = new ConfigEntry(
			"meta.mp4.disable",
			"false",
			"Disable mp4 metadata processing"
			);
	
	@Override
	public boolean initialize(StreamBabyModule m) {
		super.initialize(m);
		if (cfgMp4Metadata.getBool() == true)
			return false;
		Logger.getLogger(BoxFactory.class.getName()).setLevel(Level.WARNING);
		return true;
	}
	public static final int PRIORITY=40;
	public int getSimplePriority() {
		return PRIORITY;
	}

	public int getMetadataPriority() {
		return PRIORITY;
	}
	

	 @SuppressWarnings("unchecked")
	 public static class SBBoxFactory extends BoxFactory {
		   
	   Class[] arBoxesToParse = new Class[] { 
			   MovieBox.class,
			   UserDataBox.class,
			   MetaBox.class,
			   AppleItemListBox.class,
			   AppleNameBox.class,
			   AppleArtistBox.class,
				AppleAlbumBox.class,
				AppleCommentBox.class,
				AppleCopyrightBox.class,
				AppleCustomGenreBox.class,
				AppleRecordingYearBox.class,
				AppleTrackNumberBox.class,
				AppleTrackAuthorBox.class,
				AppleTrackTitleBox.class,
				AppleTvEpisodeBox.class,
				AppleTvSeasonBox.class,
				AppleCoverBox.class,
			   
	   };
	   
	   List<Class> boxesToParse = Arrays.asList(arBoxesToParse);
	   
	   public static class EmptyBox extends Box {
		   long bcontentSize;
		   long bsize;
		   long boffset;
		   
			protected EmptyBox(byte[] type, long size, long offset) {
				super(type);
				this.boffset = offset;
				this.bsize = size;
			}
	
			@Override
			protected void getContent(IsoOutputStream os) throws IOException {
				//
			}
	
			@Override
			protected long getContentSize() {
				return bcontentSize;
			}
	
			@Override
			public String getDisplayName() {
				return "EmptyBox";
			}
	
			@Override
			public void parse(IsoInputStream in, long contentSize, BoxFactory boxFactory)
					throws IOException {
				this.bcontentSize = contentSize;
			    if (in.getStreamPosition() - boffset < bsize && contentSize != -1) {
				    in.skip((int) (bsize - (in.getStreamPosition() - boffset)));
				 }
			}
		   
	   }
	   protected boolean parseThisBox(Box box) {
		   return boxesToParse.contains(box.getClass());
	   }
	  /**
	   * Parses the next size and type, creates a box instance and parses the box's content.
	   *
	   * @param in     the IsoInputStream pointing to the ISO file
	   * @param parent the current box's parent (null if no parent)
	   * @return the box just parsed
	   * @throws IOException if reading from <code>in</code> fails
	   */
	  public Box parseBox(IsoInputStream in, BoxInterface parent) throws IOException {
	    long offset = in.getStreamPosition();

	    long size = in.readUInt32();
	    // do plausibility check
	    if (size < 8 && size > 1) {
	      Log.warn("Plausibility check failed: size < 8 (size = " + size + "). Stop parsing!");
	      throw new IOException();
	    } else if ((offset + size) > parent.getIsoFile().getOriginalIso().length()) {
	      Log.warn("Plausibility check failed: offset + size > file size (size = " + size + "). Stop parsing!");
	      throw new IOException();
	    }


	    byte[] type = in.read(4);

	    byte[] usertype = null;
	    long contentSize;

	    if (size == 1) {
	      size = in.readUInt64();
	      contentSize = size - 16;
	    } else if (size == 0) {
	      //throw new RuntimeException("Not supported!");
	      contentSize = -1;
	      size = 1;
	    } else {
	      contentSize = size - 8;
	    }
	    if (Arrays.equals(type, IsoFile.fourCCtoBytes("uuid"))) {
	      usertype = in.read(16);
	      contentSize -= 16;
	    }
	    Box box = createBox(type, usertype,
	            parent.getType());
	    if (!parseThisBox(box)) {
	    	box = new EmptyBox(box.getType(), size, offset);
	    }
	    box.setParent((BoxContainer) parent);
	   // LOG.finest("Creating " + IsoFile.bytesToFourCC(box.getType()) + " box: (" + box.getDisplayName() + ")");
	    // System.out.println("parsing " + Arrays.toString(box.getType()) + " " + box.getClass().getName() + " size=" + size);
	    box.parse(in, contentSize, this);
	    // System.out.println("box = " + box);
	    if (in.getStreamPosition() - offset < size && contentSize != -1) {
	      // System.out.println("dead bytes found in " + box);
	      box.setDeadBytes(in.read((int) (size - (in.getStreamPosition() - offset))));
	    	//in.skip((int) (size - (in.getStreamPosition() - offset)));
	    }

	    /*if (box.getSize() < 10000000) {
		      ByteArrayOutputStream baos = new ByteArrayOutputStream((int) box.getSize());
		      box.getBox(new IsoOutputStream(baos), new RandomAccessFile(box.getIsoFile().getFile(), "r"));
		      RandomAccessFile raf = new RandomAccessFile(box.getIsoFile().getFile(), "r");
		      raf.seek(offset);
		      byte[] orig = new byte[(int) box.getSize()];
		      raf.readFully(orig);

		      if (!Arrays.equals(baos.toByteArray(), orig)) {
		        String a = dumpBytes(baos.toByteArray());
		        String b = dumpBytes(orig);
//		        throw new RuntimeException("The written box content is not equal to the actual content in the file");
		        System.err.print("content");
		        box.getBox(new IsoOutputStream(baos), new RandomAccessFile(box.getIsoFile().getFile(), "r"));
		      }
		    } */
	    /*
	    if (size != box.getSize()) {
	      //    System.err.println();
	      box.getSize();
	    }
	    */
	    box.offset = offset;

	    assert size == box.getSize() : "Reconstructed " + IsoFile.bytesToFourCC(box.getType()) + " Size is not equal to the number of parsed bytes! (" + box.getDisplayName() + ")"
	            + " Actual Box size: " + box.getSize() + " Calculated size: " + size;
	    return box;
	  }
	}
	
	public static class StreamBabyIsoFile extends IsoFile {
		public BoxFactory sbBoxFactory = new SBBoxFactory();
		
		public StreamBabyIsoFile(RandomAccessDataSource originalIso) {
			super(originalIso);
		}
		public StreamBabyIsoFile(SBDataSource source) {
			super(source);
		}
	  public void parse() throws IOException {
		    IsoInputStream isoIn = new IsoInputStream(this.getOriginalIso());
		    //List<Box> boxeList = new LinkedList<Box>();
		    boolean done = false;
		    while (!done) {
		      try {
		        long sp = isoIn.getStreamPosition();
		        Box box = sbBoxFactory.parseBox(isoIn, this);
		        if (box != null) {
		          //boxeList.add(box);
		          addBox(box);
		          //this.boxes = boxeList.toArray(new Box[boxeList.size()]);
		          assert box.calculateOffset() == sp : "calculated offset differs from offset in file";
		        } else {
		          done = true;
		        }
		      } catch (EOFException e) {
		        done = true;
		      }
		    }
		  }

	}

	protected boolean parseIsoBoxes(URI uri, File f, MetaData m, VideoInformation vi) throws IOException {
		Map<String, String> metaMap = null;
		SBDataSource isosource = new SBDataSource(f);				
		IsoFile iso = null;
		try {
			iso = new StreamBabyIsoFile(isosource);		
			iso.parse();
			MovieBox[] moov = iso.getBoxes(MovieBox.class);
			if (moov == null || moov.length != 1)
				return false;
			UserDataBox udtaBoxes[] = moov[0].getBoxes(UserDataBox.class);
			for (UserDataBox udta : udtaBoxes) {
				MetaBox[] metaBoxes = udta.getBoxes(MetaBox.class);
				for (MetaBox metaBox : metaBoxes) {
					AppleItemListBox[] listBoxes = metaBox.getBoxes(AppleItemListBox.class);
					for (AppleItemListBox listBox : listBoxes) {
						Box[] appleItems = listBox.getBoxes();
						if (appleItems.length > 0 && metaMap == null)
							metaMap = new HashMap<String, String>();
						for (Box box : appleItems) {
							if (box instanceof AppleNameBox)
								metaMap.put("name", ((AppleNameBox)box).getName());
							else if (box instanceof AppleArtistBox)
								metaMap.put("artist", ((AppleArtistBox)box).getArtist());
							else if (box instanceof AppleAlbumBox)
								metaMap.put("album", ((AppleAlbumBox)box).getAlbumTitle());
							else if (box instanceof AppleCommentBox)
								metaMap.put("comment", ((AppleCommentBox)box).getComment());
							else if (box instanceof AppleCopyrightBox)
								metaMap.put("copyright", ((AppleCopyrightBox)box).getCopyright());
							else if (box instanceof AppleCustomGenreBox)
								metaMap.put("genre", ((AppleCustomGenreBox)box).getGenre());
							else if (box instanceof AppleRecordingYearBox) {
								String year = ((AppleRecordingYearBox)box).getRecordingYear();
								if (year.length() > 4)
									year = year.substring(0, 4);
								metaMap.put("year", year);
							} else if (box instanceof AppleTrackNumberBox)
								metaMap.put("tracknumber", Integer.toString(((AppleTrackNumberBox)box).getTrackNumber()));												
							else if (box instanceof AppleTrackAuthorBox)
								metaMap.put("trackauthor", ((AppleTrackAuthorBox)box).getTrackAuthor());
							else if (box instanceof AppleTrackTitleBox)
								metaMap.put("tracktitle", ((AppleTrackTitleBox)box).getTrackTitle());
							else if (box instanceof AppleTvEpisodeBox)
								metaMap.put("tvepisodenumber", Integer.toString(((AppleTvEpisodeBox)box).getTvEpisode()));
							else if (box instanceof AppleTvSeasonBox)
								metaMap.put("tvseasonnumber", Integer.toString(((AppleTvSeasonBox)box).getTvSeason()));
							else if (box instanceof AppleCoverBox) {
								AppleCoverBox coverBox = (AppleCoverBox)box;
								Box[] dataBoxes = coverBox.getBoxes();
								if (dataBoxes != null && dataBoxes.length > 0) {
									AppleDataBox dataBox = (AppleDataBox)dataBoxes[0];
									String ext = null;
									if (dataBox.getFlags() == 13)
										ext = ".jpg";
									else if (dataBox.getFlags() == 14)
										ext = ".gif";
									if (ext != null) {
										String filename = writeArtwork(uri, dataBox.getContent(), ext);
										if (filename != null)
											metaMap.put("artwork", new File(filename).toURL().toExternalForm());
									}
								}
							}
						}
					}
				}
			}
		}
		finally {
				if (iso != null)
					try {
						iso.getOriginalIso().close();
					} catch(Exception e) { } 			
		}
		if (metaMap != null && metaMap.size() >0) {
			StringBuffer data = new StringBuffer();
			data.append("<meta>\n");
			for (Map.Entry<String, String> e : metaMap.entrySet()) {
				data.append("<" + e.getKey() + ">");
				data.append("<![CDATA[");
				data.append(e.getValue());
				data.append("]]>");
				data.append("</" + e.getKey() + ">");			
			}
			data.append("</meta>");
			
			if (metaMap.get("name") != null)
				m.setTitle(metaMap.get("name"));
			else if (metaMap.get("tracktitle") != null)
					m.setTitle(metaMap.get("tracktitle"));

			SAXSource source = new SAXSource(new InputSource(new StringReader(data.toString())));
			return transform(m, source, StreamBabyConfig.cfgMetaXsl.getValue(), null);		
		}
		return false;
	}
	
	//setMetadataItem(vidinfo, "name", formatCtx.title);
	//setMetadataItem(vidinfo, "artist", formatCtx.author);
	////setMetadataItem(vidinfo, "copyright", formatCtx.copyright);
	//setMetadataItem(vidinfo, "comment", formatCtx.comment);
	//setMetadataItem(vidinfo, "album", formatCtx.album);
	//setMetadataItem(vidinfo, "genre", formatCtx.genre);

	public boolean setMetadata(MetaData m, URI uri, VideoInformation vi) {
		if (!Utils.isFile(uri))
			return false;
		File f = new File(uri);
		String lowerCaseName = f.getName();
		IsoFile iso = null;
		if (lowerCaseName.endsWith(".mp4") || lowerCaseName.endsWith(".mov") || lowerCaseName.endsWith(".m4a") || lowerCaseName.endsWith(".m4v")) {
			try {
				m.setReference(f);
				return parseIsoBoxes(uri, f, m, vi);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
		return false;
	}
	
	public static class SBDataSource extends RandomAccessDataSource {

		RandomAccessFileInputStream ras;
		
		public SBDataSource(File f) throws FileNotFoundException {
			 ras = new RandomAccessFileInputStream(f);
		}
		
		public RandomAccessFileInputStream getInputStream() {
			return ras;
		}
		@Override
		public void seek(long pos) throws IOException {
			ras.seek(pos);
			
		}

		@Override
		public int read() throws IOException {
			return ras.read();
		}
		@Override
		public long length() throws IOException {
			return ras.length();
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			return ras.read(b);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return ras.read(b, off, len);
		}
		
		@Override
		public long skip(long n) throws IOException {
			return ras.skip(n);
		}
		
		@Override
		public void mark(int n) {
			ras.mark(n);
		}
		
		@Override
		public boolean markSupported() {
			return ras.markSupported();
		}
		
		@Override
		public void reset() throws IOException {
			ras.reset();
		}
		
		@Override
		public void close() throws IOException {
			ras.close();
		}

	}

}

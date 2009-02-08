package com.unwiredappeal.tivo.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
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
import com.coremedia.iso.RandomAccessDataSource;
import com.coremedia.iso.boxes.Box;
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

	protected boolean parseIsoBoxes(URI uri, IsoFile iso, MetaData m, VideoInformation vi) throws IOException {
		Map<String, String> metaMap = null;
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
				SBDataSource source = new SBDataSource(f);
				iso = new IsoFile(source);
				return parseIsoBoxes(uri, iso, m, vi);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} finally {
				if (iso != null)
					try {
						iso.getOriginalIso().close();
					} catch(Exception e) { }
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

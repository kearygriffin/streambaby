package com.unwiredappeal.tivo.metadata;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.sax.SAXSource;

import mp4.util.Mp4Parser;
import mp4.util.Mp4Split;
import mp4.util.atom.Atom;
import mp4.util.atom.AtomException;
import mp4.util.atom.IlstAtom;
import mp4.util.atom.MetaAtom;
import mp4.util.atom.MoovAtom;

import org.xml.sax.InputSource;


import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.tivo.config.ConfigEntry;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.RandomAccessFileInputStream;
import com.unwiredappeal.tivo.utils.Utils;
import mp4.util.atom.*;

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
		return true;
	}
	public static final int PRIORITY=40;
	public int getSimplePriority() {
		return PRIORITY;
	}

	public int getMetadataPriority() {
		return PRIORITY;
	}
	

	public boolean fetchMetadata(MoovAtom moov, URI uri, MetaData m) {
		UdtaAtom udta = moov.getUdta();
		MetaAtom meta  = null;
		if (udta != null) {
			meta = (MetaAtom)moov.getUdta().getFirstChild(MetaAtom.class);
		}
		if (meta == null)
			return false;
		IlstAtom ilst = (IlstAtom)meta.getFirstChild(IlstAtom.class);
		if (ilst == null)
			return false;
		
		Map<String, String> metaMap = new HashMap<String, String>();

		Iterator<Atom> it = ilst.getChildAtoms().iterator();
		while(it.hasNext()) {
			Atom box = it.next();
			if (box instanceof DescAtom)
				metaMap.put("name", ((DescAtom)box).getDescription());
			else if (box instanceof CartAtom)
				metaMap.put("artist", ((CartAtom)box).getArtist());
			else if (box instanceof CalbAtom)
				metaMap.put("album", ((CalbAtom)box).getAlbum());
			else if (box instanceof CcmtAtom)
				metaMap.put("comment", ((CcmtAtom)box).getComment());
			else if (box instanceof CprtAtom)
				metaMap.put("copyright", ((CprtAtom)box).getCopyright());
			else if (box instanceof GnreAtom)
				metaMap.put("genre", ((GnreAtom)box).getGenre());
			else if (box instanceof CdayAtom) {
				String year = ((CdayAtom)box).getYear();
				if (year.length() > 4)
					year = year.substring(0, 4);
				metaMap.put("year", year);
			}
			/*
			} else if (box instanceof AppleTrackNumberBox)
				metaMap.put("tracknumber", Integer.toString(((AppleTrackNumberBox)box).getTrackNumber()));
			*/												
			else if (box instanceof CwrtAtom)
				metaMap.put("trackauthor", ((CwrtAtom)box).getComposer());
			else if (box instanceof CnamAtom)
				metaMap.put("tracktitle", ((CnamAtom)box).getTitle());
			/*
			else if (box instanceof AppleTvEpisodeBox)
				metaMap.put("tvepisodenumber", Integer.toString(((AppleTvEpisodeBox)box).getTvEpisode()));
			else if (box instanceof AppleTvSeasonBox)
				metaMap.put("tvseasonnumber", Integer.toString(((AppleTvSeasonBox)box).getTvSeason()));
				*/
			else if (box instanceof TvshAtom)
				metaMap.put("tvshowname", ((TvshAtom)box).getShowName());
			else if (box instanceof TvenAtom)
				metaMap.put("tvepisodenumber", ((TvenAtom)box).getEpisodeNumber());			
			else if (box instanceof CovrAtom) {
				CovrAtom covr = (CovrAtom)box;
				DataAtom data = covr.getDataAtom();
				String ext = null;
				if (data.getFlag()[2] == 13)
					ext = ".jpg";
				else if (data.getFlag()[2] == 14)
					ext = ".gif";
				if (ext != null) {
					String filename = writeArtwork(uri, data.getMetadata(), ext);
					if (filename != null) {
						try {
							metaMap.put("artwork", new File(filename).toURL().toExternalForm());
						} catch (MalformedURLException e) {
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
	public boolean parseIsoBoxes(URI uri, File f, MetaData m) {
		DataInputStream mp4file = null;
		try {
			mp4file = new DataInputStream(new RandomAccessFileInputStream(f));
			Mp4Parser parser = new Mp4Parser(mp4file);
			parser.parseMp4();
			mp4file.close();
			return fetchMetadata(parser.getMoov(), uri, m);
		} catch (IOException e) {
			return false;
		} catch (AtomException e) {
			return false;
		} finally {
			if (mp4file != null) {
				try {
					mp4file.close();
				} catch(IOException ee) { }
			}
		}
	}
		
	/*
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
	*/

	public boolean setMetadata(MetaData m, URI uri, VideoInformation vi) {
		if (!Utils.isFile(uri))
			return false;
		File f = new File(uri);
		String lowerCaseName = f.getName();
		if (lowerCaseName.endsWith(".mp4") || lowerCaseName.endsWith(".mov") || lowerCaseName.endsWith(".m4a") || lowerCaseName.endsWith(".m4v")) {
			try {
				m.setReference(f);
				return parseIsoBoxes(uri, f, m);
			} catch (Exception e) {
				Log.error("Error parsing mp4 information");
				Log.printStackTrace(e);
			}
		}
		return false;
	}
	
}

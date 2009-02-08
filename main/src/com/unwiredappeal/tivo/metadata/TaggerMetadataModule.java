package com.unwiredappeal.tivo.metadata;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;


import com.unwiredappeal.mediastreams.VideoInformation;
import com.unwiredappeal.tivo.modules.StreamBabyModule;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.utils.Utils;

public class TaggerMetadataModule extends BaseMetadataModule {

	@Override
	public boolean initialize(StreamBabyModule m) {
		super.initialize(m);
		return false;
	}
	public boolean setMetadata(MetaData m, URI uri, VideoInformation vi) {
		return false;
	}

	/*
	public static final int PRIORITY=40;
	public int getSimplePriority() {
		return PRIORITY;
	}

	public int getMetadataPriority() {
		return PRIORITY;
	}

	public boolean setMetadata(MetaData m, URI uri, VideoInformation vi) {
		if (!Utils.isFile(uri))
			return false;
		try {
			AudioFile f = AudioFileIO.read(new File(uri));
			Tag tag = f.getTag();
			Iterator<TagField> iterator = tag.getFields();
			while(iterator.hasNext())
			{
				TagField tf = iterator.next();
			    if (!tf.isBinary()) {
			    	Log.debug(tf.toString());
			    }
			}
			TagField coverArt = tag.getFirstField(TagFieldKey.COVER_ART);
			if (coverArt != null) {
				Log.debug("hasCovertArt.");
			}
		} catch (CannotReadException e) {
			Log.debug("cant read: " + e);
		} catch (IOException e) {
		} catch (TagException e) {
		} catch (ReadOnlyFileException e) {
		} catch (InvalidAudioFrameException e) {
		}
		return false;
	}
	*/

}

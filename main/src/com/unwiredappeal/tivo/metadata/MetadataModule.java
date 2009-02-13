package com.unwiredappeal.tivo.metadata;

import java.net.URI;

import com.unwiredappeal.tivo.modules.StreamBabyModule;

public interface MetadataModule {
	boolean initialize(StreamBabyModule parent);
	boolean setMetadata(MetaData m, URI uri);
	public int getMetadataPriority();
}

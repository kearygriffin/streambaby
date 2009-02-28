package com.unwiredappeal.mediastreams.mp4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import com.unwiredappeal.tivo.utils.RandomAccessFileInputStream;

import mp4.util.Mp4Split;

public class JavaMP4Splitter extends MP4Streamer {

	public static class Splitter extends Mp4Split {


		private PipedInputStream pi;

		public Splitter(File f, long startPos, boolean reinterleave)
				throws IOException {
			time = startPos / 1000.0f;
			mp4file = new DataInputStream(new RandomAccessFileInputStream(f));
			try {
				calcSplitMp4();
				pi = new PipedInputStream();
				final PipedOutputStream po = new PipedOutputStream(pi);
				// writeSplitMp4(new DataOutputStream(po));
				(new Thread() {
					public void run() {
						try {
							writeSplitMp4(new DataOutputStream(po));
							po.close();
						} catch (IOException e) {
							try {
								po.close();
							} catch (IOException e1) {
							}
						}
					}
				}).start();
			} catch (IOException e) {
				try {
					mp4file.close();
				} catch (IOException e1) {
				}
				throw e;
			}
		}

		public InputStream getOutputStream() {
			return pi;
		}
		
		public float getCutDuration() {
			return (float) cutMoov.getMvhd().getDuration()
					/ (float) cutMoov.getMvhd().getTimeScale();			
		}

	}

	Splitter split;

	private JavaMP4Splitter(Splitter sp) {
		super(sp.getOutputStream());
		split = sp;
	}

	public JavaMP4Splitter(File f, long startPos, boolean reinterleave)
			throws IOException {
		this(new Splitter(f, startPos, reinterleave));
		// mp4 = (StreamableMP4)this.in;
	}

	@Override
	public List<String> getFormats() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getProfile() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getProfileLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getSubDuration() {
		return (long)(split.getCutDuration() * 1000L);
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

}

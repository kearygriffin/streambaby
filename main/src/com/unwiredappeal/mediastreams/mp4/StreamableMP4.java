package com.unwiredappeal.mediastreams.mp4;

/*******************************************************************************
 Ported from moov.c/mod_h264_streaming.c, a MP4 pseudo-streaming module for lighttpd

 moov - A library for splitting Quicktime/MPEG4 files.
 http://h264.code-shop.com

 Copyright (C) 2007 CodeShop B.V.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StreamableMP4 extends InputStream {
	public int width = 0;
	public int height = 0;
	File file;
	RandomAccessFile fp = null;
	long startTimePos = 0;
	LinkedList<BArray> buffers = new LinkedList<BArray>();
	BArray curByteArray = null;
	int bytePos;
	long filelength = -1;
	long chunkOffset;
	long curOffset = -1;
	private long subDuration = 0;
	public static boolean defaultReInterleave = true;
	public static float INTERLEAVE_DURATION = 0.4f;
	public static Logger logger;
	public List<String> formats = new ArrayList<String>();
	private static final long MAX_CHUNK_DELTA_BEFORE_INTERLEAVE = -1; // (causes issues, leave disabled) 4 * 1024 * 1024;

	public static BArrayFactory bfact = new BArrayFactory() {
		public BArray getBArray(int size) {
			return new ByteArrayBackedBArray(size);
		}
		
	};
	private boolean reInterleave;

	private Chunk curChunk = null;
	private ChunkQueue chunkQueue = null;

	public static void main(String[] argv) throws Exception {
		String src = argv[0];
		String dst = argv[1];
		long pos = Long.parseLong(argv[2]);
		log("In: " + src + ", out: " + dst + ", pos: " + pos);
		InputStream is = new StreamableMP4(new File(src), pos);
		OutputStream os = new FileOutputStream(new File(dst));
		final int IO_BUFFER_SIZE = 4 * 1024;

		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = is.read(b)) != -1) {
			os.write(b, 0, read);
		}
		os.close();
		is.close();
		System.out.println("Done");
	}

	public StreamableMP4(File file, long pos) throws IOException {
		this(file, pos, defaultReInterleave);
	}

	public StreamableMP4(File file, long pos, boolean interleave)
			throws IOException {
		this.reInterleave = interleave; // defaultReInterleave
		this.file = file;
		this.startTimePos = pos;
		this.filelength = file.length();
		fp = new RandomAccessFile(file, "r");
		processMP4Headers();
	}

	public byte[] singlebyte = new byte[1];

	public int read() throws IOException {
		int i = read(singlebyte, 0, 1);
		if (i == 1)
			return singlebyte[0];
		else
			return i;
	}

	private Chunk getNextChunk() throws IOException {
		Chunk n = chunkQueue.poll();
		// Chunk n = nextChunk;
		if (n != null) {
			if (curOffset != n.offset)
				fp.seek(n.offset);
			curOffset = n.offset;
			chunkOffset = 0;
		}
		return n;
	}

	private boolean moreChunkData() throws IOException {
		// return fp.getFilePointer() != filelength;
		if (curChunk == null)
			curChunk = getNextChunk();
		return curChunk != null;

	}

	private int readChunkData(byte[] b, int cp, int len) throws IOException {
		// int fl = fp.read(b, cp, (int) Math.min(len, filelength -
		// fp.getFilePointer()));
		if (!moreChunkData())
			return 0;
		int fl = fp.read(b, cp, (int) Math
				.min(len, curChunk.size - chunkOffset));
		if (fl > 0) {
			chunkOffset += fl;
			curOffset += fl;
		}
		if (curChunk.size == chunkOffset)
			curChunk = null;
		return fl;

	}
	
	public long getSubDuration() {
		return subDuration;
	}


	public int read(byte[] b, int off, int len) {
		int cp = off;
		int readLen = 0;
		try {
			while (readLen < len
					&& (curByteArray != null || buffers.size() > 0 || moreChunkData())) {
				if (curByteArray != null) {
					if (bytePos >= curByteArray.getSize()) {
						curByteArray.free();
						curByteArray = null;
					} else {
						// read as much as we can...
						while (readLen < len
								&& bytePos < curByteArray.getSize()) {
							b[cp++] = (byte) curByteArray.get(bytePos++);
							readLen++;
						}
					}
				} else if (buffers.size() > 0) {
					curByteArray = buffers.removeFirst();
					bytePos = 0;
				} else {
					/*
					 * } int fl = fp.read(b, cp, (int) Math.min((len - readLen),
					 * filelength - fp.getFilePointer()));
					 */
					int fl = readChunkData(b, cp, (int) (len - readLen));
					if (fl < 0)
						break;
					readLen += fl;
					cp += fl;

				}
			}
		} catch (IOException e) {
			return -1;
		}
		return readLen > 0 ? readLen : -1;
	}

	public static interface BArray {
		public void free();

		public int get(long index);

		public void set(long index, int v);

		public void readFromFile(RandomAccessFile fp, long offset, long len)
				throws IOException;

		public void write_char(int offset, byte value);

		public void write_int64(long off, long value);

		public void write_int32(long l, long value);

		public int read_char(long mvhd);

		public long read_int64(long off);

		public long read_int32(long off);

		public int getSize();
	}

	public static abstract class BaseBArray implements BArray {
		int siz;

		protected BaseBArray(long siz) {
			this.siz = (int) siz;
		}

		public int getSize() {
			return siz;
		}

		public void write_char(int offset, byte value) {
			set(offset, value);
		}

		public void write_int64(long off, long value) {
			write_int32(off, (long) (value >>> 32));
			write_int32(off + 4, (long) (value >>> 0));
		}

		public void write_int32(long l, long value) {
			set((int) (0 + l), (byte) ((value >>> 24) & 0xff));
			set((int) (1 + l), (byte) ((value >>> 16) & 0xff));
			set((int) (2 + l), (byte) ((value >>> 8) & 0xff));
			this.set((int) (3 + l), (byte) ((value >>> 0) & 0xff));
		}

		public int read_char(long mvhd) {
			return this.get((int) mvhd) & 0xff;
		}

		public long read_int64(long off) {
			// unsigned char* p = (unsigned char*)buffer;
			return ((long) (read_int32(off)) << 32) + read_int32(off + 4);
		}

		public long read_int32(long off) {

			// unsigned char* p = (unsigned char*)buffer;
			return ((long) (get(0 + (int) off) & 0xff) << 24)
					+ ((long) (get(1 + (int) off) & 0xff) << 16)
					+ ((long) (get(2 + (int) off) & 0xff) << 8)
					+ (long) (get(3 + (int) off) & 0xff);
		}

	}

	public static class ByteArrayBackedBArray extends BaseBArray implements BArray {
		/*
		 * Idea is to make this a disk-backed array is the data is larger than a
		 * certain size
		 */
		/* For now, memory backed array is good enough */

		public long length;
		protected byte[] bytes;

		public ByteArrayBackedBArray(long siz) {
			super(siz);
			bytes = new byte[(int) siz];
			length = siz;
		}

		public void free() {
			bytes = null;

		}

		public int get(long index) {
			int ret = bytes[(int) index] & 0xff;
			//System.err.println("get: " + index + ", " + ret);
			return ret;
		}

		public void set(long index, int v) {
			bytes[(int) index] = (byte) v;
			//System.err.println("set: " + index + ", " + v);
		}

		public void readFromFile(RandomAccessFile fp, long offset, long len)
				throws IOException {
			fp.read(bytes, (int) offset, (int) len);
		}

	}

	public interface BArrayFactory {
		public BArray getBArray(int size);
	}

	/*
	public static int MAX_BYTE_BACKED_SIZE = 100 * 1024; // 100k
	*/
	public static BArray getBArray(int siz) throws IOException {
		return bfact.getBArray(siz);
	}
	/*
		if (siz <= MAX_BYTE_BACKED_SIZE)
			return new ByteArrayBackedBArray(siz);
		else
			return new FileBackedBArray(siz);
	}
	*/
	private static class Chunk {
		Chunk(long offset, long size) {
			this.offset = offset;
			this.size = size;
		}

		long offset;
		long size;
	}

	private static class ChunkQueue {
		private BArray ba = null;
		private static int sizeof_long = 8;
		private static int sizeof_chunk = sizeof_long * 2;
		int qpos = 0;
		int qsize = 0;
		public ChunkQueue(int siz) throws IOException {
			ba = getBArray(siz * sizeof_chunk);
			qpos = 0;
			qsize = 0;
		}
		public Chunk poll() {
			if (qpos == qsize)
				return null;
			long off = ba.read_int64(getOffset(qpos));
			long size = ba.read_int64(getOffset(qpos) + sizeof_long);
			qpos++;
			return new Chunk(off, size);
		}
		
		public void add(Chunk chunk) {
			ba.write_int64(getOffset(qsize), chunk.offset);
			ba.write_int64(getOffset(qsize)+sizeof_long, chunk.size);
			qsize++;
		}
		
		private int getOffset(int qp) {
			return qp * sizeof_chunk;
		}
		
		public void free() {
			if (ba != null)
				ba.free();
			ba = null;
		}
	}

	private void processMP4Headers() {
		MP4Atom ftyp_atom = null;
		MP4Atom moov_atom = null;
		MP4Atom mdat_atom = null;
		BArray moov_data = null;
		BArray ftyp_data = null;
		BArray mdat_bytes = null;

		boolean hasError = false;
		try {
			MP4Atom leaf_atom;
			log("Processing moov atom...");
			while (fp.getFilePointer() < filelength) {
				leaf_atom = atom_read_header();
				if (leaf_atom == null)
					break;

				// atom_print(leaf_atom);

				if (atom_is(leaf_atom, "ftyp")) {
					ftyp_atom = leaf_atom;
					ftyp_data = getBArray((int) (ftyp_atom.size_));
					fp.seek(ftyp_atom.start_);
					ftyp_data.readFromFile(fp, 0, ftyp_atom.size_);
					// fp.read(ftyp_data);
					// fread(ftyp_data, ftyp_atom.size_, 1, infile);
				} else if (atom_is(leaf_atom, "moov")) {
					moov_atom = leaf_atom;
					moov_data = getBArray((int) (moov_atom.size_));
					fp.seek(moov_atom.start_);
					moov_data.readFromFile(fp, 0, moov_atom.size_);
					// fp.read(moov_data);
				} else if (atom_is(leaf_atom, "mdat")) {
					mdat_atom = leaf_atom;
				}
				atom_skip(leaf_atom);
			}

			if (moov_data == null || ftyp_atom == null || mdat_atom == null) {
				log("moov_data, ftyp, or mdat atom not found!");
				fp.seek(0);
				hasError = true;
				return;

			}

			long mdat_start = ((ftyp_data != null) ? ftyp_atom.size_ : 0)
					+ moov_atom.size_;
			float end = 0.0f;
			// long startOffset = mdat_start - mdat_atom.start_;
			// long origMdatStart = mdat_atom.start_;
			float start = (startTimePos / 1000.0f);
			if (moov_seek(moov_data, moov_atom, start, end, mdat_atom,
					mdat_start) == 0) {
				fp.seek(0);
				log("moov_seek returned failure!");
				if (start != 0 && startTimePos != 0) {
					log("Reattempting MP4 using startPos=0");
					startTimePos = 0;
					cleanup();
					if (mdat_bytes != null)
						mdat_bytes.free();
					if (ftyp_data != null)
						ftyp_data.free();
					if (moov_data != null)
						moov_data.free();
					mdat_bytes = null;
					ftyp_data = null;
					moov_data = null;
					
					processMP4Headers();
					return;
				}
				hasError = true;
				return;
			}
			log("Processing moov atom complete");

			/*
			 * long skip_from_start = mdat_atom.start_ - origMdatStart; // add
			 * this to all of the chunk positions to get a file position long
			 * chunkOffsets = -(startOffset - skip_from_start); // This is
			 * really not needed, as_start_ is an internal field, // but might
			 * as well make it consistent long fileMdatStart = mdat_atom.start_ ;
			 */
			mdat_atom.start_ = mdat_start;
			if (ftyp_data != null) {
				// buffer_append_memory(b, ftyp_data, ftyp_atom.size_);
				buffers.add(ftyp_data);
				ftyp_data = null;
			}

			buffers.add(moov_data);
			moov_data = null;
			// buffer_append_memory(b, moov_data, moov_atom.size_);

			{
				mdat_bytes = getBArray(ATOM_PREAMBLE_SIZE);
				// mdat_atom.size_ -= bytes_to_skip;
				atom_write_header(mdat_bytes, mdat_atom);
				buffers.add(mdat_bytes);
				mdat_bytes = null;
				// buffer_append_memory(b, mdat_bytes, ATOM_PREAMBLE_SIZE);
				// b.used++; /* add virtual \0 */
				// byte[] xb = new byte[1];
				// xb[0] = 0;
				// buffers.add(xb);
			}

			/*
			 * filelength = (fileMdatStart + ATOM_PREAMBLE_SIZE) +
			 * mdat_atom.size_ - ATOM_PREAMBLE_SIZE; fp.seek(fileMdatStart +
			 * ATOM_PREAMBLE_SIZE );
			 */
			// nextChunk = new Chunk(fileMdatStart + ATOM_PREAMBLE_SIZE,
			// mdat_atom.size_ - ATOM_PREAMBLE_SIZE);
			// http_chunk_append_file(srv, con, con.physical.path,
			// mdat_atom.start_ + ATOM_PREAMBLE_SIZE,
			// mdat_atom.size_ - ATOM_PREAMBLE_SIZE);
		} catch (IOException e) {
			hasError = true;
			try {
				fp.seek(0);
			} catch (Exception ee) {
			}
		} finally {
			if (hasError) {
				log("Error processing moov");
				cleanup();
				if (mdat_bytes != null)
					mdat_bytes.free();
				if (ftyp_data != null)
					ftyp_data.free();
				if (moov_data != null)
					moov_data.free();
				try {
					log("Complete mp4 failure.  Using entire file");
					chunkQueue = new ChunkQueue(1);
					chunkQueue.add(new Chunk(0, fp.length()));

				} catch (IOException e) {
				}				
			}

		}
	}

	public class MP4Atom {
		long size_;
		public byte[] type_ = new byte[4];
		long start_;
		long end_;
	}

	private long atom_header_size(BArray atom_bytes, long bufferp) {
		return ((long) (atom_bytes.get(0 + (int) bufferp) & 0xff) << 24)
				+ ((long) (atom_bytes.get(1 + (int) bufferp) & 0xff) << 16)
				+ ((long) (atom_bytes.get(2 + (int) bufferp) & 0xff) << 8)
				+ (atom_bytes.get(3 + (int) bufferp) & 0xff);
	}

	private long atom_header_size(byte[] atom_bytes, long bufferp) {
		return ((long) (atom_bytes[0 + (int) bufferp] & 0xff) << 24)
				+ ((long) (atom_bytes[1 + (int) bufferp] & 0xff) << 16)
				+ ((long) (atom_bytes[2 + (int) bufferp] & 0xff) << 8)
				+ (atom_bytes[3 + (int) bufferp] & 0xff);
	}

	static final int ATOM_PREAMBLE_SIZE = 8;

	private MP4Atom atom_read_header(BArray atom_bytes, long bufferp) {
		MP4Atom atom = new MP4Atom();
		// byte[] atom_bytes = new byte[ATOM_PREAMBLE_SIZE];

		atom.start_ = bufferp;

		// fp.read(atom_bytes, 0, ATOM_PREAMBLE_SIZE);
		for (int i = 0; i < 4; i++)
			atom.type_[i] = (byte) atom_bytes.get(4 + bufferp + i);
		// copyBytes(atom.type_, 0, atom_bytes, 4+bufferp, 4);
		atom.size_ = atom_header_size(atom_bytes, bufferp);
		atom.end_ = atom.start_ + atom.size_;
		return atom;

	}

	public void dump_mp4(BArray b, long off) {
		MP4Atom atom = atom_read_header(b, off - ATOM_PREAMBLE_SIZE);
		dump_mp4(b, off, atom.size_);
	}

	public void dump_mp4(BArray b, long off, long siz) {

		long x = 0;
		while (x < siz) {
			MP4Atom atom = atom_read_header(b, off + x);
			String typ = new String(atom.type_);
			System.out.println("Atom: " + typ + ", len: " + atom.size_
					+ ", end: " + atom.end_);
			x += atom.size_;
		}
		System.out.println("specified len: " + siz + ", calcLen: " + x);
	}

	private MP4Atom atom_read_header() throws IOException {
		MP4Atom atom = new MP4Atom();
		byte[] atom_bytes = new byte[ATOM_PREAMBLE_SIZE];

		atom.start_ = fp.getFilePointer();

		fp.read(atom_bytes, 0, ATOM_PREAMBLE_SIZE);
		for (int i = 0; i < 4; i++)
			atom.type_[i] = atom_bytes[4 + i];
		// copyBytes(atom.type_, 0, atom_bytes, 4, 4);
		atom.size_ = atom_header_size(atom_bytes, 0);
		atom.end_ = atom.start_ + atom.size_;
		return atom;
	}

	private boolean atom_is(MP4Atom atom, String typeString) {
		byte[] type = typeString.getBytes();
		return (atom.type_[0] == type[0] && atom.type_[1] == type[1]
				&& atom.type_[2] == type[2] && atom.type_[3] == type[3]);
	}

	private void atom_skip(MP4Atom atom) throws IOException {
		fp.seek(atom.end_);
	}

	private void atom_write_header(BArray outbuffer, MP4Atom atom) {
		int i;
		outbuffer.write_int32(0, atom.size_);
		for (i = 0; i != 4; ++i)
			outbuffer.write_char(+4 + i, atom.type_[i]);
	}

	private void copyBytes(BArray dst, long newp, BArray src, long l, long m) {
		for (int i = 0; i < m; i++)
			dst.set((int) newp++, src.get((int) l++));
	}

	private long stts_get_entries(BArray stts, long off) {
		return stts.read_int32(4 + off);
	}

	private long stts_get_sample_count(BArray stts, long stts2, long idx) {
		// unsigned char const* table = stts + 8 + idx * 8;
		return stts.read_int32(8 + idx * 8 + stts2);
	}

	private long stts_get_sample_duration(BArray stts, long off, long idx) {
		// unsigned char const* table = stts + 8 + idx * 8;
		return stts.read_int32(12 + idx * 8 + off);
	}

	class stts_table_t {
		long sample_count_;
		long sample_duration_;
	}

	private long ctts_get_entries(BArray ctts, long off) {
		return ctts.read_int32(4 + off);
	}

	private long ctts_get_sample_count(BArray ctts, long off, long idx) {
		return ctts.read_int32(off + 8 + idx * 8);
		// unsigned char const* table = ctts + 8 + idx * 8;
		// *sample_count = read_int32(table);
		// *sample_offset = read_int32(table + 4);
	}

	private long ctts_get_sample_offset(BArray ctts, long off, long idx) {
		return ctts.read_int32(off + 12 + idx * 8);
		// unsigned char const* table = ctts + 8 + idx * 8;
		// *sample_count = read_int32(table);
		// *sample_offset = read_int32(table + 4);
	}

	long ctts_get_samples(BArray ctts, long off) {
		long samples = 0;
		long entries = ctts_get_entries(ctts, off);
		long i;
		for (i = 0; i != entries; ++i) {
			long sample_count = ctts_get_sample_count(ctts, off, i);
			// long sample_offset = ctts_get_sample_offset(ctts, off, i);
			samples += sample_count;
		}

		return samples;
	}

	class ctts_table_t {
		long sample_count_;
		long sample_offset_;
	};

	class stsc_table_t {
		long chunk_;
		long samples_;
		long id_;
	};

	long stsc_get_entries(BArray stsc, long stsc2) {
		return stsc.read_int32(4 + stsc2);
	}

	void stsc_get_table(BArray stsc, long stsc2, long i, stsc_table_t stsc_table) {
		// struct stsc_table_t* table = (struct stsc_table_t*)(stsc + 8);
		// stsc_table.chunk_ = read_int32(&table[i].chunk_) - 1;
		// stsc_table.samples_ = read_int32(&table[i].samples_);
		// stsc_table.id_ = read_int32(&table[i].id_);
		stsc_table.chunk_ = stsc.read_int32(8 + (i * 12) + stsc2) - 1;
		stsc_table.samples_ = stsc.read_int32(8 + (i * 12) + 4 + stsc2);
		stsc_table.id_ = stsc.read_int32(8 + (i * 12) + 8 + stsc2);
	}

	long stsc_get_chunk(BArray stsc, long sample) {
		long entries = stsc.read_int32(4);
		// struct stsc_table_t* table = (struct stsc_table_t*)(stsc + 8);

		if (entries == 0) {
			return 0;
		} else
		// if(entries == 1)
		// {
		// unsigned int table_samples = read_int32(&table[0].samples_);
		// unsigned int chunk = (sample + 1) / table_samples;
		// return chunk - 1;
		// }
		// else
		{
			long total = 0;
			long chunk1 = 1;
			long chunk1samples = 0;
			long chunk2entry = 0;
			long chunk /* , chunk_sample */;

			do {
				long range_samples;
				// unsigned int chunk2 = read_int32(&table[chunk2entry].chunk_);
				long chunk2 = stsc.read_int32(8 + (chunk2entry * 12));
				chunk = chunk2 - chunk1;
				range_samples = chunk * chunk1samples;

				if (sample < total + range_samples)
					break;

				// chunk1samples = read_int32(&table[chunk2entry].samples_);
				chunk1samples = stsc.read_int32(8 + (chunk2entry * 12) + 4);
				chunk1 = chunk2;

				if (chunk2entry < entries) {
					chunk2entry++;
					total += range_samples;
				}
			} while (chunk2entry < entries);

			if (chunk1samples != 0) {
				long sample_in_chunk = (sample - total) % chunk1samples;
				if (sample_in_chunk != 0) {
					// printf("ERROR: sample must be chunk aligned: %d\n",
					// sample_in_chunk);
				}
				chunk = (sample - total) / chunk1samples + chunk1;
			} else
				chunk = 1;

			// chunk_sample = total + (chunk - chunk1) * chunk1samples;

			return chunk;
		}
	}

	long stsc_get_samples(BArray stsc) {
		long entries = stsc.read_int32(4);
		// struct stsc_table_t* table = (struct stsc_table_t*)(stsc + 8);
		long samples = 0;
		long i;
		for (i = 0; i != entries; ++i) {
			// samples += read_int32(&table[i].samples_);
			samples += stsc.read_int32(8 + (i * 12) + 4);
		}
		return samples;
	}

	long stco_get_entries(BArray stco, long stco2) {
		return stco.read_int32(stco2 + 4);
	}

	long stco_get_offset(BArray stco, long stco2, int idx) {
		// uint32_t const* table = (uint32_t const*)(stco + 8);
		// return read_int32(&table[idx]);
		return stco.read_int32(8 + (idx * 4) + stco2);
	}

	long stsz_get_sample_size(BArray stsz, long stsz2) {
		return stsz.read_int32(4 + stsz2);
	}

	long stsz_get_entries(BArray stsz, long stsz2) {
		return stsz.read_int32(8 + stsz2);
	}

	long stsz_get_size(BArray stsz, long stsz2, long idx) {
		// uint32_t const* table = (uint32_t const*)(stsz + 12);
		// return read_int32(&table[idx]);
		return stsz.read_int32(12 + (idx * 4) + stsz2);
	}

	long stts_get_duration(BArray stts, long off) {
		long duration = 0;
		long entries = stts_get_entries(stts, off);
		long i;
		for (i = 0; i != entries; ++i) {
			long sample_count = stts_get_sample_count(stts, off, i);
			long sample_duration = stts_get_sample_duration(stts, off, i);
			// stts_get_sample_count_and_duration(stts, i,
			// &sample_count, &sample_duration);
			duration += sample_duration * sample_count;
		}

		return duration;
	}

	long stts_get_samples(BArray stts, long off) {
		long samples = 0;
		long entries = stts_get_entries(stts, off);
		long i;
		for (i = 0; i != entries; ++i) {
			long sample_count = stts_get_sample_count(stts, off, i);
			// long sample_duration = stts_get_sample_duration(stts, off, i);
			// stts_get_sample_count_and_duration(stts, i,
			// &sample_count, &sample_duration);
			samples += sample_count;
		}

		return samples;
	}

	long stts_get_sample(BArray stts, long off, long time) {
		long stts_index = 0;
		long stts_count;

		long ret = 0;
		long time_count = 0;

		long entries = stts_get_entries(stts, off);
		for (; stts_index != entries; ++stts_index) {
			long sample_count = stts_get_sample_count(stts, off, stts_index);
			long sample_duration = stts_get_sample_duration(stts, off,
					stts_index);
			// stts_get_sample_count_and_duration(stts, stts_index,
			// &sample_count, &sample_duration);
			if (time_count + sample_duration * sample_count >= time) {
				stts_count = (time - time_count) / sample_duration;
				time_count += stts_count * sample_duration;
				ret += stts_count;
				break;
			} else {
				time_count += sample_duration * sample_count;
				ret += sample_count;
				// stts_index++;
			}
			// if(stts_index >= table_.size())
			// break;
		}
		// *time = time_count;
		return ret;
	}

	long stts_get_time(BArray stts, long off, long sample) {
		long ret = 0;
		long stts_index = 0;
		long sample_count = 0;

		for (;;) {
			long table_sample_count = stts_get_sample_count(stts, off,
					stts_index);
			long table_sample_duration = stts_get_sample_duration(stts, off,
					stts_index);
			// stts_get_sample_count_and_duration(stts, stts_index,
			// &table_sample_count, &table_sample_duration);

			if (sample_count + table_sample_count > sample) {
				long stts_count = (sample - sample_count);
				ret += stts_count * table_sample_duration;
				break;
			} else {
				sample_count += table_sample_count;
				ret += table_sample_count * table_sample_duration;
				stts_index++;
			}
		}
		return ret;
	}

	class stbl_t {
		long stbl_size;
		long initOffset;
		BArray start_;
		long stts_; // decoding time-to-sample
		long stss_; // sync sample
		long stsc_; // sample-to-chunk
		long stsz_; // sample size
		long stco_; // chunk offset
		long ctts_; // composition time-to-sample

		BArray new_; // the newly generated stbl
		long newp_; // the newly generated stbl
	};

	void atomPrint(MP4Atom a) {
		//
	}

	void stbl_parse(stbl_t stbl, BArray buffer, long off, long size)
			throws IOException {
		MP4Atom leaf_atom;
		// stbl.start_ = buffer;
		stbl.stss_ = 0;
		stbl.ctts_ = 0;

		stbl.start_ = buffer;
		stbl.initOffset = off;
		if (stbl.new_ != null)
			stbl.new_.free();
		stbl.new_ = getBArray((int) size + 2048);
		stbl.newp_ = 0;

		long bufferp = 0;
		while (bufferp < size) {
			leaf_atom = atom_read_header(buffer, bufferp + off);
			bufferp += ATOM_PREAMBLE_SIZE;

			atom_print(leaf_atom);

			if (atom_is(leaf_atom, "stts")) {
				stbl.stts_ = bufferp + off;
			} else if (atom_is(leaf_atom, "stss")) {
				stbl.stss_ = bufferp + off;
			} else if (atom_is(leaf_atom, "stsc")) {
				stbl.stsc_ = bufferp + off;
			} else if (atom_is(leaf_atom, "stsz")) {
				stbl.stsz_ = bufferp + off;
			} else if (atom_is(leaf_atom, "stco")) {
				stbl.stco_ = bufferp + off;
			} else if (atom_is(leaf_atom, "co64")) {
				// printf("TODO: co64");
			} else if (atom_is(leaf_atom, "ctts")) {
				stbl.ctts_ = bufferp + off;
			} else {
				if (atom_is(leaf_atom, "stsd")) {
						byte[] b = new byte[4];
						b[0] = (byte) buffer.read_char(bufferp+off+8+4);
						b[1] = (byte) buffer.read_char(bufferp+off+8+5);
						b[2] = (byte) buffer.read_char(bufferp+off+8+6);
						b[3] = (byte) buffer.read_char(bufferp+off+8+7);
						String format = new String(b);
						//log("Format: " + format);
						formats.add(format);
				}
				// copy unknown/unused atoms directly (e.g. stsd)
				copyBytes(stbl.new_, stbl.newp_, buffer, (off + bufferp)
						- ATOM_PREAMBLE_SIZE, leaf_atom.size_);
				// memcpy(stbl.newp_, buffer - ATOM_PREAMBLE_SIZE,
				// leaf_atom.size_);
				stbl.newp_ += leaf_atom.size_;
			}
			// long xbufferp = atom_skip(bufferp, off, leaf_atom);
			bufferp = bufferp + (leaf_atom.size_ - ATOM_PREAMBLE_SIZE); // atom_skip(buffer,
			// bufferp,
			// leaf_atom);
		}
	}

	private void atom_print(MP4Atom leaf_atom) {
		// TODO Auto-generated method stub

	}

	class minf_t {
		BArray start_;
		long initOffset;
		stbl_t stbl_ = new stbl_t();
	};

	void minf_parse(minf_t minf, BArray buffer, long off, long size)
			throws IOException {
		MP4Atom leaf_atom;
		long bufferp = 0;
		// unsigned char* buffer_start = buffer;

		minf.start_ = buffer;
		minf.initOffset = off;
		while (bufferp < size) {
			leaf_atom = atom_read_header(buffer, bufferp + off);
			bufferp += ATOM_PREAMBLE_SIZE;

			atom_print(leaf_atom);

			if (atom_is(leaf_atom, "stbl")) {
				stbl_parse(minf.stbl_, buffer, bufferp + off, leaf_atom.size_
						- ATOM_PREAMBLE_SIZE);
			}

			bufferp += (leaf_atom.size_ - ATOM_PREAMBLE_SIZE);
			// buffer = atom_skip(buffer, leaf_atom);
		}
	}

	class mdia_t {
		BArray start_;
		long initOffset;
		long mdhd_;
		long hdlr_;
		minf_t minf_ = new minf_t();
		// hdlr hdlr_;
	};

	void mdia_parse(mdia_t mdia, BArray buffer, long off, long size)
			throws IOException {
		MP4Atom leaf_atom;
		int bufferp = 0;

		mdia.start_ = buffer;
		mdia.initOffset = off;

		while (bufferp < size) {
			leaf_atom = atom_read_header(buffer, bufferp + off);
			bufferp += ATOM_PREAMBLE_SIZE;

			atom_print(leaf_atom);

			if (atom_is(leaf_atom, "mdhd")) {
				mdia.mdhd_ = bufferp + off;
			} else if (atom_is(leaf_atom, "minf")) {
				minf_parse(mdia.minf_, buffer, bufferp + off, leaf_atom.size_
						- ATOM_PREAMBLE_SIZE);
			} else if (atom_is(leaf_atom, "hdlr")) {
				mdia.hdlr_ = bufferp + off;
			}

			// buffer = atom_skip(buffer, leaf_atom);
			bufferp += (leaf_atom.size_ - ATOM_PREAMBLE_SIZE);
		}
	}

	class chunks_t {
		long sample_; // number of the first sample in the chunk
		long size_; // number of samples in the chunk
		long id_; // for multiple codecs mode - not used
		long pos_; // start byte position of chunk
	};

	class samples_t {
		long pts_; // decoding/presentation time
		long size_; // size in bytes
		long pos_; // byte offset
		long cto_; // composition time offset
		long sampleDuration;
	};

	class trak_t {

		long initOffset;
		BArray start_;
		long tkhd_;
		mdia_t mdia_ = new mdia_t();

		/* temporary indices */
		long chunks_size_;
		chunks_t[] chunks_;

		long samples_size_;
		samples_t[] samples_;
		public boolean deleted;
	};

	void trak_init(trak_t trak) {
		trak.chunks_ = null;
		trak.samples_ = null;
	}

	void trak_exit(trak_t trak) {
		/*
		 * if(trak.chunks_) free(trak.chunks_); if(trak.samples_)
		 * free(trak.samples_);
		 */
	}

	void trak_parse(trak_t trak, BArray buffer, long off, long size)
			throws IOException {
		MP4Atom leaf_atom;
		long bufferp = 0;

		trak.start_ = buffer;
		trak.initOffset = off;

		while (bufferp < size) {
			leaf_atom = atom_read_header(buffer, bufferp + off);
			bufferp += ATOM_PREAMBLE_SIZE;

			atom_print(leaf_atom);

			if (atom_is(leaf_atom, "tkhd")) {
				trak.tkhd_ = bufferp + off;
			} else if (atom_is(leaf_atom, "mdia")) {
				mdia_parse(trak.mdia_, buffer, bufferp + off, leaf_atom.size_
						- ATOM_PREAMBLE_SIZE);
			}

			// buffer = atom_skip(buffer, leaf_atom);
			bufferp += leaf_atom.size_ - ATOM_PREAMBLE_SIZE;
		}
	}

	public static final int MAX_TRACKS = 8;

	class moov_t {
		public moov_t() {
			for (int i = 0; i < traks_.length; i++)
				traks_[i] = new trak_t();
		}

		BArray start_;
		long tracks_;
		long mvhd_;
		trak_t[] traks_ = new trak_t[MAX_TRACKS];
	};

	void moov_init(moov_t moov) {
		moov.tracks_ = 0;
	}

	void moov_exit(moov_t moov) {
		int i;
		for (i = 0; i != moov.tracks_; ++i) {
			trak_exit(moov.traks_[i]);
		}
	}

	void trak_build_index(trak_t trak) {
		long stco = trak.mdia_.minf_.stbl_.stco_;
		BArray stbl_start = trak.mdia_.minf_.stbl_.start_;

		trak.chunks_size_ = stco_get_entries(stbl_start, stco);
		trak.chunks_ = new chunks_t[(int) trak.chunks_size_];
		for (int i = 0; i < trak.chunks_.length; i++)
			trak.chunks_[i] = new chunks_t();
		// malloc(trak.chunks_size_ * sizeof(struct chunks_t));

		{
			int i;
			for (i = 0; i != trak.chunks_size_; ++i) {
				trak.chunks_[i].pos_ = stco_get_offset(stbl_start, stco, i);
			}
		}

		// process chunkmap:
		{
			long stsc = trak.mdia_.minf_.stbl_.stsc_;
			long last = trak.chunks_size_;
			long i = stsc_get_entries(stbl_start, stsc);
			while (i > 0) {
				stsc_table_t stsc_table = new stsc_table_t();
				int j;

				--i;

				stsc_get_table(stbl_start, stsc, i, stsc_table);
				for (j = (int) stsc_table.chunk_; j < last; j++) {
					trak.chunks_[j].id_ = stsc_table.id_;
					trak.chunks_[j].size_ = stsc_table.samples_;
				}
				last = stsc_table.chunk_;
			}
		}

		// calc pts of chunks:
		{
			long stsz = trak.mdia_.minf_.stbl_.stsz_;
			long sample_size = stsz_get_sample_size(stbl_start, stsz);
			long s = 0;
			{
				int j;
				for (j = 0; j < trak.chunks_size_; j++) {
					trak.chunks_[j].sample_ = s;
					s += trak.chunks_[j].size_;
				}
			}

			if (sample_size == 0) {
				trak.samples_size_ = stsz_get_entries(stbl_start, stsz);
			} else {
				trak.samples_size_ = s;
			}

			trak.samples_ = new samples_t[(int) trak.samples_size_];
			for (int i = 0; i < trak.samples_.length; i++)
				trak.samples_[i] = new samples_t();
			// malloc(trak.samples_size_ * sizeof(struct samples_t));

			if (sample_size == 0) {
				int i;
				for (i = 0; i != trak.samples_size_; ++i)
					trak.samples_[i].size_ = stsz_get_size(stbl_start, stsz, i);
			} else {
				int i;
				for (i = 0; i != trak.samples_size_; ++i)
					trak.samples_[i].size_ = sample_size;
			}
		}

		// i = 0;
		// for (j = 0; j < trak.durmap_size; j++)
		// i += trak.durmap[j].num;
		// if (i != s) {
		// mp_msg(MSGT_DEMUX, MSGL_WARN,
		// "MOV: durmap and chunkmap sample count differ (%i vs %i)\n", i, s);
		// if (i > s) s = i;
		// }

		// calc pts:
		{
			long stts = trak.mdia_.minf_.stbl_.stts_;
			long s = 0;
			long pts = 0;
			long entries = stts_get_entries(stbl_start, stts);
			int j;
			for (j = 0; j < entries; j++) {
				long i;
				long sample_count = stts_get_sample_count(stbl_start, stts, j);
				long sample_duration = stts_get_sample_duration(stbl_start,
						stts, j);
				;
				// stts_get_sample_count_and_duration(stbl_start, stts, j,
				// &sample_count, &sample_duration);
				for (i = 0; i < sample_count; i++) {
					trak.samples_[(int) s].pts_ = pts;
					// KG:OOB
					trak.samples_[(int) s].sampleDuration = sample_duration;
					++s;
					pts += sample_duration;
				}
			}
		}

		// calc composition times:
		{
			long ctts = trak.mdia_.minf_.stbl_.ctts_;
			if (ctts != 0) {
				long s = 0;
				long entries = ctts_get_entries(stbl_start, ctts);
				long j;
				for (j = 0; j < entries; j++) {
					long i;
					long sample_count = ctts_get_sample_count(stbl_start, ctts,
							j);
					long sample_offset = ctts_get_sample_offset(stbl_start,
							ctts, j);
					// ctts_get_sample_count_and_offset(stbl_start, ctts, j,
					// &sample_count, &sample_offset);
					for (i = 0; i < sample_count; i++) {
						trak.samples_[(int) s].cto_ = sample_offset;
						++s;
					}
				}
			}
		}

		// calc sample offsets
		{
			long s = 0;
			long j;
			for (j = 0; j != trak.chunks_size_; j++) {
				long pos = trak.chunks_[(int) j].pos_;
				long i;
				for (i = 0; i != trak.chunks_[(int) j].size_; i++) {
					trak.samples_[(int) s].pos_ = pos;
					pos += trak.samples_[(int) s].size_;
					++s;
				}
			}
		}
	}

	int trak_write_index(trak_t trak, long start, long end) {
		// write samples [start,end>

		//log("Trak_write_index:" + start + ", " + end);
		long newp = trak.mdia_.minf_.stbl_.newp_;
		BArray new_start = trak.mdia_.minf_.stbl_.new_;
		BArray stbl_start = trak.mdia_.minf_.stbl_.start_;

		// stts = [entries * [sample_count, sample_duration]
		{
			long stts_atom_start = newp;
			long stts = trak.mdia_.minf_.stbl_.stts_;

			long entries = 0;
			// struct stts_table_t const* table = (struct stts_table_t*)(stts +
			// 8);
			long s;

			// copy header
			// atom + version + flags
			copyBytes(new_start, newp, stbl_start, stts - ATOM_PREAMBLE_SIZE,
					ATOM_PREAMBLE_SIZE + 4);

			// memcpy(newp, stts - ATOM_PREAMBLE_SIZE, ATOM_PREAMBLE_SIZE + 4);
			newp += ATOM_PREAMBLE_SIZE + 4;
			newp += 4; // Number Of Entries

			for (s = start; s != end; ++s) {
				long sample_count = 1;
				long sample_duration = trak.samples_[(int)s].sampleDuration;  //trak.samples_[(int) (s + 1)].pts_
						//- trak.samples_[(int) s].pts_;
				while (s != end - 1) {
					if ((trak.samples_[(int) (s + 1)].sampleDuration) != sample_duration)
						break;
					++sample_count;
					++s;
				}
				// write entry
				new_start.write_int32(newp, sample_count);
				newp += 4;
				new_start.write_int32(newp, sample_duration);
				newp += 4;

				++entries;
			}

			//log("stts entries: " + entries);
			stts = trak.mdia_.minf_.stbl_.stts_ = stts_atom_start
					+ ATOM_PREAMBLE_SIZE;
			new_start.write_int32(stts_atom_start + ATOM_PREAMBLE_SIZE + 4,
					entries);
			new_start.write_int32(stts_atom_start, newp - stts_atom_start);
			// printf("Atom(%c%c%c%c,%d)\n", stts_atom_start[4],
			// stts_atom_start[5],
			// stts_atom_start[6], stts_atom_start[7],
			// read_int32(stts_atom_start));
			if (stts_get_samples(new_start, stts_atom_start
					+ ATOM_PREAMBLE_SIZE) != end - start) {
				// printf("ERROR: stts_get_samples=%d, should be %d\n",
				// stts_get_samples(stts_atom_start + ATOM_PREAMBLE_SIZE), end -
				// start);
			}
		}

		// ctts = [entries * [sample_count, sample_offset]
		{
			long ctts_atom_start = newp;
			long ctts = trak.mdia_.minf_.stbl_.ctts_;
			if (ctts != 0) {
				int entries = 0;
				// struct ctts_table_t const* table = (struct
				// ctts_table_t*)(ctts + 8);
				long s;

				// copy header
				// atom + version + flags
				copyBytes(new_start, newp, stbl_start, ctts
						- ATOM_PREAMBLE_SIZE, ATOM_PREAMBLE_SIZE + 4);
				// memcpy(newp, ctts - ATOM_PREAMBLE_SIZE, ATOM_PREAMBLE_SIZE +
				// 4);
				newp += ATOM_PREAMBLE_SIZE + 4;
				newp += 4; // Number Of Entries

				for (s = start; s != end; ++s) {
					long sample_count = 1;
					long sample_offset = trak.samples_[(int) s].cto_;
					while (s != end - 1) {
						if (trak.samples_[(int) (s + 1)].cto_ != sample_offset)
							break;
						++sample_count;
						++s;
					}
					// write entry
					new_start.write_int32(newp, sample_count);
					newp += 4;
					new_start.write_int32(newp, sample_offset);
					newp += 4;
					++entries;
				}
				//log("ctts entries: " + entries);

				trak.mdia_.minf_.stbl_.ctts_ = ctts_atom_start
						+ ATOM_PREAMBLE_SIZE;
				new_start.write_int32(ctts_atom_start + ATOM_PREAMBLE_SIZE + 4,
						entries);
				new_start.write_int32(ctts_atom_start, newp - ctts_atom_start);
				// printf("Atom(%c%c%c%c,%d)\n", ctts_atom_start[4],
				// ctts_atom_start[5],
				// ctts_atom_start[6], ctts_atom_start[7],
				// read_int32(ctts_atom_start));
				if (ctts_get_samples(new_start, ctts_atom_start
						+ ATOM_PREAMBLE_SIZE) != end - start) {
					// printf("ERROR: ctts_get_samples=%d, should be %d\n",
					// ctts_get_samples(ctts_atom_start + ATOM_PREAMBLE_SIZE),
					// end - start);
				}
			}
		}

		// process chunkmap:
		{
			long stsc_atom_start = newp;
			long stsc = trak.mdia_.minf_.stbl_.stsc_;
			// struct stsc_table_t const* stsc_table = (struct
			// stsc_table_t*)(stsc + 8);
			long i;

			// copy header
			// atom + version + flags
			copyBytes(new_start, newp, stbl_start, stsc - ATOM_PREAMBLE_SIZE,
					ATOM_PREAMBLE_SIZE + 4);
			// memcpy(newp, stsc - ATOM_PREAMBLE_SIZE, ATOM_PREAMBLE_SIZE + 4);
			newp += ATOM_PREAMBLE_SIZE + 4;
			newp += 4; // Number Of Entries

			for (i = 0; i != trak.chunks_size_; ++i) {

				if (trak.chunks_[(int) i].sample_ + trak.chunks_[(int) i].size_ > start)
					break;
			}

			{
				long stsc_entries = 0;
				long chunk_start = i;
				long chunk_end;
				// problem.mp4: reported by Jin-seok Lee. Second track contains
				// no samples
				if (trak.chunks_size_ != 0) {
					long idx = i;
					/*
					 * if (i >= trak.chunks_size_) idx = i - 1;
					 */
					long samples = trak.chunks_[(int) idx].sample_
							+ trak.chunks_[(int) idx].size_ - start;
					long id = trak.chunks_[(int) idx].id_;

					// write entry [chunk,samples,id]
					new_start.write_int32(newp, 1);
					newp += 4;
					new_start.write_int32(newp, samples);
					newp += 4;
					new_start.write_int32(newp, id);
					newp += 4;
					++stsc_entries;
					if (i != trak.chunks_size_) {
						for (i += 1; i != trak.chunks_size_; ++i) {
							if (trak.chunks_[(int) i].sample_ >= end)
								break;

							if (trak.chunks_[(int) i].size_ != samples) {
								samples = trak.chunks_[(int) i].size_;
								id = trak.chunks_[(int) i].id_;
								new_start
										.write_int32(newp, i - chunk_start + 1);
								newp += 4;
								new_start.write_int32(newp, samples);
								newp += 4;
								new_start.write_int32(newp, id);
								newp += 4;
								++stsc_entries;
							}
						}
					}
				}
				chunk_end = i;
				trak.mdia_.minf_.stbl_.stsc_ = stsc_atom_start
						+ ATOM_PREAMBLE_SIZE;
				new_start.write_int32(stsc_atom_start + ATOM_PREAMBLE_SIZE + 4,
						stsc_entries);
				new_start.write_int32(stsc_atom_start, newp - stsc_atom_start);
				// printf("Atom(%c%c%c%c,%d)\n", stsc_atom_start[4],
				// stsc_atom_start[5],
				// stsc_atom_start[6], stsc_atom_start[7],
				// read_int32(stsc_atom_start));

				{
					long stco_atom_start = newp;
					long stco = trak.mdia_.minf_.stbl_.stco_;
					long entries = chunk_end;
					// uint32_t const* stco_table = (uint32_t*)(stco + 8);

					// copy header
					// atom + version + flags
					copyBytes(new_start, newp, stbl_start, stco
							- ATOM_PREAMBLE_SIZE, ATOM_PREAMBLE_SIZE + 4);
					// memcpy(newp, stco - ATOM_PREAMBLE_SIZE,
					// ATOM_PREAMBLE_SIZE + 4);
					newp += ATOM_PREAMBLE_SIZE + 4;
					newp += 4; // Number Of Entries

					for (i = chunk_start; i != entries; ++i) {
						// write_int32(new_start, newp,
						// read_int32(&stco_table[i]));
						new_start.write_int32(newp, stbl_start.read_int32(stco
								+ 8 + (i * 4)));
						newp += 4;
					}
					trak.mdia_.minf_.stbl_.stco_ = stco_atom_start
							+ ATOM_PREAMBLE_SIZE;
					new_start.write_int32(stco_atom_start + ATOM_PREAMBLE_SIZE
							+ 4, entries - chunk_start);
					new_start.write_int32(stco_atom_start, newp
							- stco_atom_start);
					// printf("Atom(%c%c%c%c,%d)\n", stco_atom_start[4],
					// stco_atom_start[5],
					// stco_atom_start[6], stco_atom_start[7],
					// read_int32(stco_atom_start));

					// patch first chunk with correct sample offset
					if (start < trak.samples_size_)
						new_start.write_int32(stco_atom_start
								+ ATOM_PREAMBLE_SIZE + 8,
								trak.samples_[(int) start].pos_);
				}
			}
		}

		// process sync samples:
		if (trak.mdia_.minf_.stbl_.stss_ != 0) {
			long stss_atom_start = newp;
			long stss = trak.mdia_.minf_.stbl_.stss_;
			long entries = stbl_start.read_int32(stss + 4);
			// uint32_t const* table = (uint32_t*)(stss + 8);
			long stss_start;
			long i;

			// copy header
			// atom + version + flags
			copyBytes(new_start, newp, stbl_start, stss - ATOM_PREAMBLE_SIZE,
					ATOM_PREAMBLE_SIZE + 4);
			// memcpy(newp, stss - ATOM_PREAMBLE_SIZE, ATOM_PREAMBLE_SIZE + 4);
			newp += ATOM_PREAMBLE_SIZE + 4;
			newp += 4; // Number Of Entries

			for (i = 0; i != entries; ++i) {
				// if(read_int32(&table[i]) >= start + 1)
				if (stbl_start.read_int32(stss + 8 + (4 * i)) >= start + 1)
					break;
			}
			stss_start = i;
			for (; i != entries; ++i) {
				// unsigned int sync_sample = read_int32(&table[i]);
				long sync_sample = stbl_start.read_int32(stss + 8 + (4 * i));
				if (sync_sample >= end + 1)
					break;
				new_start.write_int32(newp, sync_sample - start);
				newp += 4;
			}
			trak.mdia_.minf_.stbl_.stss_ = stss_atom_start + ATOM_PREAMBLE_SIZE;
			new_start.write_int32(stss_atom_start + ATOM_PREAMBLE_SIZE + 4, i
					- stss_start);
			new_start.write_int32(stss_atom_start, newp - stss_atom_start);
			// printf("Atom(%c%c%c%c,%d)\n", stss_atom_start[4],
			// stss_atom_start[5],
			// stss_atom_start[6], stss_atom_start[7],
			// read_int32(stss_atom_start));
		}

		// process sample sizes
		{
			long stsz_atom_start = newp;
			long stsz = trak.mdia_.minf_.stbl_.stsz_;

			// copy header
			// atom + version + flags, sample_size, number_of_etries
			copyBytes(new_start, newp, stbl_start, stsz - ATOM_PREAMBLE_SIZE,
					ATOM_PREAMBLE_SIZE + 12);
			// memcpy(newp, stsz - ATOM_PREAMBLE_SIZE, ATOM_PREAMBLE_SIZE + 12);
			newp += ATOM_PREAMBLE_SIZE + 12;

			if (stsz_get_sample_size(stbl_start, stsz) == 0) {
				// uint32_t const* table = (uint32_t*)(stsz + 12);
				copyBytes(new_start, newp, stbl_start, stsz + 12 + (start * 4),
						(end - start) * 4);
				// memcpy(newp, &table[start], (end - start) *
				// sizeof(uint32_t));
				newp += (end - start) * 4;
				new_start.write_int32(stsz_atom_start + ATOM_PREAMBLE_SIZE + 8,
						end - start);
			}
			trak.mdia_.minf_.stbl_.stsz_ = stsz_atom_start + ATOM_PREAMBLE_SIZE;
			new_start.write_int32(stsz_atom_start, newp - stsz_atom_start);
			// printf("Atom(%c%c%c%c,%d)\n", stsz_atom_start[4],
			// stsz_atom_start[5],
			// stsz_atom_start[6], stsz_atom_start[7],
			// read_int32(stsz_atom_start));
		}

		trak.mdia_.minf_.stbl_.newp_ = newp;

		// copy newly generated stbl over old one
		{
			stbl_t stbl = trak.mdia_.minf_.stbl_;

			// unsigned int old_stbl_size = read_int32(stbl.start_ -
			// ATOM_PREAMBLE_SIZE);
			// unsigned int new_stbl_size = stbl.newp_ - stbl.new_ +
			// ATOM_PREAMBLE_SIZE;
			long old_stbl_size = stbl_start.read_int32(stbl.initOffset
					- ATOM_PREAMBLE_SIZE);
			// dump_mp4(stbl_start, stbl.initOffset - ATOM_PREAMBLE_SIZE,
			// old_stbl_size);
			long new_stbl_size = stbl.newp_ + ATOM_PREAMBLE_SIZE;
			// printf("Atom(stbl,old=%d,new=%d)\n", old_stbl_size,
			// new_stbl_size);

			if (new_stbl_size > old_stbl_size) {
				log("new meta data larger than old (" + new_stbl_size + " > " + old_stbl_size + ")(delta=" + (new_stbl_size-old_stbl_size) + ")");
				return 0;
			}

			/* KG */
			// stbl.start_ = stbl.new_;
			copyBytes(stbl_start, stbl.initOffset, new_start, 0, new_stbl_size
					- ATOM_PREAMBLE_SIZE);
			// memcpy(stbl.start_, stbl.new_, new_stbl_size -
			// ATOM_PREAMBLE_SIZE);
			// write_int32(stbl.start_, stbl.initOffset - ATOM_PREAMBLE_SIZE,
			// new_stbl_size);

			// relocate stbl pointers

			if (stbl.stts_ != 0)
				stbl.stts_ += stbl.initOffset;
			if (stbl.stss_ != 0)
				stbl.stss_ += stbl.initOffset;
			if (stbl.stsc_ != 0)
				stbl.stsc_ += stbl.initOffset;
			if (stbl.stsz_ != 0)
				stbl.stsz_ += stbl.initOffset;
			if (stbl.stco_ != 0)
				stbl.stco_ += stbl.initOffset;
			if (stbl.ctts_ != 0)
				stbl.ctts_ += stbl.initOffset;

			/*
			 * free(trak.mdia_.minf_.stbl_.new_);
			 */
			trak.mdia_.minf_.stbl_.new_.free();
			trak.mdia_.minf_.stbl_.new_ = null;
			trak.mdia_.minf_.stbl_.newp_ = 0;

			// add free atom for left over
			if (new_stbl_size < old_stbl_size - ATOM_PREAMBLE_SIZE) {
				long free_size = old_stbl_size - new_stbl_size;
				long free_atom = newp + stbl.initOffset; // new_stbl_size -
				// ATOM_PREAMBLE_SIZE
				// +
				// (int)stbl.initOffset;
				stbl.start_.write_int32(free_atom, free_size);
				stbl.start_.set(4 + (int) free_atom, 'f');
				stbl.start_.set(5 + (int) free_atom, 'r');
				stbl.start_.set(6 + (int) free_atom, 'e');
				stbl.start_.set(7 + (int) free_atom, 'e');
				{
					byte[] free_bytes = "CodeShop".getBytes();
					/*
					 * const char free_bytes[8] = { 'C', 'o', 'd', 'e','S','h',
					 * 'o', 'p' };
					 */
					long padding_index;
					for (padding_index = ATOM_PREAMBLE_SIZE; padding_index != free_size; ++padding_index) {
						stbl.start_.set((int) free_atom + (int) padding_index,
								free_bytes[(int) padding_index
										% (int) free_bytes.length]);
					}
				}
			}
			// dump_mp4(stbl.start_, stbl.initOffset-ATOM_PREAMBLE_SIZE,
			// old_stbl_size);
			// dump_mp4(stbl.start_, stbl.initOffset, old_stbl_size -
			// ATOM_PREAMBLE_SIZE);
		}

		return 1;
	}

	public int moov_parse(moov_t moov, BArray buffer, long off, long size)
			throws IOException {
		MP4Atom leaf_atom;
		long bufferp = 0;
		// unsigned char* buffer_start = buffer;

		moov.start_ = buffer;

		while (bufferp < size) {
			leaf_atom = atom_read_header(buffer, bufferp + off);
			bufferp += ATOM_PREAMBLE_SIZE;
			// buffer = atom_read_header(buffer, leaf_atom);

			atom_print(leaf_atom);

			if (atom_is(leaf_atom, "cmov")) {
				return 0;
			} else if (atom_is(leaf_atom, "mvhd")) {
				moov.mvhd_ = bufferp + off;
			} else if (atom_is(leaf_atom, "trak")) {
				if (moov.tracks_ == MAX_TRACKS)
					return 0;
				else {
					trak_t trak = moov.traks_[(int) moov.tracks_];
					trak_init(trak);
					trak_parse(trak, buffer, bufferp + off, leaf_atom.size_
							- ATOM_PREAMBLE_SIZE);
					++moov.tracks_;
				}
			} else if (atom_is(leaf_atom, "iods")) {
				freeAtom(buffer, bufferp + off - ATOM_PREAMBLE_SIZE);
			}
			// buffer = atom_skip(buffer, leaf_atom);
			bufferp += leaf_atom.size_ - ATOM_PREAMBLE_SIZE;
		}

		// build the indexing tables
		{
			int i;
			for (i = 0; i != moov.tracks_; ++i) {
				trak_build_index(moov.traks_[i]);
			}
		}

		return 1;
	}

	void stco_shift_offsets(BArray stco_start, long stco, long offset) {
		long entries = stco_start.read_int32(stco + 4);
		// unsigned int* table = (unsigned int*)(stco + 8);
		long i;
		for (i = 0; i != entries; ++i) {
			// write_int32(&table[i], (read_int32(&table[i]) + offset));
			stco_start.write_int32(stco + 8 + (i * 4), (stco_start
					.read_int32(stco + 8 + (i * 4)) + offset));
		}
	}

	void trak_shift_offsets(trak_t trak, long offset) {
		// if (trak.deleted)
		// return;
		long stco = trak.mdia_.minf_.stbl_.stco_;
		stco_shift_offsets(trak.mdia_.minf_.stbl_.start_, stco, offset);
	}

	void moov_shift_offsets(moov_t moov, long offset) {
		long i;
		for (i = 0; i != moov.tracks_; ++i) {
			trak_shift_offsets(moov.traks_[(int) i], offset);
		}
	}

	long mvhd_get_time_scale(BArray buffer, long mvhd) {
		int version = buffer.read_char(mvhd);
		return buffer.read_int32(mvhd + (version == 0 ? 12 : 20));
		// unsigned char* p = mvhd + (version == 0 ? 12 : 20);
		// return read_int32(p);
	}

	void mvhd_set_duration(BArray buffer, long mvhd, long duration) {
		int version = buffer.read_char(mvhd);
		if (version == 0) {
			buffer.write_int32(mvhd + 16, duration);
		} else {
			buffer.write_int64(mvhd + 24, duration);
		}
	}

	long mdhd_get_time_scale(BArray buffer, long mdhd) {
		int version = buffer.read_char(mdhd);
		return buffer.read_int32(mdhd + (version == 0 ? 12 : 20));

		// return read_int32(p);
	}

	long mdhd_get_duration(BArray buffer, long mdhd) {
		int version = buffer.read_char(mdhd);
		if (version == 0) {
			return buffer.read_int32(mdhd + 16);
		} else {
			return buffer.read_int64(mdhd + 24);
		}
	}

	void mdhd_set_duration(BArray buffer, long mdhd, long duration) {
		int version = buffer.read_char(mdhd);
		if (version == 0) {
			buffer.write_int32(mdhd + 16, duration);
		} else {
			buffer.write_int64(mdhd + 24, duration);
		}
	}

	int tkhd_get_width_height(BArray buffer, long tkhd, int bo) {
		int version = buffer.read_char(tkhd);
		int offset = 56;
		if (version == 0)
			offset += 20;
		else
			offset += 28;
		offset += bo;
		long val = (int)buffer.read_int32(offset+tkhd);
		long reg = val / 65536;
		//long frac = val % 65536;
		return (int)reg;
	}
	int tkhd_get_width(BArray buffer, long tkhd) {
		return tkhd_get_width_height(buffer, tkhd, 0);
	}
	int tkhd_get_height(BArray buffer, long tkhd) {
		return tkhd_get_width_height(buffer, tkhd, 4);
	}

	void tkhd_set_duration(BArray buffer, long tkhd, long duration) {
		int version = buffer.read_char(tkhd);
		if (version == 0) {
			buffer.write_int32(tkhd + 20, duration);
		} else {
			buffer.write_int64(tkhd + 28, duration);
		}
	}

	long stss_get_entries(BArray buffer, long stss) {
		return buffer.read_int32(stss + 4);
	}

	long stss_get_sample(BArray buffer, long stss, long idx) {
		// unsigned char const* p = stss + 8 + idx * 4;
		return buffer.read_int32(stss + 8 + idx * 4);
	}

	long stss_get_nearest_keyframe(BArray buffer, long stss, long sample) {
		// scan the sync samples to find the key frame that precedes the sample
		// number
		long i;
		long entries = stss_get_entries(buffer, stss);
		long table_sample = 0;
		for (i = 0; i != entries; ++i) {
			table_sample = stss_get_sample(buffer, stss, i);
			if (table_sample >= sample)
				break;
		}
		if (table_sample == sample)
			return table_sample;
		else
			return stss_get_sample(buffer, stss, i - 1);
	}

	long stbl_get_nearest_keyframe(stbl_t stbl, long sample) {
		// If the sync atom is not present, all samples are implicit sync
		// samples.
		if (stbl.stss_ == 0)
			return sample;

		return stss_get_nearest_keyframe(stbl.start_, stbl.stss_, sample);
	}

	String get_handler_type(BArray b, long off) {
		byte[] type = new byte[4];

		for (int i = 0; i < 4; i++)
			type[i] = (byte) b.get(off + 8 + i);
		return new String(type);

	}

	public static final List<String> includeHandlers = Arrays
			.asList(new String[] { "sbtl", "vide", "soun" });

	boolean includeTrak(trak_t t) {
		long hdler = t.mdia_.hdlr_;
		if (hdler == 0)
			return false;
		String handlerType = get_handler_type(t.mdia_.start_, hdler);
		if (handlerType == null || handlerType.length() == 0)
			return false;
		return includeHandlers.contains(handlerType);
		/*
		 * Old code, which includes traks with a vmhd or smhd (video or sound
		 * header)
		 */
		/*
		 * long off = t.mdia_.minf_.initOffset; BArray b = t.mdia_.minf_.start_;
		 * MP4Atom batom = atom_read_header(b, off - ATOM_PREAMBLE_SIZE); long x =
		 * 0; while (x < batom.size_) { MP4Atom atom = atom_read_header(b, off +
		 * x); String typ = new String(atom.type_); if (typ.compareTo("vmhd") ==
		 * 0 || typ.compareTo("smhd") == 0) return true; //
		 * System.out.println("Atom: " + typ + ", len: " + atom.size_ + ", //
		 * end: " + atom.end_); x += atom.size_; } return false;
		 */
	}

	void freeAtom(BArray b, long free_off) {
		free_off += 4;
		b.set(free_off + 0, 'f');
		b.set(free_off + 1, 'r');
		b.set(free_off + 2, 'e');
		b.set(free_off + 3, 'e');

	}

	/*
	 * void stco_shift_offsets(BArray stco_start, long stco, long offset) { long
	 * entries = read_int32(stco_start, stco + 4); // unsigned int* table =
	 * (unsigned int*)(stco + 8); long i; for (i = 0; i != entries; ++i) { //
	 * write_int32(&table[i], (read_int32(&table[i]) + offset));
	 * write_int32(stco_start, stco + 8 + (i * 4), (read_int32(stco_start, stco +
	 * 8 + (i * 4)) + offset)); } }
	 * 
	 * void trak_shift_offsets(trak_t trak, long offset) { //if (trak.deleted)
	 * //return; long stco = trak.mdia_.minf_.stbl_.stco_;
	 * stco_shift_offsets(trak.mdia_.minf_.stbl_.start_, stco, offset); }
	 * 
	 * void moov_shift_offsets(moov_t moov, long offset) { long i; for (i = 0; i !=
	 * moov.tracks_; ++i) { trak_shift_offsets(moov.traks_[(int) i], offset); } }
	 */

	void reInterleave(moov_t moov, MP4Atom mdat_atom,
			long mdatStart, long skip_from_start, long end_offset) throws IOException {

		/*
		 * long newoffset = (mdatStart - mdat_atom.start_) - skip_from_start;
		 * moov_shift_offsets(moov, newoffset); // *mdat_start +=
		 * skip_from_start; if (end_offset != 0) { mdat_atom.size_ = end_offset; //
		 * *mdat_size = end_offset; } mdat_atom.size_ -= skip_from_start; //
		 * *mdat_size -= skip_from_start; chunkQueue.add(new
		 * Chunk(mdat_atom.start_ + ATOM_PREAMBLE_SIZE, mdat_atom.size_ -
		 * ATOM_PREAMBLE_SIZE));
		 */
		// chunk offsets are currently set to abs. position in file
		// as we write out a chunk, we need to change the chunk table to the
		// current pos in the file
		// then update mdat_atom based on new size....
		// rebuild the idexes
		for (int i = 0; i < moov.tracks_; i++) {
			trak_build_index(moov.traks_[i]);
		}

		long maxdelta = 0;
		for (int i=0;i<moov.tracks_;i++) {
			if (moov.traks_[i].chunks_.length == 0)
				continue;
			for (int j=0;j<moov.tracks_;j++) {
				if (moov.traks_[j].chunks_.length == 0)
					continue;				
				long delta = Math.abs(moov.traks_[i].chunks_[0].pos_ -moov.traks_[j].chunks_[0].pos_);
				maxdelta = Math.max(delta, maxdelta);
			}
		}
		log("ChunkDelta: " + maxdelta);
		if (maxdelta < MAX_CHUNK_DELTA_BEFORE_INTERLEAVE) {
			log("Skipping reinterleave...");
			addAsOneChunk(moov, mdat_atom, skip_from_start, end_offset, mdatStart);
			return;
		}
		log("Continuing reinterleave...");
		long curOffset = mdatStart + ATOM_PREAMBLE_SIZE;
		int unfinishedTraks = (int) moov.tracks_;

		long moov_time_scale = mvhd_get_time_scale(moov.start_, moov.mvhd_);
		long interDur = (long) (INTERLEAVE_DURATION * moov_time_scale);

		int chunkPositions[] = new int[(int) moov.tracks_];
		long chunkCounts[] = new long[(int) moov.tracks_];
		float trackToMoovTime[] = new float[(int) moov.tracks_];
		int totalChunks = 0;
		for (int i = 0; i != moov.tracks_; ++i) {
			trak_t trak = moov.traks_[(int) i];
			chunkCounts[i] = trak.chunks_size_;
			totalChunks += trak.chunks_size_;
			chunkPositions[i] = 0;
			long trak_time_scale = mdhd_get_time_scale(trak.mdia_.start_,
					trak.mdia_.mdhd_);
			trackToMoovTime[i] = (float) moov_time_scale
					/ (float) trak_time_scale;

		}

		chunkQueue = new ChunkQueue(totalChunks);
		// track we are working on
		int t = -1;
		long smallestTime = 0;
		do {
			long curTrakPos = 0;
			if (t != -1 && chunkPositions[t] != -1)
				curTrakPos = (long) (stts_get_time(
						moov.traks_[t].mdia_.minf_.stbl_.start_,
						moov.traks_[t].mdia_.minf_.stbl_.stts_,
						moov.traks_[t].chunks_[chunkPositions[t]].sample_) * trackToMoovTime[t]);
			if (t == -1 || chunkPositions[t] == -1
					|| (curTrakPos - smallestTime) >= interDur) {
				t = -1;
				long minPos = Long.MAX_VALUE;
				smallestTime = Long.MAX_VALUE;
				for (int i = 0; i < moov.tracks_; i++) {
					if (chunkPositions[i] == -1) // this chunk is done
						continue;
					long timPos = (long) (stts_get_time(
							moov.traks_[i].mdia_.minf_.stbl_.start_,
							moov.traks_[i].mdia_.minf_.stbl_.stts_,
							moov.traks_[i].chunks_[chunkPositions[i]].sample_) * trackToMoovTime[i]);
					if (t == -1 || timPos < minPos) {
						smallestTime = minPos;
						minPos = timPos;
						t = i;
					} else {
						if (timPos < smallestTime)
							smallestTime = timPos;
					}
				}
			}
			if (t != -1) {
				// Add chunk to buffer
				trak_t trak = moov.traks_[(int) t];
				chunks_t chunk = trak.chunks_[chunkPositions[t]];
				long stco = trak.mdia_.minf_.stbl_.stco_;
				BArray b = trak.mdia_.minf_.stbl_.start_;

				// long siz = get_chunk_size(trak, chunkPositions[t]); // read
				// chunk size;
				long num_samples = chunk.size_;
				long sample_start = chunk.sample_;
				long pos = chunk.pos_;
				long siz = 0;
				for (long s = sample_start; s < sample_start + num_samples; s++) {
					siz += trak.samples_[(int) s].size_;
				}

				chunkQueue.add(new Chunk(pos, siz));
				// update chunk table
				b.write_int32(stco + 8 + (chunkPositions[t] * 4), curOffset);
				chunkPositions[t]++;
				curOffset += siz;
				if (chunkPositions[t] == chunkCounts[t]) {
					chunkPositions[t] = -1;
					unfinishedTraks--;
				}
			}

		} while (unfinishedTraks > 0);
		mdat_atom.start_ = mdatStart;
		mdat_atom.size_ = curOffset - mdatStart;
	}

	long moov_seek(BArray moov_data, MP4Atom moov_atom, float start_time,
			float end_time, MP4Atom mdat_atom, long mdatStart) throws IOException {
		moov_t moov = new moov_t();

		moov_init(moov);
		if (0 == moov_parse(moov, moov_data, ATOM_PREAMBLE_SIZE,
				moov_atom.size_ - ATOM_PREAMBLE_SIZE)) {
			moov_exit(moov);
			// free(moov);
			log("moov_parse failed");
			return 0;
		}
		try {
			long moov_time_scale = mvhd_get_time_scale(moov.start_, moov.mvhd_);
			long start = (long) (start_time * moov_time_scale);
			long end = (long) (end_time * moov_time_scale);
			long skip_from_start = 0x7fffffffffffffffL;
			long end_offset = 0;
			long i;
			long pass;

			// for every trak, convert seconds to sample (time-to-sample).
			// adjust sample to keyframe
			long[] trak_sample_start = new long[MAX_TRACKS];
			long[] trak_sample_end = new long[MAX_TRACKS];

			long moov_duration = 0;

			// clayton.mp4 has a third track with one sample that lasts the
			// whole clip.
			// Assuming the first two tracks are the audio and video track, we
			// patch
			// the remaining tracks to 'free' atoms.
			/*
			 * if(moov.tracks_ > 2) { for(i = 2; i != moov.tracks_; ++i) { //
			 * patch 'trak' to 'free' //unsigned char* p = moov.traks_[i].start_ -
			 * 4; long free_off = moov.traks_[(int)i].initOffset - 4;
			 * moov.traks_[(int)i].start_.set(free_off+0, 'f');
			 * moov.traks_[(int)i].start_.set(free_off+1, 'r');
			 * moov.traks_[(int)i].start_.set(free_off+2, 'e');
			 * moov.traks_[(int)i].start_.set(free_off+3, 'e'); } moov.tracks_ =
			 * 2; }
			 */
			for (i = 0; i < moov.tracks_; i++) {
				if (!includeTrak(moov.traks_[(int) i])) {
					// patch 'trak' to 'free'
					// unsigned char* p = moov.traks_[i].start_ - 4;
					long free_off = moov.traks_[(int) i].initOffset
							- ATOM_PREAMBLE_SIZE;
					freeAtom(moov.traks_[(int) i].start_, free_off);
					moov.traks_[(int) i].deleted = true;
				}
			}

			// Now physically remove delete tracks from arrays
			for (i = 0; i < moov.tracks_;) {
				if (moov.traks_[(int) i].deleted) {
					for (int j = (int) i + 1; j < moov.tracks_; j++) {
						moov.traks_[(int) i] = moov.traks_[j];
					}
					moov.tracks_--;
				} else
					i++;
			}
			// reported by everwanna:
			// av out of sync because:
			// audio track 0 without stss, seek to the exact time.
			// video track 1 with stss, seek to the nearest key frame time.
			//
			// fixed:
			// first pass we get the new aligned times for traks with an stss
			// present
			// second pass is for traks without an stss
			for (pass = 0; pass != 2; ++pass) {
				for (i = 0; i != moov.tracks_; ++i) {
					trak_t trak = moov.traks_[(int) i];
					// if (trak.deleted)
					// continue;
					stbl_t stbl = trak.mdia_.minf_.stbl_;
					long trak_time_scale = mdhd_get_time_scale(
							trak.mdia_.start_, trak.mdia_.mdhd_);
					float moov_to_trak_time = (float) trak_time_scale
							/ (float) moov_time_scale;
					float trak_to_moov_time = (float) moov_time_scale
							/ (float) trak_time_scale;

					// 1st pass: stss present, 2nd pass: no stss present
					if (pass == 0 && stbl.stss_ == 0)
						continue;
					if (pass == 1 && stbl.stss_ != 0)
						continue;

					// ignore empty track

					if (mdhd_get_duration(trak.mdia_.start_, trak.mdia_.mdhd_) == 0)
						continue;

					// get start
					if (start == 0) {
						trak_sample_start[(int) i] = start;
					} else {
						start = stts_get_sample(stbl.start_, stbl.stts_,
								(long) (start * moov_to_trak_time));
						// printf("start=%u (trac time)=%.2f (seconds)", start,
						// stts_get_time(stbl.stts_, start) /
						// (float)trak_time_scale);
						start = stbl_get_nearest_keyframe(stbl, start + 1) - 1;
						// printf("=%u (zero based keyframe)", start);
						trak_sample_start[(int) i] = start;
						start = (long) (stts_get_time(stbl.start_, stbl.stts_,
								start) * trak_to_moov_time);
						// printf("=%u (moov time)\n", start);
					}

					// get end
					if (end == 0) {
						trak_sample_end[(int) i] = trak.samples_size_;
					} else {
						end = stts_get_sample(stbl.start_, stbl.stts_,
								(long) (end * moov_to_trak_time));
						if (end >= trak.samples_size_) {
							end = trak.samples_size_;
						} else {
							end = stbl_get_nearest_keyframe(stbl, end + 1) - 1;
						}
						trak_sample_end[(int) i] = end;
						// printf("endframe=%u, samples_size_=%u\n", end,
						// trak.samples_size_);
						end = (long) (stts_get_time(stbl.start_, stbl.stts_,
								end) * trak_to_moov_time);
					}
				}
			}

			// printf("start=%u\n", start);
			// printf("end=%u\n", end);

			if (end > 0 && start >= end) {
				log("Error, start >= end");
				return 0;
			}
			log("Start sample: " + start +", end: " + end);

			for (i = 0; i != moov.tracks_; ++i) {
				trak_t trak = moov.traks_[(int) i];
				// if (trak.deleted)
				// continue;
				stbl_t stbl = trak.mdia_.minf_.stbl_;

				long start_sample = trak_sample_start[(int) i];
				long end_sample = trak_sample_end[(int) i];

				// if (start_sample == end_sample)
				// continue;
				// ignore empty track
				if (mdhd_get_duration(trak.mdia_.start_, trak.mdia_.mdhd_) == 0)
					continue;

				if (0 == trak_write_index(trak, start_sample, end_sample)) {
					log("trak_write_index failed");
					return 0;
				}

				{
					long skip = 0;
					if (start_sample < trak.samples_size_)
						skip = trak.samples_[(int) start_sample].pos_
								- trak.samples_[0].pos_;
					if (skip < skip_from_start)
						skip_from_start = skip;
					// printf("Trak can skip %llu bytes\n", skip);

					if (end_sample != trak.samples_size_) {
						long end_pos = trak.samples_[(int) end_sample].pos_;
						if (end_pos > end_offset)
							end_offset = end_pos;
						// printf("New endpos=%llu\n", end_pos);
						// printf("Trak can skip %llu bytes at end\n",
						// *mdat_start + *mdat_size - end_offset);
					}
				}

				{
					// fixup trak (duration)
					long trak_duration = stts_get_duration(stbl.start_,
							stbl.stts_);
					long trak_time_scale = mdhd_get_time_scale(
							trak.mdia_.start_, trak.mdia_.mdhd_);
					float trak_to_moov_time = (float) moov_time_scale
							/ (float) trak_time_scale;
					long duration = (long) ((float) trak_duration * trak_to_moov_time);
					mdhd_set_duration(trak.start_, trak.mdia_.mdhd_,
							trak_duration);
					tkhd_set_duration(trak.start_, trak.tkhd_, duration);
					//log("trak: new_dur: " + duration);
					// printf("trak: new_duration=%lld\n", duration);

					if (duration > moov_duration)
						moov_duration = duration;
					//log("maxDur:" + duration);
				}
				int w = tkhd_get_width(trak.start_, trak.tkhd_);
				int h = tkhd_get_height(trak.start_, trak.tkhd_);
				if (h > this.height)
					this.height = h;
				if (w > this.width)
					this.width = w;

				// printf("stco.size=%d, ", read_int32(stbl.stco_ + 4));
				// printf("stts.size=%d samples=%d\n", read_int32(stbl.stts_ +
				// 4), stts_get_samples(stbl.stts_));
				// printf("stsz.size=%d\n", read_int32(stbl.stsz_ + 8));
				// printf("stsc.samples=%d\n", stsc_get_samples(stbl.stsc_));
			}
			mvhd_set_duration(moov.start_, moov.mvhd_, moov_duration);
			subDuration = (long) (1000L * (moov_duration / (float) moov_time_scale));

			// printf("shifting offsets by %lld\n", offset);
			mdat_atom.start_ += skip_from_start;
			if (reInterleave) {
				reInterleave(moov, mdat_atom, mdatStart,
						skip_from_start, end_offset);
			} else {
				log("reinterleave turned off. Skipping.");
				addAsOneChunk(moov, mdat_atom, skip_from_start, end_offset, mdatStart);
			}

		} finally {
			for (int i = 0; i < moov.tracks_; i++) {
				if (moov.traks_[i].mdia_.minf_.stbl_.new_ != null) {
					moov.traks_[i].mdia_.minf_.stbl_.new_.free();
					moov.traks_[i].mdia_.minf_.stbl_.new_ = null;
				}

			}
		}
		
		log("Width: " + width + ", Height: " + height);


		moov_exit(moov);
		// free(moov);

		return 1;
	}
	
	public void addAsOneChunk(moov_t moov, MP4Atom mdat_atom, long skip_from_start, long end_offset, long mdatStart) throws IOException {
		long newoffset = (mdatStart - mdat_atom.start_);		
		moov_shift_offsets(moov, newoffset);
		// *mdat_start += skip_from_start;
		if (end_offset != 0) {
			mdat_atom.size_ = end_offset;
			// *mdat_size = end_offset;
		}
		mdat_atom.size_ -= skip_from_start;
		// *mdat_size -= skip_from_start;
		chunkQueue = new ChunkQueue(1);
		chunkQueue.add(new Chunk(mdat_atom.start_ + ATOM_PREAMBLE_SIZE,
				mdat_atom.size_ - ATOM_PREAMBLE_SIZE));

	}

	public void cleanup() {
		if (curByteArray == null && !buffers.isEmpty())
			curByteArray = buffers.removeFirst();
		while (curByteArray != null) {
			curByteArray.free();
			curByteArray = buffers.poll();
		}
		if (chunkQueue != null)
			chunkQueue.free();
		chunkQueue = null;
	}

	@Override
	public void close() throws IOException {
		if (fp != null)
			fp.close();
		fp = null;
		super.close();
	}
	
	public static void log(String s) {
		if (logger != null)
			logger.log(s);
		else
			System.err.println(s);		
	}
	public static interface Logger {
		public void log(String str);
	}
		

}

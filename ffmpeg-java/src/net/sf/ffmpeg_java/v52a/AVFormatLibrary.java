package net.sf.ffmpeg_java.v52a;

import net.sf.ffmpeg_java.FFMPEGLibrary;
import net.sf.ffmpeg_java.FFmpegMgr;
import net.sf.ffmpeg_java.custom_protocol.URLProtocol;


import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Based on FFMPEG Aug 12 2007.  From avformat.h, avio.h
 * @author Ken Larson
 *
 */
public interface AVFormatLibrary extends FFMPEGLibrary 
{
	// Make sure the library is inited BEFORE we set the INSTANCE variable
	public static final int avFormatLibVer = FFmpegMgr.getAvFormatVersion();

    public static final AVFormatLibrary INSTANCE = (AVFormatLibrary) Native.loadLibrary(
    		System.getProperty("avformat.lib",
    	    		System.getProperty("os.name").startsWith("Windows") ? "avformat-52" : "avformat"), 
    		AVFormatLibrary.class);

//------------------------------------------------------------------------------------------------------------------------
// avformat.h
    
    public static final int LIBAVFORMAT_VERSION_INT = ((52<<16)+(23<<8)+1);
    public static final String LIBAVFORMAT_VERSION     = "52.23.1";
    public static final int LIBAVFORMAT_BUILD       = LIBAVFORMAT_VERSION_INT;

    public static final String LIBAVFORMAT_IDENT       = "Lavf" + LIBAVFORMAT_VERSION;
    
    
	//	typedef struct AVPacket {
	//    int64_t pts;                            ///< presentation time stamp in time_base units
	//    int64_t dts;                            ///< decompression time stamp in time_base units
	//    uint8_t *data;
	//    int   size;
	//    int   stream_index;
	//    int   flags;
	//    int   duration;                         ///< presentation duration in time_base units (0 if not available)
	//    void  (*destruct)(struct AVPacket *);
	//    void  *priv;
	//    int64_t pos;                            ///< byte position in stream, -1 if unknown
	//} AVPacket;
	
    interface Destruct extends Callback 
    {
        void callback(AVPacket pkt);
    }

	public static class AVPacket extends Structure
	{
		public long pts;                           
		public long dts;                           
		public Pointer data;
		public int   size;
		public int   stream_index;
		public int   flags;
		public int   duration;                        
		public Destruct destruct;
		public Pointer priv;
		public long pos;   
		public long convergence_duration;
	}
	
	public static final int PKT_FLAG_KEY   = 0x0001;
	
	void av_destruct_packet_nofree(AVPacket pkt);
	void av_destruct_packet(AVPacket pkt);
	void av_init_packet(AVPacket pkt);
	int av_new_packet(AVPacket pkt, int size);
	int av_get_packet(ByteIOContext s, AVPacket pkt, int size);
	int av_dup_packet(AVPacket pkt);

	
//	/**
//	 * Free a packet
//	 *
//	 * @param pkt packet to free
//	 */
//	static inline void av_free_packet(AVPacket *pkt)
//	{
//	    if (pkt && pkt->destruct) {
//	        pkt->destruct(pkt);
//	    }
//	}
	void av_free_packet(AVPacket pkt);	// TODO: this won't work with JNA, since it is inlined.
	
	//	typedef struct AVFrac {
	//    int64_t val, num, den;
	//} AVFrac attribute_deprecated;
	
	public static class AVFrac extends Structure
	{
	    public long val;
	    public long num;
	    public long den;
	}
	
//	/** this structure contains the data a format has to probe a file */
//	typedef struct AVProbeData {
//	    const char *filename;
//	    unsigned char *buf;
//	    int buf_size;
//	} AVProbeData;
//
//	#define AVPROBE_SCORE_MAX 100               ///< max score, half of that is used for file extension based detection
//	#define AVPROBE_PADDING_SIZE 32             ///< extra allocated bytes at the end of the probe buffer

	/** this structure contains the data a format has to probe a file */
	public static class AVProbeData extends Structure
	{
	    public Pointer filename;
	    public Pointer buf;
	    public int buf_size;
	}

	public static final int AVPROBE_SCORE_MAX =100;               ///< max score, half of that is used for file extension based detection
	public static final int AVPROBE_PADDING_SIZE =32;             ///< extra allocated bytes at the end of the probe buffer

	public static class AVFormatParameters extends Structure
	{
	    public AVRational time_base;
	    public int sample_rate;
	    public int channels;
	    public int width;
	    public int height;
	    public int pix_fmt;
	    public int channel; /**< used to select dv channel */
	//#if LIBAVFORMAT_VERSION_INT < (52<<16)
	    //public Pointer device; /**< video, audio or DV device */
	//#endif
	    public Pointer standard; /**< tv standard, NTSC, PAL, SECAM */
		public int bitFields;	// bit fields not supported by JNA
//	    int mpeg2ts_raw:1;  /**< force raw MPEG2 transport stream output, if possible */
//	    int mpeg2ts_compute_pcr:1; /**< compute exact PCR for each transport
//	                                  stream packet (only meaningful if
//	                                  mpeg2ts_raw is TRUE) */
//	    int initial_pause:1;       /**< do not begin to play the stream
//	                                  immediately (RTSP only) */
//	    int prealloced_context:1;
	    public int video_codec_id;
	    public int audio_codec_id;
	}
	
	public static final int AVFMT_NOFILE        =0x0001;
	public static final int AVFMT_NEEDNUMBER    =0x0002; /**< needs '%d' in filename */
	public static final int AVFMT_SHOW_IDS      =0x0008; /**< show format stream IDs numbers */
	public static final int AVFMT_RAWPICTURE    =0x0020; /**< format wants AVPicture structure for
	                                      raw picture data */
	public static final int AVFMT_GLOBALHEADER  =0x0040; /**< format wants global header */
	public static final int AVFMT_NOTIMESTAMPS  =0x0080; /**< format does not need / have any timestamps */
	public static final int AVFMT_GENERIC_INDEX =0x0100; /**< use generic index building code */
	
	
//	typedef struct AVOutputFormat {
//	    const char *name;
//	    const char *long_name;
//	    const char *mime_type;
//	    const char *extensions; /**< comma separated filename extensions */
//	    /** size of private data so that it can be allocated in the wrapper */
//	    int priv_data_size;
//	    /* output support */
//	    enum CodecID audio_codec; /**< default audio codec */
//	    enum CodecID video_codec; /**< default video codec */
//	    int (*write_header)(struct AVFormatContext *);
//	    int (*write_packet)(struct AVFormatContext *, AVPacket *pkt);
//	    int (*write_trailer)(struct AVFormatContext *);
//	    /** can use flags: AVFMT_NOFILE, AVFMT_NEEDNUMBER, AVFMT_GLOBALHEADER */
//	    int flags;
//	    /** currently only used to set pixel format if not YUV420P */
//	    int (*set_parameters)(struct AVFormatContext *, AVFormatParameters *);
//	    int (*interleave_packet)(struct AVFormatContext *, AVPacket *out, AVPacket *in, int flush);
//
//	    /**
//	     * list of supported codec_id-codec_tag pairs, ordered by "better choice first"
//	     * the arrays are all CODEC_ID_NONE terminated
//	     */
//	    const struct AVCodecTag **codec_tag;
//
//	    enum CodecID subtitle_codec; /**< default subtitle codec */
//
//	    /* private fields */
//	    struct AVOutputFormat *next;
//	} AVOutputFormat;
	
	public static class AVOutputFormat extends Structure
	{
	    public String name;
	    public String long_name;
	    public String mime_type;
	    public String extensions; /**< comma separated filename extensions */
	    public int priv_data_size;
	    public int audio_codec; /**< default audio codec */
	    public int video_codec; /**< default video codec */
	    public Pointer write_header;
	    public Pointer write_packet;
	    public Pointer write_trailer;
	    public int flags;
	    public Pointer set_parameters;
	    public Pointer interleave_packet;
	    public Pointer codec_tag;
	    public int subtitle_codec; /**< default subtitle codec */
	    public Pointer next;

	    public AVOutputFormat() {
	    	super();
	    }
	    
	    public AVOutputFormat(Pointer p)
		{	super();
			useMemory(p);
			read();
		}
	}
	
//	typedef struct AVInputFormat {
//	    const char *name;
//	    const char *long_name;
//	    /** size of private data so that it can be allocated in the wrapper */
//	    int priv_data_size;
//	    /**
//	     * tell if a given file has a chance of being parsed by this format.
//	     * The buffer provided is guranteed to be AVPROBE_PADDING_SIZE bytes big
//	     * so you dont have to check for that unless you need more.
//	     */
//	    int (*read_probe)(AVProbeData *);
//	    /** read the format header and initialize the AVFormatContext
//	       structure. Return 0 if OK. 'ap' if non NULL contains
//	       additional paramters. Only used in raw format right
//	       now. 'av_new_stream' should be called to create new streams.  */
//	    int (*read_header)(struct AVFormatContext *,
//	                       AVFormatParameters *ap);
//	    /** read one packet and put it in 'pkt'. pts and flags are also
//	       set. 'av_new_stream' can be called only if the flag
//	       AVFMTCTX_NOHEADER is used. */
//	    int (*read_packet)(struct AVFormatContext *, AVPacket *pkt);
//	    /** close the stream. The AVFormatContext and AVStreams are not
//	       freed by this function */
//	    int (*read_close)(struct AVFormatContext *);
//	    /**
//	     * seek to a given timestamp relative to the frames in
//	     * stream component stream_index
//	     * @param stream_index must not be -1
//	     * @param flags selects which direction should be preferred if no exact
//	     *              match is available
//	     * @return >= 0 on success (but not necessarily the new offset)
//	     */
//	    int (*read_seek)(struct AVFormatContext *,
//	                     int stream_index, int64_t timestamp, int flags);
//	    /**
//	     * gets the next timestamp in AV_TIME_BASE units.
//	     */
//	    int64_t (*read_timestamp)(struct AVFormatContext *s, int stream_index,
//	                              int64_t *pos, int64_t pos_limit);
//	    /** can use flags: AVFMT_NOFILE, AVFMT_NEEDNUMBER */
//	    int flags;
//	    /** if extensions are defined, then no probe is done. You should
//	       usually not use extension format guessing because it is not
//	       reliable enough */
//	    const char *extensions;
//	    /** general purpose read only value that the format can use */
//	    int value;
//
//	    /** start/resume playing - only meaningful if using a network based format
//	       (RTSP) */
//	    int (*read_play)(struct AVFormatContext *);
//
//	    /** pause playing - only meaningful if using a network based format
//	       (RTSP) */
//	    int (*read_pause)(struct AVFormatContext *);
//
//	    const struct AVCodecTag **codec_tag;
//
//	    /* private fields */
//	    struct AVInputFormat *next;
//	} AVInputFormat;
	
	public static class AVInputFormat extends Structure
	{
	    public String name;
	    public String long_name;
	    public int priv_data_size;
	    public Pointer read_probe;
	    public Pointer read_header;
	    public Pointer read_packet;
	    public Pointer read_close;
	    public Pointer read_seek;
	    public Pointer read_timestamp;
	    public int flags;
	    public String extensions;
	    public int value;
	    public Pointer read_play;
	    public Pointer read_pause;
	    public Pointer codec_tag;
	    public Pointer next;

	    public AVInputFormat() {
	    	super();
	    }
	    
	    public AVInputFormat(Pointer p)
		{	super();
			useMemory(p);
			read();
		}
	}
	
	//enum AVStreamParseType:
    public static final int AVSTREAM_PARSE_NONE = 0;
    public static final int AVSTREAM_PARSE_FULL = 1;       /**< full parsing and repack */
    public static final int AVSTREAM_PARSE_HEADERS = 2;    /**< only parse headers, don't repack */
    public static final int AVSTREAM_PARSE_TIMESTAMPS = 3; /**< full parsing and interpolation of timestamps for frames not starting on packet boundary */

//    typedef struct AVIndexEntry {
//        int64_t pos;
//        int64_t timestamp;
//    #define AVINDEX_KEYFRAME 0x0001
//        int flags:2;
//        int size:30; //Yeah, trying to keep the size of this small to reduce memory requirements (it is 24 vs 32 byte due to possible 8byte align).
//        int min_distance;         /**< min distance between this and the previous keyframe, used to avoid unneeded searching */
//    } AVIndexEntry;
    
   public static class AVIndexEntry extends Structure
	{
    	public long pos;
        public long timestamp;
        	public static final int AVINDEX_KEYFRAME = 0x0001;
        public int bit_fields; // JNA does not support bit fields
//        int flags:2;
//        int size:30; //Yeah, trying to keep the size of this small to reduce memory requirements (it is 24 vs 32 byte due to possible 8byte align).
        public int min_distance;         /**< min distance between this and the previous keyframe, used to avoid unneeded searching */
    }
	
//	typedef struct AVStream {
//   int index;    /**< stream index in AVFormatContext */
//   int id;       /**< format specific stream id */
//   AVCodecContext *codec; /**< codec context */
//   /**
//    * real base frame rate of the stream.
//    * this is the lowest framerate with which all timestamps can be
//    * represented accurately (it is the least common multiple of all
//    * framerates in the stream), Note, this value is just a guess!
//    * for example if the timebase is 1/90000 and all frames have either
//    * approximately 3600 or 1800 timer ticks then r_frame_rate will be 50/1
//    */
//   AVRational r_frame_rate;
//   void *priv_data;
//
//   /* internal data used in av_find_stream_info() */
//   int64_t first_dts;
//#if LIBAVFORMAT_VERSION_INT < (52<<16)
//   int codec_info_nb_frames;
//#endif
//   /** encoding: PTS generation when outputing stream */
//   struct AVFrac pts;
//
//   /**
//    * this is the fundamental unit of time (in seconds) in terms
//    * of which frame timestamps are represented. for fixed-fps content,
//    * timebase should be 1/framerate and timestamp increments should be
//    * identically 1.
//    */
//   AVRational time_base;
//   int pts_wrap_bits; /**< number of bits in pts (used for wrapping control) */
//   /* ffmpeg.c private use */
//   int stream_copy; /**< if set, just copy stream */
//   enum AVDiscard discard; ///< selects which packets can be discarded at will and do not need to be demuxed
//   //FIXME move stuff to a flags field?
//   /** quality, as it has been removed from AVCodecContext and put in AVVideoFrame
//    * MN: dunno if that is the right place for it */
//   float quality;
//   /** decoding: pts of the first frame of the stream, in stream time base. */
//   int64_t start_time;
//   /** decoding: duration of the stream, in stream time base. */
//   int64_t duration;
//
//   char language[4]; /** ISO 639 3-letter language code (empty string if undefined) */
//
//   /* av_read_frame() support */
//   enum AVStreamParseType need_parsing;
//   struct AVCodecParserContext *parser;
//
//   int64_t cur_dts;
//   int last_IP_duration;
//   int64_t last_IP_pts;
//   /* av_seek_frame() support */
//   AVIndexEntry *index_entries; /**< only used if the format does not
//                                   support seeking natively */
//   int nb_index_entries;
//   unsigned int index_entries_allocated_size;
//
//   int64_t nb_frames;                 ///< number of frames in this stream if known or 0
//
//#define MAX_REORDER_DELAY 4
//   int64_t pts_buffer[MAX_REORDER_DELAY+1];
//} AVStream;

	public static class AVStream extends Structure
	{
	   public int index;   
	   public int id;      
	   public Pointer codec; 
	   public AVRational r_frame_rate;
	   public Pointer priv_data;
	   public long first_dts;
	//#if LIBAVFORMAT_VERSION_INT < (52<<16)
	   //public int codec_info_nb_frames;
	//#endif
	   public AVFrac pts;
	   public AVRational time_base;
	   public int pts_wrap_bits; 
	   public int stream_copy; 
	   public int discard; 
	   public float quality;
	   public long start_time;
	   public long duration;
	   public byte[] language = new byte[4]; 
	   public int need_parsing;
	   public Pointer parser;
	   public long cur_dts;
	   public int last_IP_duration;
	   public long last_IP_pts;
	   public Pointer index_entries; 
	   public int nb_index_entries;
	   public int index_entries_allocated_size;
	   public long nb_frames;              
	   public static final int old_MAX_REORDER_DELAY = 4;
	   public long[] old_pts_buffer = new long[old_MAX_REORDER_DELAY+1];
	   public Pointer filename;
	   public int disposition;
	   public AVProbeData probe_data;
	   public static final int MAX_REORDER_DELAY = 16;
	   public long[] pts_buffer = new long[MAX_REORDER_DELAY+1];
	   public AVRational sample_aspect_ratio;
		public AVStream()
		{
			super();
		}
		
		public AVStream(Pointer p)
		{	super();
			useMemory(p);
			read();
		}
		public AVStream(AVStream s) {
			this(s.getPointer());
		}
		
	}
	
	public static final int AVFMTCTX_NOHEADER      = 0x0001; /**< signal that no header is present
    (streams are added dynamically) */

	public static final int MAX_STREAMS = 20;
	
//	/* format I/O context */
//	typedef struct AVFormatContext {
//	    const AVClass *av_class; /**< set by av_alloc_format_context */
//	    /* can only be iformat or oformat, not both at the same time */
//	    struct AVInputFormat *iformat;
//	    struct AVOutputFormat *oformat;
//	    void *priv_data;
//	    ByteIOContext pb;
//	    unsigned int nb_streams;
//	    AVStream *streams[MAX_STREAMS];
//	    char filename[1024]; /**< input or output filename */
//	    /* stream info */
//	    int64_t timestamp;
//	    char title[512];
//	    char author[512];
//	    char copyright[512];
//	    char comment[512];
//	    char album[512];
//	    int year;  /**< ID3 year, 0 if none */
//	    int track; /**< track number, 0 if none */
//	    char genre[32]; /**< ID3 genre */
//
//	    int ctx_flags; /**< format specific flags, see AVFMTCTX_xx */
//	    /* private data for pts handling (do not modify directly) */
//	    /** This buffer is only needed when packets were already buffered but
//	       not decoded, for example to get the codec parameters in mpeg
//	       streams */
//	    struct AVPacketList *packet_buffer;
//
//	    /** decoding: position of the first frame of the component, in
//	       AV_TIME_BASE fractional seconds. NEVER set this value directly:
//	       it is deduced from the AVStream values.  */
//	    int64_t start_time;
//	    /** decoding: duration of the stream, in AV_TIME_BASE fractional
//	       seconds. NEVER set this value directly: it is deduced from the
//	       AVStream values.  */
//	    int64_t duration;
//	    /** decoding: total file size. 0 if unknown */
//	    int64_t file_size;
//	    /** decoding: total stream bitrate in bit/s, 0 if not
//	       available. Never set it directly if the file_size and the
//	       duration are known as ffmpeg can compute it automatically. */
//	    int bit_rate;
//
//	    /* av_read_frame() support */
//	    AVStream *cur_st;
//	    const uint8_t *cur_ptr;
//	    int cur_len;
//	    AVPacket cur_pkt;
//
//	    /* av_seek_frame() support */
//	    int64_t data_offset; /** offset of the first packet */
//	    int index_built;
//
//	    int mux_rate;
//	    int packet_size;
//	    int preload;
//	    int max_delay;
//
//	#define AVFMT_NOOUTPUTLOOP -1
//	#define AVFMT_INFINITEOUTPUTLOOP 0
//	    /** number of times to loop output in formats that support it */
//	    int loop_output;
//
//	    int flags;
//	#define AVFMT_FLAG_GENPTS       0x0001 ///< generate pts if missing even if it requires parsing future frames
//	#define AVFMT_FLAG_IGNIDX       0x0002 ///< ignore index
//	#define AVFMT_FLAG_NONBLOCK     0x0004 ///< do not block when reading packets from input
//
//	    int loop_input;
//	    /** decoding: size of data to probe; encoding unused */
//	    unsigned int probesize;
//
//	    /**
//	     * maximum duration in AV_TIME_BASE units over which the input should be analyzed in av_find_stream_info()
//	     */
//	    int max_analyze_duration;
//
//	    const uint8_t *key;
//	    int keylen;
//	} AVFormatContext;
	
	public static class AVFormatContext extends Structure
	{
		public Pointer av_class;
		public Pointer iformat;
		public Pointer oformat;
		public Pointer priv_data;
		public Pointer pb;
		public int nb_streams;
		public static final int MAX_STREAMS = 20;
		// don't know how to do an array of pointers with JNA, so we'll just do 20 pointers:
		public Pointer streams0;
		public Pointer streams1;
		public Pointer streams2;
		public Pointer streams3;
		public Pointer streams4;
		public Pointer streams5;
		public Pointer streams6;
		public Pointer streams7;
		public Pointer streams8;
		public Pointer streams9;
		public Pointer streams10;
		public Pointer streams11;
		public Pointer streams12;
		public Pointer streams13;
		public Pointer streams14;
		public Pointer streams15;
		public Pointer streams16;
		public Pointer streams17;
		public Pointer streams18;
		public Pointer streams19;
		public Pointer [] getStreams()
		{	return new Pointer [] {
				streams0,
				streams1,
				streams2,
				streams3,
				streams4,
				streams5,
				streams6,
				streams7,
				streams8,
				streams9,
				streams10,
				streams11,
				streams12,
				streams13,
				streams14,
				streams15,
				streams16,
				streams17,
				streams18,
				streams19
			};
		}		//public Pointer[] streams = new Pointer[MAX_STREAMS]; //	    AVStream *streams[MAX_STREAMS];
		//public Pointer[] streams = new Pointer[MAX_STREAMS]; //	    AVStream *streams[MAX_STREAMS];
		public byte[] filename = new byte[1024]; 
		public long timestamp;
		public byte[] title = new byte[512];
		public byte[] author = new byte[512];
		public byte[] copyright = new byte[512];
		public byte[] comment = new byte[512];
		public byte[] album = new byte[512];
		public int year;  
		public int track; 
		public byte[] genre = new byte[32]; 
		public int ctx_flags; 
		public Pointer packet_buffer;
		public long start_time;
		public long duration;
		public long file_size;
		public int bit_rate;
		public Pointer cur_st;
		public Pointer cur_ptr;
		public int cur_len;
		public AVPacket cur_pkt;
		public long data_offset;
		public int index_built;
		public int mux_rate;
		public int packet_size;
		public int preload;
		public int max_delay;
			public static final int AVFMT_NOOUTPUTLOOP = -1;
			public static final int AVFMT_INFINITEOUTPUTLOOP = 0;
		public int loop_output;
		public int flags;
			public static final int AVFMT_FLAG_GENPTS       =0x0001; ///< generate pts if missing even if it requires parsing future frames
			public static final int AVFMT_FLAG_IGNIDX       =0x0002; ///< ignore index
			public static final int AVFMT_FLAG_NONBLOCK     =0x0004; ///< do not block when reading packets from input
		public int loop_input;
		public int probesize;
		public int max_analyze_duration;
		public Pointer key;
		public int keylen;
		
		public AVFormatContext()
		{
			super();
		}
		
		public AVFormatContext(Pointer p)
		{	super();
			useMemory(p);
			read();
		}
	}
	
//	typedef struct AVPacketList {
//	    AVPacket pkt;
//	    struct AVPacketList *next;
//	} AVPacketList;
	
	public static class AVPacketList extends Structure
	{
	    public AVPacket pkt;
	    public Pointer next;
	}
	
//	extern AVInputFormat *first_iformat;
//	extern AVOutputFormat *first_oformat;
	
	// TODO: JNA does not support global variable access
	
	//enum CodecID av_guess_image2_codec(const char *filename);
	int av_guess_image2_codec(String filename);
	
	
	void av_register_input_format(AVInputFormat format);
	void av_register_output_format(AVOutputFormat format);
	AVOutputFormat guess_stream_format(String short_name,
	                                    String filename, String mime_type);
	AVOutputFormat guess_format(String short_name,
	                             String filename, String mime_type);
	
	
	int av_guess_codec(AVOutputFormat fmt, String short_name,
            String filename, String mime_type, int type);

	void av_hex_dump(Pointer f, Pointer buf, int size);
	void av_hex_dump_log(Pointer avcl, int level, Pointer buf, int size);
	void av_pkt_dump(Pointer f, AVPacket pkt, int dump_payload);
	
	void av_pkt_dump_log(Pointer avcl, int level, AVPacket pkt, int dump_payload);
	

	void av_register_all();
	
	int av_codec_get_id(PointerByReference tags, int tag);
	int av_codec_get_tag(PointerByReference tags, int id);
	
	AVInputFormat av_find_input_format(String short_name);

	AVInputFormat av_probe_input_format(AVProbeData pd, int is_opened);

	/**
	 * Allocates all the structures needed to read an input stream.
	 *        This does not open the needed codecs for decoding the stream[s].
	 */
	int av_open_input_stream(PointerByReference ic_ptr,
			ByteIOContext pb, String filename,
			AVInputFormat fmt, AVFormatParameters ap);
	/**
	 * Open a media file as input. The codecs are not opened. Only the file
	 * header (if present) is read.
	 *
	 * @param ic_ptr the opened media file handle is put here
	 * @param filename filename to open.
	 * @param fmt if non NULL, force the file format to use
	 * @param buf_size optional buffer size (zero if default is OK)
	 * @param ap additional parameters needed when opening the file (NULL if default)
	 * @return 0 if OK. AVERROR_xxx otherwise.
	 */
	int av_open_input_file(PointerByReference ic_ptr, String filename,
			AVInputFormat fmt,
	        int buf_size,
	        AVFormatParameters ap);

	AVFormatContext av_alloc_format_context();
	int av_find_stream_info(AVFormatContext ic);
	int av_read_packet(AVFormatContext s, AVPacket pkt);
	/**
	 * Return the next frame of a stream.
	 *
	 * The returned packet is valid
	 * until the next av_read_frame() or until av_close_input_file() and
	 * must be freed with av_free_packet. For video, the packet contains
	 * exactly one frame. For audio, it contains an integer number of
	 * frames if each frame has a known fixed size (e.g. PCM or ADPCM
	 * data). If the audio frames have a variable size (e.g. MPEG audio),
	 * then it contains one frame.
	 *
	 * pkt->pts, pkt->dts and pkt->duration are always set to correct
	 * values in AVStream.timebase units (and guessed if the format cannot
	 * provided them). pkt->pts can be AV_NOPTS_VALUE if the video format
	 * has B frames, so it is better to rely on pkt->dts if you do not
	 * decompress the payload.
	 *
	 * @return 0 if OK, < 0 if error or end of file.
	 */
	int av_read_frame(AVFormatContext s, AVPacket pkt);
	/**
	 * Seek to the key frame at timestamp.
	 * 'timestamp' in 'stream_index'.
	 * @param stream_index If stream_index is (-1), a default
	 * stream is selected, and timestamp is automatically converted
	 * from AV_TIME_BASE units to the stream specific time_base.
	 * @param timestamp timestamp in AVStream.time_base units
	 *        or if there is no stream specified then in AV_TIME_BASE units
	 * @param flags flags which select direction and seeking mode
	 * @return >= 0 on success
	 */
	int av_seek_frame(AVFormatContext s, int stream_index, long timestamp, int flags);
	/**
	 * start playing a network based stream (e.g. RTSP stream) at the
	 * current position
	 */
	int av_read_play(AVFormatContext s);
	/**
	 * Pause a network based stream (e.g. RTSP stream).
	 *
	 * Use av_read_play() to resume it.
	 */
	int av_read_pause(AVFormatContext s);
	void av_close_input_file(AVFormatContext s);
	AVStream av_new_stream(AVFormatContext s, int id);
	void av_set_pts_info(AVStream s, int pts_wrap_bits,
	                     int pts_num, int pts_den);

	
	public static final int AVSEEK_FLAG_BACKWARD =1; ///< seek backward
	public static final int AVSEEK_FLAG_BYTE     =2; ///< seeking based on position in bytes
	public static final int AVSEEK_FLAG_ANY      =4; ///< seek to any frame, even non keyframes
	
	
	int av_find_default_stream_index(AVFormatContext s);
	int av_index_search_timestamp(AVStream st, long timestamp, int flags);
	int av_add_index_entry(AVStream st,
	                       long pos, long timestamp, int size, int distance, int flags);
	int av_seek_frame_binary(AVFormatContext s, int stream_index, long target_ts, int flags);
	void av_update_cur_dts(AVFormatContext s, AVStream ref_st, long timestamp);
	long av_gen_search(AVFormatContext s, int stream_index, long target_ts, long pos_min, long pos_max, long pos_limit, long ts_min, long ts_max, int flags, LongByReference ts_ret, Pointer read_timestamp);
	int av_set_parameters(AVFormatContext s, AVFormatParameters ap);
	int av_write_header(AVFormatContext s);
	int av_write_frame(AVFormatContext s, AVPacket pkt);
	int av_interleaved_write_frame(AVFormatContext s, AVPacket pkt);
	int av_interleave_packet_per_dts(AVFormatContext s, AVPacket out, AVPacket pkt, int flush);
	int av_write_trailer(AVFormatContext s);
	void dump_format(AVFormatContext ic,
	                 int index,
	                 String url,
	                 int is_output);
	/** @deprecated */
	int parse_image_size(IntByReference width_ptr, IntByReference height_ptr, String str);
	/** @deprecated */
	int parse_frame_rate(IntByReference frame_rate, IntByReference frame_rate_base, String arg);
	long parse_date(String datestr, int duration);
	long av_gettime();
	public static final int FFM_PACKET_SIZE = 4096;
	long ffm_read_write_index(int fd);
	void ffm_write_write_index(int fd, long pos);
	void ffm_set_write_index(AVFormatContext s, long pos, long file_size);
	int find_info_tag(Pointer arg, int arg_size, String tag1, String info);
	int av_get_frame_filename(Pointer buf, int buf_size,
	                          String path, int number);
	int av_filename_number_test(String filename);
	int avf_sdp_create(Pointer ac, int n_files, Pointer buff, int size);

//	#ifdef HAVE_AV_CONFIG_H

//	#include "os_support.h"
//
//	void __dynarray_add(unsigned long **tab_ptr, int *nb_ptr, unsigned long elem);
//
//	#ifdef __GNUC__
//	#define dynarray_add(tab, nb_ptr, elem)\
//	do {\
//	    typeof(tab) _tab = (tab);\
//	    typeof(elem) _elem = (elem);\
//	    (void)sizeof(**_tab == _elem); /* check that types are compatible */\
//	    __dynarray_add((unsigned long **)_tab, nb_ptr, (unsigned long)_elem);\
//	} while(0)
//	#else
//	#define dynarray_add(tab, nb_ptr, elem)\
//	do {\
//	    __dynarray_add((unsigned long **)(tab), nb_ptr, (unsigned long)(elem));\
//	} while(0)
//	#endif

	// TODO: __dynarray_add and dynarray_add with JNA
	
//	time_t mktimegm(struct tm *tm);
//	struct tm *brktimegm(time_t secs, struct tm *tm);
	// TODO: mktimegm with JNA
	// TODO: brktimegm with JNA
	
	String small_strptime(String p, String fmt,
	                           Pointer dt);

	int resolve_host(Pointer sin_addr, String hostname);

	void url_split(Pointer proto, int proto_size,
	               Pointer authorization, int authorization_size,
	               Pointer hostname, int hostname_size,
	               IntByReference port_ptr,
	               Pointer path, int path_size,
	               String url);

	int match_ext(String filename, String extensions);
	
	
// end avformat.h
//------------------------------------------------------------------------------------------------------------------------

//------------------------------------------------------------------------------------------------------------------------
// avio.h


//	struct URLContext {
//	    struct URLProtocol *prot;
//	    int flags;
//	    int is_streamed;  /**< true if streamed (no seek possible), default = false */
//	    int max_packet_size;  /**< if non zero, the stream is packetized with this max packet size */
//	    void *priv_data;
//	#if LIBAVFORMAT_VERSION_INT >= (52<<16)
//	    char *filename; /**< specified filename */
//	#else
//	    char filename[1]; /**< specified filename */
//	#endif
//	};
	
	public static class URLContext extends Structure
	{
	    public Pointer prot;
	    public int flags;
	    public int is_streamed;  /**< true if streamed (no seek possible), default = false */
	    public int max_packet_size;  /**< if non zero, the stream is packetized with this max packet size */
	    public Pointer priv_data;
	//#if LIBAVFORMAT_VERSION_INT >= (52<<16)
	    public Pointer filename; /**< specified filename */
	//#else
	    //public byte filename; /**< specified filename */
	//#endif
	    // TODO: not sure how this should be mapped for JNA.  This is most likely a variable length structure with the filename following the structure.
	}
	
//	typedef struct URLPollEntry {
//	    URLContext *handle;
//	    int events;
//	    int revents;
//	} URLPollEntry;
	
	public static class URLPollEntry extends Structure
	{
	    public Pointer handle;
	    public int events;
	    public int revents;
	} 

	public static final int URL_RDONLY =0;
	public static final int URL_WRONLY =1;
	public static final int URL_RDWR   =2;

	//typedef int URLInterruptCB(void);

	int url_open(PointerByReference h, String filename, int flags);
	int url_read(URLContext h, Pointer buf, int size);
	int url_write(URLContext h, Pointer buf, int size);
	long url_seek(URLContext h, long pos, int whence);
	int url_close(URLContext h);
	int url_exist(String filename);
	long url_filesize(URLContext h);

	int url_get_max_packet_size(URLContext h);
	void url_get_filename(URLContext h, Pointer buf, int buf_size);
	void url_set_interrupt_cb(Pointer/*URLInterruptCB*/ interrupt_cb);
	int url_poll(URLPollEntry poll_table, int n, int timeout);
	public static final int AVSEEK_SIZE = 0x10000;

//	typedef struct URLProtocol {
//	    const char *name;
//	    int (*url_open)(URLContext *h, const char *filename, int flags);
//	    int (*url_read)(URLContext *h, unsigned char *buf, int size);
//	    int (*url_write)(URLContext *h, unsigned char *buf, int size);
//	    offset_t (*url_seek)(URLContext *h, offset_t pos, int whence);
//	    int (*url_close)(URLContext *h);
//	    struct URLProtocol *next;
//	} URLProtocol;

	
//	extern URLProtocol *first_protocol;
//	extern URLInterruptCB *url_interrupt_cb;
	// TODO: JNA does not allow access to global variables

	int register_protocol(URLProtocol protocol);

	
	
//	typedef struct {
//	    unsigned char *buffer;
//	    int buffer_size;
//	    unsigned char *buf_ptr, *buf_end;
//	    void *opaque;
//	    int (*read_packet)(void *opaque, uint8_t *buf, int buf_size);
//	    int (*write_packet)(void *opaque, uint8_t *buf, int buf_size);
//	    offset_t (*seek)(void *opaque, offset_t offset, int whence);
//	    offset_t pos; /**< position in the file of the current buffer */
//	    int must_flush; /**< true if the next seek should flush */
//	    int eof_reached; /**< true if eof reached */
//	    int write_flag;  /**< true if open for writing */
//	    int is_streamed;
//	    int max_packet_size;
//	    unsigned long checksum;
//	    unsigned char *checksum_ptr;
//	    unsigned long (*update_checksum)(unsigned long checksum, const uint8_t *buf, unsigned int size);
//	    int error;         ///< contains the error code or 0 if no error happened
//	} ByteIOContext;

	public static class ByteIOContext extends Structure
	{
		public Pointer buffer;
		public int buffer_size;
		public Pointer buf_ptr;
		public Pointer buf_end;
		public Pointer opaque;
		public Pointer read_packet;
		public Pointer write_packet;
		public Pointer seek;
		public long pos; 
		public int must_flush; 
		public int eof_reached; 
		public int write_flag; 
		public int is_streamed;
		public int max_packet_size;
		public NativeLong checksum;	
		public Pointer checksum_ptr;
		public Pointer update_checksum;
		public int error;
		public Pointer read_pause;
		public Pointer read_seek;
	}

	int init_put_byte(Pointer s,
            Pointer buffer,
            int buffer_size,
            int write_flag,
            Pointer opaque,
            Pointer read_packet,
            Pointer write_packet,
            Pointer seek);

	void put_byte(ByteIOContext s, int b);
	void put_buffer(ByteIOContext s, Pointer buf, int size);
	void put_le64(ByteIOContext s, long val);
	void put_be64(ByteIOContext s, long val);
	void put_le32(ByteIOContext s, int val);
	void put_be32(ByteIOContext s, int val);
	void put_le24(ByteIOContext s, int val);
	void put_be24(ByteIOContext s, int val);
	void put_le16(ByteIOContext s, int val);
	void put_be16(ByteIOContext s, int val);
	void put_tag(ByteIOContext s, String tag);
	
	void put_strz(ByteIOContext s, String buf);
	
	long url_fseek(ByteIOContext s, long offset, int whence);
	void url_fskip(ByteIOContext s, long offset);
	long url_ftell(ByteIOContext s);
	long url_fsize(ByteIOContext s);
	int url_feof(ByteIOContext s);
	int url_ferror(ByteIOContext s);
	
	public static final int URL_EOF = -1;
	int url_fgetc(ByteIOContext s);
	
//	/** @warning currently size is limited */
//	#ifdef __GNUC__
//	int url_fprintf(ByteIOContext s, const char *fmt, ...) __attribute__ ((__format__ (__printf__, 2, 3)));
//	#else
//	int url_fprintf(ByteIOContext s, const char *fmt, ...);
//	#endif
// TODO: don't know how to map url_fprintf with JNA, given variable parameters.
	
	/** @note unlike fgets, the EOL character is not returned and a whole
	line is parsed. return NULL if first char read was EOF */
	Pointer url_fgets(ByteIOContext s, Pointer buf, int buf_size);
	
	void put_flush_packet(ByteIOContext s);
	
	int get_buffer(ByteIOContext s, Pointer buf, int size);
	int get_partial_buffer(ByteIOContext s, Pointer buf, int size);
	
	/** @note return 0 if EOF, so you cannot use it if EOF handling is
	necessary */
	int get_byte(ByteIOContext s);
	int get_le24(ByteIOContext s);
	int get_le32(ByteIOContext s);
	long get_le64(ByteIOContext s);
	int get_le16(ByteIOContext s);
	
	Pointer get_strz(ByteIOContext s, Pointer buf, int maxlen);
	int get_be16(ByteIOContext s);
	int get_be24(ByteIOContext s);
	int get_be32(ByteIOContext s);
	long get_be64(ByteIOContext s);
	
//	static inline int url_is_streamed(ByteIOContext s)
//	{
//	return s->is_streamed;
//	}
	// TODO: cannot call inlined functions with JNA.
	
	int url_fdopen(ByteIOContext s, Pointer h);
	
	/** @warning must be called before any I/O */
	int url_setbufsize(ByteIOContext s, int buf_size);
	
	/** @note when opened as read/write, the buffers are only used for
	reading */
	int url_fopen(ByteIOContext s, String filename, int flags);
	int url_fclose(ByteIOContext s);
	URLContext url_fileno(ByteIOContext s);
	
	/**
	* Return the maximum packet size associated to packetized buffered file
	* handle. If the file is not packetized (stream like http or file on
	* disk), then 0 is returned.
	*
	* @param h buffered file handle
	* @return maximum packet size in bytes
	*/
	int url_fget_max_packet_size(Pointer s);
	
	int url_open_buf(ByteIOContext s, Pointer buf, int buf_size, int flags);
	
	/** return the written or read size */
	int url_close_buf(ByteIOContext s);
	
	/**
	* Open a write only memory stream.
	*
	* @param s new IO context
	* @return zero if no error.
	*/
	int url_open_dyn_buf(ByteIOContext s);
	
	/**
	* Open a write only packetized memory stream with a maximum packet
	* size of 'max_packet_size'.  The stream is stored in a memory buffer
	* with a big endian 4 byte header giving the packet size in bytes.
	*
	* @param s new IO context
	* @param max_packet_size maximum packet size (must be > 0)
	* @return zero if no error.
	*/
	int url_open_dyn_packet_buf(ByteIOContext s, int max_packet_size);
	
	/**
	* Return the written size and a pointer to the buffer. The buffer
	*  must be freed with av_free().
	* @param s IO context
	* @param pointer to a byte buffer
	* @return the length of the byte buffer
	*/
	int url_close_dyn_buf(ByteIOContext s, PointerByReference pbuffer);
	
	NativeLong get_checksum(ByteIOContext s);
	void init_checksum(ByteIOContext s, Pointer update_checksum, NativeLong checksum);
	
	/* file.c */
//	extern URLProtocol file_protocol;
//	extern URLProtocol pipe_protocol;
//	
//	/* udp.c */
//	extern URLProtocol udp_protocol;
	// TODO: JNA does not allow access to global variables
	
	int udp_set_remote_url(URLContext h, String uri);
	int udp_get_local_port(URLContext h);
	int udp_get_file_handle(URLContext h);
	
//	/* tcp.c  */
//	extern URLProtocol tcp_protocol;
//	
//	/* http.c */
//	extern URLProtocol http_protocol;
	// TODO: JNA does not allow access to global variables
	
// end avio.h
//------------------------------------------------------------------------------------------------------------------------

	
}


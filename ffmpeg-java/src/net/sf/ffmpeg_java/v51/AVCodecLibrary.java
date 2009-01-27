package net.sf.ffmpeg_java.v51;

import net.sf.ffmpeg_java.FFMPEGLibrary;
import net.sf.ffmpeg_java.FFmpegMgr;


import com.sun.jna.Native;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Based on FFMPEG Aug 12 2007.  From avcodec.h, opt.h.
 * @author Ken Larson
 *
 */
public interface AVCodecLibrary extends FFMPEGLibrary 
{
	// Make sure the library is inited BEFORE we set the INSTANCE variable
	public static final int avCodecLibVer = FFmpegMgr.getAvCodecVersion();

    public static final AVCodecLibrary INSTANCE = (AVCodecLibrary) Native.loadLibrary(
    		System.getProperty("avcodec.lib",
    	    		System.getProperty("os.name").startsWith("Windows") ? "avcodec-51" : "avcodec"), 

    		AVCodecLibrary.class);

    
//------------------------------------------------------------------------------------------------------------------------
// avcodec.h

    public static final int LIBAVCODEC_VERSION_INT = ((51<<16)+(40<<8)+4);	// version this comes from
    public static final String LIBAVCODEC_VERSION  = "51.40.4";
    public static final int LLIBAVCODEC_BUILD = LIBAVCODEC_VERSION_INT;

    public static final String LIBAVCODEC_IDENT = "Lavc" + LIBAVCODEC_VERSION;

    public static final long AV_NOPTS_VALUE = 0x8000000000000000L;
    public static final int AV_TIME_BASE = 1000000;
    public static final AVRational AV_TIME_BASE_Q = new AVRational(1, AV_TIME_BASE);


    //enum CodecID: 
	public static final int CODEC_ID_NONE 				= 0;
    public static final int CODEC_ID_MPEG1VIDEO 		= 1;
    public static final int CODEC_ID_MPEG2VIDEO 		= 2; /* preferred ID for MPEG-1/2 video decoding */
    public static final int CODEC_ID_MPEG2VIDEO_XVMC 	= 3;
    public static final int CODEC_ID_H261 				= 4;
    public static final int CODEC_ID_H263 				= 5;
    public static final int CODEC_ID_RV10 				= 6;
    public static final int CODEC_ID_RV20 				= 7;
    public static final int CODEC_ID_MJPEG 				= 8;
    public static final int CODEC_ID_MJPEGB				= 9;
    public static final int CODEC_ID_LJPEG				= 10;
    public static final int CODEC_ID_SP5X				= 11;
    public static final int CODEC_ID_JPEGLS				= 12;
    public static final int CODEC_ID_MPEG4				= 13;
    public static final int CODEC_ID_RAWVIDEO			= 14;
    public static final int CODEC_ID_MSMPEG4V1			= 15;
    public static final int CODEC_ID_MSMPEG4V2			= 16;
    public static final int CODEC_ID_MSMPEG4V3			= 17;
    public static final int CODEC_ID_WMV1				= 18;
    public static final int CODEC_ID_WMV2				= 19;
    public static final int CODEC_ID_H263P				= 20;
    public static final int CODEC_ID_H263I				= 21;
    public static final int CODEC_ID_FLV1				= 22;
    public static final int CODEC_ID_SVQ1				= 23;
    public static final int CODEC_ID_SVQ3				= 24;
    public static final int CODEC_ID_DVVIDEO			= 25;
    public static final int CODEC_ID_HUFFYUV			= 26;
    public static final int CODEC_ID_CYUV				= 27;
    public static final int CODEC_ID_H264				= 28;
    public static final int CODEC_ID_INDEO3				= 29;
    public static final int CODEC_ID_VP3				= 30;
    public static final int CODEC_ID_THEORA				= 31;
    public static final int CODEC_ID_ASV1				= 32;
    public static final int CODEC_ID_ASV2				= 33;
    public static final int CODEC_ID_FFV1				= 34;
    public static final int CODEC_ID_4XM				= 35;
    public static final int CODEC_ID_VCR1				= 36;
    public static final int CODEC_ID_CLJR				= 37;
    public static final int CODEC_ID_MDEC				= 38;
    public static final int CODEC_ID_ROQ				= 39;
    public static final int CODEC_ID_INTERPLAY_VIDEO	= 40;
    public static final int CODEC_ID_XAN_WC3			= 41;
    public static final int CODEC_ID_XAN_WC4			= 42;
    public static final int CODEC_ID_RPZA				= 43;
    public static final int CODEC_ID_CINEPAK			= 44;
    public static final int CODEC_ID_WS_VQA				= 45;
    public static final int CODEC_ID_MSRLE				= 46;
    public static final int CODEC_ID_MSVIDEO1			= 47;
    public static final int CODEC_ID_IDCIN				= 48;
    public static final int CODEC_ID_8BPS				= 49;
    public static final int CODEC_ID_SMC				= 50;
    public static final int CODEC_ID_FLIC				= 51;
    public static final int CODEC_ID_TRUEMOTION1		= 52;
    public static final int CODEC_ID_VMDVIDEO			= 53;
    public static final int CODEC_ID_MSZH				= 54;
    public static final int CODEC_ID_ZLIB				= 55;
    public static final int CODEC_ID_QTRLE				= 56;
    public static final int CODEC_ID_SNOW				= 57;
    public static final int CODEC_ID_TSCC				= 58;
    public static final int CODEC_ID_ULTI				= 59;
    public static final int CODEC_ID_QDRAW				= 60;
    public static final int CODEC_ID_VIXL				= 61;
    public static final int CODEC_ID_QPEG				= 62;
    public static final int CODEC_ID_XVID				= 63;
    public static final int CODEC_ID_PNG				= 64;
    public static final int CODEC_ID_PPM				= 65;
    public static final int CODEC_ID_PBM				= 66;
    public static final int CODEC_ID_PGM				= 67;
    public static final int CODEC_ID_PGMYUV				= 68;
    public static final int CODEC_ID_PAM				= 69;
    public static final int CODEC_ID_FFVHUFF			= 70;
    public static final int CODEC_ID_RV30				= 71;
    public static final int CODEC_ID_RV40				= 72;
    public static final int CODEC_ID_VC1				= 73;
    public static final int CODEC_ID_WMV3				= 74;
    public static final int CODEC_ID_LOCO				= 75;
    public static final int CODEC_ID_WNV1				= 76;
    public static final int CODEC_ID_AASC				= 77;
    public static final int CODEC_ID_INDEO2				= 78;
    public static final int CODEC_ID_FRAPS				= 79;
    public static final int CODEC_ID_TRUEMOTION2		= 80;
    public static final int CODEC_ID_BMP				= 81;
    public static final int CODEC_ID_CSCD				= 82;
    public static final int CODEC_ID_MMVIDEO			= 83;
    public static final int CODEC_ID_ZMBV				= 84;
    public static final int CODEC_ID_AVS				= 85;
    public static final int CODEC_ID_SMACKVIDEO			= 86;
    public static final int CODEC_ID_NUV				= 87;
    public static final int CODEC_ID_KMVC				= 88;
    public static final int CODEC_ID_FLASHSV			= 89;
    public static final int CODEC_ID_CAVS				= 90;
    public static final int CODEC_ID_JPEG2000			= 91;
    public static final int CODEC_ID_VMNC				= 92;
    public static final int CODEC_ID_VP5				= 93;
    public static final int CODEC_ID_VP6				= 94;
    public static final int CODEC_ID_VP6F				= 95;
    public static final int CODEC_ID_TARGA				= 96;
    public static final int CODEC_ID_DSICINVIDEO		= 97;
    public static final int CODEC_ID_TIERTEXSEQVIDEO	= 98;
    public static final int CODEC_ID_TIFF				= 99;
    public static final int CODEC_ID_GIF				= 100;
    public static final int CODEC_ID_FFH264				= 101;
    public static final int CODEC_ID_DXA				= 102;
    public static final int CODEC_ID_DNXHD				= 103;
    public static final int CODEC_ID_THP				= 104;
    public static final int CODEC_ID_SGI				= 105;
    public static final int CODEC_ID_C93				= 106;
    public static final int CODEC_ID_BETHSOFTVID		= 107;
    public static final int CODEC_ID_PTX				= 108;
    public static final int CODEC_ID_TXD				= 109;

    /* various PCM "codecs" */
    public static final int CODEC_ID_PCM_S16LE			= 0x10000;
    public static final int CODEC_ID_PCM_S16BE 			= 0x10001;
    public static final int CODEC_ID_PCM_U16LE 			= 0x10002;
    public static final int CODEC_ID_PCM_U16BE 			= 0x10003;
    public static final int CODEC_ID_PCM_S8 			= 0x10004;
    public static final int CODEC_ID_PCM_U8 			= 0x10005;
    public static final int CODEC_ID_PCM_MULAW 			= 0x10006;
    public static final int CODEC_ID_PCM_ALAW 			= 0x10007;
    public static final int CODEC_ID_PCM_S32LE 			= 0x10008;
    public static final int CODEC_ID_PCM_S32BE 			= 0x10009;
    public static final int CODEC_ID_PCM_U32LE 			= 0x1000a;
    public static final int CODEC_ID_PCM_U32BE 			= 0x1000b;
    public static final int CODEC_ID_PCM_S24LE 			= 0x1000c;
    public static final int CODEC_ID_PCM_S24BE 			= 0x1000d;
    public static final int CODEC_ID_PCM_U24LE 			= 0x1000e;
    public static final int CODEC_ID_PCM_U24BE 			= 0x1000f;
    public static final int CODEC_ID_PCM_S24DAUD 		= 0x10010;
    public static final int CODEC_ID_PCM_ZORK 			= 0x10011;

    /* various ADPCM codecs */
    public static final int CODEC_ID_ADPCM_IMA_QT		= 0x11000;
    public static final int CODEC_ID_ADPCM_IMA_WAV		= 0x11001;
    public static final int CODEC_ID_ADPCM_IMA_DK3		= 0x11002;
    public static final int CODEC_ID_ADPCM_IMA_DK4		= 0x11003;
    public static final int CODEC_ID_ADPCM_IMA_WS		= 0x11004;
    public static final int CODEC_ID_ADPCM_IMA_SMJPEG	= 0x11005;
    public static final int CODEC_ID_ADPCM_MS			= 0x11006;
    public static final int CODEC_ID_ADPCM_4XM			= 0x11007;
    public static final int CODEC_ID_ADPCM_XA			= 0x11008;
    public static final int CODEC_ID_ADPCM_ADX			= 0x11009;
    public static final int CODEC_ID_ADPCM_EA			= 0x1100a;
    public static final int CODEC_ID_ADPCM_G726			= 0x1100b;
    public static final int CODEC_ID_ADPCM_CT			= 0x1100c;
    public static final int CODEC_ID_ADPCM_SWF			= 0x1100d;
    public static final int CODEC_ID_ADPCM_YAMAHA		= 0x1100e;
    public static final int CODEC_ID_ADPCM_SBPRO_4		= 0x1100f;
    public static final int CODEC_ID_ADPCM_SBPRO_3		= 0x11010;
    public static final int CODEC_ID_ADPCM_SBPRO_2		= 0x11011;
    public static final int CODEC_ID_ADPCM_THP			= 0x11012;

    /* AMR */
    public static final int CODEC_ID_AMR_NB				= 0x12000;
    public static final int CODEC_ID_AMR_WB 			= 0x12001;

    /* RealAudio codecs*/
    public static final int CODEC_ID_RA_144				= 0x13000;
    public static final int CODEC_ID_RA_288 			= 0x13001;

    /* various DPCM codecs */
    public static final int CODEC_ID_ROQ_DPCM			= 0x14000;
    public static final int CODEC_ID_INTERPLAY_DPCM 	= 0x14001;
    public static final int CODEC_ID_XAN_DPCM 			= 0x14002;
    public static final int CODEC_ID_SOL_DPCM 			= 0x14003;

    public static final int CODEC_ID_MP2				= 0x15000;
    public static final int CODEC_ID_MP3				= 0x15001; /* preferred ID for decoding MPEG audio layer 1, 2 or 3 */
    public static final int CODEC_ID_AAC				= 0x15002;
//#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
//    public static final int CODEC_ID_MPEG4AAC			= 0x15003;	// re-defined below
//#endif
    public static final int CODEC_ID_AC3				= 0x15004;
    public static final int CODEC_ID_DTS				= 0x15005;
    public static final int CODEC_ID_VORBIS				= 0x15006;
    public static final int CODEC_ID_DVAUDIO			= 0x15007;
    public static final int CODEC_ID_WMAV1				= 0x15008;
    public static final int CODEC_ID_WMAV2				= 0x15009;
    public static final int CODEC_ID_MACE3				= 0x1500a;
    public static final int CODEC_ID_MACE6				= 0x1500b;
    public static final int CODEC_ID_VMDAUDIO			= 0x1500c;
    public static final int CODEC_ID_SONIC				= 0x1500d;
    public static final int CODEC_ID_SONIC_LS			= 0x1500e;
    public static final int CODEC_ID_FLAC				= 0x1500f;
    public static final int CODEC_ID_MP3ADU				= 0x15010;
    public static final int CODEC_ID_MP3ON4				= 0x15011;
    public static final int CODEC_ID_SHORTEN			= 0x15012;
    public static final int CODEC_ID_ALAC				= 0x15013;
    public static final int CODEC_ID_WESTWOOD_SND1		= 0x15014;
    public static final int CODEC_ID_GSM				= 0x15015; /* as in Berlin toast format */
    public static final int CODEC_ID_QDM2				= 0x15016;
    public static final int CODEC_ID_COOK				= 0x15017;
    public static final int CODEC_ID_TRUESPEECH			= 0x15018;
    public static final int CODEC_ID_TTA				= 0x15019;
    public static final int CODEC_ID_SMACKAUDIO			= 0x1501a;
    public static final int CODEC_ID_QCELP				= 0x1501b;
    public static final int CODEC_ID_WAVPACK			= 0x1501c;
    public static final int CODEC_ID_DSICINAUDIO		= 0x1501d;
    public static final int CODEC_ID_IMC				= 0x1501e;
    public static final int CODEC_ID_MUSEPACK7			= 0x1501f;
    public static final int CODEC_ID_MLP				= 0x15020;
    public static final int CODEC_ID_GSM_MS				= 0x15021; /* as found in WAV */
    public static final int CODEC_ID_ATRAC3				= 0x15022;
    public static final int CODEC_ID_VOXWARE			= 0x15023;

    /* subtitle codecs */
    public static final int CODEC_ID_DVD_SUBTITLE		= 0x17000;
    public static final int CODEC_ID_DVB_SUBTITLE 		= 0x17001;
    public static final int CODEC_ID_TEXT 				= 0x17002;  /* raw UTF-8 text */
    public static final int CODEC_ID_XSUB 				= 0x17003;

    public static final int CODEC_ID_MPEG2TS			= 0x20000; /* _FAKE_ codec to indicate a raw MPEG-2 TS stream (only used by libavformat) */


    
    //#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
    /* CODEC_ID_MP3LAME is obsolete */
    public static final int CODEC_ID_MP3LAME = CODEC_ID_MP3;
    public static final int CODEC_ID_MPEG4AAC = CODEC_ID_AAC;
    //#endif
    
    
	//	enum CodecType:
	public static final int CODEC_TYPE_UNKNOWN = -1;
	public static final int CODEC_TYPE_VIDEO = 0;
	public static final int CODEC_TYPE_AUDIO = 1;
	public static final int CODEC_TYPE_DATA = 2;
	public static final int CODEC_TYPE_SUBTITLE = 3;
	public static final int CODEC_TYPE_NB = 4;

	
	/* Currently unused, may be used if 24/32 bits samples are ever supported. */
	/* all in native-endian format */
	//enum SampleFormat:
	public static final int SAMPLE_FMT_NONE = -1;
	public static final int SAMPLE_FMT_U8 = 0;              ///< unsigned 8 bits
	public static final int SAMPLE_FMT_S16 = 1;             ///< signed 16 bits
	public static final int SAMPLE_FMT_S24 = 2;             ///< signed 24 bits
	public static final int SAMPLE_FMT_S32 = 3;             ///< signed 32 bits
	public static final int SAMPLE_FMT_FLT = 4;             ///< float
	public static final int SAMPLE_FMT_NB = 5;

	/* in bytes */
	public static final int AVCODEC_MAX_AUDIO_FRAME_SIZE = 192000; // 1 second of 48khz 32bit audio

	/**
	 * Required number of additionally allocated bytes at the end of the input bitstream for decoding.
	 * This is mainly needed because some optimized bitstream readers read
	 * 32 or 64 bit at once and could read over the end.<br>
	 * Note: If the first 23 bits of the additional bytes are not 0, then damaged
	 * MPEG bitstreams could cause overread and segfault.
	 */
	public static final int FF_INPUT_BUFFER_PADDING_SIZE = 8;

	/**
	 * minimum encoding buffer size
	 * Used to avoid some checks during header writing.
	 */
	public static final int  FF_MIN_BUFFER_SIZE = 16384;

	/* motion estimation type, EPZS by default */
	//enum Motion_Est_ID:
	public static final int SME_ZERO = 1;
	public static final int SME_FULL = 2;
	public static final int SME_LOG = 3;
	public static final int SME_PHODS = 4;
	public static final int SME_EPZS = 5;
	public static final int SME_X1 = 6;
	public static final int SME_HEX = 7;
	public static final int SME_UMH = 8;
	public static final int SME_ITER = 9;


	//enum AVDiscard:
	    /* We leave some space between them for extensions (drop some
	     * keyframes for intra-only or drop just some bidir frames). */
	public static final int AVDISCARD_NONE   =-16; ///< discard nothing
	public static final int AVDISCARD_DEFAULT=  0; ///< discard useless packets like 0 size packets in avi
	public static final int AVDISCARD_NONREF =  8; ///< discard all non reference
	public static final int AVDISCARD_BIDIR  = 16; ///< discard all bidirectional frames
	public static final int AVDISCARD_NONKEY = 32; ///< discard all frames except keyframes
	public static final int AVDISCARD_ALL    = 48; ///< discard all


//	typedef struct RcOverride{
//	    int start_frame;
//	    int end_frame;
//	    int qscale; // If this is 0 then quality_factor will be used instead.
//	    float quality_factor;
//	} RcOverride;
	
	public static class RcOverride extends Structure {
	    public int start_frame;
	    public int end_frame;
	    public int qscale; // If this is 0 then quality_factor will be used instead.
	    public float quality_factor;
	};

	public static final int  FF_MAX_B_FRAMES = 16;

	
	/* encoding support
	   These flags can be passed in AVCodecContext.flags before initialization.
	   Note: Not everything is supported yet.
	*/

	public static final int CODEC_FLAG_QSCALE = 0x0002;  ///< Use fixed qscale.
	public static final int CODEC_FLAG_4MV    = 0x0004;  ///< 4 MV per MB allowed / advanced prediction for H.263.
	public static final int CODEC_FLAG_QPEL   = 0x0010;  ///< Use qpel MC.
	public static final int CODEC_FLAG_GMC    = 0x0020;  ///< Use GMC.
	public static final int CODEC_FLAG_MV0    = 0x0040;  ///< Always try a MB with MV=<0,0>.
	public static final int CODEC_FLAG_PART   = 0x0080;  ///< Use data partitioning.
	/* The parent program guarantees that the input for B-frames containing
	 * streams is not written to for at least s->max_b_frames+1 frames, if
	 * this is not set the input will be copied. */
	public static final int CODEC_FLAG_INPUT_PRESERVED = 0x0100;
	public static final int CODEC_FLAG_PASS1 = 0x0200;  ///< Use internal 2pass ratecontrol in first pass mode.
	public static final int CODEC_FLAG_PASS2 = 0x0400;  ///< Use internal 2pass ratecontrol in second pass mode.
	public static final int CODEC_FLAG_EXTERN_HUFF = 0x1000; ///< Use external Huffman table (for MJPEG).
	public static final int CODEC_FLAG_GRAY  = 0x2000;  ///< Only decode/encode grayscale.
	public static final int CODEC_FLAG_EMU_EDGE = 0x4000;///< Don't draw edges.
	public static final int CODEC_FLAG_PSNR           = 0x8000; ///< error[?] variables will be set during encoding.
	public static final int CODEC_FLAG_TRUNCATED  = 0x00010000; /** Input bitstream might be truncated at a random location instead
	                                            of only at frame boundaries. */
	public static final int CODEC_FLAG_NORMALIZE_AQP  = 0x00020000; ///< Normalize adaptive quantization.
	public static final int CODEC_FLAG_INTERLACED_DCT = 0x00040000; ///< Use interlaced DCT.
	public static final int CODEC_FLAG_LOW_DELAY      = 0x00080000; ///< Force low delay.
	public static final int CODEC_FLAG_ALT_SCAN       = 0x00100000; ///< Use alternate scan.
	public static final int CODEC_FLAG_TRELLIS_QUANT  = 0x00200000; ///< Use trellis quantization.
	public static final int CODEC_FLAG_GLOBAL_HEADER  = 0x00400000; ///< Place global headers in extradata instead of every keyframe.
	public static final int CODEC_FLAG_BITEXACT       = 0x00800000; ///< Use only bitexact stuff (except (I)DCT).
	/* Fx : Flag for h263+ extra options */
	//#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
	public static final int CODEC_FLAG_H263P_AIC      = 0x01000000; ///< H.263 advanced intra coding / MPEG-4 AC prediction (remove this)
	//#endif
	public static final int CODEC_FLAG_AC_PRED        = 0x01000000; ///< H.263 advanced intra coding / MPEG-4 AC prediction
	public static final int CODEC_FLAG_H263P_UMV      = 0x02000000; ///< unlimited motion vector
	public static final int CODEC_FLAG_CBP_RD         = 0x04000000; ///< Use rate distortion optimization for cbp.
	public static final int CODEC_FLAG_QP_RD          = 0x08000000; ///< Use rate distortion optimization for qp selectioon.
	public static final int CODEC_FLAG_H263P_AIV      = 0x00000008; ///< H.263 alternative inter VLC
	public static final int CODEC_FLAG_OBMC           = 0x00000001; ///< OBMC
	public static final int CODEC_FLAG_LOOP_FILTER    = 0x00000800; ///< loop filter
	public static final int CODEC_FLAG_H263P_SLICE_STRUCT = 0x10000000;
	public static final int CODEC_FLAG_INTERLACED_ME  = 0x20000000; ///< interlaced motion estimation
	public static final int CODEC_FLAG_SVCD_SCAN_OFFSET = 0x40000000; ///< Will reserve space for SVCD scan offset user data.
	public static final int CODEC_FLAG_CLOSED_GOP     = ((int)0x80000000);
	public static final int CODEC_FLAG2_FAST          = 0x00000001; ///< Allow non spec compliant speedup tricks.
	public static final int CODEC_FLAG2_STRICT_GOP    = 0x00000002; ///< Strictly enforce GOP size.
	public static final int CODEC_FLAG2_NO_OUTPUT     = 0x00000004; ///< Skip bitstream encoding.
	public static final int CODEC_FLAG2_LOCAL_HEADER  = 0x00000008; ///< Place global headers at every keyframe instead of in extradata.
	public static final int CODEC_FLAG2_BPYRAMID      = 0x00000010; ///< H.264 allow B-frames to be used as references.
	public static final int CODEC_FLAG2_WPRED         = 0x00000020; ///< H.264 weighted biprediction for B-frames
	public static final int CODEC_FLAG2_MIXED_REFS    = 0x00000040; ///< H.264 one reference per partition, as opposed to one reference per macroblock
	public static final int CODEC_FLAG2_8X8DCT        = 0x00000080; ///< H.264 high profile 8x8 transform
	public static final int CODEC_FLAG2_FASTPSKIP     = 0x00000100; ///< H.264 fast pskip
	public static final int CODEC_FLAG2_AUD           = 0x00000200; ///< H.264 access unit delimiters
	public static final int CODEC_FLAG2_BRDO          = 0x00000400; ///< B-frame rate-distortion optimization
	public static final int CODEC_FLAG2_INTRA_VLC     = 0x00000800; ///< Use MPEG-2 intra VLC table.
	public static final int CODEC_FLAG2_MEMC_ONLY     = 0x00001000; ///< Only do ME/MC (I frames -> ref, P frame -> ME+MC).
	public static final int CODEC_FLAG2_DROP_FRAME_TIMECODE = 0x00002000; ///< timecode is in drop frame format.
	public static final int CODEC_FLAG2_SKIP_RD       = 0x00004000; ///< RD optimal MB level residual skipping
	public static final int CODEC_FLAG2_CHUNKS        = 0x00008000; ///< Input bitstream might be truncated at a packet boundaries instead of only at frame boundaries.
	public static final int CODEC_FLAG2_NON_LINEAR_QUANT = 0x00010000; ///< Use MPEG-2 nonlinear quantizer.

	/* Unsupported options :
	 *              Syntax Arithmetic coding (SAC)
	 *              Reference Picture Selection
	 *              Independent Segment Decoding */
	/* /Fx */
	/* codec capabilities */

	public static final int CODEC_CAP_DRAW_HORIZ_BAND = 0x0001; ///< Decoder can use draw_horiz_band callback.
	/**
	 * Codec uses get_buffer() for allocating buffers.
	 * direct rendering method 1
	 */
	public static final int CODEC_CAP_DR1             = 0x0002;
	/* If 'parse_only' field is true, then avcodec_parse_frame() can be used. */
	public static final int CODEC_CAP_PARSE_ONLY      = 0x0004;
	public static final int CODEC_CAP_TRUNCATED       = 0x0008;
	/* Codec can export data for HW decoding (XvMC). */
	public static final int CODEC_CAP_HWACCEL         = 0x0010;
	/**
	 * Codec has a nonzero delay and needs to be fed with NULL at the end to get the delayed data.
	 * If this is not set, the codec is guaranteed to never be fed with NULL data.
	 */
	public static final int CODEC_CAP_DELAY           = 0x0020;
	/**
	 * Codec can be fed a final frame with a smaller size.
	 * This can be used to prevent truncation of the last audio samples.
	 */
	public static final int CODEC_CAP_SMALL_LAST_FRAME = 0x0040;

	//The following defines may change, don't expect compatibility if you use them.
	public static final int MB_TYPE_INTRA4x4   = 0x0001;
	public static final int MB_TYPE_INTRA16x16 = 0x0002; //FIXME H.264-specific
	public static final int MB_TYPE_INTRA_PCM  = 0x0004; //FIXME H.264-specific
	public static final int MB_TYPE_16x16      = 0x0008;
	public static final int MB_TYPE_16x8       = 0x0010;
	public static final int MB_TYPE_8x16       = 0x0020;
	public static final int MB_TYPE_8x8        = 0x0040;
	public static final int MB_TYPE_INTERLACED = 0x0080;
	public static final int MB_TYPE_DIRECT2     = 0x0100; //FIXME
	public static final int MB_TYPE_ACPRED     = 0x0200;
	public static final int MB_TYPE_GMC        = 0x0400;
	public static final int MB_TYPE_SKIP       = 0x0800;
	public static final int MB_TYPE_P0L0       = 0x1000;
	public static final int MB_TYPE_P1L0       = 0x2000;
	public static final int MB_TYPE_P0L1       = 0x4000;
	public static final int MB_TYPE_P1L1       = 0x8000;
	public static final int MB_TYPE_L0         = (MB_TYPE_P0L0 | MB_TYPE_P1L0);
	public static final int MB_TYPE_L1         = (MB_TYPE_P0L1 | MB_TYPE_P1L1);
	public static final int MB_TYPE_L0L1       = (MB_TYPE_L0   | MB_TYPE_L1);
	public static final int MB_TYPE_QUANT      = 0x00010000;
	public static final int MB_TYPE_CBP        = 0x00020000;
	//Note bits 24-31 are reserved for codec specific use (h264 ref0, mpeg1 0mv, ...)
	
	
//	/**
//	 * Pan Scan area.
//	 * This specifies the area which should be displayed.
//	 * Note there may be multiple such areas for one frame.
//	 */
//	typedef struct AVPanScan{
//	    /**
//	     * id
//	     * - encoding: Set by user.
//	     * - decoding: Set by libavcodec.
//	     */
//	    int id;
//
//	    /**
//	     * width and height in 1/16 pel
//	     * - encoding: Set by user.
//	     * - decoding: Set by libavcodec.
//	     */
//	    int width;
//	    int height;
//
//	    /**
//	     * position of the top left corner in 1/16 pel for up to 3 fields/frames
//	     * - encoding: Set by user.
//	     * - decoding: Set by libavcodec.
//	     */
//	    int16_t position[3][2];
//	}AVPanScan;
	
	/**
	 * Pan Scan area.
	 * This specifies the area which should be displayed.
	 * Note there may be multiple such areas for one frame.
	 */
	public static class AVPanScan extends Structure
	{
	    /**
	     * id
	     * - encoding: Set by user.
	     * - decoding: Set by libavcodec.
	     */
	    public int id;

	    /**
	     * width and height in 1/16 pel
	     * - encoding: Set by user.
	     * - decoding: Set by libavcodec.
	     */
	    public int width;
	    public int height;

	    /**
	     * position of the top left corner in 1/16 pel for up to 3 fields/frames
	     * - encoding: Set by user.
	     * - decoding: Set by libavcodec.
	     */
	    public short[] position = new short[3*2];	// JNA doesn't support multidimensional arrays (I don't think), so we'll just use a larger 1-dimensional array, which is equivalent memory-wise.
	};
	
	
//	#define FF_COMMON_FRAME \
//  /**\
//   * pointer to the picture planes.\
//   * This might be different from the first allocated byte\
//   * - encoding: \
//   * - decoding: \
//   */\
//  uint8_t *data[4];\
//  int linesize[4];\
//  /**\
//   * pointer to the first allocated byte of the picture. Can be used in get_buffer/release_buffer.\
//   * This isn't used by libavcodec unless the default get/release_buffer() is used.\
//   * - encoding: \
//   * - decoding: \
//   */\
//  uint8_t *base[4];\
//  /**\
//   * 1 -> keyframe, 0-> not\
//   * - encoding: Set by libavcodec.\
//   * - decoding: Set by libavcodec.\
//   */\
//  int key_frame;\
//\
//  /**\
//   * Picture type of the frame, see ?_TYPE below.\
//   * - encoding: Set by libavcodec. for coded_picture (and set by user for input).\
//   * - decoding: Set by libavcodec.\
//   */\
//  int pict_type;\
//\
//  /**\
//   * presentation timestamp in time_base units (time when frame should be shown to user)\
//   * If AV_NOPTS_VALUE then frame_rate = 1/time_base will be assumed.\
//   * - encoding: MUST be set by user.\
//   * - decoding: Set by libavcodec.\
//   */\
//  int64_t pts;\
//\
//  /**\
//   * picture number in bitstream order\
//   * - encoding: set by\
//   * - decoding: Set by libavcodec.\
//   */\
//  int coded_picture_number;\
//  /**\
//   * picture number in display order\
//   * - encoding: set by\
//   * - decoding: Set by libavcodec.\
//   */\
//  int display_picture_number;\
//\
//  /**\
//   * quality (between 1 (good) and FF_LAMBDA_MAX (bad)) \
//   * - encoding: Set by libavcodec. for coded_picture (and set by user for input).\
//   * - decoding: Set by libavcodec.\
//   */\
//  int quality; \
//\
//  /**\
//   * buffer age (1->was last buffer and dint change, 2->..., ...).\
//   * Set to INT_MAX if the buffer has not been used yet.\
//   * - encoding: unused\
//   * - decoding: MUST be set by get_buffer().\
//   */\
//  int age;\
//\
//  /**\
//   * is this picture used as reference\
//   * - encoding: unused\
//   * - decoding: Set by libavcodec. (before get_buffer() call)).\
//   */\
//  int reference;\
//\
//  /**\
//   * QP table\
//   * - encoding: unused\
//   * - decoding: Set by libavcodec.\
//   */\
//  int8_t *qscale_table;\
//  /**\
//   * QP store stride\
//   * - encoding: unused\
//   * - decoding: Set by libavcodec.\
//   */\
//  int qstride;\
//\
//  /**\
//   * mbskip_table[mb]>=1 if MB didn't change\
//   * stride= mb_width = (width+15)>>4\
//   * - encoding: unused\
//   * - decoding: Set by libavcodec.\
//   */\
//  uint8_t *mbskip_table;\
//\
//  /**\
//   * motion vector table\
//   * @code\
//   * example:\
//   * int mv_sample_log2= 4 - motion_subsample_log2;\
//   * int mb_width= (width+15)>>4;\
//   * int mv_stride= (mb_width << mv_sample_log2) + 1;\
//   * motion_val[direction][x + y*mv_stride][0->mv_x, 1->mv_y];\
//   * @endcode\
//   * - encoding: Set by user.\
//   * - decoding: Set by libavcodec.\
//   */\
//  int16_t (*motion_val[2])[2];\
//\
//  /**\
//   * macroblock type table\
//   * mb_type_base + mb_width + 2\
//   * - encoding: Set by user.\
//   * - decoding: Set by libavcodec.\
//   */\
//  uint32_t *mb_type;\
//\
//  /**\
//   * log2 of the size of the block which a single vector in motion_val represents: \
//   * (4->16x16, 3->8x8, 2-> 4x4, 1-> 2x2)\
//   * - encoding: unused\
//   * - decoding: Set by libavcodec.\
//   */\
//  uint8_t motion_subsample_log2;\
//\
//  /**\
//   * for some private data of the user\
//   * - encoding: unused\
//   * - decoding: Set by user.\
//   */\
//  void *opaque;\
//\
//  /**\
//   * error\
//   * - encoding: Set by libavcodec. if flags&CODEC_FLAG_PSNR.\
//   * - decoding: unused\
//   */\
//  uint64_t error[4];\
//\
//  /**\
//   * type of the buffer (to keep track of who has to deallocate data[*])\
//   * - encoding: Set by the one who allocates it.\
//   * - decoding: Set by the one who allocates it.\
//   * Note: User allocated (direct rendering) & internal buffers cannot coexist currently.\
//   */\
//  int type;\
//  \
//  /**\
//   * When decoding, this signals how much the picture must be delayed.\
//   * extra_delay = repeat_pict / (2*fps)\
//   * - encoding: unused\
//   * - decoding: Set by libavcodec.\
//   */\
//  int repeat_pict;\
//  \
//  /**\
//   * \
//   */\
//  int qscale_type;\
//  \
//  /**\
//   * The content of the picture is interlaced.\
//   * - encoding: Set by user.\
//   * - decoding: Set by libavcodec. (default 0)\
//   */\
//  int interlaced_frame;\
//  \
//  /**\
//   * If the content is interlaced, is top field displayed first.\
//   * - encoding: Set by user.\
//   * - decoding: Set by libavcodec.\
//   */\
//  int top_field_first;\
//  \
//  /**\
//   * Pan scan.\
//   * - encoding: Set by user.\
//   * - decoding: Set by libavcodec.\
//   */\
//  AVPanScan *pan_scan;\
//  \
//  /**\
//   * Tell user application that palette has changed from previous frame.\
//   * - encoding: ??? (no palette-enabled encoder yet)\
//   * - decoding: Set by libavcodec. (default 0).\
//   */\
//  int palette_has_changed;\
//  \
//  /**\
//   * codec suggestion on buffer type if != 0\
//   * - encoding: unused\
//   * - decoding: Set by libavcodec. (before get_buffer() call)).\
//   */\
//  int buffer_hints;\
//\
//  /**\
//   * DCT coefficients\
//   * - encoding: unused\
//   * - decoding: Set by libavcodec.\
//   */\
//  short *dct_coeff;\
//\
//  /**\
//   * motion referece frame index\
//   * - encoding: Set by user.\
//   * - decoding: Set by libavcodec.\
//   */\
//  int8_t *ref_index[2];
//
//#define FF_QSCALE_TYPE_MPEG1 0
//#define FF_QSCALE_TYPE_MPEG2 1
//#define FF_QSCALE_TYPE_H264  2
//
//#define FF_BUFFER_TYPE_INTERNAL 1
//#define FF_BUFFER_TYPE_USER     2 ///< direct rendering buffers (image is (de)allocated by user)
//#define FF_BUFFER_TYPE_SHARED   4 ///< Buffer from somewhere else; don't deallocate image (data/base), all other tables are not shared.
//#define FF_BUFFER_TYPE_COPY     8 ///< Just a (modified) copy of some other buffer, don't deallocate anything.
//
//
//#define FF_I_TYPE 1 // Intra
//#define FF_P_TYPE 2 // Predicted
//#define FF_B_TYPE 3 // Bi-dir predicted
//#define FF_S_TYPE 4 // S(GMC)-VOP MPEG4
//#define FF_SI_TYPE 5
//#define FF_SP_TYPE 6
//
//#define FF_BUFFER_HINTS_VALID    0x01 // Buffer hints value is meaningful (if 0 ignore).
//#define FF_BUFFER_HINTS_READABLE 0x02 // Codec will read from buffer.
//#define FF_BUFFER_HINTS_PRESERVE 0x04 // User must not alter buffer content.
//#define FF_BUFFER_HINTS_REUSABLE 0x08 // Codec will reuse the buffer (update).

//	/**
//	 * Audio Video Frame.
//	 */
//	typedef struct AVFrame {
//	    FF_COMMON_FRAME
//	} AVFrame;
	
	public static class AVFrame extends Structure
	{
		//uint8_t *data[4];
	    public Pointer data0;
	    public Pointer data1;
	    public Pointer data2;
	    public Pointer data3;
	    
	    public int[] linesize = new int[4];
	   
	    //uint8_t *base[4];
	    public Pointer base0;
	    public Pointer base1;
	    public Pointer base2;
	    public Pointer base3;
	    
	    public int key_frame;
	    public int pict_type;
	    public long pts;
	    public int coded_picture_number;
	    public int display_picture_number;
	    public int quality;
	    public int age;
	    public int reference;
	    public Pointer qscale_table;
	    public int qstride;
	    public Pointer mbskip_table;
	    //int16_t (*motion_val[2])[2];
	    public Pointer motion_val0;
	    public Pointer motion_val1;
	    public Pointer mb_type;
	    public byte motion_subsample_log2;
	    public Pointer opaque;
	    public long[] error = new long[4];
	    public int type;
	    public int repeat_pict;
	    public int qscale_type;
	    public int interlaced_frame;
	    public int top_field_first;
	    public Pointer pan_scan;
	    public int palette_has_changed;
	    public int buffer_hints;
	    public Pointer dct_coeff;
		//int8_t *ref_index[2];
	    public Pointer ref_index0;
	    public Pointer ref_index1;
	    
		public static final int FF_QSCALE_TYPE_MPEG1 =0;
		public static final int FF_QSCALE_TYPE_MPEG2 =1;
		public static final int FF_QSCALE_TYPE_H264  =2;
		//
		public static final int FF_BUFFER_TYPE_INTERNAL =1;
		public static final int FF_BUFFER_TYPE_USER     =2; ///< direct rendering buffers (image is (de)allocated by user)
		public static final int FF_BUFFER_TYPE_SHARED   =4; ///< Buffer from somewhere else; don't deallocate image (data/base), all other tables are not shared.
		public static final int FF_BUFFER_TYPE_COPY     =8; ///< Just a (modified) copy of some other buffer, don't deallocate anything.
		//
		//
		public static final int FF_I_TYPE =1; // Intra
		public static final int FF_P_TYPE =2; // Predicted
		public static final int FF_B_TYPE =3; // Bi-dir predicted
		public static final int FF_S_TYPE =4; // S(GMC)-VOP MPEG4
		public static final int FF_SI_TYPE =5;
		public static final int FF_SP_TYPE =6;
		//
		public static final int FF_BUFFER_HINTS_VALID    =0x01; // Buffer hints value is meaningful (if 0 ignore).
		public static final int FF_BUFFER_HINTS_READABLE =0x02; // Codec will read from buffer.
		public static final int FF_BUFFER_HINTS_PRESERVE =0x04; // User must not alter buffer content.
		public static final int FF_BUFFER_HINTS_REUSABLE =0x08; // Codec will reuse the buffer (update).
	    
		public AVFrame()
		{
			super();
		}
		
		public AVFrame(Pointer p)
		{	super();
			useMemory(p);
			read();
		}
	}
	
	public static final int DEFAULT_FRAME_RATE_BASE = 1001000;
	

	
//	/**
//	 * main external API structure
//	 */
//	typedef struct AVCodecContext {
//	    /**
//	     * information on struct for av_log
//	     * - set by avcodec_alloc_context
//	     */
//	    AVClass *av_class;
//	    /**
//	     * the average bitrate
//	     * - encoding: Set by user; unused for constant quantizer encoding.
//	     * - decoding: Set by libavcodec. 0 or some bitrate if this info is available in the stream.
//	     */
//	    int bit_rate;
//
//	    /**
//	     * number of bits the bitstream is allowed to diverge from the reference.
//	     *           the reference can be CBR (for CBR pass1) or VBR (for pass2)
//	     * - encoding: Set by user; unused for constant quantizer encoding.
//	     * - decoding: unused
//	     */
//	    int bit_rate_tolerance;
//
//	    /**
//	     * CODEC_FLAG_*.
//	     * - encoding: Set by user.
//	     * - decoding: Set by user.
//	     */
//	    int flags;
//
//	    /**
//	     * Some codecs need additional format info. It is stored here.
//	     * If any muxer uses this then ALL demuxers/parsers AND encoders for the
//	     * specific codec MUST set it correctly otherwise stream copy breaks.
//	     * In general use of this field by muxers is not recommanded.
//	     * - encoding: Set by libavcodec.
//	     * - decoding: Set by libavcodec. (FIXME: Is this OK?)
//	     */
//	    int sub_id;
//
//	    /**
//	     * Motion estimation algorithm used for video coding.
//	     * 1 (zero), 2 (full), 3 (log), 4 (phods), 5 (epzs), 6 (x1), 7 (hex),
//	     * 8 (umh), 9 (iter) [7, 8 are x264 specific, 9 is snow specific]
//	     * - encoding: MUST be set by user.
//	     * - decoding: unused
//	     */
//	    int me_method;
//
//	    /**
//	     * some codecs need / can use extradata like Huffman tables.
//	     * mjpeg: Huffman tables
//	     * rv10: additional flags
//	     * mpeg4: global headers (they can be in the bitstream or here)
//	     * The allocated memory should be FF_INPUT_BUFFER_PADDING_SIZE bytes larger
//	     * than extradata_size to avoid prolems if it is read with the bitstream reader.
//	     * The bytewise contents of extradata must not depend on the architecture or CPU endianness.
//	     * - encoding: Set/allocated/freed by libavcodec.
//	     * - decoding: Set/allocated/freed by user.
//	     */
//	    uint8_t *extradata;
//	    int extradata_size;
//
//	    /**
//	     * This is the fundamental unit of time (in seconds) in terms
//	     * of which frame timestamps are represented. For fixed-fps content,
//	     * timebase should be 1/framerate and timestamp increments should be
//	     * identically 1.
//	     * - encoding: MUST be set by user.
//	     * - decoding: Set by libavcodec.
//	     */
//	    AVRational time_base;
//
//	    /* video only */
//	    /**
//	     * picture width / height.
//	     * - encoding: MUST be set by user.
//	     * - decoding: Set by libavcodec.
//	     * Note: For compatibility it is possible to set this instead of
//	     * coded_width/height before decoding.
//	     */
//	    int width, height;
//
//	#define FF_ASPECT_EXTENDED 15
//
//	    /**
//	     * the number of pictures in a group of pictures, or 0 for intra_only
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int gop_size;
//
//	    /**
//	     * Pixel format, see PIX_FMT_xxx.
//	     * - encoding: Set by user.
//	     * - decoding: Set by libavcodec.
//	     */
//	    enum PixelFormat pix_fmt;
//
//	    /**
//	     * Frame rate emulation. If not zero, the lower layer (i.e. format handler)
//	     * has to read frames at native frame rate.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int rate_emu;
//
//	    /**
//	     * If non NULL, 'draw_horiz_band' is called by the libavcodec
//	     * decoder to draw a horizontal band. It improves cache usage. Not
//	     * all codecs can do that. You must check the codec capabilities
//	     * beforehand.
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     * @param height the height of the slice
//	     * @param y the y position of the slice
//	     * @param type 1->top field, 2->bottom field, 3->frame
//	     * @param offset offset into the AVFrame.data from which the slice should be read
//	     */
//	    void (*draw_horiz_band)(struct AVCodecContext *s,
//	                            const AVFrame *src, int offset[4],
//	                            int y, int type, int height);
//
//	    /* audio only */
//	    int sample_rate; ///< samples per second
//	    int channels;
//
//	    /**
//	     * audio sample format
//	     * - encoding: Set by user.
//	     * - decoding: Set by libavcodec.
//	     */
//	    enum SampleFormat sample_fmt;  ///< sample format, currently unused
//
//	    /* The following data should not be initialized. */
//	    /**
//	     * Samples per packet, initialized when calling 'init'.
//	     */
//	    int frame_size;
//	    int frame_number;   ///< audio or video frame number
//	    int real_pict_num;  ///< Returns the real picture number of previous encoded frame.
//
//	    /**
//	     * Number of frames the decoded output will be delayed relative to
//	     * the encoded input.
//	     * - encoding: Set by libavcodec.
//	     * - decoding: unused
//	     */
//	    int delay;
//
//	    /* - encoding parameters */
//	    float qcompress;  ///< amount of qscale change between easy & hard scenes (0.0-1.0)
//	    float qblur;      ///< amount of qscale smoothing over time (0.0-1.0)
//
//	    /**
//	     * minimum quantizer
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int qmin;
//
//	    /**
//	     * maximum quantizer
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int qmax;
//
//	    /**
//	     * maximum quantizer difference between frames
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int max_qdiff;
//
//	    /**
//	     * maximum number of B-frames between non-B-frames
//	     * Note: The output will be delayed by max_b_frames+1 relative to the input.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int max_b_frames;
//
//	    /**
//	     * qscale factor between IP and B-frames
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float b_quant_factor;
//
//	    /** obsolete FIXME remove */
//	    int rc_strategy;
//	#define FF_RC_STRATEGY_XVID 1
//
//	    int b_frame_strategy;
//
//	    /**
//	     * hurry up amount
//	     * - encoding: unused
//	     * - decoding: Set by user. 1-> Skip B-frames, 2-> Skip IDCT/dequant too, 5-> Skip everything except header
//	     * @deprecated Deprecated in favor of skip_idct and skip_frame.
//	     */
//	    int hurry_up;
//
//	    struct AVCodec *codec;
//
//	    void *priv_data;
//
//	#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
//	    /* unused, FIXME remove*/
//	    int rtp_mode;
//	#endif
//
//	    int rtp_payload_size;   /* The size of the RTP payload: the coder will  */
//	                            /* do its best to deliver a chunk with size     */
//	                            /* below rtp_payload_size, the chunk will start */
//	                            /* with a start code on some codecs like H.263. */
//	                            /* This doesn't take account of any particular  */
//	                            /* headers inside the transmitted RTP payload.  */
//
//
//	    /* The RTP callback: This function is called    */
//	    /* every time the encoder has a packet to send. */
//	    /* It depends on the encoder if the data starts */
//	    /* with a Start Code (it should). H.263 does.   */
//	    /* mb_nb contains the number of macroblocks     */
//	    /* encoded in the RTP payload.                  */
//	    void (*rtp_callback)(struct AVCodecContext *avctx, void *data, int size, int mb_nb);
//
//	    /* statistics, used for 2-pass encoding */
//	    int mv_bits;
//	    int header_bits;
//	    int i_tex_bits;
//	    int p_tex_bits;
//	    int i_count;
//	    int p_count;
//	    int skip_count;
//	    int misc_bits;
//
//	    /**
//	     * number of bits used for the previously encoded frame
//	     * - encoding: Set by libavcodec.
//	     * - decoding: unused
//	     */
//	    int frame_bits;
//
//	    /**
//	     * Private data of the user, can be used to carry app specific stuff.
//	     * - encoding: Set by user.
//	     * - decoding: Set by user.
//	     */
//	    void *opaque;
//
//	    char codec_name[32];
//	    enum CodecType codec_type; /* see CODEC_TYPE_xxx */
//	    enum CodecID codec_id; /* see CODEC_ID_xxx */
//
//	    /**
//	     * fourcc (LSB first, so "ABCD" -> ('D'<<24) + ('C'<<16) + ('B'<<8) + 'A').
//	     * This is used to work around some encoder bugs.
//	     * A demuxer should set this to what is stored in the field used to identify the codec.
//	     * If there are multiple such fields in a container then the demuxer should choose the one
//	     * which maximizes the information about the used codec.
//	     * If the codec tag field in a container is larger then 32 bits then the demuxer should
//	     * remap the longer ID to 32 bits with a table or other structure. Alternatively a new
//	     * extra_codec_tag + size could be added but for this a clear advantage must be demonstrated
//	     * first.
//	     * - encoding: Set by user, if not then the default based on codec_id will be used.
//	     * - decoding: Set by user, will be converted to uppercase by libavcodec during init.
//	     */
//	    unsigned int codec_tag;
//
//	    /**
//	     * Work around bugs in encoders which sometimes cannot be detected automatically.
//	     * - encoding: Set by user
//	     * - decoding: Set by user
//	     */
//	    int workaround_bugs;
//	#define FF_BUG_AUTODETECT       1  ///< autodetection
//	#define FF_BUG_OLD_MSMPEG4      2
//	#define FF_BUG_XVID_ILACE       4
//	#define FF_BUG_UMP4             8
//	#define FF_BUG_NO_PADDING       16
//	#define FF_BUG_AMV              32
//	#define FF_BUG_AC_VLC           0  ///< Will be removed, libavcodec can now handle these non-compliant files by default.
//	#define FF_BUG_QPEL_CHROMA      64
//	#define FF_BUG_STD_QPEL         128
//	#define FF_BUG_QPEL_CHROMA2     256
//	#define FF_BUG_DIRECT_BLOCKSIZE 512
//	#define FF_BUG_EDGE             1024
//	#define FF_BUG_HPEL_CHROMA      2048
//	#define FF_BUG_DC_CLIP          4096
//	#define FF_BUG_MS               8192 ///< Work around various bugs in Microsoft's broken decoders.
//	//#define FF_BUG_FAKE_SCALABILITY 16 //Autodetection should work 100%.
//
//	    /**
//	     * luma single coefficient elimination threshold
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int luma_elim_threshold;
//
//	    /**
//	     * chroma single coeff elimination threshold
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int chroma_elim_threshold;
//
//	    /**
//	     * strictly follow the standard (MPEG4, ...).
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int strict_std_compliance;
//	#define FF_COMPLIANCE_VERY_STRICT   2 ///< Strictly conform to a older more strict version of the spec or reference software.
//	#define FF_COMPLIANCE_STRICT        1 ///< Strictly conform to all the things in the spec no matter what consequences.
//	#define FF_COMPLIANCE_NORMAL        0
//	#define FF_COMPLIANCE_INOFFICIAL   -1 ///< Allow inofficial extensions.
//	#define FF_COMPLIANCE_EXPERIMENTAL -2 ///< Allow nonstandardized experimental things.
//
//	    /**
//	     * qscale offset between IP and B-frames
//	     * If > 0 then the last P-frame quantizer will be used (q= lastp_q*factor+offset).
//	     * If < 0 then normal ratecontrol will be done (q= -normal_q*factor+offset).
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float b_quant_offset;
//
//	    /**
//	     * Error resilience; higher values will detect more errors but may
//	     * misdetect some more or less valid parts as errors.
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	    int error_resilience;
//	#define FF_ER_CAREFUL         1
//	#define FF_ER_COMPLIANT       2
//	#define FF_ER_AGGRESSIVE      3
//	#define FF_ER_VERY_AGGRESSIVE 4
//
//	    /**
//	     * Called at the beginning of each frame to get a buffer for it.
//	     * If pic.reference is set then the frame will be read later by libavcodec.
//	     * avcodec_align_dimensions() should be used to find the required width and
//	     * height, as they normally need to be rounded up to the next multiple of 16.
//	     * - encoding: unused
//	     * - decoding: Set by libavcodec., user can override.
//	     */
//	    int (*get_buffer)(struct AVCodecContext *c, AVFrame *pic);
//
//	    /**
//	     * Called to release buffers which where allocated with get_buffer.
//	     * A released buffer can be reused in get_buffer().
//	     * pic.data[*] must be set to NULL.
//	     * - encoding: unused
//	     * - decoding: Set by libavcodec., user can override.
//	     */
//	    void (*release_buffer)(struct AVCodecContext *c, AVFrame *pic);
//
//	    /**
//	     * If 1 the stream has a 1 frame delay during decoding.
//	     * - encoding: Set by libavcodec.
//	     * - decoding: Set by libavcodec.
//	     */
//	    int has_b_frames;
//
//	    /**
//	     * number of bytes per packet if constant and known or 0
//	     * Used by some WAV based audio codecs.
//	     */
//	    int block_align;
//
//	    int parse_only; /* - decoding only: If true, only parsing is done
//	                       (function avcodec_parse_frame()). The frame
//	                       data is returned. Only MPEG codecs support this now. */
//
//	    /**
//	     * 0-> h263 quant 1-> mpeg quant
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int mpeg_quant;
//
//	    /**
//	     * pass1 encoding statistics output buffer
//	     * - encoding: Set by libavcodec.
//	     * - decoding: unused
//	     */
//	    char *stats_out;
//
//	    /**
//	     * pass2 encoding statistics input buffer
//	     * Concatenated stuff from stats_out of pass1 should be placed here.
//	     * - encoding: Allocated/set/freed by user.
//	     * - decoding: unused
//	     */
//	    char *stats_in;
//
//	    /**
//	     * ratecontrol qmin qmax limiting method
//	     * 0-> clipping, 1-> use a nice continous function to limit qscale wthin qmin/qmax.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float rc_qsquish;
//
//	    float rc_qmod_amp;
//	    int rc_qmod_freq;
//
//	    /**
//	     * ratecontrol override, see RcOverride
//	     * - encoding: Allocated/set/freed by user.
//	     * - decoding: unused
//	     */
//	    RcOverride *rc_override;
//	    int rc_override_count;
//
//	    /**
//	     * rate control equation
//	     * - encoding: Set by user
//	     * - decoding: unused
//	     */
//	    char *rc_eq;
//
//	    /**
//	     * maximum bitrate
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int rc_max_rate;
//
//	    /**
//	     * minimum bitrate
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int rc_min_rate;
//
//	    /**
//	     * decoder bitstream buffer size
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int rc_buffer_size;
//	    float rc_buffer_aggressivity;
//
//	    /**
//	     * qscale factor between P and I-frames
//	     * If > 0 then the last p frame quantizer will be used (q= lastp_q*factor+offset).
//	     * If < 0 then normal ratecontrol will be done (q= -normal_q*factor+offset).
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float i_quant_factor;
//
//	    /**
//	     * qscale offset between P and I-frames
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float i_quant_offset;
//
//	    /**
//	     * initial complexity for pass1 ratecontrol
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float rc_initial_cplx;
//
//	    /**
//	     * DCT algorithm, see FF_DCT_* below
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int dct_algo;
//	#define FF_DCT_AUTO    0
//	#define FF_DCT_FASTINT 1
//	#define FF_DCT_INT     2
//	#define FF_DCT_MMX     3
//	#define FF_DCT_MLIB    4
//	#define FF_DCT_ALTIVEC 5
//	#define FF_DCT_FAAN    6
//
//	    /**
//	     * luminance masking (0-> disabled)
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float lumi_masking;
//
//	    /**
//	     * temporary complexity masking (0-> disabled)
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float temporal_cplx_masking;
//
//	    /**
//	     * spatial complexity masking (0-> disabled)
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float spatial_cplx_masking;
//
//	    /**
//	     * p block masking (0-> disabled)
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float p_masking;
//
//	    /**
//	     * darkness masking (0-> disabled)
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float dark_masking;
//
//
//	    /* for binary compatibility */
//	    int unused;
//
//	    /**
//	     * IDCT algorithm, see FF_IDCT_* below.
//	     * - encoding: Set by user.
//	     * - decoding: Set by user.
//	     */
//	    int idct_algo;
//	#define FF_IDCT_AUTO         0
//	#define FF_IDCT_INT          1
//	#define FF_IDCT_SIMPLE       2
//	#define FF_IDCT_SIMPLEMMX    3
//	#define FF_IDCT_LIBMPEG2MMX  4
//	#define FF_IDCT_PS2          5
//	#define FF_IDCT_MLIB         6
//	#define FF_IDCT_ARM          7
//	#define FF_IDCT_ALTIVEC      8
//	#define FF_IDCT_SH4          9
//	#define FF_IDCT_SIMPLEARM    10
//	#define FF_IDCT_H264         11
//	#define FF_IDCT_VP3          12
//	#define FF_IDCT_IPP          13
//	#define FF_IDCT_XVIDMMX      14
//	#define FF_IDCT_CAVS         15
//	#define FF_IDCT_SIMPLEARMV5TE 16
//	#define FF_IDCT_SIMPLEARMV6  17
//
//	    /**
//	     * slice count
//	     * - encoding: Set by libavcodec.
//	     * - decoding: Set by user (or 0).
//	     */
//	    int slice_count;
//	    /**
//	     * slice offsets in the frame in bytes
//	     * - encoding: Set/allocated by libavcodec.
//	     * - decoding: Set/allocated by user (or NULL).
//	     */
//	    int *slice_offset;
//
//	    /**
//	     * error concealment flags
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	    int error_concealment;
//	#define FF_EC_GUESS_MVS   1
//	#define FF_EC_DEBLOCK     2
//
//	    /**
//	     * dsp_mask could be add used to disable unwanted CPU features
//	     * CPU features (i.e. MMX, SSE. ...)
//	     *
//	     * With the FORCE flag you may instead enable given CPU features.
//	     * (Dangerous: Usable in case of misdetection, improper usage however will
//	     * result into program crash.)
//	     */
//	    unsigned dsp_mask;
//	#define FF_MM_FORCE    0x80000000 /* Force usage of selected flags (OR) */
//	    /* lower 16 bits - CPU features */
//	#define FF_MM_MMX      0x0001 /* standard MMX */
//	#define FF_MM_3DNOW    0x0004 /* AMD 3DNOW */
//	#define FF_MM_MMXEXT   0x0002 /* SSE integer functions or AMD MMX ext */
//	#define FF_MM_SSE      0x0008 /* SSE functions */
//	#define FF_MM_SSE2     0x0010 /* PIV SSE2 functions */
//	#define FF_MM_3DNOWEXT 0x0020 /* AMD 3DNowExt */
//	#define FF_MM_SSE3     0x0040 /* Prescott SSE3 functions */
//	#define FF_MM_SSSE3    0x0080 /* Conroe SSSE3 functions */
//	#define FF_MM_IWMMXT   0x0100 /* XScale IWMMXT */
//
//	    /**
//	     * bits per sample/pixel from the demuxer (needed for huffyuv).
//	     * - encoding: Set by libavcodec.
//	     * - decoding: Set by user.
//	     */
//	     int bits_per_sample;
//
//	    /**
//	     * prediction method (needed for huffyuv)
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	     int prediction_method;
//	#define FF_PRED_LEFT   0
//	#define FF_PRED_PLANE  1
//	#define FF_PRED_MEDIAN 2
//
//	    /**
//	     * sample aspect ratio (0 if unknown)
//	     * Numerator and denominator must be relatively prime and smaller than 256 for some video standards.
//	     * - encoding: Set by user.
//	     * - decoding: Set by libavcodec.
//	     */
//	    AVRational sample_aspect_ratio;
//
//	    /**
//	     * the picture in the bitstream
//	     * - encoding: Set by libavcodec.
//	     * - decoding: Set by libavcodec.
//	     */
//	    AVFrame *coded_frame;
//
//	    /**
//	     * debug
//	     * - encoding: Set by user.
//	     * - decoding: Set by user.
//	     */
//	    int debug;
//	#define FF_DEBUG_PICT_INFO 1
//	#define FF_DEBUG_RC        2
//	#define FF_DEBUG_BITSTREAM 4
//	#define FF_DEBUG_MB_TYPE   8
//	#define FF_DEBUG_QP        16
//	#define FF_DEBUG_MV        32
//	#define FF_DEBUG_DCT_COEFF 0x00000040
//	#define FF_DEBUG_SKIP      0x00000080
//	#define FF_DEBUG_STARTCODE 0x00000100
//	#define FF_DEBUG_PTS       0x00000200
//	#define FF_DEBUG_ER        0x00000400
//	#define FF_DEBUG_MMCO      0x00000800
//	#define FF_DEBUG_BUGS      0x00001000
//	#define FF_DEBUG_VIS_QP    0x00002000
//	#define FF_DEBUG_VIS_MB_TYPE 0x00004000
//
//	    /**
//	     * debug
//	     * - encoding: Set by user.
//	     * - decoding: Set by user.
//	     */
//	    int debug_mv;
//	#define FF_DEBUG_VIS_MV_P_FOR  0x00000001 //visualize forward predicted MVs of P frames
//	#define FF_DEBUG_VIS_MV_B_FOR  0x00000002 //visualize forward predicted MVs of B frames
//	#define FF_DEBUG_VIS_MV_B_BACK 0x00000004 //visualize backward predicted MVs of B frames
//
//	    /**
//	     * error
//	     * - encoding: Set by libavcodec if flags&CODEC_FLAG_PSNR.
//	     * - decoding: unused
//	     */
//	    uint64_t error[4];
//
//	    /**
//	     * minimum MB quantizer
//	     * - encoding: unused
//	     * - decoding: unused
//	     */
//	    int mb_qmin;
//
//	    /**
//	     * maximum MB quantizer
//	     * - encoding: unused
//	     * - decoding: unused
//	     */
//	    int mb_qmax;
//
//	    /**
//	     * motion estimation comparison function
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int me_cmp;
//	    /**
//	     * subpixel motion estimation comparison function
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int me_sub_cmp;
//	    /**
//	     * macroblock comparison function (not supported yet)
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int mb_cmp;
//	    /**
//	     * interlaced DCT comparison function
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int ildct_cmp;
//	#define FF_CMP_SAD  0
//	#define FF_CMP_SSE  1
//	#define FF_CMP_SATD 2
//	#define FF_CMP_DCT  3
//	#define FF_CMP_PSNR 4
//	#define FF_CMP_BIT  5
//	#define FF_CMP_RD   6
//	#define FF_CMP_ZERO 7
//	#define FF_CMP_VSAD 8
//	#define FF_CMP_VSSE 9
//	#define FF_CMP_NSSE 10
//	#define FF_CMP_W53  11
//	#define FF_CMP_W97  12
//	#define FF_CMP_DCTMAX 13
//	#define FF_CMP_DCT264 14
//	#define FF_CMP_CHROMA 256
//
//	    /**
//	     * ME diamond size & shape
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int dia_size;
//
//	    /**
//	     * amount of previous MV predictors (2a+1 x 2a+1 square)
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int last_predictor_count;
//
//	    /**
//	     * prepass for motion estimation
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int pre_me;
//
//	    /**
//	     * motion estimation prepass comparison function
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int me_pre_cmp;
//
//	    /**
//	     * ME prepass diamond size & shape
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int pre_dia_size;
//
//	    /**
//	     * subpel ME quality
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int me_subpel_quality;
//
//	    /**
//	     * callback to negotiate the pixelFormat
//	     * @param fmt is the list of formats which are supported by the codec,
//	     * it is terminated by -1 as 0 is a valid format, the formats are ordered by quality.
//	     * The first is always the native one.
//	     * @return the chosen format
//	     * - encoding: unused
//	     * - decoding: Set by user, if not set the native format will be chosen.
//	     */
//	    enum PixelFormat (*get_format)(struct AVCodecContext *s, const enum PixelFormat * fmt);
//
//	    /**
//	     * DTG active format information (additional aspect ratio
//	     * information only used in DVB MPEG-2 transport streams)
//	     * 0 if not set.
//	     *
//	     * - encoding: unused
//	     * - decoding: Set by decoder.
//	     */
//	    int dtg_active_format;
//	#define FF_DTG_AFD_SAME         8
//	#define FF_DTG_AFD_4_3          9
//	#define FF_DTG_AFD_16_9         10
//	#define FF_DTG_AFD_14_9         11
//	#define FF_DTG_AFD_4_3_SP_14_9  13
//	#define FF_DTG_AFD_16_9_SP_14_9 14
//	#define FF_DTG_AFD_SP_4_3       15
//
//	    /**
//	     * maximum motion estimation search range in subpel units
//	     * If 0 then no limit.
//	     *
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int me_range;
//
//	    /**
//	     * intra quantizer bias
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int intra_quant_bias;
//	#define FF_DEFAULT_QUANT_BIAS 999999
//
//	    /**
//	     * inter quantizer bias
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int inter_quant_bias;
//
//	    /**
//	     * color table ID
//	     * - encoding: unused
//	     * - decoding: Which clrtable should be used for 8bit RGB images.
//	     *             Tables have to be stored somewhere. FIXME
//	     */
//	    int color_table_id;
//
//	    /**
//	     * internal_buffer count
//	     * Don't touch, used by libavcodec default_get_buffer().
//	     */
//	    int internal_buffer_count;
//
//	    /**
//	     * internal_buffers
//	     * Don't touch, used by libavcodec default_get_buffer().
//	     */
//	    void *internal_buffer;
//
//	#define FF_LAMBDA_SHIFT 7
//	#define FF_LAMBDA_SCALE (1<<FF_LAMBDA_SHIFT)
//	#define FF_QP2LAMBDA 118 ///< factor to convert from H.263 QP to lambda
//	#define FF_LAMBDA_MAX (256*128-1)
//
//	#define FF_QUALITY_SCALE FF_LAMBDA_SCALE //FIXME maybe remove
//	    /**
//	     * Global quality for codecs which cannot change it per frame.
//	     * This should be proportional to MPEG-1/2/4 qscale.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int global_quality;
//
//	#define FF_CODER_TYPE_VLC       0
//	#define FF_CODER_TYPE_AC        1
//	#define FF_CODER_TYPE_RAW       2
//	#define FF_CODER_TYPE_RLE       3
//	#define FF_CODER_TYPE_DEFLATE   4
//	    /**
//	     * coder type
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int coder_type;
//
//	    /**
//	     * context model
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int context_model;
//	#if 0
//	    /**
//	     *
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	    uint8_t * (*realloc)(struct AVCodecContext *s, uint8_t *buf, int buf_size);
//	#endif
//
//	    /**
//	     * slice flags
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	    int slice_flags;
//	#define SLICE_FLAG_CODED_ORDER    0x0001 ///< draw_horiz_band() is called in coded order instead of display
//	#define SLICE_FLAG_ALLOW_FIELD    0x0002 ///< allow draw_horiz_band() with field slices (MPEG2 field pics)
//	#define SLICE_FLAG_ALLOW_PLANE    0x0004 ///< allow draw_horiz_band() with 1 component at a time (SVQ1)
//
//	    /**
//	     * XVideo Motion Acceleration
//	     * - encoding: forbidden
//	     * - decoding: set by decoder
//	     */
//	    int xvmc_acceleration;
//
//	    /**
//	     * macroblock decision mode
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int mb_decision;
//	#define FF_MB_DECISION_SIMPLE 0        ///< uses mb_cmp
//	#define FF_MB_DECISION_BITS   1        ///< chooses the one which needs the fewest bits
//	#define FF_MB_DECISION_RD     2        ///< rate distoration
//
//	    /**
//	     * custom intra quantization matrix
//	     * - encoding: Set by user, can be NULL.
//	     * - decoding: Set by libavcodec.
//	     */
//	    uint16_t *intra_matrix;
//
//	    /**
//	     * custom inter quantization matrix
//	     * - encoding: Set by user, can be NULL.
//	     * - decoding: Set by libavcodec.
//	     */
//	    uint16_t *inter_matrix;
//
//	    /**
//	     * fourcc from the AVI stream header (LSB first, so "ABCD" -> ('D'<<24) + ('C'<<16) + ('B'<<8) + 'A').
//	     * This is used to work around some encoder bugs.
//	     * - encoding: unused
//	     * - decoding: Set by user, will be converted to uppercase by libavcodec during init.
//	     */
//	    unsigned int stream_codec_tag;
//
//	    /**
//	     * scene change detection threshold
//	     * 0 is default, larger means fewer detected scene changes.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int scenechange_threshold;
//
//	    /**
//	     * minimum Lagrange multipler
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int lmin;
//
//	    /**
//	     * maximum Lagrange multipler
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int lmax;
//
//	    /**
//	     * palette control structure
//	     * - encoding: ??? (no palette-enabled encoder yet)
//	     * - decoding: Set by user.
//	     */
//	    struct AVPaletteControl *palctrl;
//
//	    /**
//	     * noise reduction strength
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int noise_reduction;
//
//	    /**
//	     * Called at the beginning of a frame to get cr buffer for it.
//	     * Buffer type (size, hints) must be the same. libavcodec won't check it.
//	     * libavcodec will pass previous buffer in pic, function should return
//	     * same buffer or new buffer with old frame "painted" into it.
//	     * If pic.data[0] == NULL must behave like get_buffer().
//	     * - encoding: unused
//	     * - decoding: Set by libavcodec., user can override
//	     */
//	    int (*reget_buffer)(struct AVCodecContext *c, AVFrame *pic);
//
//	    /**
//	     * Number of bits which should be loaded into the rc buffer before decoding starts.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int rc_initial_buffer_occupancy;
//
//	    /**
//	     *
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int inter_threshold;
//
//	    /**
//	     * CODEC_FLAG2_*
//	     * - encoding: Set by user.
//	     * - decoding: Set by user.
//	     */
//	    int flags2;
//
//	    /**
//	     * Simulates errors in the bitstream to test error concealment.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int error_rate;
//
//	    /**
//	     * MP3 antialias algorithm, see FF_AA_* below.
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	    int antialias_algo;
//	#define FF_AA_AUTO    0
//	#define FF_AA_FASTINT 1 //not implemented yet
//	#define FF_AA_INT     2
//	#define FF_AA_FLOAT   3
//	    /**
//	     * quantizer noise shaping
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int quantizer_noise_shaping;
//
//	    /**
//	     * thread count
//	     * is used to decide how many independent tasks should be passed to execute()
//	     * - encoding: Set by user.
//	     * - decoding: Set by user.
//	     */
//	    int thread_count;
//
//	    /**
//	     * The codec may call this to execute several independent things.
//	     * It will return only after finishing all tasks.
//	     * The user may replace this with some multithreaded implementation,
//	     * the default implementation will execute the parts serially.
//	     * @param count the number of things to execute
//	     * - encoding: Set by libavcodec, user can override.
//	     * - decoding: Set by libavcodec, user can override.
//	     */
//	    int (*execute)(struct AVCodecContext *c, int (*func)(struct AVCodecContext *c2, void *arg), void **arg2, int *ret, int count);
//
//	    /**
//	     * thread opaque
//	     * Can be used by execute() to store some per AVCodecContext stuff.
//	     * - encoding: set by execute()
//	     * - decoding: set by execute()
//	     */
//	    void *thread_opaque;
//
//	    /**
//	     * Motion estimation threshold below which no motion estimation is
//	     * performed, but instead the user specified motion vectors are used.
//	     *
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	     int me_threshold;
//
//	    /**
//	     * Macroblock threshold below which the user specified macroblock types will be used.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	     int mb_threshold;
//
//	    /**
//	     * precision of the intra DC coefficient - 8
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	     int intra_dc_precision;
//
//	    /**
//	     * noise vs. sse weight for the nsse comparsion function
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	     int nsse_weight;
//
//	    /**
//	     * Number of macroblock rows at the top which are skipped.
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	     int skip_top;
//
//	    /**
//	     * Number of macroblock rows at the bottom which are skipped.
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	     int skip_bottom;
//
//	    /**
//	     * profile
//	     * - encoding: Set by user.
//	     * - decoding: Set by libavcodec.
//	     */
//	     int profile;
//	#define FF_PROFILE_UNKNOWN -99
//	#define FF_PROFILE_AAC_MAIN 0
//	#define FF_PROFILE_AAC_LOW 1
//	#define FF_PROFILE_AAC_SSR 2
//	#define FF_PROFILE_AAC_LTP 3
//
//	    /**
//	     * level
//	     * - encoding: Set by user.
//	     * - decoding: Set by libavcodec.
//	     */
//	     int level;
//	#define FF_LEVEL_UNKNOWN -99
//
//	    /**
//	     * low resolution decoding, 1-> 1/2 size, 2->1/4 size
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	     int lowres;
//
//	    /**
//	     * Bitstream width / height, may be different from width/height if lowres
//	     * or other things are used.
//	     * - encoding: unused
//	     * - decoding: Set by user before init if known. Codec should override / dynamically change if needed.
//	     */
//	    int coded_width, coded_height;
//
//	    /**
//	     * frame skip threshold
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int frame_skip_threshold;
//
//	    /**
//	     * frame skip factor
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int frame_skip_factor;
//
//	    /**
//	     * frame skip exponent
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int frame_skip_exp;
//
//	    /**
//	     * frame skip comparison function
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int frame_skip_cmp;
//
//	    /**
//	     * Border processing masking, raises the quantizer for mbs on the borders
//	     * of the picture.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float border_masking;
//
//	    /**
//	     * minimum MB lagrange multipler
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int mb_lmin;
//
//	    /**
//	     * maximum MB lagrange multipler
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int mb_lmax;
//
//	    /**
//	     *
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int me_penalty_compensation;
//
//	    /**
//	     *
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	    enum AVDiscard skip_loop_filter;
//
//	    /**
//	     *
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	    enum AVDiscard skip_idct;
//
//	    /**
//	     *
//	     * - encoding: unused
//	     * - decoding: Set by user.
//	     */
//	    enum AVDiscard skip_frame;
//
//	    /**
//	     *
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int bidir_refine;
//
//	    /**
//	     *
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int brd_scale;
//
//	    /**
//	     * constant rate factor - quality-based VBR - values ~correspond to qps
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float crf;
//
//	    /**
//	     * constant quantization parameter rate control method
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int cqp;
//
//	    /**
//	     * minimum GOP size
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int keyint_min;
//
//	    /**
//	     * number of reference frames
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int refs;
//
//	    /**
//	     * chroma qp offset from luma
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int chromaoffset;
//
//	    /**
//	     * Influences how often B-frames are used.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int bframebias;
//
//	    /**
//	     * trellis RD quantization
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int trellis;
//
//	    /**
//	     * Reduce fluctuations in qp (before curve compression).
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    float complexityblur;
//
//	    /**
//	     * in-loop deblocking filter alphac0 parameter
//	     * alpha is in the range -6...6
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int deblockalpha;
//
//	    /**
//	     * in-loop deblocking filter beta parameter
//	     * beta is in the range -6...6
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int deblockbeta;
//
//	    /**
//	     * macroblock subpartition sizes to consider - p8x8, p4x4, b8x8, i8x8, i4x4
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int partitions;
//	#define X264_PART_I4X4 0x001  /* Analyse i4x4 */
//	#define X264_PART_I8X8 0x002  /* Analyse i8x8 (requires 8x8 transform) */
//	#define X264_PART_P8X8 0x010  /* Analyse p16x8, p8x16 and p8x8 */
//	#define X264_PART_P4X4 0x020  /* Analyse p8x4, p4x8, p4x4 */
//	#define X264_PART_B8X8 0x100  /* Analyse b16x8, b8x16 and b8x8 */
//
//	    /**
//	     * direct MV prediction mode - 0 (none), 1 (spatial), 2 (temporal)
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int directpred;
//
//	    /**
//	     * Audio cutoff bandwidth (0 means "automatic"), currently used only by FAAC.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int cutoff;
//
//	    /**
//	     * Multiplied by qscale for each frame and added to scene_change_score.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int scenechange_factor;
//
//	    /**
//	     *
//	     * Note: Value depends upon the compare function used for fullpel ME.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int mv0_threshold;
//
//	    /**
//	     * Adjusts sensitivity of b_frame_strategy 1.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int b_sensitivity;
//
//	    /**
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int compression_level;
//	#define FF_COMPRESSION_DEFAULT -1
//
//	    /**
//	     * Sets whether to use LPC mode - used by FLAC encoder.
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int use_lpc;
//
//	    /**
//	     * LPC coefficient precision - used by FLAC encoder
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int lpc_coeff_precision;
//
//	    /**
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int min_prediction_order;
//
//	    /**
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int max_prediction_order;
//
//	    /**
//	     * search method for selecting prediction order
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int prediction_order_method;
//
//	    /**
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int min_partition_order;
//
//	    /**
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int max_partition_order;
//
//	    /**
//	     * GOP timecode frame start number, in non drop frame format
//	     * - encoding: Set by user.
//	     * - decoding: unused
//	     */
//	    int64_t timecode_frame_start;
//	} AVCodecContext;
	
	
	
	/**
	 * main external API structure
	 */
	public static class AVCodecContext extends Structure 
	{
	    public Pointer av_class;
	    public int bit_rate;
	    public int bit_rate_tolerance;
	    public int flags;
	    public int sub_id;
	    public int me_method;
	    public Pointer extradata;
	    public int extradata_size;
	    public AVRational time_base;
	    public int width;
	    public int height;

	    	public static final int FF_ASPECT_EXTENDED =15;

	    public int gop_size;
	    public int pix_fmt;
	    public int rate_emu;
	    public Pointer draw_horiz_band;

	    /* audio only */
	    public int sample_rate; ///< samples per second
	    public int channels;
	    public int sample_fmt;  ///< sample format, currently unused
	    public int frame_size;
	    public int frame_number;   ///< audio or video frame number
	    public int real_pict_num;  ///< Returns the real picture number of previous encoded frame.
	    public int delay;
	    public float qcompress;  ///< amount of qscale change between easy & hard scenes (0.0-1.0)
	    public float qblur;      ///< amount of qscale smoothing over time (0.0-1.0)
	    public int qmin;
	    public int qmax;
	    public int max_qdiff;
	    public int max_b_frames;
	    public float b_quant_factor;
	    public int rc_strategy;
	    	public static final int FF_RC_STRATEGY_XVID =1;

	    public int b_frame_strategy;
	    public int hurry_up;

	    public Pointer codec;

	    public Pointer priv_data;

//	#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
//	    /* unused, FIXME remove*/
	    public int rtp_mode;
//	#endif

		public int rtp_payload_size;   
		public Pointer rtp_callback;

	    public int mv_bits;
	    public int header_bits;
	    public int i_tex_bits;
	    public int p_tex_bits;
	    public int i_count;
	    public int p_count;
	    public int skip_count;
	    public int misc_bits;
	    public int frame_bits;
	    public Pointer opaque;

	    public byte[] codec_name = new byte[32];
	    public int codec_type; /* see CODEC_TYPE_xxx */
	    public int codec_id; /* see CODEC_ID_xxx */

	    public int codec_tag;
	    public int workaround_bugs;
			public static final int FF_BUG_AUTODETECT       =1;  ///< autodetection
			public static final int FF_BUG_OLD_MSMPEG4      =2;
			public static final int FF_BUG_XVID_ILACE       =4;
			public static final int FF_BUG_UMP4             =8;
			public static final int FF_BUG_NO_PADDING       =16;
			public static final int FF_BUG_AMV              =32;
			public static final int FF_BUG_AC_VLC           =0;  ///< Will be removed, libavcodec can now handle these non-compliant files by default.
			public static final int FF_BUG_QPEL_CHROMA      =64;
			public static final int FF_BUG_STD_QPEL         =128;
			public static final int FF_BUG_QPEL_CHROMA2     =256;
			public static final int FF_BUG_DIRECT_BLOCKSIZE =512;
			public static final int FF_BUG_EDGE             =1024;
			public static final int FF_BUG_HPEL_CHROMA      =2048;
			public static final int FF_BUG_DC_CLIP          =4096;
			public static final int FF_BUG_MS               =8192; ///< Work around various bugs in Microsoft's broken decoders.
	    public int luma_elim_threshold;
	    public int chroma_elim_threshold;
	    public int strict_std_compliance;
			public static final int FF_COMPLIANCE_VERY_STRICT   =2; ///< Strictly conform to a older more strict version of the spec or reference software.
			public static final int FF_COMPLIANCE_STRICT        =1; ///< Strictly conform to all the things in the spec no matter what consequences.
			public static final int FF_COMPLIANCE_NORMAL        =0;
			public static final int FF_COMPLIANCE_INOFFICIAL   =-1; ///< Allow inofficial extensions.
			public static final int FF_COMPLIANCE_EXPERIMENTAL =-2; ///< Allow nonstandardized experimental things.
	    public float b_quant_offset;
	    public int error_resilience;
			public static final int FF_ER_CAREFUL         =1;
			public static final int FF_ER_COMPLIANT       =2;
			public static final int FF_ER_AGGRESSIVE      =3;
			public static final int FF_ER_VERY_AGGRESSIVE =4;
	    public Pointer get_buffer;

	    public Pointer release_buffer;
	    public int has_b_frames;
	    public int block_align;
	    public int parse_only;
	    public int mpeg_quant;
	    public Pointer stats_out;
	    public Pointer stats_in;
	    public float rc_qsquish;
	    public float rc_qmod_amp;
	    public int rc_qmod_freq;
	    public Pointer rc_override;
	    public int rc_override_count;
	    public Pointer rc_eq;
	    public int rc_max_rate;
	    public int rc_min_rate;
	    public int rc_buffer_size;
	    public float rc_buffer_aggressivity;
	    public float i_quant_factor;
	    public float i_quant_offset;
	    public float rc_initial_cplx;
	    public int dct_algo;
			public static final int FF_DCT_AUTO    =0;
			public static final int FF_DCT_FASTINT =1;
			public static final int FF_DCT_INT     =2;
			public static final int FF_DCT_MMX     =3;
			public static final int FF_DCT_MLIB    =4;
			public static final int FF_DCT_ALTIVEC =5;
			public static final int FF_DCT_FAAN    =6;
	    public float lumi_masking;
	    public float temporal_cplx_masking;
	    public float spatial_cplx_masking;
	    public float p_masking;
	    public float dark_masking;
	    public int unused;
	    public int idct_algo;
			public static final int FF_IDCT_AUTO         =0;
			public static final int FF_IDCT_INT          =1;
			public static final int FF_IDCT_SIMPLE       =2;
			public static final int FF_IDCT_SIMPLEMMX    =3;
			public static final int FF_IDCT_LIBMPEG2MMX  =4;
			public static final int FF_IDCT_PS2          =5;
			public static final int FF_IDCT_MLIB         =6;
			public static final int FF_IDCT_ARM          =7;
			public static final int FF_IDCT_ALTIVEC      =8;
			public static final int FF_IDCT_SH4          =9;
			public static final int FF_IDCT_SIMPLEARM    =10;
			public static final int FF_IDCT_H264         =11;
			public static final int FF_IDCT_VP3          =12;
			public static final int FF_IDCT_IPP          =13;
			public static final int FF_IDCT_XVIDMMX      =14;
			public static final int FF_IDCT_CAVS         =15;
			public static final int FF_IDCT_SIMPLEARMV5TE =16;
			public static final int FF_IDCT_SIMPLEARMV6  =17;
	    public int slice_count;
	    public Pointer slice_offset;
	    public int error_concealment;
			public static final int FF_EC_GUESS_MVS   =1;
			public static final int FF_EC_DEBLOCK     =2;

	    /**
	     * dsp_mask could be add used to disable unwanted CPU features
	     * CPU features (i.e. MMX, SSE. ...)
	     *
	     * With the FORCE flag you may instead enable given CPU features.
	     * (Dangerous: Usable in case of misdetection, improper usage however will
	     * result into program crash.)
	     */
	    public int dsp_mask;
			public static final int FF_MM_FORCE    =0x80000000; /* Force usage of selected flags (OR) */
			//	    /* lower 16 bits - CPU features */
			public static final int FF_MM_MMX      =0x0001; /* standard MMX */
			public static final int FF_MM_3DNOW    =0x0004; /* AMD 3DNOW */
			public static final int FF_MM_MMXEXT   =0x0002; /* SSE integer functions or AMD MMX ext */
			public static final int FF_MM_SSE      =0x0008; /* SSE functions */
			public static final int FF_MM_SSE2     =0x0010; /* PIV SSE2 functions */
			public static final int FF_MM_3DNOWEXT =0x0020; /* AMD 3DNowExt */
			public static final int FF_MM_SSE3     =0x0040; /* Prescott SSE3 functions */
			public static final int FF_MM_SSSE3    =0x0080; /* Conroe SSSE3 functions */
			public static final int FF_MM_IWMMXT   =0x0100; /* XScale IWMMXT */

	    public int bits_per_sample;
	    public int prediction_method;
			public static final int FF_PRED_LEFT   =0;
			public static final int FF_PRED_PLANE  =1;
			public static final int FF_PRED_MEDIAN =2;
	    public AVRational sample_aspect_ratio;
	    public Pointer coded_frame;
	    public int debug;
			public static final int FF_DEBUG_PICT_INFO =1;
			public static final int FF_DEBUG_RC        =2;
			public static final int FF_DEBUG_BITSTREAM =4;
			public static final int FF_DEBUG_MB_TYPE   =8;
			public static final int FF_DEBUG_QP        =16;
			public static final int FF_DEBUG_MV        =32;
			public static final int FF_DEBUG_DCT_COEFF =0x00000040;
			public static final int FF_DEBUG_SKIP      =0x00000080;
			public static final int FF_DEBUG_STARTCODE =0x00000100;
			public static final int FF_DEBUG_PTS       =0x00000200;
			public static final int FF_DEBUG_ER        =0x00000400;
			public static final int FF_DEBUG_MMCO      =0x00000800;
			public static final int FF_DEBUG_BUGS      =0x00001000;
			public static final int FF_DEBUG_VIS_QP    =0x00002000;
			public static final int FF_DEBUG_VIS_MB_TYPE =0x00004000;
	    public int debug_mv;
			public static final int FF_DEBUG_VIS_MV_P_FOR  =0x00000001; //visualize forward predicted MVs of P frames
			public static final int FF_DEBUG_VIS_MV_B_FOR  =0x00000002; //visualize forward predicted MVs of B frames
			public static final int FF_DEBUG_VIS_MV_B_BACK =0x00000004; //visualize backward predicted MVs of B frames
	    public long[] error = new long[4];
	    public int mb_qmin;
	    public int mb_qmax;
	    public int me_cmp;
	    public int me_sub_cmp;
	    public int mb_cmp;
	    public int ildct_cmp;
			public static final int FF_CMP_SAD  =0;
			public static final int FF_CMP_SSE  =1;
			public static final int FF_CMP_SATD =2;
			public static final int FF_CMP_DCT  =3;
			public static final int FF_CMP_PSNR =4;
			public static final int FF_CMP_BIT  =5;
			public static final int FF_CMP_RD   =6;
			public static final int FF_CMP_ZERO =7;
			public static final int FF_CMP_VSAD =8;
			public static final int FF_CMP_VSSE =9;
			public static final int FF_CMP_NSSE =10;
			public static final int FF_CMP_W53  =11;
			public static final int FF_CMP_W97  =12;
			public static final int FF_CMP_DCTMAX =13;
			public static final int FF_CMP_DCT264 =14;
			public static final int FF_CMP_CHROMA =256;
	    public int dia_size;
	    public int last_predictor_count;
	    public int pre_me;
	    public int me_pre_cmp;
	    public int pre_dia_size;
	    public int me_subpel_quality;
	    public Pointer get_format;
	    public int dtg_active_format;
			public static final int FF_DTG_AFD_SAME         =8;
			public static final int FF_DTG_AFD_4_3          =9;
			public static final int FF_DTG_AFD_16_9         =10;
			public static final int FF_DTG_AFD_14_9         =11;
			public static final int FF_DTG_AFD_4_3_SP_14_9  =13;
			public static final int FF_DTG_AFD_16_9_SP_14_9 =14;
			public static final int FF_DTG_AFD_SP_4_3       =15;
	    public int me_range;
	    public int intra_quant_bias;
	    		public static final int FF_DEFAULT_QUANT_BIAS =999999;
	    public int inter_quant_bias;
	    public int color_table_id;
	    public int internal_buffer_count;
	    public Pointer internal_buffer;

			public static final int FF_LAMBDA_SHIFT =7;
			public static final int FF_LAMBDA_SCALE =(1<<FF_LAMBDA_SHIFT);
			public static final int FF_QP2LAMBDA =118; ///< factor to convert from H.263 QP to lambda
			public static final int FF_LAMBDA_MAX =(256*128-1);
			//
			public static final int FF_QUALITY_SCALE = FF_LAMBDA_SCALE; //FIXME maybe remove
	    public int global_quality;

			public static final int FF_CODER_TYPE_VLC       =0;
			public static final int FF_CODER_TYPE_AC        =1;
			public static final int FF_CODER_TYPE_RAW       =2;
			public static final int FF_CODER_TYPE_RLE       =3;
			public static final int FF_CODER_TYPE_DEFLATE   =4;
		public int coder_type;
		public int context_model;
		public int slice_flags;
			public static final int SLICE_FLAG_CODED_ORDER    =0x0001; ///< draw_horiz_band() is called in coded order instead of display
			public static final int SLICE_FLAG_ALLOW_FIELD    =0x0002;///< allow draw_horiz_band() with field slices (MPEG2 field pics)
			public static final int SLICE_FLAG_ALLOW_PLANE    =0x0004; ///< allow draw_horiz_band() with 1 component at a time (SVQ1)
		public int xvmc_acceleration;
		public int mb_decision;
			public static final int FF_MB_DECISION_SIMPLE =0;        ///< uses mb_cmp
			public static final int FF_MB_DECISION_BITS   =1;        ///< chooses the one which needs the fewest bits
			public static final int FF_MB_DECISION_RD     =2;        ///< rate distoration
		public Pointer intra_matrix;
		public Pointer inter_matrix;
	    public int stream_codec_tag;
	    public int scenechange_threshold;
	    public int lmin;
	    public int lmax;
	    public Pointer palctrl;
		public int noise_reduction;
		public Pointer reget_buffer;
		public int rc_initial_buffer_occupancy;
		public int inter_threshold;
		public int flags2;
		public int error_rate;
		public int antialias_algo;
			public static final int FF_AA_AUTO    =0;
			public static final int FF_AA_FASTINT =1; //not implemented yet
			public static final int FF_AA_INT     =2;
			public static final int FF_AA_FLOAT   =3;
		public int quantizer_noise_shaping;
		public int thread_count;
		public Pointer execute;
		public Pointer thread_opaque;
		public int me_threshold;
		public int mb_threshold;
		public int intra_dc_precision;
		public int nsse_weight;
		public int skip_top;
		public int skip_bottom;
		public int profile;
			public static final int FF_PROFILE_UNKNOWN =-99;
			public static final int FF_PROFILE_AAC_MAIN =0;
			public static final int FF_PROFILE_AAC_LOW =1;
			public static final int FF_PROFILE_AAC_SSR =2;
			public static final int FF_PROFILE_AAC_LTP =3;
		public int level;
				public static final int FF_LEVEL_UNKNOWN =-99;
		public int lowres;
		public int coded_width;
		public int coded_height;
		public int frame_skip_threshold;
		public int frame_skip_factor;
		public int frame_skip_exp;
		public int frame_skip_cmp;
		public float border_masking;
		public int mb_lmin;
		public int mb_lmax;
		public int me_penalty_compensation;
		public int skip_loop_filter;
		public int skip_idct;
		public int skip_frame;
	    public int bidir_refine;
	    public int brd_scale;

	    public float crf;
	    public int cqp;
	    public int keyint_min;
	    public int refs;
	    public int chromaoffset;
	    public int bframebias;
	    public int trellis;
	    public float complexityblur;
	    public int deblockalpha;
	    public int deblockbeta;
	    public int partitions;
			public static final int X264_PART_I4X4 =0x001;  /* Analyse i4x4 */
			public static final int X264_PART_I8X8 =0x002;  /* Analyse i8x8 (requires 8x8 transform) */
			public static final int X264_PART_P8X8 =0x010;  /* Analyse p16x8, p8x16 and p8x8 */
			public static final int X264_PART_P4X4 =0x020;  /* Analyse p8x4, p4x8, p4x4 */
			public static final int X264_PART_B8X8 =0x100;  /* Analyse b16x8, b8x16 and b8x8 */
	    public int directpred;
	    public int cutoff;
	    public int scenechange_factor;
	    public int mv0_threshold;
	    public int b_sensitivity;
	    public int compression_level;
	    	public static final int FF_COMPRESSION_DEFAULT =-1;
	    public  int use_lpc;
	    public int lpc_coeff_precision;
	    public int min_prediction_order;
	    public int max_prediction_order;
	    public int prediction_order_method;
	    public int min_partition_order;
	    public int max_partition_order;
	    public long timecode_frame_start;
	    
		public AVCodecContext()
		{
			super();
		}
		
		public AVCodecContext(Pointer p)
		{	super();
			useMemory(p);
			read();
		}
	}
	
//	/**
//	 * AVCodec.
//	 */
//	typedef struct AVCodec {
//	    /**
//	     * Name of the codec implementation.
//	     * The name is globally unique among encoders and among decoders (but an
//	     * encoder and a decoder can share the same name).
//	     * This is the primary way to find a codec from the user perspective.
//	     */
//	    const char *name;
//	    enum CodecType type;
//	    enum CodecID id;
//	    int priv_data_size;
//	    int (*init)(AVCodecContext *);
//	    int (*encode)(AVCodecContext *, uint8_t *buf, int buf_size, void *data);
//	    int (*close)(AVCodecContext *);
//	    int (*decode)(AVCodecContext *, void *outdata, int *outdata_size,
//	                  uint8_t *buf, int buf_size);
//	    int capabilities;
//	    struct AVCodec *next;
//	    void (*flush)(AVCodecContext *);
//	    const AVRational *supported_framerates; ///array of supported framerates, or NULL if any, array is terminated by {0,0}
//	    const enum PixelFormat *pix_fmts;       ///array of supported pixel formats, or NULL if unknown, array is terminanted by -1
//	} AVCodec;
	
	public static class AVCodec extends Structure 
	{
	    public String name;
	    public int type;
	    public int id;
	    public int  priv_data_size;
	    public Pointer init;
	    public Pointer encode;
	    public Pointer close;
	    public Pointer decode;
	    public int capabilities;
	    public Pointer next;
	    public Pointer flush;
	    public Pointer supported_framerates; ///array of supported framerates, or NULL if any, array is terminated by {0,0}
	    public Pointer pix_fmts;       ///array of supported pixel formats, or NULL if unknown, array is terminanted by -1
	    
		public AVCodec()
		{
			super();
		}
		
		public AVCodec(Pointer p)
		{	super();
			useMemory(p);
			read();
		}
	}
	
//	/**
//	 * four components are given, that's all.
//	 * the last component is alpha
//	 */
//	typedef struct AVPicture {
//	    uint8_t *data[4];
//	    int linesize[4];       ///< number of bytes per line
//	} AVPicture;
	
	
	/**
	 * four components are given, that's all.
	 * the last component is alpha
	 */
	public static class AVPicture extends Structure
	{
	    public Pointer data0;
	    public Pointer data1;
	    public Pointer data2;
	    public Pointer data3;
	    
	    public int[] linesize = new int[4];       ///< number of bytes per line
	    
	}
	
	
//	/**
//	 * AVPaletteControl
//	 * This structure defines a method for communicating palette changes
//	 * between and demuxer and a decoder.
//	 *
//	 * @deprecated Use AVPacket to send palette changes instead.
//	 * This is totally broken.
//	 */
//	#define AVPALETTE_SIZE 1024
//	#define AVPALETTE_COUNT 256
//	typedef struct AVPaletteControl {
//
//	    /* Demuxer sets this to 1 to indicate the palette has changed;
//	     * decoder resets to 0. */
//	    int palette_changed;
//
//	    /* 4-byte ARGB palette entries, stored in native byte order; note that
//	     * the individual palette components should be on a 8-bit scale; if
//	     * the palette data comes from an IBM VGA native format, the component
//	     * data is probably 6 bits in size and needs to be scaled. */
//	    unsigned int palette[AVPALETTE_COUNT];
//
//	} AVPaletteControl attribute_deprecated;

	
	public static final int AVPALETTE_SIZE = 1024;
	public static final int AVPALETTE_COUNT = 256;
	public static class AVPaletteControl extends Structure
	{

	    public int palette_changed;
	    public int[] palette = new int[AVPALETTE_COUNT];

	} 

//	typedef struct AVSubtitleRect {
//	    uint16_t x;
//	    uint16_t y;
//	    uint16_t w;
//	    uint16_t h;
//	    uint16_t nb_colors;
//	    int linesize;
//	    uint32_t *rgba_palette;
//	    uint8_t *bitmap;
//	} AVSubtitleRect;
//
//	typedef struct AVSubtitle {
//	    uint16_t format; /* 0 = graphics */
//	    uint32_t start_display_time; /* relative to packet pts, in ms */
//	    uint32_t end_display_time; /* relative to packet pts, in ms */
//	    uint32_t num_rects;
//	    AVSubtitleRect *rects;
//	} AVSubtitle;
	
	
	public static class AVSubtitleRect extends Structure
	{
	    public short x;
	    public short y;
	    public short w;
	    public short h;
	    public short nb_colors;
	    public int linesize;
	    public Pointer rgba_palette;
	    public Pointer bitmap;
	} 

	public static class AVSubtitle extends Structure
	{
		public short format; /* 0 = graphics */
		public short start_display_time; /* relative to packet pts, in ms */
		public short end_display_time; /* relative to packet pts, in ms */
		public short num_rects;
	    public Pointer rects;
	} 
	
	
//	/* resample.c */
//
//	struct ReSampleContext;
//	struct AVResampleContext;
//
//	typedef struct ReSampleContext ReSampleContext;
//
//	ReSampleContext *audio_resample_init(int output_channels, int input_channels,
//	                                     int output_rate, int input_rate);
//	int audio_resample(ReSampleContext *s, short *output, short *input, int nb_samples);
//	void audio_resample_close(ReSampleContext *s);
//
//	struct AVResampleContext *av_resample_init(int out_rate, int in_rate, int filter_length, int log2_phase_count, int linear, double cutoff);
//	int av_resample(struct AVResampleContext *c, short *dst, short *src, int *consumed, int src_size, int dst_size, int update_ctx);
//	void av_resample_compensate(struct AVResampleContext *c, int sample_delta, int compensation_distance);
//	void av_resample_close(struct AVResampleContext *c);

	/* resample.c */

	// TODO: map ReSampleContext into JNA.
	public Pointer /*ReSampleContext*/ audio_resample_init(int output_channels, int input_channels,
	                                     int output_rate, int input_rate);
	public int audio_resample(Pointer /*ReSampleContext*/ s, Pointer output, Pointer input, int nb_samples);
	public void audio_resample_close(Pointer /*ReSampleContext*/ s);

	public Pointer /*ReSampleContext*/ av_resample_init(int out_rate, int in_rate, int filter_length, int log2_phase_count, int linear, double cutoff);
	int av_resample(Pointer /*ReSampleContext*/ c, Pointer dst, Pointer src, Pointer consumed, int src_size, int dst_size, int update_ctx);
	void av_resample_compensate(Pointer /*ReSampleContext*/ c, int sample_delta, int compensation_distance);
	void av_resample_close(Pointer /*ReSampleContext*/ c);

	
//	#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
//	/* YUV420 format is assumed ! */
//
//	/**
//	 * @deprecated Use the software scaler (swscale) instead.
//	 */
//	typedef struct ImgReSampleContext ImgReSampleContext attribute_deprecated;
//
//	/**
//	 * @deprecated Use the software scaler (swscale) instead.
//	 */
//	attribute_deprecated ImgReSampleContext *img_resample_init(int output_width, int output_height,
//	                                      int input_width, int input_height);
//
//	/**
//	 * @deprecated Use the software scaler (swscale) instead.
//	 */
//	attribute_deprecated ImgReSampleContext *img_resample_full_init(int owidth, int oheight,
//	                                      int iwidth, int iheight,
//	                                      int topBand, int bottomBand,
//	                                      int leftBand, int rightBand,
//	                                      int padtop, int padbottom,
//	                                      int padleft, int padright);
//
//	/**
//	 * @deprecated Use the software scaler (swscale) instead.
//	 */
//	attribute_deprecated void img_resample(struct ImgReSampleContext *s,
//	                  AVPicture *output, const AVPicture *input);
//
//	/**
//	 * @deprecated Use the software scaler (swscale) instead.
//	 */
//	attribute_deprecated void img_resample_close(struct ImgReSampleContext *s);
//
//	#endif
	
	// TODO: did not map ImgReSampleContext for JNA, as its structure is complex, and these functions are deprecated.
	//#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
	/** YUV420 format is assumed ! 
	 * @deprecated */
	public Pointer/*ImgReSampleContext*/ img_resample_init(int output_width, int output_height,
	                                      int input_width, int input_height);

	/**
	 * @deprecated Use the software scaler (swscale) instead.
	 */
	public Pointer/*ImgReSampleContext*/ img_resample_full_init(int owidth, int oheight,
	                                      int iwidth, int iheight,
	                                      int topBand, int bottomBand,
	                                      int leftBand, int rightBand,
	                                      int padtop, int padbottom,
	                                      int padleft, int padright);

	/**
	 * @deprecated Use the software scaler (swscale) instead.
	 */
	public void img_resample(Pointer/*ImgReSampleContext*/ s,
			AVPicture output, AVPicture input);

	/**
	 * @deprecated Use the software scaler (swscale) instead.
	 */
	public void img_resample_close(Pointer/*ImgReSampleContext*/ s);

	//#endif
	
	

//	/**
//	 * Allocate memory for a picture.  Call avpicture_free to free it.
//	 *
//	 * @param picture the picture to be filled in
//	 * @param pix_fmt the format of the picture
//	 * @param width the width of the picture
//	 * @param height the height of the picture
//	 * @return zero if successful, a negative value if not
//	 */
//	int avpicture_alloc(AVPicture *picture, int pix_fmt, int width, int height);
//
//	/**
//	 * Free a picture previously allocated by avpicture_alloc().
//	 *
//	 * @param picture the AVPicture to be freed
//	 */
//	void avpicture_free(AVPicture *picture);
//
//	/**
//	 * Fill in the AVPicture fields.
//	 * The fields of the given AVPicture are filled in by using the 'ptr' address
//	 * which points to the image data buffer. Depending on the specified picture
//	 * format, one or multiple image data pointers and line sizes will be set.
//	 * If a planar format is specified, several pointers will be set pointing to
//	 * the different picture planes and the line sizes of the different planes
//	 * will be stored in the lines_sizes array.
//	 *
//	 * @param picture AVPicture whose fields are to be filled in
//	 * @param ptr Buffer which will contain or contains the actual image data
//	 * @param pix_fmt The format in which the picture data is stored.
//	 * @param width the width of the image in pixels
//	 * @param height the height of the image in pixels
//	 * @return size of the image data in bytes
//	 */
//	int avpicture_fill(AVPicture *picture, uint8_t *ptr,
//	                   int pix_fmt, int width, int height);
//	int avpicture_layout(const AVPicture* src, int pix_fmt, int width, int height,
//	                     unsigned char *dest, int dest_size);
//
//	/**
//	 * Calculate the size in bytes that a picture of the given width and height
//	 * would occupy if stored in the given picture format.
//	 *
//	 * @param pix_fmt the given picture format
//	 * @param width the width of the image
//	 * @param height the height of the image
//	 * @return Image data size in bytes
//	 */
//	int avpicture_get_size(int pix_fmt, int width, int height);


	public int avpicture_alloc(AVPicture picture, int pix_fmt, int width, int height);
	public void avpicture_free(AVPicture picture);
	public int avpicture_fill(AVPicture picture, Pointer ptr, int pix_fmt, int width, int height);
	// JNA: This is the same exact function, for convenience, since AVFrame is basically AVPicture plus fields on the end.
	public int avpicture_fill(AVFrame picture, Pointer ptr, int pix_fmt, int width, int height);
	public int avpicture_layout(AVPicture src, int pix_fmt, int width, int height, Pointer dest, int dest_size);
	public int avpicture_get_size(int pix_fmt, int width, int height);

	
//	void avcodec_get_chroma_sub_sample(int pix_fmt, int *h_shift, int *v_shift);
//	const char *avcodec_get_pix_fmt_name(int pix_fmt);
//	void avcodec_set_dimensions(AVCodecContext *s, int width, int height);
//	enum PixelFormat avcodec_get_pix_fmt(const char* name);
//	unsigned int avcodec_pix_fmt_to_codec_tag(enum PixelFormat p);

	
	public void avcodec_get_chroma_sub_sample(int pix_fmt, IntByReference h_shift, IntByReference v_shift);
	public String avcodec_get_pix_fmt_name(int pix_fmt);
	public void avcodec_set_dimensions(AVCodecContext s, int width, int height);
	public int avcodec_get_pix_fmt(String name);
	public int avcodec_pix_fmt_to_codec_tag(int p);

//	#define FF_LOSS_RESOLUTION  0x0001 /**< loss due to resolution change */
//	#define FF_LOSS_DEPTH       0x0002 /**< loss due to color depth change */
//	#define FF_LOSS_COLORSPACE  0x0004 /**< loss due to color space conversion */
//	#define FF_LOSS_ALPHA       0x0008 /**< loss of alpha bits */
//	#define FF_LOSS_COLORQUANT  0x0010 /**< loss due to color quantization */
//	#define FF_LOSS_CHROMA      0x0020 /**< loss of chroma (e.g. RGB to gray conversion) */
//
//	/**
//	 * Computes what kind of losses will occur when converting from one specific
//	 * pixel format to another.
//	 * When converting from one pixel format to another, information loss may occur.
//	 * For example, when converting from RGB24 to GRAY, the color information will
//	 * be lost. Similarly, other losses occur when converting from some formats to
//	 * other formats. These losses can involve loss of chroma, but also loss of
//	 * resolution, loss of color depth, loss due to the color space conversion, loss
//	 * of the alpha bits or loss due to color quantization.
//	 * avcodec_get_fix_fmt_loss() informs you about the various types of losses
//	 * which will occur when converting from one pixel format to another.
//	 *
//	 * @param[in] dst_pix_fmt destination pixel format
//	 * @param[in] src_pix_fmt source pixel format
//	 * @param[in] has_alpha Whether the source pixel format alpha channel is used.
//	 * @return Combination of flags informing you what kind of losses will occur.
//	 */
//	int avcodec_get_pix_fmt_loss(int dst_pix_fmt, int src_pix_fmt,
//	                             int has_alpha);
	
	
	public static final int FF_LOSS_RESOLUTION  =0x0001; /**< loss due to resolution change */
	public static final int FF_LOSS_DEPTH       =0x0002; /**< loss due to color depth change */
	public static final int FF_LOSS_COLORSPACE  =0x0004; /**< loss due to color space conversion */
	public static final int FF_LOSS_ALPHA       =0x0008; /**< loss of alpha bits */
	public static final int FF_LOSS_COLORQUANT  =0x0010; /**< loss due to color quantization */
	public static final int FF_LOSS_CHROMA      =0x0020; /**< loss of chroma (e.g. RGB to gray conversion) */

	public int avcodec_get_pix_fmt_loss(int dst_pix_fmt, int src_pix_fmt, int has_alpha);

//	int avcodec_find_best_pix_fmt(int pix_fmt_mask, int src_pix_fmt,
//            int has_alpha, int *loss_ptr);
	public int avcodec_find_best_pix_fmt(int pix_fmt_mask, int src_pix_fmt,
            int has_alpha, IntByReference loss_ptr);
	
// void avcodec_pix_fmt_string (char *buf, int buf_size, int pix_fmt);
	public void avcodec_pix_fmt_string (Pointer buf, int buf_size, int pix_fmt);
	
//	#define FF_ALPHA_TRANSP       0x0001 /* image has some totally transparent pixels */
//	#define FF_ALPHA_SEMI_TRANSP  0x0002 /* image has some transparent pixels */
//
//	/**
//	 * Tell if an image really has transparent alpha values.
//	 * @return ored mask of FF_ALPHA_xxx constants
//	 */
//	int img_get_alpha_info(const AVPicture *src,
//	                       int pix_fmt, int width, int height);
	
	public static final int FF_ALPHA_TRANSP       =0x0001; /* image has some totally transparent pixels */
	public static final int FF_ALPHA_SEMI_TRANSP  =0x0002; /* image has some transparent pixels */

	public int img_get_alpha_info(AVPicture src, int pix_fmt, int width, int height);
	
//	#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
//	/**
//	 * convert among pixel formats
//	 * @deprecated Use the software scaler (swscale) instead.
//	 */
//	attribute_deprecated int img_convert(AVPicture *dst, int dst_pix_fmt,
//	                const AVPicture *src, int pix_fmt,
//	                int width, int height);
//	#endif
	
	
	public int img_convert(AVPicture dst, int dst_pix_fmt,
					AVPicture src, int pix_fmt,
	                int width, int height);
	// JNA: This is the same exact function, for convenience, since AVFrame is basically AVPicture plus fields on the end.
	public int img_convert(AVFrame dst, int dst_pix_fmt,
			AVFrame src, int pix_fmt,
            int width, int height);
	
//	/* deinterlace a picture */
//	/* deinterlace - if not supported return -1 */
//	int avpicture_deinterlace(AVPicture *dst, const AVPicture *src,
//	                          int pix_fmt, int width, int height);
	
	int avpicture_deinterlace(AVPicture dst, AVPicture src,
	                          int pix_fmt, int width, int height);
	
	
//	/* external high level API */
//
//	extern AVCodec *first_avcodec;
//
//	/* returns LIBAVCODEC_VERSION_INT constant */
//	unsigned avcodec_version(void);
//	/* returns LIBAVCODEC_BUILD constant */
//	unsigned avcodec_build(void);
//
//	/**
//	 * Initializes libavcodec.
//	 *
//	 * @warning This function \e must be called before any other libavcodec
//	 * function.
//	 */
//	void avcodec_init(void);
//
//	void register_avcodec(AVCodec *format);

	
	//extern AVCodec *first_avcodec;	// no way to make this accessible with JNA?  Probably not really important anyway.
	public int avcodec_version();
	public int avcodec_build();
	public void avcodec_init();
	public void register_avcodec(AVCodec format);

//	/**
//	 * Finds an encoder with a matching codec ID.
//	 *
//	 * @param id CodecID of the requested encoder
//	 * @return An encoder if one was found, NULL otherwise.
//	 */
//	AVCodec *avcodec_find_encoder(enum CodecID id);
//
//	/**
//	 * Finds an encoder with the specified name.
//	 *
//	 * @param name name of the requested encoder
//	 * @return An encoder if one was found, NULL otherwise.
//	 */
//	AVCodec *avcodec_find_encoder_by_name(const char *name);
//
//	/**
//	 * Finds a decoder with a matching codec ID.
//	 *
//	 * @param id CodecID of the requested decoder
//	 * @return A decoder if one was found, NULL otherwise.
//	 */
//	AVCodec *avcodec_find_decoder(enum CodecID id);
//
//	/**
//	 * Finds an decoder with the specified name.
//	 *
//	 * @param name name of the requested decoder
//	 * @return A decoder if one was found, NULL otherwise.
//	 */
//	AVCodec *avcodec_find_decoder_by_name(const char *name);
//	void avcodec_string(char *buf, int buf_size, AVCodecContext *enc, int encode);

	public AVCodec avcodec_find_encoder(int id);
	public AVCodec avcodec_find_encoder_by_name(String name);
	public AVCodec avcodec_find_decoder(int id);
	public AVCodec avcodec_find_decoder_by_name(String name);
	public void avcodec_string(Pointer buf, int buf_size, AVCodecContext enc, int encode);

	
//	/**
//	 * Sets the fields of the given AVCodecContext to default values.
//	 *
//	 * @param s The AVCodecContext of which the fields should be set to default values.
//	 */
//	void avcodec_get_context_defaults(AVCodecContext *s);
//
//	/** THIS FUNCTION IS NOT YET PART OF THE PUBLIC API!
//	 *  we WILL change its arguments and name a few times! */
//	void avcodec_get_context_defaults2(AVCodecContext *s, enum CodecType);
//
//	/**
//	 * Allocates an AVCodecContext and sets its fields to default values.  The
//	 * resulting struct can be deallocated by simply calling av_free().
//	 *
//	 * @return An AVCodecContext filled with default values or NULL on failure.
//	 * @see avcodec_get_context_defaults
//	 */
//	AVCodecContext *avcodec_alloc_context(void);
//
//	/** THIS FUNCTION IS NOT YET PART OF THE PUBLIC API!
//	 *  we WILL change its arguments and name a few times! */
//	AVCodecContext *avcodec_alloc_context2(enum CodecType);
//
//	/**
//	 * Sets the fields of the given AVFrame to default values.
//	 *
//	 * @param pic The AVFrame of which the fields should be set to default values.
//	 */
//	void avcodec_get_frame_defaults(AVFrame *pic);
//
//	/**
//	 * Allocates an AVFrame and sets its fields to default values.  The resulting
//	 * struct can be deallocated by simply calling av_free().
//	 *
//	 * @return An AVFrame filled with default values or NULL on failure.
//	 * @see avcodec_get_frame_defaults
//	 */
//	AVFrame *avcodec_alloc_frame(void);
	
	public void avcodec_get_context_defaults(AVCodecContext s);
	public void avcodec_get_context_defaults2(AVCodecContext s, int e);
	public AVCodecContext avcodec_alloc_context();
	public AVCodecContext avcodec_alloc_context2(int e);
	void avcodec_get_frame_defaults(AVFrame pic);
	public AVFrame avcodec_alloc_frame();

//	int avcodec_default_get_buffer(AVCodecContext *s, AVFrame *pic);
//	void avcodec_default_release_buffer(AVCodecContext *s, AVFrame *pic);
//	int avcodec_default_reget_buffer(AVCodecContext *s, AVFrame *pic);
//	void avcodec_align_dimensions(AVCodecContext *s, int *width, int *height);
	public int avcodec_default_get_buffer(AVCodecContext s, AVFrame pic);
	public void avcodec_default_release_buffer(AVCodecContext s, AVFrame pic);
	public int avcodec_default_reget_buffer(AVCodecContext s, AVFrame pic);
	public void avcodec_align_dimensions(AVCodecContext s, IntByReference width, IntByReference height);

//	int avcodec_check_dimensions(void *av_log_ctx, unsigned int w, unsigned int h);
//	enum PixelFormat avcodec_default_get_format(struct AVCodecContext *s, const enum PixelFormat * fmt);
	public int avcodec_check_dimensions(Pointer av_log_ctx, int w, int h);
	public int avcodec_default_get_format(AVCodecContext s, IntByReference fmt);

	
//	int avcodec_thread_init(AVCodecContext *s, int thread_count);
//	void avcodec_thread_free(AVCodecContext *s);
//	int avcodec_thread_execute(AVCodecContext *s, int (*func)(AVCodecContext *c2, void *arg2),void **arg, int *ret, int count);
//	int avcodec_default_execute(AVCodecContext *c, int (*func)(AVCodecContext *c2, void *arg2),void **arg, int *ret, int count);
	public int avcodec_thread_init(AVCodecContext s, int thread_count);
	public void avcodec_thread_free(AVCodecContext s);
	public int avcodec_thread_execute(AVCodecContext s, Pointer func, PointerByReference arg, IntByReference ret, int count);
	public int avcodec_default_execute(AVCodecContext c, Pointer func, PointerByReference arg, IntByReference ret, int count);

	//int avcodec_open(AVCodecContext *avctx, AVCodec *codec);
	public int avcodec_open(AVCodecContext avctx, AVCodec codec);
	
//	int avcodec_decode_audio2(AVCodecContext *avctx, int16_t *samples,
//            int *frame_size_ptr,
//            uint8_t *buf, int buf_size);
	/**
	 * Decodes an audio frame from \p buf into \p samples.
	 * The avcodec_decode_audio2() function decodes an audio frame from the input
	 * buffer \p buf of size \p buf_size. To decode it, it makes use of the
	 * audio codec which was coupled with \p avctx using avcodec_open(). The
	 * resulting decoded frame is stored in output buffer \p samples.  If no frame
	 * could be decompressed, \p frame_size_ptr is zero. Otherwise, it is the
	 * decompressed frame size in \e bytes.
	 *
	 * @warning You \e must set \p frame_size_ptr to the allocated size of the
	 * output buffer before calling avcodec_decode_audio2().
	 *
	 * @warning The input buffer must be \c FF_INPUT_BUFFER_PADDING_SIZE larger than
	 * the actual read bytes because some optimized bitstream readers read 32 or 64
	 * bits at once and could read over the end.
	 *
	 * @warning The end of the input buffer \p buf should be set to 0 to ensure that
	 * no overreading happens for damaged MPEG streams.
	 *
	 * @note You might have to align the input buffer \p buf and output buffer \p
	 * samples. The alignment requirements depend on the CPU: On some CPUs it isn't
	 * necessary at all, on others it won't work at all if not aligned and on others
	 * it will work but it will have an impact on performance. In practice, the
	 * bitstream should have 4 byte alignment at minimum and all sample data should
	 * be 16 byte aligned unless the CPU doesn't need it (AltiVec and SSE do). If
	 * the linesize is not a multiple of 16 then there's no sense in aligning the
	 * start of the buffer to 16.
	 *
	 * @param avctx the codec context
	 * @param[out] samples the output buffer
	 * @param[in,out] frame_size_ptr the output buffer size in bytes
	 * @param[in] buf the input buffer
	 * @param[in] buf_size the input buffer size in bytes
	 * @return On error a negative value is returned, otherwise the number of bytes
	 * used or zero if no frame could be decompressed.
	 */	
	public int avcodec_decode_audio2(AVCodecContext avctx, Pointer samples,
            IntByReference frame_size_ptr,
            Pointer buf, int buf_size);
//	
//	int avcodec_decode_video(AVCodecContext *avctx, AVFrame *picture,
//            int *got_picture_ptr,
//            uint8_t *buf, int buf_size);
	public int avcodec_decode_video(AVCodecContext avctx, AVFrame picture,
			IntByReference got_picture_ptr,
            Pointer buf, int buf_size);
	
	
//	int avcodec_decode_subtitle(AVCodecContext *avctx, AVSubtitle *sub,
//            int *got_sub_ptr,
//            const uint8_t *buf, int buf_size);
//	int avcodec_parse_frame(AVCodecContext *avctx, uint8_t **pdata,
//        int *data_size_ptr,
//        uint8_t *buf, int buf_size);
	public int avcodec_decode_subtitle(AVCodecContext avctx, AVSubtitle sub,
			IntByReference got_sub_ptr,
            Pointer buf, int buf_size);
	public int avcodec_parse_frame(AVCodecContext avctx, PointerByReference pdata,
	        IntByReference data_size_ptr,
	        Pointer buf, int buf_size);
//	int avcodec_encode_audio(AVCodecContext *avctx, uint8_t *buf, int buf_size,
//            const short *samples);
	public int avcodec_encode_audio(AVCodecContext avctx, Pointer buf, int buf_size,
            Pointer samples);
	
	
	
//	int avcodec_encode_video(AVCodecContext *avctx, uint8_t *buf, int buf_size,
//            const AVFrame *pict);
//	int avcodec_encode_subtitle(AVCodecContext *avctx, uint8_t *buf, int buf_size,
//	               const AVSubtitle *sub);
//	
//	int avcodec_close(AVCodecContext *avctx);
//	
//	void avcodec_register_all(void);
	public int avcodec_encode_video(AVCodecContext avctx, Pointer buf, int buf_size,
			AVFrame pict);
	public int avcodec_encode_subtitle(AVCodecContext avctx, Pointer buf, int buf_size,
			AVSubtitle sub);
	
	public int avcodec_close(AVCodecContext avctx);
	
	public void avcodec_register_all();
	
	
//	void avcodec_flush_buffers(AVCodecContext *avctx);
//
//	void avcodec_default_free_buffers(AVCodecContext *s);
	public void avcodec_flush_buffers(AVCodecContext avctx);

	public void avcodec_default_free_buffers(AVCodecContext s);
	
	//char av_get_pict_type_char(int pict_type);
	public byte av_get_pict_type_char(int pict_type);
	
	//int av_get_bits_per_sample(enum CodecID codec_id);
	public int av_get_bits_per_sample(int codec_id);
	
	
//	/* frame parsing */
//	typedef struct AVCodecParserContext {
//	    void *priv_data;
//	    struct AVCodecParser *parser;
//	    int64_t frame_offset; /* offset of the current frame */
//	    int64_t cur_offset; /* current offset
//	                           (incremented by each av_parser_parse()) */
//	    int64_t last_frame_offset; /* offset of the last frame */
//	    /* video info */
//	    int pict_type; /* XXX: Put it back in AVCodecContext. */
//	    int repeat_pict; /* XXX: Put it back in AVCodecContext. */
//	    int64_t pts;     /* pts of the current frame */
//	    int64_t dts;     /* dts of the current frame */
//
//	    /* private data */
//	    int64_t last_pts;
//	    int64_t last_dts;
//	    int fetch_timestamp;
//
//	#define AV_PARSER_PTS_NB 4
//	    int cur_frame_start_index;
//	    int64_t cur_frame_offset[AV_PARSER_PTS_NB];
//	    int64_t cur_frame_pts[AV_PARSER_PTS_NB];
//	    int64_t cur_frame_dts[AV_PARSER_PTS_NB];
//
//	    int flags;
//	#define PARSER_FLAG_COMPLETE_FRAMES           0x0001
//
//	    int64_t offset;      ///< byte offset from starting packet start
//	    int64_t last_offset;
//	} AVCodecParserContext;
	
	/* frame parsing */
	public static class AVCodecParserContext extends Structure 
	{
	    public Pointer priv_data;
	    public Pointer parser;
	    public long frame_offset; /* offset of the current frame */
	    public long cur_offset; /* current offset
	                           (incremented by each av_parser_parse()) */
	    public long last_frame_offset; /* offset of the last frame */
	    /* video info */
	    public int pict_type; /* XXX: Put it back in AVCodecContext. */
	    public int repeat_pict; /* XXX: Put it back in AVCodecContext. */
	    public long pts;     /* pts of the current frame */
	    public long dts;     /* dts of the current frame */

	    /* private data */
	    public long last_pts;
	    public long last_dts;
	    public int fetch_timestamp;

	    	public static final int AV_PARSER_PTS_NB =4;
	    public int cur_frame_start_index;
		public long[] cur_frame_offset = new long[AV_PARSER_PTS_NB];
		public long[] cur_frame_pts = new long[AV_PARSER_PTS_NB];
		public long[] cur_frame_dts = new long[AV_PARSER_PTS_NB];

		public int flags;
	    	public static final int PARSER_FLAG_COMPLETE_FRAMES           =0x0001;

    	public long offset;      ///< byte offset from starting packet start
    	public long last_offset;
	} 
	
	
//	typedef struct AVCodecParser {
//	    int codec_ids[5]; /* several codec IDs are permitted */
//	    int priv_data_size;
//	    int (*parser_init)(AVCodecParserContext *s);
//	    int (*parser_parse)(AVCodecParserContext *s,
//	                        AVCodecContext *avctx,
//	                        const uint8_t **poutbuf, int *poutbuf_size,
//	                        const uint8_t *buf, int buf_size);
//	    void (*parser_close)(AVCodecParserContext *s);
//	    int (*split)(AVCodecContext *avctx, const uint8_t *buf, int buf_size);
//	    struct AVCodecParser *next;
//	} AVCodecParser;
	
	public static class AVCodecParser extends Structure 
	{
	    public int[] codec_ids = new int[5]; /* several codec IDs are permitted */
	    public int priv_data_size;
	    public Pointer parser_init;
	    public Pointer parser_parse;
	    public Pointer parser_close;
	    public Pointer split;
	    public Pointer next;
	}
	
	//extern AVCodecParser *av_first_parser;
	// TODO: no way to access global variables with JNA
	
//	void av_register_codec_parser(AVCodecParser *parser);
//	AVCodecParserContext *av_parser_init(int codec_id);
//	int av_parser_parse(AVCodecParserContext *s,
//	                    AVCodecContext *avctx,
//	                    uint8_t **poutbuf, int *poutbuf_size,
//	                    const uint8_t *buf, int buf_size,
//	                    int64_t pts, int64_t dts);
//	int av_parser_change(AVCodecParserContext *s,
//	                     AVCodecContext *avctx,
//	                     uint8_t **poutbuf, int *poutbuf_size,
//	                     const uint8_t *buf, int buf_size, int keyframe);
//	void av_parser_close(AVCodecParserContext *s);
	
	public void av_register_codec_parser(AVCodecParser parser);
	public AVCodecParser av_parser_init(int codec_id);
	public int av_parser_parse(AVCodecParserContext s,
						AVCodecContext avctx,
	                    PointerByReference poutbuf, IntByReference poutbuf_size,
	                    Pointer buf, int buf_size,
	                    long pts, long dts);
	public int av_parser_change(AVCodecParserContext  s,
						 AVCodecContext avctx,
	                     PointerByReference poutbuf, IntByReference poutbuf_size,
	                     Pointer buf, int buf_size, int keyframe);
	public void av_parser_close(AVCodecParserContext s);
	
//	extern AVCodecParser aac_parser;
//	extern AVCodecParser ac3_parser;
//	extern AVCodecParser cavsvideo_parser;
//	extern AVCodecParser dca_parser;
//	extern AVCodecParser dvbsub_parser;
//	extern AVCodecParser dvdsub_parser;
//	extern AVCodecParser h261_parser;
//	extern AVCodecParser h263_parser;
//	extern AVCodecParser h264_parser;
//	extern AVCodecParser mjpeg_parser;
//	extern AVCodecParser mpeg4video_parser;
//	extern AVCodecParser mpegaudio_parser;
//	extern AVCodecParser mpegvideo_parser;
//	extern AVCodecParser pnm_parser;
//	extern AVCodecParser vc1_parser;
	
	// TODO: no way to access global variables with JNA
	
//	typedef struct AVBitStreamFilterContext {
//	    void *priv_data;
//	    struct AVBitStreamFilter *filter;
//	    AVCodecParserContext *parser;
//	    struct AVBitStreamFilterContext *next;
//	} AVBitStreamFilterContext;
	
	public static class AVBitStreamFilterContext extends Structure
	{
	    public AVBitStreamFilterContext(Pointer p) {
	    	super();
			useMemory(p);
			read();
		}		
		public Pointer priv_data;
	    public Pointer filter;
	    public Pointer parser;
	    public Pointer next;
	}
	
	
//	typedef struct AVBitStreamFilter {
//	    const char *name;
//	    int priv_data_size;
//	    int (*filter)(AVBitStreamFilterContext *bsfc,
//	                  AVCodecContext *avctx, const char *args,
//	                  uint8_t **poutbuf, int *poutbuf_size,
//	                  const uint8_t *buf, int buf_size, int keyframe);
//	    struct AVBitStreamFilter *next;
//	} AVBitStreamFilter;
	
	
	public static class AVBitStreamFilter extends Structure
	{
		public Pointer name;
	    public int priv_data_size;
	    public Pointer filter;
	    public Pointer next;
	};
	
	//extern AVBitStreamFilter *av_first_bitstream_filter;
	// TODO: no way to access global variables with JNA
	
	
//	void av_register_bitstream_filter(AVBitStreamFilter *bsf);
//	AVBitStreamFilterContext *av_bitstream_filter_init(const char *name);
//	int av_bitstream_filter_filter(AVBitStreamFilterContext *bsfc,
//	                               AVCodecContext *avctx, const char *args,
//	                               uint8_t **poutbuf, int *poutbuf_size,
//	                               const uint8_t *buf, int buf_size, int keyframe);
//	void av_bitstream_filter_close(AVBitStreamFilterContext *bsf);
	public void av_register_bitstream_filter(AVBitStreamFilter bsf);
	public AVBitStreamFilterContext av_bitstream_filter_init(String name);
	public int av_bitstream_filter_filter(AVBitStreamFilterContext bsfc,
								   AVCodecContext avctx, String args,
	                               PointerByReference poutbuf, IntByReference poutbuf_size,
	                               Pointer buf, int buf_size, int keyframe);
	public void av_bitstream_filter_close(AVBitStreamFilterContext bsf);
	
	
//	extern AVBitStreamFilter dump_extradata_bsf;
//	extern AVBitStreamFilter remove_extradata_bsf;
//	extern AVBitStreamFilter noise_bsf;
//	extern AVBitStreamFilter mp3_header_compress_bsf;
//	extern AVBitStreamFilter mp3_header_decompress_bsf;
//	extern AVBitStreamFilter mjpega_dump_header_bsf;
//	extern AVBitStreamFilter imx_dump_header_bsf;
	// TODO: no way to access global variables with JNA
	
	
	
	/* memory */
	//void *av_fast_realloc(void *ptr, unsigned int *size, unsigned int min_size);
	public Pointer av_fast_realloc(Pointer ptr, IntByReference size, int min_size);
	
	
	//attribute_deprecated void av_free_static(void);
	/** @deprecated */
	public void av_free_static();
	
	//attribute_deprecated void *av_mallocz_static(unsigned int size);
	/** @deprecated */
	public Pointer av_mallocz_static(int size);
	
//	void av_picture_copy(AVPicture *dst, const AVPicture *src,
//            int pix_fmt, int width, int height);
	public void av_picture_copy(AVPicture dst, AVPicture src,
            int pix_fmt, int width, int height);
	
//	int av_picture_crop(AVPicture *dst, const AVPicture *src,
//            int pix_fmt, int top_band, int left_band);
	public int av_picture_crop(AVPicture dst, AVPicture src,
            int pix_fmt, int top_band, int left_band);

//	int av_picture_pad(AVPicture *dst, const AVPicture *src, int height, int width, int pix_fmt,
//            int padtop, int padbottom, int padleft, int padright, int *color);
	public int av_picture_pad(AVPicture dst, AVPicture src, int height, int width, int pix_fmt,
            int padtop, int padbottom, int padleft, int padright, IntByReference color);

	
	
//	#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
//	/**
//	 * @deprecated Use the software scaler (swscale) instead.
//	 */
//	attribute_deprecated void img_copy(AVPicture *dst, const AVPicture *src,
//	              int pix_fmt, int width, int height);
//
//	/**
//	 * @deprecated Use the software scaler (swscale) instead.
//	 */
//	attribute_deprecated int img_crop(AVPicture *dst, const AVPicture *src,
//	             int pix_fmt, int top_band, int left_band);
//
//	/**
//	 * @deprecated Use the software scaler (swscale) instead.
//	 */
//	attribute_deprecated int img_pad(AVPicture *dst, const AVPicture *src, int height, int width, int pix_fmt,
//	            int padtop, int padbottom, int padleft, int padright, int *color);
//	#endif
	
	//#if LIBAVCODEC_VERSION_INT < ((52<<16)+(0<<8)+0)
	/**
	 * @deprecated Use the software scaler (swscale) instead.
	 */
	public void img_copy(AVPicture dst, AVPicture src,
	              int pix_fmt, int width, int height);

	/**
	 * @deprecated Use the software scaler (swscale) instead.
	 */
	public int img_crop(AVPicture dst, AVPicture src,
	             int pix_fmt, int top_band, int left_band);

	/**
	 * @deprecated Use the software scaler (swscale) instead.
	 */
	public int img_pad(AVPicture dst, AVPicture src, int height, int width, int pix_fmt,
	            int padtop, int padbottom, int padleft, int padright, IntByReference color);
	//#endif
	
	//extern unsigned int av_xiphlacing(unsigned char *s, unsigned int v);
	public int av_xiphlacing(Pointer s, int v);
	
	//int av_parse_video_frame_size(int *width_ptr, int *height_ptr, const char *str);
	public int av_parse_video_frame_size(IntByReference width_ptr, IntByReference height_ptr, String str);
	
	//int av_parse_video_frame_rate(AVRational *frame_rate, const char *str);
	public int av_parse_video_frame_rate(AVRational frame_rate, String str);
	
	
//	/* error handling */
//	#if EINVAL > 0
//	#define AVERROR(e) (-(e)) /**< Returns a negative error code from a POSIX error code, to return from library functions. */
//	#define AVUNERROR(e) (-(e)) /**< Returns a POSIX error code from a library function error return value. */
//	#else
//	/* Some platforms have E* and errno already negated. */
//	#define AVERROR(e) (e)
//	#define AVUNERROR(e) (e)
//	#endif
//	#define AVERROR_UNKNOWN     AVERROR(EINVAL)  /**< unknown error */
//	#define AVERROR_IO          AVERROR(EIO)     /**< I/O error */
//	#define AVERROR_NUMEXPECTED AVERROR(EDOM)    /**< Number syntax expected in filename. */
//	#define AVERROR_INVALIDDATA AVERROR(EINVAL)  /**< invalid data found */
//	#define AVERROR_NOMEM       AVERROR(ENOMEM)  /**< not enough memory */
//	#define AVERROR_NOFMT       AVERROR(EILSEQ)  /**< unknown format */
//	#define AVERROR_NOTSUPP     AVERROR(ENOSYS)  /**< Operation not supported. */
//	#define AVERROR_NOENT       AVERROR(ENOENT)  /**< No such file or directory. */
	
	// TODO: not sure how best to define these.

// end avcodec.h
//------------------------------------------------------------------------------------------------------------------------


//------------------------------------------------------------------------------------------------------------------------
// libavcodec/opt.h


	//enum AVOptionType:
    public static final int FF_OPT_TYPE_FLAGS = 0;
    public static final int FF_OPT_TYPE_INT = 1;
    public static final int FF_OPT_TYPE_INT64 = 2;
    public static final int FF_OPT_TYPE_DOUBLE = 3;
    public static final int FF_OPT_TYPE_FLOAT = 4;
    public static final int FF_OPT_TYPE_STRING = 5;
    public static final int FF_OPT_TYPE_RATIONAL = 6;
    public static final int FF_OPT_TYPE_CONST=128;


	//  typedef struct AVOption {
	//  const char *name;
	//
	//  /**
	//   * short English text help.
	//   * @todo what about other languages
	//   */
	//  const char *help;
	//  int offset;             ///< offset to context structure where the parsed value should be stored
	//  enum AVOptionType type;
	//
	//  double default_val;
	//  double min;
	//  double max;
	//
	//  int flags;
	//#define AV_OPT_FLAG_ENCODING_PARAM  1   ///< a generic parameter which can be set by the user for muxing or encoding
	//#define AV_OPT_FLAG_DECODING_PARAM  2   ///< a generic parameter which can be set by the user for demuxing or decoding
	//#define AV_OPT_FLAG_METADATA        4   ///< some data extracted or inserted into the file like title, comment, ...
	//#define AV_OPT_FLAG_AUDIO_PARAM     8
	//#define AV_OPT_FLAG_VIDEO_PARAM     16
	//#define AV_OPT_FLAG_SUBTITLE_PARAM  32
	////FIXME think about enc-audio, ... style flags
	//  const char *unit;
	//} AVOption;
	
	public static class AVOption extends Structure
	{
		public AVOption()
		{
			super();
		}
		
		public AVOption(Pointer p)
		{	super();
			useMemory(p);
			read();
		}

		public String name;
		public String help;
		public int offset; 
		public int type;
		public double default_val;
		public double min;
		public double max;
		public int flags;
		
			public static final int AV_OPT_FLAG_ENCODING_PARAM  =1;   ///< a generic parameter which can be set by the user for muxing or encoding
			public static final int AV_OPT_FLAG_DECODING_PARAM  =2;   ///< a generic parameter which can be set by the user for demuxing or decoding
			public static final int AV_OPT_FLAG_METADATA        =4;   ///< some data extracted or inserted into the file like title, comment, ...
			public static final int AV_OPT_FLAG_AUDIO_PARAM     =8;
			public static final int AV_OPT_FLAG_VIDEO_PARAM     =16;
			public static final int AV_OPT_FLAG_SUBTITLE_PARAM  =32;
		
		public String unit;
	}	

//	const AVOption *av_find_opt(void *obj, const char *name, const char *unit, int mask, int flags);
//	const AVOption *av_set_string(void *obj, const char *name, const char *val);
//	const AVOption *av_set_double(void *obj, const char *name, double n);
//	const AVOption *av_set_q(void *obj, const char *name, AVRational n);
//	const AVOption *av_set_int(void *obj, const char *name, int64_t n);
//	double av_get_double(void *obj, const char *name, const AVOption **o_out);
//	AVRational av_get_q(void *obj, const char *name, const AVOption **o_out);
//	int64_t av_get_int(void *obj, const char *name, const AVOption **o_out);
//	const char *av_get_string(void *obj, const char *name, const AVOption **o_out, char *buf, int buf_len);
//	const AVOption *av_next_option(void *obj, const AVOption *last);
//	int av_opt_show(void *obj, void *av_log_obj);
//	void av_opt_set_defaults(void *s);
//	void av_opt_set_defaults2(void *s, int mask, int flags);
	public AVOption av_find_opt(Pointer obj, String name, String unit, int mask, int flags);
	public AVOption av_set_string(Pointer obj, String name, Pointer str);
	public AVOption av_set_double(Pointer obj, String name, double n);
	public AVOption av_set_q(Pointer obj, String name, AVRational n);	// TODO: this appears to be by value, will JNA map correctly?
	public AVOption av_set_int(Pointer obj, String name, long n);
	public double av_get_double(Pointer obj, String name, PointerByReference o_out);
	public AVRational av_get_q(Pointer obj, String name, PointerByReference o_out);
	public long av_get_int(Pointer obj, String name, PointerByReference o_out);
	public String av_get_string(Pointer obj, String name, PointerByReference o_out, Pointer buf, int buf_len);
	public AVOption av_next_option(Pointer obj, AVOption last);
	public int av_opt_show(Pointer obj, Pointer av_log_obj);
	public void av_opt_set_defaults(Pointer s);
	public void av_opt_set_defaults2(Pointer s, int mask, int flags);

// end libavcodec/opt.h
//------------------------------------------------------------------------------------------------------------------------

}

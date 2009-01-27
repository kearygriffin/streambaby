package net.sf.ffmpeg_java;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface SWScaleLibrary extends Library {
	// Make sure the library is inited BEFORE we set the INSTANCE variable
	public static final int avCodecLibVer = FFmpegMgr.getAvCodecVersion();

    public static final SWScaleLibrary INSTANCE = (SWScaleLibrary) Native.loadLibrary(
    		System.getProperty("swscale.lib",
    	    		System.getProperty("os.name").startsWith("Windows") ? "swscale-0" : "swscale"), 
    		SWScaleLibrary.class);
    
    public static final int LIBSWSCALE_VERSION_INT = ((0<<16)+(6<<8)+1);	// version this comes from
    public static final String LIBSWSCALE_VERSION  = "0.6.1";
    public static final int LIBSWSCALE_BUILD = LIBSWSCALE_VERSION_INT;

    public static final String LIBAVCODEC_IDENT = "SwS" + LIBSWSCALE_VERSION;
    
    public int swscale_version();

    /* values for the flags, the stuff on the command line is different */
    /*
    #define SWS_FAST_BILINEAR     1
    #define SWS_BILINEAR          2
    #define SWS_BICUBIC           4
    #define SWS_X                 8
    #define SWS_POINT          0x10
    #define SWS_AREA           0x20
    #define SWS_BICUBLIN       0x40
    #define SWS_GAUSS          0x80
    #define SWS_SINC          0x100
    #define SWS_LANCZOS       0x200
    #define SWS_SPLINE        0x400

    #define SWS_SRC_V_CHR_DROP_MASK     0x30000
    #define SWS_SRC_V_CHR_DROP_SHIFT    16

    #define SWS_PARAM_DEFAULT           123456

    #define SWS_PRINT_INFO              0x1000

    //the following 3 flags are not completely implemented
    //internal chrominace subsampling info
    #define SWS_FULL_CHR_H_INT    0x2000
    //input subsampling info
    #define SWS_FULL_CHR_H_INP    0x4000
    #define SWS_DIRECT_BGR        0x8000
    #define SWS_ACCURATE_RND      0x40000
    #define SWS_BITEXACT          0x80000

    #define SWS_CPU_CAPS_MMX      0x80000000
    #define SWS_CPU_CAPS_MMX2     0x20000000
    #define SWS_CPU_CAPS_3DNOW    0x40000000
    #define SWS_CPU_CAPS_ALTIVEC  0x10000000
    #define SWS_CPU_CAPS_BFIN     0x01000000

    #define SWS_MAX_REDUCE_CUTOFF 0.002

    #define SWS_CS_ITU709         1
    #define SWS_CS_FCC            4
    #define SWS_CS_ITU601         5
    #define SWS_CS_ITU624         5
    #define SWS_CS_SMPTE170M      5
    #define SWS_CS_SMPTE240M      7
    #define SWS_CS_DEFAULT        5
*/
    public static final int SWS_FAST_BILINEAR    = 1;
    public static final int SWS_BILINEAR =         2;
    public static final int SWS_BICUBIC   =        4;
    public static final int SWS_X          =       8;
    public static final int SWS_POINT       =   0x10;
    public static final int SWS_AREA         =  0x20;
    public static final int SWS_BICUBLIN   =    0x40;
    public static final int SWS_GAUSS       =   0x80;
    public static final int SWS_SINC      =    0x100;
    public static final int SWS_LANCZOS    =   0x200;
    public static final int SWS_SPLINE      =  0x400;

    public static final int SWS_SRC_V_CHR_DROP_MASK   =  0x30000;
    public static final int SWS_SRC_V_CHR_DROP_SHIFT =   16;

    public static final int SWS_PARAM_DEFAULT    =       123456;

    public static final int SWS_PRINT_INFO      =        0x1000;

    //the following 3 flags are not completely implemented
    //internal chrominace subsampling info
    public static final int SWS_FULL_CHR_H_INT  =  0x2000;
    //input subsampling info
    public static final int SWS_FULL_CHR_H_INP  =  0x4000;
    public static final int SWS_DIRECT_BGR     =   0x8000;
    public static final int SWS_ACCURATE_RND   =   0x40000;
    public static final int SWS_BITEXACT       =   0x80000;

    public static final int SWS_CPU_CAPS_MMX    =  0x80000000;
    public static final int SWS_CPU_CAPS_MMX2   =  0x20000000;
    public static final int SWS_CPU_CAPS_3DNOW  =  0x40000000;
    public static final int SWS_CPU_CAPS_ALTIVEC = 0x10000000;
    public static final int SWS_CPU_CAPS_BFIN =    0x01000000;

    public static final double SWS_MAX_REDUCE_CUTOFF = 0.002;

    public static final int SWS_CS_ITU709       =  1;
    public static final int SWS_CS_FCC          =  4;
    public static final int SWS_CS_ITU601       =  5;
    public static final int SWS_CS_ITU624      =   5;
    public static final int SWS_CS_SMPTE170M   =   5;
    public static final int SWS_CS_SMPTE240M   =   7;
    public static final int SWS_CS_DEFAULT     =   5;


    // when used for filters they must have an odd number of elements
    // coeffs cannot be shared between vectors
    /*
    typedef struct {
        double *coeff;
        int length;
    } SwsVector; */
    public static class SwsVector extends Structure {
    	Pointer coeff;   // Pointer to a double
    	int length;
    }

    // vectors can be shared
    /*
    typedef struct {
        SwsVector *lumH;
        SwsVector *lumV;
        SwsVector *chrH;
        SwsVector *chrV;
    } SwsFilter;
    */
    public static class SwsFilter extends Structure {
    	Pointer lumH;
    	Pointer lumV;
    	Pointer chrH;
    	Pointer chrV;
    }

    //struct SwsContext;

    public void sws_freeContext(Pointer swsContext);

    //struct SwsContext *sws_getContext(int srcW, int srcH, int srcFormat, int dstW, int dstH, int dstFormat, int flags,
      //                                SwsFilter *srcFilter, SwsFilter *dstFilter, double *param);
    public Pointer sws_getContext(int srcW, int srcH, int srcFormat, int dstW, int dstH, int dstFormat, int flags,
                                    SwsFilter srcFilter, SwsFilter dstFilter, Pointer param);
    
//    int sws_scale(struct SwsContext *context, uint8_t* src[], int srcStride[], int srcSliceY,
  //                int srcSliceH, uint8_t* dst[], int dstStride[]);
    public int sws_scale(Pointer context, Pointer src[], int srcStride[], int srcSliceY,
            int srcSliceH, Pointer dst[], int dstStride[]);

//    int sws_scale_ordered(struct SwsContext *context, uint8_t* src[], int srcStride[], int srcSliceY,
  //
  public int sws_scale_ordered(Pointer context, Pointer src[], int srcStride[], int srcSliceY,
            int srcSliceH, Pointer dst[], int dstStride[]) ; //attribute_deprecated;

//    int sws_setColorspaceDetails(struct SwsContext *c, const int inv_table[4], int srcRange, const int table[4], int dstRange, int brightness, int contrast, int saturation);
  public int sws_setColorspaceDetails(Pointer c, int inv_table[], int srcRange, int table[], int dstRange, int brightness, int contrast, int saturation);

//    int sws_getColorspaceDetails(struct SwsContext *c, int **inv_table, int *srcRange, int **table, int *dstRange, int *brightness, int *contrast, int *saturation);
  // FIXME:  translate to better JNA
  public int sws_getColorspaceDetails(Pointer c, Pointer inv_table, Pointer srcRange, Pointer table, Pointer dstRange, Pointer brightness, Pointer contrast, Pointer saturation);

    /*
    SwsVector *sws_getGaussianVec(double variance, double quality);
    SwsVector *sws_getConstVec(double c, int length);
    SwsVector *sws_getIdentityVec(void);
	*/
  public Pointer sws_getGaussianVec(double variance, double quality);
  public Pointer sws_getConstVec(double c, int length);
  public Pointer sws_getIdentityVec();
    
    /*
    void sws_scaleVec(SwsVector *a, double scalar);
    void sws_normalizeVec(SwsVector *a, double height);
    void sws_convVec(SwsVector *a, SwsVector *b);
    void sws_addVec(SwsVector *a, SwsVector *b);
    void sws_subVec(SwsVector *a, SwsVector *b);
    void sws_shiftVec(SwsVector *a, int shift);
    SwsVector *sws_cloneVec(SwsVector *a);

    void sws_printVec(SwsVector *a);
    void sws_freeVec(SwsVector *a);
	*/
  public void sws_scaleVec(SwsVector a, double scalar);
  public void sws_normalizeVec(SwsVector a, double height);
  public void sws_convVec(SwsVector a, SwsVector b);
  public void sws_addVec(SwsVector a, SwsVector b);
  public void sws_subVec(SwsVector a, SwsVector b);
  public void sws_shiftVec(SwsVector a, int shift);
  public Pointer sws_cloneVec(SwsVector a);

    public void sws_printVec(SwsVector a);
    public void sws_freeVec(SwsVector a);
    
    /*
    SwsFilter *sws_getDefaultFilter(float lumaGBlur, float chromaGBlur,
                                    float lumaSarpen, float chromaSharpen,
                                    float chromaHShift, float chromaVShift,
                                    int verbose);
    void sws_freeFilter(SwsFilter *filter);

    struct SwsContext *sws_getCachedContext(struct SwsContext *context,
                                            int srcW, int srcH, int srcFormat,
                                            int dstW, int dstH, int dstFormat, int flags,
                                            SwsFilter *srcFilter, SwsFilter *dstFilter, double *param);
    
    */
    public Pointer sws_getDefaultFilter(float lumaGBlur, float chromaGBlur,
            float lumaSarpen, float chromaSharpen,
            float chromaHShift, float chromaVShift,
            int verbose);
    public void sws_freeFilter(SwsFilter filter);

    public Pointer sws_getCachedContext(Pointer context,
                    int srcW, int srcH, int srcFormat,
                    int dstW, int dstH, int dstFormat, int flags,
                    SwsFilter srcFilter, SwsFilter dstFilter, Pointer param);

}

package net.sf.ffmpeg_java.bcel;

//import java.util.Locale;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
public class FFmpegFixup {
    private JavaClass _clazz;
    private ClassGen _cg;



    //private int ver;
    private int codecVer;
    private int formatVer;
    public FFmpegFixup(JavaClass clazz, int codecVer, int formatVer) {
    	this.codecVer = codecVer;
    	this.formatVer = formatVer;
        _clazz = clazz;
    }


    /** Start Java code generation
     */
	private static String lookFor = "net/sf/ffmpeg_java/v";

    public JavaClass create() {
    	_cg = new ClassGen(_clazz);
    	
    	// This should be a lot smarter
    	// Currently just looks at all constants, and replaces all occurences of
    	// of net/sf/ffmpeg_java/v??/ with the correct version number.
    	// Should only do this for actual class refeerences, not strings, etc...
    	ConstantPoolGen cp = _cg.getConstantPool();
    	for (int i=0;i<cp.getSize();i++) {
    		Constant c = cp.getConstant(i);
    		//System.err.println("Constant:" + c);
    		if (c instanceof ConstantUtf8) {
    			ConstantUtf8 utf = (ConstantUtf8)c;
    			String str = utf.getBytes();
    			int prevIndex = 0;
    			int index;
    			while ((index= str.indexOf(lookFor, prevIndex)) >= 0) {
    				prevIndex = index+1;
    				index += lookFor.length();
	    			String sub1 = safe_substring(str, index, index + 2);
	    			String sub2 = safe_substring(str, index+2, index+2+1);
	    			if (sub2.equals("/")) {
	    				int thisver = -1;
	    				int ver;
	    				String type = safe_substring(str, index+3, index+3+1);
	    				if (type.startsWith("AVCodecLibrary"))
	    					ver = codecVer;
	    				else
	    					ver = formatVer;
	    				try {
	    					thisver = Integer.parseInt(sub1);
	    				} catch(NumberFormatException e) {   }
	    				if (thisver != -1 && thisver != ver) {
	    					String beg = safe_substring(str, 0, index);
	    					String end = safe_substring(str, index+3, str.length());
	    					String newStr = beg  + ver + "/" + end;
	    					str = newStr;
	    					utf.setBytes(str);
	    				}
	    			}
    			}
    		}
    	}

    	return _cg.getJavaClass();
        //_out.flush();
    }

    public static String safe_substring(String str, int beg, int end) {
    	if (beg > str.length())
    		return "";
    	if (end > str.length())
    		end = str.length();
    	return str.substring(beg, end);
    }


}

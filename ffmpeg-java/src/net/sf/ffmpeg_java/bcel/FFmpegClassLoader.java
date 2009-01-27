package net.sf.ffmpeg_java.bcel;

import net.sf.ffmpeg_java.FFmpegMgr;

import org.apache.bcel.classfile.JavaClass;

@SuppressWarnings("unchecked")
public class FFmpegClassLoader extends org.apache.bcel.util.ClassLoader {
	String initClass;
	public FFmpegClassLoader(String class_name)  {
		this.initClass = class_name;
	}
	@Override 
	public Class loadClass(String class_name) throws ClassNotFoundException {
		return this.loadClass(class_name, true);
	}
	@Override
	protected Class loadClass(String class_name, boolean resolve) throws ClassNotFoundException {
		if (class_name.startsWith(initClass) || class_name.contains("FFmpeg$$")) {
		//if (class_name.indexOf("$$FFmpeg") >= 0) {
			return super.loadClass(class_name, resolve);
		} 
		else
			return this.getParent().loadClass(class_name);
	}
	
	@Override 
	protected JavaClass modifyClass(JavaClass clazz) {
        FFmpegFixup fixup = new FFmpegFixup(clazz, FFmpegMgr.getAvCodecVersion(), FFmpegMgr.getAvFormatVersion());
        JavaClass newClazz = fixup.create();
        return newClazz;
	}
}

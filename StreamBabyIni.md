# streambaby.ini options #

Note:  Unlike standard Java property files, streambaby.ini files support the use of the blackslash character without a prefix.  This means that windows directories can (and should) be specified as c:\my data\my videos and _not_ c:\\my data\\my videos.



[Settings/description for streambaby quality settings](QualitySettings.md)

### dir.x=yyyyyy ###
  * Specifies directories that StreamBaby will look for movies/videos in.
  * Example:
  * dir.1=/home/someone/videos
  * dir.1.name=!Myideos
  * dir.2=/usr/local/movies
  * dir.2.name=System Movies
  * Passwords can also be specified.  So to allow the password roger or tomcat to access dir.1, and anyone to access dir.2 (assuming password= is not set)
    * dir.1=/home/someone/videos
    * dir.1.name=MyVideos
    * dir.1.password=roger,tomcat
    * dir.2=/usr/local/movies
    * dir.2.name=System Movies
### tivo.username= (default:none) ###
### tivo.password= (default:none) ###
> > > allows TiVo to push videos to your TiVo's instead of streaming.
> > > Should be set to username+password of you tivo.com account
### password=  (default:none) ###
> > > default password list for directories (comma seperated)
### trimextensions=  (default:false) ###
> > > Trim extensions from filenames when displaying
### extensions=  (default:mp4,mpeg,vob,mpg,mpeg2,mp2,avi,wmv,asf) ###
> > > Extensions to scan for when listing files
### port=  (default:7290) ###
> > > HME port to attach to
### ip=  (default:none) ###
> > > IP address to bine HME to
### title=  (default:streambaby) ###
> > > Title to use for HME
### preview.quality=  (default:10) ###
> > > jpeg quality for preview (1-100, 1=lowest/100=highest)
### preview.big=  (default:false) ###
> > > use full screen for preview (otherwise it is thumbnailed like netfilix) thumbnail is default.
### mdns.disable=  (default:false) ###
> > > Disable mDNS
### mp4module.interleave=  (default:true) ###
> > > enable re-interleaving of mp4s
### lib.native=  (default:/home/keary/twork/streambaby/native) ###
> > > path to look for native libraries
### transcode.disable=  (default:false) ###
> > > disable transcoding for incompatible video streams
### tmp.path=  (default:OS standard tmp path) ###
> > > Temporary files path
### preview.disable=  (default:0) ###
        * 0=Don't disable
        * 1=Disable, but show window with time during ffwd/rewind in previewmode
        * 2=Disable completely, only move shuttle bar during preview
### preview.displaytime=  (default:true) ###
> > > Display the time over the preview image when ffwding/rwding
### preview.cache=  (default:cache) ###
> > > Directory to look for (and possibly store) static pvw files
### preview.autogenerate=  (default:true) ###
> > > Autogenerate static pvw files when file is played for first time
### autogenerate.continue=  (default:true) ###
> > > continue autogeneration of previews, even when movie stopped
### autogenerate.delete=  (default:false) ###
> > > deletes preview files when the original file can no longer be found
### player.pleasewait=  (default:true) ###
> > > Shows pleaseWait graphic when seeking.  If disabled shows text

## Video playback display related parameters ##
### display.timeout\_status\_bar=  (default:5) ###

> Number of seconds for status bar to timeout
### display.timeout\_icon=  (default:5) ###
> Number of seconds for icon display to timeout
### display.timeout\_info=  (default:10) ###
> Number of seconds for program information to timeout
### display.skip\_backwards=  (default:8) ###
> Number of seconds for skip backwards
### display.skip\_forwards=  (default:30) ###
> Number of seconds for skip forwards

## Video Module features/arguments enable/disable ##
Note:  Streambaby has the concept of video modules.  There are currently 4 video modules.
  * built-in mp4 streamer module
  * built-in mpeg streamer module
  * ffmpegjava module which uses the ffmpeg native integration to retrieve video information and generate real-time previews
  * ffmpegexe module which runs ffmpeg.exe as an external process for transcoding and generating pvw thumbnail files.
The options below specify various configuration settings for the various modules.
### ffmpeg.path=  (default:ffmpeg or native/ffmpeg.exe for win32) ###
> > path of ffmpeg
### mp4module.disable=  (default:false) ###
> > Disable built-in mp4 streaming module
### mpegmodule.disable=  (default:false) ###
> > Disable built-in mpeg streaming module
### mp4mod.fillvidinfo=  (default:true) ###
> > Allow mp4module to parse video informaton
### mp4mod.streamformats=  (default:MP4 files with h264/aac/ac3 format) ###
> > list of formats mp4module should attempt to stream
### mp4mod.streamformats.disallow=  (default:default) ###
> > list of formats mp4module should not attempt to stream
### mpegmod.fillvidinfo=  (default:true) ###
> > Allow mpegmodule to parse video informaton
### mpegmod.streamformats=  (default:`mpeg,mpv2,*`) ###
> > list of formats mpegmodule should attempt to stream
### mpegmod.streamformats.disallow=  (default:none) ###
> > list of formats mpegmodule should not attempt to stream
### ffmpegjava.previewformats=  (default:`*,*,*`) ###
> > list of formats the ffmpegjava (native ffmpeg libs) module should attempt to preview
### ffmpegjava.previewformats.disallow=  (default:`mpeg,*,*;*,none,*;mpeges,*,*`) ###
> > list of formats ffmpegjava (native ffmpeg libs) should not attempt to preview
### ffmpegexe.transcode.mime=  (default:video/mpeg) ###
> > mimetype to use when transcoding from ffmpeg
### ffmpegexe.preview=  (default:-r 1 -f mjpeg -v 0) ###
> > Arguments to use when generating previews from ffmpeg.exe (for pvw file generation)
### ffmpegexe.previewformats=  (default:all containers, all formats) ###
> > list of formats the ffmpegexe module should attempt to preview
### ffmpegexe.previewformats.disallow=  (default:`*,none,*`) ###
> > list of formats ffmpegexe should not attempt to preview
### ffmpegexe.transcodeformats=  (default:all) ###
> > list of formats the ffmpegexe module should attempt to transcode
### ffmpegexe.transcodeformats.disallow=  (default:`*,none,*;mpeges,*,*`) ###
> > list of formats ffmpegexe should not attempt transcode (defaults to any file with no video, or mpeges streams)

### ffmpegjava.mpegexactseek=  (default:false) ###

> when realtime previewing mpeg files, look for the exact frame to preview (can be slow)
### ffmpegjava.avutil=  (default:avutil) ###
> path of libavutil  to load
### ffmpegjava.avcodec=  (default:system default) ###
> path of libavcodec to load
### ffmpegjava.avformat=  (default:system default) ###
> path of libavformat to load
### ffmpegjava.swscale=  (default:system default) ###
> path of libswscale to load
### sockets.start=  (default:8500) ###
> Socket start for internal socket use
### sockets.count=  (default:500) ###
> Number of sockets to use for internal socket use

## How to specify video formats in the ini file ##
Note:  When specifying "formats', the format is specified as container,videocodec,audiocodec
  * To specify multiple format, seperate them with a semi-colon  A `*` is a wildcard
  * Possible containers:  mp4,avi,mkv,mpeg,mpegts,wmv,mpeges
  * Possible videocodecs: h264,mp2v,mp1v,vc1,none
  * Possible audiocodecs:  mp2,ac3,aac,mp3,wma2,none

# ffmpeg\_transcoding\_configuration #
Note:  The following "parameters" can be used within the ffmpegexe arguments:
  * closest.mpeg.fps= The closest frame rate of the original video to a legal mpeg frame rate
  * bitrate=The video bitrate to transcode to
  * abitrate=The audio bitrate to transcode to
  * xres=The x resolution to transcode to
  * yres=The y resolution to transcode to

### transcode.bufferlimit= (default:20) ###
> The maximum buffer size in GB allowed for transcoding videos or for videos whose file size is undetermined
### ffmpegexe.transcode=  (default:-acodec ac3 -vcodec mpeg2video -f vob -async 1 -r ${closest.mpeg.fps} -v 0) ###
> Arguments to use when transcoding from ffmpeg
### ffmpegexe.transcode.sameqargs (default: -qscale 0 -ab 192k) ###
> The arguments to add to the transcode line when transcoding to the same quality as the original.  This is also used if quality selection is turned off (assuming quality.default=same, which is also default)===
### ffmpegexe.transcode.qualargs (default: -bufsize 4096k -b ${bitrate}k -maxrate 8000k -ab ${abitrate}k -s ${xres}x${yres}) ###
> The arguments to add the transcode line when transcoding to a specific audio/video bitrate.



## Advanced Options ##
Options that may or may not have the desired effect, and are mainly for debugging/experimental.  Touching these may break everything.
### preview.threaded=  (default:true) ###
> Use threads to generate preview frames
### preview.predictive=  (default:false) ###
> Attempt to predict which frame will be previewed next, and pre-generate it
### ffmpegjava.mpegseek=  (default:false) ###
> use internal mpeg seeking code when real-time previewing mpeg videos, instead of ffmpegs seeking code
### autogenerate.background=  (default:0) ###
> minutes to check for files to autogenerate previews in the background 0=disabled.  If this is set, all movies will be scanned and those movies that are determined not to support real-time preview generation, thumbnail files will be generated.

## Closed Captioning Options ##

### cc.minchartime=50 ###
Minimum time to leave a CC on the screen (per character) in ms

### cc.mintime=1500 ###
Minimum time to leave a CC on the screen (absolute) in ms

### cc.fontsize=small ###
Size of font as either absolute integer, or medium, small, tiny

### cc.yoff=24 ###
Offset from bottom for CC

### cc.background.transparency=0.25 ###
CC background transparency (0=>off)

### cc.background=1 ###
CC background (1=>on 0=>off)

### cc.backgroundcolor=black ###
CC background color

### cc.textcolor=white ###
CC text color


---


# newV020 #
[Settings/description for streambaby quality settings](QualitySettings.md)

### icon.icon ###
### icon.movie ###
### icon.folder ###
Set to PNG files to represent the icons to use for the streambaby application icon, folder icon, and movie icon.  files are relative to the assets directory.

### background.image ###
Set to point to the PNG file to use for the background

### ignore.dotfiles (default: true) ###
Set to true if you want files/folders beginning with a dot to show up in selection screen.

### remember.password (default: true) ###
Set to false to not cache passwords on a tivo by tivo basis

### toplevel.skip (default: false) ###
Set to true to skip the "Top Level" folder screen if there is only one folder

### streambaby.dir ###
Set this to manually specify where the streambaby main directory is

### cut.startoffset (default: 0) ###
### cut.endoffset (default: 0) ###
When using an EDL file to mark "cut" positions (commercial skipping) offset the start and end points by this many milliseconds.  So if startoffset is set to 500 and endoffset is set to 1000 the jump will be made 1/2 second after the cutpoint specified, and it will jump to 1 second before the end of the cutpoint
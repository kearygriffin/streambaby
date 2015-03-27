# Known Issues #
  * Aspect must be set to Panel before entering streambaby or videos may be squished/stretched.
    * Aspect must be set with the aspect button to panel before entering streambaby
  * FFMPEG with SVN revisions `r10939-r11109` (Nov 7 2007 - Nov 28 2007) will likely not work.  Between those versions there doesn't seem to be figure out what libformat is being used. (because there is no avformat\_version call, and there are different avformat versions in that range, and avcodec\_version returns the same version for the entire range)
  * Under windows, if you exit streambaby by closing the console (ctrl-c is OK), while it is generating a thumbnail file, FFmpeg may continue to run in the background
  * Streambaby doesn't recognize that TiVo can't stream H264 > level 4.1.  MP4 code should parse the level, and if it is > 4.1 either:
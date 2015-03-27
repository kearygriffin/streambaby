# StreamBaby #

## A TiVo HME application for streaming videos ##
### Requires TiVoHD or TiVo Series 3 ###

Support:  http://www.tivocommunity.com/tivo-vb/showthread.php?t=416858

[Getting Started](http://code.google.com/p/streambaby/wiki/getting_started)

Mac users may also download & install [pyTivoX](http://pytivox.googlecode.com/) which includes a full GUI and install for both streambaby and pyTiVo.

### Thanks: ###
First thanks to the [tivostream](http://code.google.com/p/tivostream/) project, of which the original code of this project is based on.  Without the heavy lifting done by tivostream this project would not exist.  A double thanks to moyekj (tivostream) for also investing a lot of his time into testing, debugging and general helping out with StreamBaby.

Thanks also to Steve C who along with moyekj was heavily involved in initial testing/suggestions/etc.

Also many thanks to [mod\_h264\_streaming](http://h264.code-shop.com/trac/), from which the MP4 random positioning/streaming code was ported from C to Java.  Google search needs to give this project some love, as I stumbled upon it only after searching for MP4 streaming code for weeks.
Note:  I also just noticed they released V2 which I will have to look at.

ffmpeg-java - Java library that uses JNA to access ffmpeg native libraries.  The version used in StreamBaby has been modified  to support newer versions of ffmpeg, and to automatically modify classes (on the fly) to support different ffmpeg versions.

And many thanks to everyone else who has helped in investigating the streaming capabilities of the TiVo.

## Main Features ##

  * Random access streaming of MP4 & MPG video files.  Able to seek to anywhere in the video.  Also removes need for qt-faststart.

  * Ability to FF/RWD to points in the video that have not been buffered yet.  When outside of the buffer, a "preview" of the movie (low-quality static images) will appear, and when play is pressed the video stream will be repositioned to start streaming at that point (ala Netflix/TiVo)

  * Currently streambaby supports real-time preview generation, however MPEG files do not seem to do well with real-time preview generation.  For MPEG files as soon as you start playing, a small (30mb or so) file of static thumbnail images is created.  It usually takes around 5 minutes for a 2 hour movie for the complete thumbnail file to be created.  It is used as it is created, so as the images are generated they will be available for use for previewing.  The pvw file will be cached and automatically next time the movie is played.

  * Ability to transcode and stream video that is not inherently TiVo compatible.  Supports most wmv, avi, and mkv.  Preview mode and random-positioning work with most transcoded files.

  * Attempts to work around the 1.1G limit for streaming videos on the TiVo.  If you reach the 1.1G limit, the video you are watching will be paused and restarted with a fresh buffer at the position you were in before.

  * Remembers your position in the video when you stop watching and automatically starts at that position next time you watch.  Works even when exiting playing via the "TiVo" button.

## Requirements: ##

  * Requires Java 1.5 or above to be installed.
  * Uses ffmpeg binary executable for transcoding.  MPG/MP4 streaming is built-in
  * Uses ffmpeg-java and ffmpeg native libraries to generate "preview" views on the fly.

For windows, ffmpeg.exe and the native library DLL's will automatically be download into the native directory on the fist-run of streambaby.
For Linux, ffmpeg must already be installed on the system, as well as libavcodec, libavformat, libavutil, and libswscale. (All of the libs are usually automatically installed when ffmpeg is installed)


## Getting Started: ##
Download the latest streambaby.zip, unzip, and at a minimum edit the streambaby.ini file and add a dir.1=xxxxx entry to point to your video directories.
From windows you can then use the file explorer to open the directory streambaby was unzipped in, and double click on the streambaby.bat file to run it.  It will open a console window and start runnning.  Closing the console window will close the application.

On Linux (or from the windows cmd line) you can run streambaby by going to the streambaby directory and typing:
streambaby

Note: On windows installation, ffmpeg will automatically be downloaded and put into the native directory the first time streambaby runs, so it may take a minute to start.

Then access the Stream, Baby. Stream! application from the TiVo's "Music, photos, & Showcases" menu.

Select the video you want to view, and it should start playing

## Special Remote Control Commands while viewing: ##

  * Using the keypad and typing numbers and then pressing enter will automatically jump to that minute in the video.   If that position is not buffered, it will be reloaded and restarted at that position
  * When fast forwarding or rewinding if you go past the point the TiVo has buffered, preview mode will begin and you will start seeing low-res images of the video.  When you press play, if you are in a section of the video that has not been buffered, the video will be repositioned and restarted at that position.
  * When fast forwarding or rewinding, pressing the skip-forward or skip-back buttons will jump 15 minutes in the appropriate direction.  If that point is past the buffer, "preview" mode will be entered.

## Other features that may only be useful to me: ##

  * Ability to password protect directories.  Each directory can have multiple passwords (so if you protect a directory with "roger,tomcat" both the password roger and tomcat will work.
    * If any of the directories are not  password protected, you will not be asked for one when connecting to streambaby.
    * Once you have entered a password, it will be remembered and you will not be prompted for it next time you connect to the application
    * If you wish to enter a new password (for instance if it didn't ask for a password because some directories were not password protected) pressing the CLEAR button when browsing videos will bring you back to the password screen.

## Configuration: ##
See the wiki page [streambaby.ini](StreamBabyIni.md) for options.

Configuration information is contained in streambaby.ini.  You can get help for the config file by running "streambaby --help".  You can also specify where the config file is by running streambaby with:
streambaby --config /path/to/config.ini
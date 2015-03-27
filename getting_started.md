# GETTING STARTED WITH STREAMBABY #

Other useful reference pages:
[remote\_button\_summary](http://code.google.com/p/streambaby/wiki/remote_button_summary)

  * If you don't already have Java Runtime Environment (JRE) 1.5 or later installed you need to do that first. You can download latest JRE from here:
    * http://www.java.com/en/download/index.jsp
  * Download latest streambaby installation zip file from [downloads page](http://code.google.com/p/streambaby/downloads/list)
  * Unpack to a fresh folder somewhere
  * Edit **streambaby.ini** file and at minimum you must define at least 1 top level folder under which your video files are located (video files can be in any folder structure you like). For first entry use dir.1=xxx for second entry dir.2=xxx etc. Lines starting with a **#** character are comment lines. Example:
    * # Top level video shares
    * **dir.1=c:\home\dvd**
    * **dir.2=f:\videos**
  * NOTE:  Make sure you have at least one dir.1=xxx line in the configuration file, and make sure it does not start with #.  **Lines starting with # are ignored**
  * Now you are ready to run the application. For windows double click on **streambaby.bat** or for Mac/Unix systems run **streambaby** script. For windows you will see a console window similar to the one shown below. You should leave this console window running.
> > NOTE: For windows if double clicking on streambaby.bat does not work it most likely means you don't have java.exe in your windows path, so you have 2 choices:
      * Add full path to folder containing java.exe to windows PATH environment variable. As an example for Vista 64 this would be something like: **C:\Program Files (x86)\Java\jre6\bin**
      * To edit environment variables in Windows go to **Control Panel-System-Advanced-Environment Variables** and then look for **Path** entry under **System Variables**. Then add a semicolon followed by full path to java.exe on your system as in the example given above (will vary depending on your particular OS).
      * Alternative to the above is to simply edit the streambaby.bat file and replace **java** with the full path to **java.exe**, for example: **"C:\Program Files (x86)\Java\jre6\bin\java.exe"**
![http://streambaby.googlecode.com/files/console.gif](http://streambaby.googlecode.com/files/console.gif)
  * On one of your Series 3 Tivos go to **Tivo Central - Music, Photos & Showcases** and scroll down all the way to close to the bottom and look for **Stream, Baby, Stream** item:
![http://streambaby.googlecode.com/files/stream_baby_stream.gif](http://streambaby.googlecode.com/files/stream_baby_stream.gif)
  * Once you select or right arrow that item you are presented with **Top Level** screen:
![http://streambaby.googlecode.com/files/top_level.gif](http://streambaby.googlecode.com/files/top_level.gif)
  * Select or right arrow one of the top level entries and then you can navigate the folder structure and select a video of interest:
![http://streambaby.googlecode.com/files/selection_screen.gif](http://streambaby.googlecode.com/files/selection_screen.gif)
  * Select or right arrow one of the video entries to enter **Play Screen**:
![http://streambaby.googlecode.com/files/play_screen.gif](http://streambaby.googlecode.com/files/play_screen.gif)
  * For Quality selection if this video can be played back natively there will be a "Same" choice which means play back video as is without transcoding it. If the bit rate is too high for streaming in real time or better you can choose a lower quality rate which will transcode to mpeg2 and stream at that bit rate.
  * Here is an example of pause during playback:
![http://streambaby.googlecode.com/files/plyaback.gif](http://streambaby.googlecode.com/files/plyaback.gif)

Consult the [remote\_button\_summary](http://code.google.com/p/streambaby/wiki/remote_button_summary) wiki page for details about all the remote control functions in selection screen and playback screen modes.
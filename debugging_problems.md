# COLLECTING DEBUGGING INFORMATION #

When things are not working properly there are a few steps you can take to generate additional debugging information which can be very useful when reporting issues.

  * Stop streambaby if it's still running
  * Edit **simplelog.properties** file and change the first line to read:
    * **com.unwiredappeal=debug**
  * Now start streambaby and run it until the point you run into your problem
  * Exit streambaby again and then make a copy of the contents of **streambaby.log** file where all the debug information is saved


If the problem is related to failed video playback collecting some additional information could be helpful:
  * Using ffmpeg (for windows this is **ffmpeg.exe** under **native** folder) run the following on the video file you are having trouble with:
    * **ffmpeg -i file**
  * Save the output information
  * Use **[mediainfo](http://mediainfo.sourceforge.net/en)** to gather detailed information about the video file and save that information

Post problem description along with all the collected debug information in the  [tivo forum thread](http://www.tivocommunity.com/tivo-vb/showthread.php?t=416858)
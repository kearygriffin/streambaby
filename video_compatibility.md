## Tivo Native Video Compatibility ##
The following specific formats can be **pushed** to Series 3 TiVos natively (i.e. no transcoding) using **streambaby** or **pyTivo** and streamed to Series 3 TiVos using **streambaby**.
Note that **pyTivo** pulls support mpeg2 format only for series 3 TiVos, so any other types will be transcoded on the fly. For series 4 TiVos or later you can also pull H.264 TS files.

| **DESCRIPTION** | **CONTAINER** | **VIDEO CODECS** | **AUDIO CODECS** | **TYPICAL SUFFIXES** | **~MAX BIT RATE** | **NOTES** |
|:----------------|:--------------|:-----------------|:-----------------|:---------------------|:------------------|:----------|
| mpeg1/mpeg2 | mpeg2 program stream | mpeg1, mpeg2 | mpeg1 - layer II (mp2), AC3 (multi-channel) | .mpeg .mpg .mp2 | 25 Mbps | **1** |
| H.264 (AVC1) | mpeg4 | H.264 (up to level 4.1) | multi-channel AC3 or AAC | .mp4 | 25 Mbps | **1, 2, 3, 5** |
| H.264 (AVC1) | mpeg2 transport stream | H.264 (up to level 4.1) | multi-channel AC3 or AAC (LATM) | .ts | 25 Mbps | **1, 6** |
| VC-1 (WVC1) | asf (wmv) | VC-1 AP L2 or L3 | WMA9 (2 channel CBR) | .wmv | ?? | **4** |

**NOTE 1:** Audio sampling rates need to be either **44.1 KHz** or **48 KHz** and audio bit rates should not exceed **448 Kbps** for Series 3 TiVos or **640 Kbps** for Series 4 TiVos. Also, standard NTSC video frame rates of **23.976, 29.97 or 59.94 fps** are highly recommended as other frame rates may not decode correctly.

**NOTE 2:** Not all encoders or decoders are capable of handling AC3 audio in mpeg4 container. TiVo also requires the MOOV atom to be at the start of the file in order to work, which depending on encoder you use means you may need to run qt-faststart or equivalent to ensure that is the case.

**NOTE 3:** TiVo decoder can only properly output stereo or 5.1 AC3 audio, so while > 2 channel AAC audio tracks are supported, they will not sound right as output by TiVo.

**NOTE 4:** See section below on encoding profile that works. Currently Tivo is very picky about specific format of wmv files.
  * Streambaby does not currently support streaming of this format.  It will be transcoded.

**NOTE 5:** For h264 playback, the Tivo decoder has a bug with the aspect ratio used to display any 1280 based video that is not 1280x720 exactly. So for example 1280x720 displays fine but 1280x544 does not. 1920 based video does not suffer from the same kind of problem.

**NOTE 6:** Only Series 4 TiVos or later support H.264 in mpeg2 TS container. Note that .TiVo H.264 files can be pulled with TiVo Desktop and/or pyTivo. pyTivo also supports pulling unencrypted TS H.264 video files which TiVo Desktop does not. Advantage of TS H.264 video files is there is no MOOV atom to worry about and since they can be pulled to TiVo they won't have copy protection set or depend on mind.tivo.com server to be pushed to a TiVo. Also means pyTivo can transcode to this format on the fly for pyTivo pushes or pulls.

# ffmpeg encoding H.264 mpeg4 with AC3 audio for Tivo (from mpeg2 source) #

If you are encoding from an mpeg2 source with AC3 audio the following encoding profile using **ffmpeg** seems to work pretty well:

**ffmpeg -y -i inputFile.mpg -threads 2 -acodec copy -vcodec libx264 -flags +loop -coder ac -level 41 -b 8000k -refs 3 -bf 3 -me\_method umh -subq 9 -me\_range 16 -qmin 10 -qmax 50 -qscale 1 -sameq -g 24 -f mp4 outputFile.mp4**

NOTE: In the above you can adjust bitrate (-b 8000k) as needed depending on quality desired. For HD sources using 8000k bitrate works pretty well, for SD sources 3000k is usually enough.

Another ffmpeg recipe roughly equivalent to the Handbrake recipe posted below:

**ffmpeg -y -i inputFile.mpg -acodec copy -vcodec libx264 -level 41 -b 5000k -refs 3 -flags2 +mixed\_refs+wpred+bpyramid+dct8x8-fastpskip -bf 3 -me\_method umh -subq 9 -me\_range 16 -qmin 10 -qmax 50 -g 24 -keyint\_min 2 -f mp4 ouputFile.mp4**

NOTE: In the above you can adjust video bit rate (-b 5000k) to be higher or lower depending on quality desired, for example using -b 2000k for SD sources.

# Handbrake encoding H.264 mpeg4 with AC3 audio for Tivo (from mpeg2 source) #
(Thanks to txporter for contributing this one)

If you are encoding from an mpeg2 source with AC3 audio the following encoding profile using **handbrake** works very nicely:

**handbrakeCLI -i inputFile.mpg -f mp4 -O -e x264 -b 5000 -a 1 -E ac3 -x ref=3:mixed-refs=1:bframes=3:b-pyramid=1:weightb=1:analyse=all:8x8dct=1:me=umh:subq=9:psy-rd=1,0.2:direct=auto:keyint=24:min-keyint=2:no-fast-pskip=1:no-dct-decimate=1 -v -o outputFile.mp4**

NOTE: In the above you can adjust video bit rate (-b 5000) to be higher or lower depending on quality desired, for example using -b 2000 for SD sources.

# Handbrake encoding H.264 mpeg4 with AC3 audio for Tivo from DVD source #
(Again thanks to txporter for contributing this one)

**HandBrakeCLI -i "path\_to\_VIDEO\_TS" -t 1 -f mp4 -O -e x264 -b 5000 -2 -T -a 1 -E ac3 -s 4 -F -x ref=2:bframes=2:me=umh --strict-anamorphic -v -o outputFile.mp4**

# Encoding VC-1 Advanced Profile using Windows Media Encoder 9 #

Starting with Tivo Series 3 software 11.x the VC-1 decoder is enabled which allows playback of Microsoft WMV files in ASF container with VC-1 Advanced Profile (know as wvc1) video and wma9 audio. Note that very few types of WMV files which actually work. It took a lot of experimentation, but I’ve determined the following custom encoding setup using Windows Media Encoder produces WMV files that can be streamed to Series 3 Tivos.

NOTE: Video must be VC-1 Advanced Profile (wvc1). L3 and L2 profiles are known to work.

NOTE: Audio must be 2-channel CBR wma2 (multi-channel audio does not seem to work)

## VideoRedo TVSuite4 ##

The new VideoRedo TVSuite4 software has H.264 & VC-1 encoding built in. I have confirmed that VC-1 output from VideoRedo is TiVo compatible and is the easiest way to generate a VC-1 file compatible for TiVo playback.

Following is a summary of encoding profile that works using Microsoft Windows Media 9 encoder:

  * Install [Windows Media Encoder 9](http://www.microsoft.com/windows/windowsmedia/forpros/encoder/default.mspx) if you haven’t already
  * Start the Encoder
  * In **New Session window** choose **Custom session**
  * Sources tab
    * Set **Source from**: to **File** and browse to set source file
  * Output tab
    * Disable **Pull from encoder** and enable **Encode to file**
  * Compression tab
    * Destination = File download (computer playback)
    * Video = **High definition quality video (5 Mbps VBR)** OR **DVD quality video (2 Mbps VBR)**
    * Audio = **High definition quality audio (VBR)**
    * Click on **Edit…** button
    * Custom Encoding Settings window General tab:
      * Change video codec to **Windows Media Video 9 Advanced Profile**
      * Changed audio codec to **Windows Media Audio 9.2**
      * Change audio Mode to **CBR**
      * Click on other tab and set Video size = **Same as video input**
      * **OK**
  * Click on **Apply** button to apply all settings
  * Click on **Start Encoding**

As an alternative better GUI for encoding you can now also use [Microsoft Expression Encoder 3](http://www.microsoft.com/downloads/details.aspx?FamilyID=b6c8015b-e5de-46c0-98cd-1be12eef89a8&displaylang=en). As an example:
  * Start Encoder
  * File->Import... and select file you want to encode
  * In **Encode** section set:
    * Output Format = Windows Media
    * Video = VC-1 Advanced
    * Audio = WMA
  * Expand **Video** and set the parameters you want, particularly:
    * Frame Rate = Source
    * Size Mode = Source
    * Video Aspect Ratio = Source
  * Expand **Audio** and make sure that mode is set to CBR & Stereo
  * At the top in **System** area you can choose Encoding Quality such as Balanced or Best Quality, etc.
  * Choose the **Output** tab to set the output file Directory, and you probably want to turn off Sub-folder by Job ID option.

# pyTivo vs TiVo Desktop Plus 2.8.1 Pushes #

This is a comparison of pushes of video types that TiVo can natively decode (no need to transcode). For TiVo Desktop this refers to video Auto Transfers from PC->TiVo.

| **File Type** | **pyTivo push result** | **TD+ 2.8.1 push result** |
|:--------------|:-----------------------|:--------------------------|
| .TiVo (PS format) | configured with proper tivodecode + MAK decrypts to mpeg2 | fails |
| mpeg2 | pushes natively | pushes natively for 2.8.1 or later |
| mp4 + h.264 + ac3/aac (moov atom at end) | pushes natively (applies qt-faststart automatically) | transcodes, for ac3 loses audio |
| mp4 + h.264 + ac3/aac (moov atom at start - qt-faststart applied) | pushes natively | pushes natively |
| mp4 with embedded metadata | gets proper title, episode title and description | title = (auto push folder name), episode title = file name, description OK |
| wmv | pushes natively | always transcodes |
| general with metadata file | includes subset of metadata information | does not support metadata other than embedded mp4 (see above) |
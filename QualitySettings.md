# Quality settings/description #

Streambaby allows for the selection of a "quality" at which to stream a file to the tivo.  If enabled, you will be able to select the quality on the "play" screen.  Only qualities that are less than the original will be displayed. (So you can't select a bitrate that is higher than the original, as it would serve no purpose)

Quality selection is disabled by default.  To enable use of qualities in streambaby, use the following ini setting:

### quality.select (default=false) ###
Set to true to enable quality selection in streambaby

### All qualities other than Same _force_ transcoding. ###

The possible qualities are:
  * Same
    * Stream the file in the original quality
  * Auto
    * Base the bitrate on the bandwidth of the connection, which is tested when you first access Streambaby.
    * NOTE: AUTO IS NOT YET IMPLEMENTED
  * Highest, High, medium-high, Medium, medium-low, low, lowest

## General Quality INI settings ##
### quality.highestvbr (default: 5000) ###
The video bitrate to use for "highest" quality

### quality.lowestvbr (default: 512) ###
The video bitrate to use for "lowest" quality

### quality.highestabr (default: 192) ###
Audio bitrate to use for highest quality

### quality.lowestabr (default: 128) ###
Audio bitrate to use for lowest quality

Bitrates for qualities  in between highest and lowest are calculated using the above parameters.

### quality.highres= (default: 720) ###
The height (Y) resolution to use for highest quality

### quality.lowres= (default: 720) ###
The height (Y) resolution to use for lowest quality

Resolution for qualities in between highest/lowest are caclulated using the above parameters.

### quality.2channel (default: 4) ###
Any qualities below or equal to this (default 4=medium) force 2-channel audio.

### quality.default (default: same) ###
The default quality to use, Can be same, auto, or 1-7 (lowest=1, highest=7)
Can also be set to an exact bitrate (i.e. 1000 for 1000 kbps)
The playscreen will default to this if it is lower than the original quality of the video.

## Configuration settings for AUTO mode ##
NOTE:  Not yet implemented

### quality.auto default: false) ###
Turn on auto quality (enables testing of bandwidth when tivo first connects to streambaby)

### quality.auto.percent (default: 70) ###
Use this percentage of calculated bandwidth for streaming

## FFMpeg arguments for quality settings ##
Please see [FFmpg transcoding configuration](StreamBabyIni#ffmpeg_transcoding_configuration.md)
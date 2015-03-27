# Other Features #

  * To play an entire folder, while in the video selection screen, press the play button while a folder is selected.
  * To play a list of files, while in the video selection screen, move to a file and press the FF button.  The selected file and all files under it will be played.
    * All the selected movies will be played one after another, when one ends, the next will automatically start.
    * While playing multiple videos, channel up/down can also move between the files

  * To bypass the "resume from saved position/play from beginning" screen, you can press play while a movie is highlighted in the selection screen.  The movie will start from the saved position (or the beginning if there is no saved position)

  * To generate a pvw file (thumbnail file) manually (and store it in the cache):
    * streambaby --genpreview `<path_to_video>`
  * To clean out the thumbnail cache and remove any pvw files for videos that don't seem to exists any more:
    * streambay --cleancache
    * note:  This command uses the streambaby.ini dir.x entries to locate the videos, so it will remove any cached thumbnails for videos that may exist, but aren't accessible via streambaby.ini anymore.

  * Streambaby also looks in the same directory as the video for a thumbnail file in the format video\_name.ext.pvw For example a video file called sw.mpg the pvw would be looked for in sw.mpg.pvw
  * These files are note cleaned by the --cleancache command or the autocleaner.

PVW file format:
  * The pvw file format is simply a renamed zip file with jpg images in the format img-%05d.jpg where the number is the second in the video.  So the preview image for the 3000 second of the video would be stored in the zip as "img-03000.jpg"
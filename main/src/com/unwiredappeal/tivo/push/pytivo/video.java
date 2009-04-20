package com.unwiredappeal.tivo.push.pytivo;

public class video {
	   String file, Url;
	   public video(String file, String Url) {
	      this.file = file;
	      this.Url = Url;
	   }
	   
	   public String toString() {
	      String s = "file=" + file + " Url=" + Url;
	      return s;
	   }
}

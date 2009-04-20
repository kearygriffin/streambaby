package com.unwiredappeal.tivo.push.pytivo;

public class container {
	   String title, type, Url, path;
	   public container(String title, String type, String Url) {
	      this.title = title;
	      this.type = type;
	      this.Url = Url;
	      this.path = "";
	   }
	   
	   public String toString() {
	      String s = "title=" + title + " type=" + type + " Url=" + Url + " path=" + path;
	      return s;
	   }
}

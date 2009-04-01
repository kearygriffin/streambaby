package com.unwiredappeal.tivo.pyTivo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Stack;
import com.unwiredappeal.tivo.utils.Log;

public class pyTivo {
   public static String ip = "localhost";
   public static String port = "9032";
   private static Stack<String> tivos = new Stack<String>();
   private static Stack<String> errors = new Stack<String>();
   private static Stack<container> containers = new Stack<container>();
   private static Stack<video> videos = new Stack<video>();

   // Main pyTivo query - call before anything else
   public Boolean init () {
      errors.clear();
      String urlString = "http://" + ip + ":" + port + "/TiVoConnect?Command=QueryContainer&Container=%2F";
      try 
      {
          URL url = new URL(urlString);
      
          BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
          String line="", title="", type="", Url="";

          while ((line = in.readLine()) != null) 
          {
             // Get rid of leading and trailing white space
             line = line.replaceFirst("^\\s*(.*$)", "$1");
             line = line.replaceFirst("^(.*)\\s*$", "$1");
             if (line.length() == 0) continue; // skip empty lines
             if (line.matches("^<Title>.+")) {
                title = line.replaceFirst("^<Title>(.+)</Title>$", "$1");
                continue;                
             }
             if (line.matches("^<ContentType>.+")) {
                type = line.replaceFirst("^<ContentType>(.+)</ContentType>$", "$1");
                continue;                
             }
             if (line.matches("^<Url>.+")) {
                Url = line.replaceFirst("^<Url>(.+)</Url>$", "$1");
                if (type.equals("x-container/tivo-videos")) {
                   addContainer(title, type, Url);
                }
                continue;                
             }
          }

          in.close();
          
          for (int i=0; i<containers.size(); ++i) {            
             queryContainer(containers.get(i));
          }
          getContainerPaths();
          debug();

          return true;
      } 
      catch (MalformedURLException e) {
         errors.push(e.toString() + " - " + urlString);
         return false;
      } 
      catch (IOException e) {
         errors.push(e.toString() + " - " + urlString);
         return false;
      }
   }

   // Return available tivos on network (call only after init)
   public Stack<String> getTivos() {
      return tivos;
   }
   
   // pyTivo push video request (call only after init)
   public Boolean pushVideo(video v, String tivo) {
      String s[] = v.Url.split("/");
      String container = s[1];
      String urlString = "http://" + ip + ":" + port + "/TiVoConnect?Command=Push&Container=" + container + "&File=";
      String separator = urlEncode(System.getProperty("file.separator"));
      for (int i=2; i<s.length; i++) {
         urlString = urlString + separator + s[i];
      }
      urlString = urlString + "&tsn=" + urlEncode(tivo);
      Log.info(urlString);
      errors.clear();
      try 
      {
          URL url = new URL(urlString);
          url.openStream();
          return true;
      } 
      catch (MalformedURLException e) {
         errors.push(e.toString() + " - " + urlString);
         return false;
      } 
      catch (IOException e) {
         errors.push(e.toString() + " - " + urlString);
         return false;
      }
   }

   public void handleErrors() {
      for (int i=0; i<errors.size(); i++) {
    	  Log.error(errors.get(i));
      }
   }

   public Boolean addContainer(String title, String type, String Url) {
      Boolean add = true;
	  String titleEncoded = urlEncode(title);
      for (int i=0; i<containers.size(); i++) {
          if (containers.get(i).Url.equals(Url)) {
             add = false;
          }
       }
       if (add) {
          container c = new container(titleEncoded, type, Url);
          containers.push(c);
          return true;
       } else {
          return false;
       }
   }
   
   public Boolean addVideo(String file, String Url) {
      Boolean add = true;
      for (int i=0; i<videos.size(); i++) {
         if (videos.get(i).Url.equals(Url)) {
            add = false;
         }
      }
      if (add) {
         video f = new video(file, Url);
         videos.push(f);
         return true;
      } else {
         return false;
      }
   }
   
   public Boolean addTivo(String tivo) {
      Boolean add = true;
      for (int i=0; i<tivos.size(); i++) {
         if (tivos.get(i).equals(tivo)) {
            add = false;
         }
      }
      if (add) {
         tivos.push(tivo);
         return true;
      } else {
         return false;
      }
   }

   public Boolean queryContainer(container c) {
      String urlString = "http://" + ip + ":" + port + c.Url;
      try 
      {
          URL url = new URL(urlString);
      
          BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
          String line="", tivo="", title="", type="", Url="";

          while ((line = in.readLine()) != null) 
          {
             // Get rid of leading and trailing white space
             line = line.replaceFirst("^\\s*(.*$)", "$1");
             line = line.replaceFirst("^(.*)\\s*$", "$1");
             if (line.length() == 0) continue; // skip empty lines
             if (line.matches("^<Tivo>.+")) {
                tivo = line.replaceFirst("^<Tivo>(.+)</Tivo>$", "$1");
                addTivo(tivo);
                continue;                
             }
             if (line.matches("^<Title>.+")) {
                title = line.replaceFirst("^<Title>(.+)</Title>$", "$1");
                continue;                
             }
             if (line.matches("^<ContentType>.+")) {
                type = line.replaceFirst("^<ContentType>(.+)</ContentType>$", "$1");
                continue;                
             }
             if (line.matches("^<Url>.+")) {
                Url = line.replaceFirst("^<Url>(.+)</Url>$", "$1");
                if (type.equals("x-container/folder")) {
                   addContainer(title, type, Url);
                }
                if (type.startsWith("video/x-tivo-mpeg")) {
                   String s[] = Url.split("/");
                   String file = s[s.length-1];
                   addVideo(file, Url);
                }
                continue;                
             }
          }

          in.close();
          return true;
      } 
      catch (MalformedURLException e) {
         errors.push(e.toString() + " - " + urlString);
         return false;
      } 
      catch (IOException e) {
         errors.push(e.toString() + " - " + urlString);
         return false;
      }
   }

   // pyTivo url: /videos/testing/Daylight_Sat_Mar_08.mpg
   // pyTivo path: c:\home\dvd
   public static String pathFromUrl(String url) {
      String s[] = url.split("/");
      String name = s[1];
      String path = "";
      for (int i=0; i<containers.size(); i++) {
         if (containers.get(i).title.equals(name)) {
            path = containers.get(i).path;
         }
      }
      path = path.replaceAll("\\\\", "/");
      for (int i=2; i<s.length; ++i) {
         path = path + "/" + s[i];
      }
      
      return path;
   }

   public video findVideo(String sUrl) {
      sUrl = sUrl.replaceFirst("file:/", "");
      for (int i=0; i<videos.size(); i++) {
         String pUrl = pathFromUrl(videos.get(i).Url);
         if (matchFiles(sUrl, pUrl)) {
            return(videos.get(i));
         }
      }
      return null;
   }
	   
   public static boolean matchFiles(String f1, String f2) {
	  // In Windows same volume number can be upper or lowercase so lowercase them before compare
      if (f1.matches("^[A-Z]:.+")) {
         f1 = Character.toLowerCase(f1.charAt(0)) + f1.substring(1);
      }
      if (f2.matches("^[A-Z]:.+")) {
         f2 = Character.toLowerCase(f2.charAt(0)) + f2.substring(1);
      }
      
      // Ignore leading slashes in file names
      f1.replaceFirst("^/", "");
      f2.replaceFirst("^/", "");
      
      Log.debug("matchFiles: f1=" + f1 + " f2=" + f2);
      return f1.equals(f2);
   }
   
   static void debug() {
      // Debug print statements
      for (int i=0; i<containers.size(); ++i) {            
         Log.debug("Container: " + containers.get(i).toString());
      }
      for (int i=0; i<tivos.size(); ++i) {
         Log.debug("Tivo: " + tivos.get(i));
      }
      for (int i=0; i<videos.size(); ++i) {
         Log.debug("Video: " + videos.get(i).toString());
      }
   }
   // <input type="hidden" name="videos" id="videos" value="section-2">
   // id="section-1.type" name="section-2.type" value="video"
   // id="section-1.path" name="section-2.path" value="C:\home\dvd"
   public static Boolean getContainerPaths() {
      errors.clear();
      String urlString = "http://" + ip + ":" + port + "/TiVoConnect?Command=Admin&Container=Admin";
      try 
      {
          URL url = new URL(urlString);
      
          BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
          String line="", name="", type="", path="";

          while ((line = in.readLine()) != null) 
          {
             // Get rid of leading and trailing white space
             line = line.replaceFirst("^\\s*(.*$)", "$1");
             line = line.replaceFirst("^(.*)\\s*$", "$1");
             if (line.length() == 0) continue; // skip empty lines
             
             if (line.matches("^.+value=\"section-[\\d]+\".*$")) {
                name = line.replaceFirst("^.+name=\"([^\"]+)\".+$", "$1");
                name = urlEncode(name);
                continue;                
             }
             
             if (line.matches("^.+name=\"section-[\\d]+[.]type.+$")) {
                type = line.replaceFirst("^.+value=\"([^\"]+)\".+$", "$1");
                continue;                
             }
             
             if (line.matches("^.+name=\"section-[\\d]+[.]path.+$")) {
                path = line.replaceFirst("^.+value=\"([^\"]+)\".+$", "$1");
                path = new File(path).toURI().toString().replaceFirst("file:/", "");
                if (path.endsWith("/")) {
                	path = path.substring(0,path.length()-1);
                }
                if (type.equals("video")) {
                   setContainerPath(name, path);
                }
                continue;                
             }
          }

          in.close();
          return true;
      } 
      catch (MalformedURLException e) {
         errors.push(e.toString() + " - " + urlString);
         return false;
      } 
      catch (IOException e) {
         errors.push(e.toString() + " - " + urlString);
         return false;
      }
   } 
   
   public static void setContainerPath(String name, String path) {
      for (int i=0; i<containers.size(); i++) {
         if (containers.get(i).title.equals(name)) {
            containers.get(i).path = path;
         }
      }
   }

   public static String urlEncode(String s) {
	  String encoded;
	  try {
		  encoded = URLEncoder.encode(s, "UTF-8");
		  encoded = encoded.replaceAll("\\+", "%20");
		  return encoded;
	  } catch (UnsupportedEncodingException e) {
		  e.printStackTrace();
		  return null;
	  }
   }
}

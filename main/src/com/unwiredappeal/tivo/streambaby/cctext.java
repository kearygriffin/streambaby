package com.unwiredappeal.tivo.streambaby;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.arabidopsis.interval.Interval;
import org.arabidopsis.interval.IntervalTree;

import com.tivo.hme.bananas.BView;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.views.bgtext;

public class cctext {
	
   double minCCTime = 0;
   double minCCTimePerChar = 0;
   BView view;
   //String fsize;
   int fontSize;
   Stack<bgtext> text = new Stack<bgtext>();
   //long start=-1, stop=-1, size=0, time;
   long time;
   //int index=0;
   //Stack<Hashtable<String,Object>> ccstack = new Stack<Hashtable<String,Object>>();
   IntervalTree ccIntervals	= new IntervalTree();

   public Boolean on = true;

   public static class CCTextInterval extends Interval {
	public String ccText;
	public CCTextInterval(double low, double high, String txt) {
			super(low, high);
		this.ccText = txt;
	}
	   
   }
   
   String lastCC = null;
   
   public cctext(BView view, int fontSize, String file) {
	   minCCTime = StreamBabyConfig.cfgCCMinTime.getInt()/1000.0;
	   minCCTimePerChar = StreamBabyConfig.cfgCCMinTimePerChar.getInt()/1000.0;
      this.fontSize = fontSize;
      this.view = view;
      if (! file.toLowerCase().endsWith(".srt")) {
         file = file.replaceFirst("^(.+)\\..+$", "$1.srt");
      }
      
      // Parse srtFile and populate ccstack
      if ( ! parseSrtFile(file) ) return;
      //size = ccstack.size();
      
   }
   
   public Boolean exists() {
      if (ccIntervals.size() > 0)
         return true;
      else
         return false;
   }
   
   @SuppressWarnings("unchecked")
public void display(long time) {
      if (on) {
    	  //Log.debug("CCTime: " + time/1000.0); //  + " (" + start + "," + stop + ")");
    	 if (time < this.time && (this.time-time) < 1000)
    		 return;
         this.time = time;
         String thisCC = null;
         Interval interval = new Interval(time/1000.0,time/1000.0);
         List<CCTextInterval> tis = ccIntervals.searchAll(interval);
         if (tis.size() > 1) {
        	 //Log.debug("Combining CC");
        	 Collections.sort(tis);
         }
         
         if (tis.size() > 0)
        	 thisCC = tis.get(tis.size()-1).ccText;
         /*
         for (CCTextInterval i : tis) {
        	 if (thisCC == null)
        		 thisCC = new String(i.ccText).trim();
        	 else
        		 thisCC = thisCC + "\n" + i.ccText.trim();
         }
         */
        	 
         if (thisCC == null && lastCC == null)
        	 return;
         if (thisCC != null && thisCC.equals(lastCC))
        	 return;

         lastCC = thisCC;
         setVisible(false);
         if (thisCC != null) {
        	 //Log.debug(System.currentTimeMillis()/1000.0f + ": " + tis.get(0).getLow() + " - " + tis.get(0).getHigh() + ": " + thisCC);
        	 showText(thisCC);
         } else {
        	 //Log.debug("Removing at: " + System.currentTimeMillis()/ 1000.0f);
         }
      }
   }

   // Create and display multi-line cc text with dark backgrounds
   void showText(String cc) {
      Boolean debug = false;
      removeText();
      String[] lines = Pattern.compile("\n", Pattern.MULTILINE).split(cc);
      // This used for debugging sync (set debug=true)
      if (debug) {
         long elapsed = time/1000;
         int hours = (int)(elapsed/3600);
         int mins = (int)(elapsed/60) - hours*60;
         int secs = (int)elapsed % 60;
         String s = String.format("%02d:%02d:%02d ", hours, mins, secs);
         lines[0] = s + "--" + lines[0];
      }

      
      for (int i=0; i<lines.length; i++) {
         bgtext t = new bgtext(view, fontSize, lines[i]);
         text.push(t);
      }
      
      
      // Shift lines up appropriately
      for (int i=0; i<lines.length; i++) {
         bgtext t = text.get(i);
         t.setBounds(t.x, t.y - (lines.length-i)*t.h, t.w, t.h);
      }
      setVisible(true);
   }
   
   void removeText() {
      for (int i=0; i<text.size(); i++) {
         text.get(i).remove();
      }
      text.clear();      
   }

   public void insertCC(double start, double end, String txt) {
	  txt = txt.trim();
	  double minTime = Math.max((minCCTimePerChar*txt.length()), minCCTime);
	  if (end - start < minTime) {
		  end = start + minTime;
	  }
 	  ccIntervals.insert(new CCTextInterval(start, end, txt));
   }
   // Parse cc file and populate ccstack
   Boolean parseSrtFile(String srtFile) {
      try {
         BufferedReader ifp = new BufferedReader(
            new FileReader(srtFile)
         );
         try {
            System.out.println(">> Parsing srtFile: " + srtFile);
            String line = null;
            String text="";
            String timeFormat = "([0-9]*):([0-9][0-9]):([0-9][0-9]),([0-9][0-9][0-9])\\s+-->\\s+([0-9]*):([0-9][0-9]):([0-9][0-9]),([0-9][0-9][0-9])";
            Pattern p = Pattern.compile(timeFormat);
            Matcher m;
            long t1=0, t2=0;
            int HH, MM, SS, mmm;
            while (( line = ifp.readLine()) != null) {
               // Get rid of leading and trailing white space
               line = line.replaceFirst("^\\s*(.*$)", "$1");
               line = line.replaceFirst("^(.*)\\s*$", "$1");
               if (line.length() == 0) continue; // skip empty lines
               if (line.matches("^[0-9]+$")) continue; // skip lines starting with integer
               if (line.matches("^[0-9]+:[0-9]+:[0-9].*$")) {
                  // New time entry: HH:MM:SS,mmm --> HH:MM:SS,mmm
                  if( t2 != 0) {
                     //Hashtable<String,Object> h = new Hashtable<String,Object>();
                     //h.put("start", t1);
                     //h.put("stop", t2);
                     //h.put("text", text);
                     //ccstack.push(h);
                	  insertCC(t1/1000.0,t2/1000.0,text);
                     t1=0; t2=0;
                     text = "";
                  }
                  m = p.matcher(line);
                  if (m.matches() && m.groupCount() == 8) {
                     HH = Integer.parseInt(m.group(1));
                     MM = Integer.parseInt(m.group(2));
                     SS = Integer.parseInt(m.group(3));
                     mmm = Integer.parseInt(m.group(4));
                     t1 = mmm + 1000*(SS+60*MM+3600*HH);
                     HH = Integer.parseInt(m.group(5));
                     MM = Integer.parseInt(m.group(6));
                     SS = Integer.parseInt(m.group(7));
                     mmm = Integer.parseInt(m.group(8));
                     t2 = mmm + 1000*(SS+60*MM+3600*HH);
                  }
                  continue;
               }
               text = text + line + "\n";
            }
            if( t2 != 0) {
               //Hashtable<String,Object> h = new Hashtable<String,Object>();
               //h.put("start", t1);
               //h.put("stop", t2);
               //h.put("text", text);
               //ccstack.push(h);
          	   //ccIntervals.insert(new CCTextInterval(t1/1000.0, t2/1000.0, text));
            	insertCC(t1/1000.0,t2/1000.0,text);
            }
         }
         finally {
            ifp.close();
         }
         return true;
      }      
      catch (IOException ex) {
         return false;
      }
      
   }
   
   public void setVisible(Boolean visible) {
      for (int i=0; i<text.size(); i++)
         text.get(i).setVisible(visible);
   }
   
   void setLocation(bgtext t, int x, int y) {
      t.setLocation(x, y);
   }
   
   void setBounds(bgtext t, int x, int y, int w, int h) {
      t.setBounds(x, y, w, h);
   }
   
   void setValue(bgtext t, String value) {
      t.setValue(value);
   }
         
   public void clearResource() {
      for (int i=0; i<text.size(); i++)
         text.get(i).clearResource();
   }
   
   public void remove() {
      for (int i=0; i<text.size(); i++) {
         text.get(i).setValue(null);
         text.get(i).clearResource();
         text.get(i).remove();
      }
   }
}
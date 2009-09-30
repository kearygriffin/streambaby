// $Id: InitialScreen.java 11 2008-11-28 18:10:07Z moyekj@yahoo.com $

package com.unwiredappeal.tivo.streambaby;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BHighlights;
import com.tivo.hme.bananas.BListPlus;
import com.tivo.hme.bananas.BScreen;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BViewPlus;
import com.tivo.hme.bananas.IBananasPlus;
import com.tivo.hme.bananas.PlusSupport;
import com.tivo.hme.bananas.ViewUtils;
import com.tivo.hme.bananas.BSkin.Element;
import com.tivo.hme.bananas.layout.Layout;
import com.tivo.hme.bananas.layout.LayoutManager;
import com.tivo.hme.sdk.util.Ticker;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.dir.MediaFileType;
import com.unwiredappeal.tivo.metadata.MetaData;
import com.unwiredappeal.tivo.utils.Log;
import com.unwiredappeal.tivo.views.VText;

public class SelectionScreen extends ScreenTemplate implements Ticker.Client {
   public StandardList list;
   //public VText dirText;
   public VText message;
   
    public String title = null;
	public int level = 0;
	StreamBabyStream sapp;
	DirEntry de;
	private BViewPlus pleaseWait;
	//private Stack<String> lastEntry = new Stack<String>();
//	private DirEntry curDir;
    //private SelectionScreen screen;
   
   public SelectionScreen(BApplicationPlus app, DirEntry de, int level) {
      super(app);
      this.de = de;
      sapp = (StreamBabyStream)app;
      this.level = level;
      //curDir = de;
      //screen = this;

      /*
      dirText = new VText(
         getNormal(), SAFE_TITLE_H+10, SAFE_TITLE_V+40, 2, "smthisall"
      );
      dirText.setFlags(RSRC_VALIGN_TOP | RSRC_TEXT_WRAP);
      dirText.setValue("Top Level");      
		*/
      // No files found message text      
      /*
      // Entry display list
      int height = dirText.h + (int)(dirText.h/2);
      int n = (int)(getHeight()-2*SAFE_TITLE_V-80)/height;
      list = new ScreenList(
         (BView) getNormal(), SAFE_TITLE_H+10, SAFE_TITLE_V+80,
         getWidth()-2*SAFE_TITLE_H, n*height, height
      );
      */
      
      LayoutManager lm = new LayoutManager(getNormal());
      Layout safeTitle = lm.safeTitle(this);
      Layout layout = safeTitle;

      float stretchy = .98f;
      int safeY = -5;
      if (this.getBApp().getCurrentResolution().getHeight() == 720) {
    	  stretchy = .963f;
    	  safeY = -5;
      }
      layout = lm.relativeY(layout, true);
      layout = lm.safeAction(layout, this, 0, safeY);
      layout = lm.stretchWidth(layout, GLOBAL.SELECT_STRETCH);
      layout = lm.stretchHeight(layout, stretchy);

      list = new StandardList(getNormal(), layout);   

      //updateFileList(de);

      setFocusDefault(list);
	
      lm = new LayoutManager(getNormal());
      safeTitle = lm.safeTitle(this);
      layout = safeTitle;

      
      Element e = getBApp().getSkin().get(IBananasPlus.H_PLEASE_WAIT);
      layout = lm.size(layout, e.getWidth(), e.getHeight());
      layout = lm.align(layout, A_CENTER, A_CENTER);
		
      pleaseWait = new BViewPlus(this, layout);
      pleaseWait.setResource(e.getResource());
      pleaseWait.setVisible(true);
      
	  title = de.getStrippedFilename();
	  resetTitle();
      Ticker.master.add(this, System.currentTimeMillis()+200, null);
      
   }
   
   public void resetTitle() {
	   setTitle(this.toString());
   }
   public synchronized boolean handleEnter(Object arg, boolean isReturn) {
       ((StreamBabyStream)getBApp()).setCurrentScreen(this);
	   this.setPainting(false);
	   resetTitle();
	      // Clear out old list
	      if (list != null) {
	         list.clear();
	      }
	   this.setPainting(true);

	   pleaseWait.setVisible(true);
	   Ticker.master.add(this, System.currentTimeMillis()+200, null);

       //if (!isReturn)
       	//focusOnDefault();
       	
       if(isReturn)	   
       {
			//refresh on return
			doRefresh();
	   }
	          
       return true;
       //return super.handleEnter(arg, isReturn);
   }

   public void doRefresh() {
	   this.setPainting(false);
	   resetTitle();
	   
		updateFileList(de);
		setFocusDefault(list);
		focusOnDefault();
		pleaseWait.setVisible(false);
		//pleaseWait.remove();
		this.setPainting(true);

   }
	public synchronized long tick(long tm, Object arg) {
		doRefresh();

		flush();
		
		return 0;
		
	}   
   
  
   public void updateFileList(DirEntry de) {
	   
	   List<DirEntry> dirEntries = de.getEntryList(((StreamBabyStream)getBApp()).getPassword());
	   if (!de.isRoot()) {
		   if (StreamBabyConfig.cfgSortByFilename.getBool()) {
			   Comparator<DirEntry> cmp = new Comparator<DirEntry>() {
				public int compare(DirEntry o1, DirEntry o2) {
					if (o2.isFolder() != o1.isFolder()) {
						return o2.isFolder() ? 1 : -1;
					}
					return o1.getFilename().compareToIgnoreCase(o2.getFilename());
				}  
			   };
			   Collections.sort(dirEntries, cmp);
		   }
		   else
			   Collections.sort(dirEntries);
	   }
      // Update directory text
      //dirText.setValue(de.getName());
       
      // Clear out old list
      if (list != null) {
         list.clear();
      }
 
      int i = 0;
      if ( dirEntries.size() > 0 ) {
    	  //check if files still exist
    	  for (i=0; i<dirEntries.size();i++)
    	  {
			  File f = new File(dirEntries.get(i).uri);
			  if(!f.exists() && dirEntries.get(i).isFile())
			  {
				  //do nothing
			  }
			  else
			  {
				  list.add(dirEntries.get(i));
			  }
    	  }
    	  //list.add(dirEntries.toArray());
      }
        
      // If there are no entries then show message
      if (dirEntries.size()== 0) {
          message = new VText(
        	         getNormal(), SAFE_TITLE_H, (int)getHeight()/2, 1, ""
        	      );
        	      message.setFlags(RSRC_VALIGN_TOP | RSRC_HALIGN_CENTER);
        	      message.setValue("No entries in this directory");
        	      message.setVisible(false);    	  
         message.setVisible(true);
      } else {
    	 if (message != null) {
         message.setVisible(false);
         message.clearResource();
         message.remove();
         message = null;
    	 }
      }
   }
   
   public void focusOnDefault() {
	   focusOn(getLastEntry());
   }
   public boolean focusOn(Object entry) {
      Log.debug("focusOn entry=" + entry);
      if (entry != null) {
         for (int i=0; i<list.size(); i++) {
            if (list.get(i).equals(entry)) {
               list.setFocus(i,true);
               return true;
            }
         }
      }
      if (list.size() != 0) {
    	  list.setFocus(0, true);
      }
      return false;
   }

   
   public String toString() {
      return (title == null) ? "Top Level" : title;
   }
   
   public boolean handleAction(BView view, Object action) {
	   Log.debug("action=" + action);
      //if (action.equals("push")) {
	   if (action.equals("right")) {
		   moveRight();
         return true;
      }
      if (action.equals("pop")) {
         //pop();
    	 moveLeft();
         return true;
      }
      return super.handleAction(view, action);
   }
   
   public void moveRight() {
	   if (list.size() == 0)
		   return;
       DirEntry entry = (DirEntry)list.get(list.getFocus());// s.de;
          // Update lastEntry stack
       lastEntryPush(entry);
 
          if (entry.isFolder() == true) {
             // Entry is a dir, so update list entries to new dir
             //DIR = fullName;
        	SelectionScreen newScreen = new SelectionScreen(getBApp(), entry, level+1);
          	//newScreen.curDir = entry;
             //newScreen.level = level + 1;
             //newScreen.updateFileList(entry);
             //newScreen.setTitle(newScreen.toString());
             getBApp().push(newScreen, TRANSITION_LEFT);
          } else {
        	  BScreen newScreen = entry.getFileType().createPlayScreen(getBApp(), entry);
        	  //PlayScreen newScreen = new PlayScreen(getBApp(), entry);
             getBApp().push(newScreen, TRANSITION_LEFT);
       }

   }
   public void moveLeft() {
       //level -= 1;
       if (level == 0) {
          // Exit application
          this.getBApp().setActive(false);
       }
       if (list.size() > 0) {
	       DirEntry entry = (DirEntry)list.get(list.getFocus());// s.de;       
	       lastEntryPush(entry);
       }
       /*
      curDir = curDir.getParent();
      updateFileList(curDir);
      focusOn(lastEntryPop());
      setTitle(this.toString());
       */
      getBApp().pop();
      PlusSupport.viewRemoveNotify(this);
   }
   
   public synchronized boolean handleKeyPress(int code, long rawcode) {
	   Log.debug("code=" + code + " rawcode=" + rawcode);
	   
	   DirEntry entry ;
	   switch (code) {
       
	   case KEY_CLEAR:
		   ((StreamBabyStream)getBApp()).popTop();
		   break;
	   case KEY_SELECT:
		   moveRight();
		   	break;
	   case KEY_ADVANCE:
		   if (list.size() == 0)
			   return true;
		   if (list.getFocus() == list.size()-1) {
				play("left.snd");
			   list.setFocus(0, false);
		   } else {
				play("right.snd");
			   list.setFocus(list.size()-1, false);
		   }
		   return true;
	   case KEY_PLAY:
	      entry = (DirEntry)list.get(list.getFocus());// s.de;
	      if (entry.isFolder()) {
	    	  List<DirEntry> dlist = entry.getEntryList(sapp.getPassword());
	    	  if (dlist != null && !dlist.isEmpty()) {
		    	  BScreen newScreen = entry.getFileType().createViewerScreen(getBApp(), dlist, de.getName(), StreamBabyConfig.inst.getDefaultQuality());

	    		  //ViewScreen newScreen = new ViewScreen(getBApp(), dlist, de.getName(), StreamBabyConfig.inst.getDefaultQuality());
		          getBApp().push(newScreen, TRANSITION_LEFT);
		          lastEntryPush(entry);	    		  
	    	  } else {
	    		  play("bonk.snd");
	    	  }
	      } else {
	    	  BScreen newScreen = entry.getFileType().createViewerScreen(getBApp(), Arrays.asList(new DirEntry[] { entry }), null, StreamBabyConfig.inst.getDefaultQuality());
	     	  //ViewScreen newScreen = new ViewScreen(getBApp(), entry, StreamBabyConfig.inst.getDefaultQuality());
	          getBApp().push(newScreen, TRANSITION_LEFT);
	          // Update lastEntry stack
	          lastEntryPush(entry);
	      }
          
		   break;
	   case KEY_FORWARD:
		    int focus = list.getFocus();
		    List<DirEntry> dlist = new ArrayList<DirEntry>();
		    for (int i=focus;i<list.size();i++) {
		    	entry = (DirEntry)list.get(i);// s.de;
		    	if (!entry.isFolder)
		    		dlist.add(entry);
		    }
	    	  if (dlist != null && !dlist.isEmpty()) {
		    	  BScreen newScreen = dlist.get(0).getFileType().createViewerScreen(getBApp(), dlist, de.getName(), StreamBabyConfig.inst.getDefaultQuality());
	    		  //ViewScreen newScreen = new ViewScreen(getBApp(), dlist, de.getName(), StreamBabyConfig.inst.getDefaultQuality());
		          getBApp().push(newScreen, TRANSITION_LEFT);
		          lastEntryPush(list.get(list.getFocus()));	    		  
	    	  } else {
	    		  play("bonk.snd");
	    	  }		    
	     	break;
         case KEY_LEFT:
        	 moveLeft();
        	 break;
        	/*
            level -= 1;
            if (level < 0) {
               // Exit application
               this.getBApp().setActive(false);
            }
            
            if (level == 0) {
               // Return to top level
               curDir = EntryManager.getRootDirEntry();
               screen.topDirList(curDir.getEntryList());
               
               // Set focus on lastEntry top of the stack
               screen.focusOn(lastEntryPop());
            }
            
            if (level > 0) {
               // Return to previous directory
               curDir = curDir.getParent();
               screen.updateFileList(curDir);
               //screen.updateFileList(DIR);
               
               // Set focus on lastEntry top of the stack
               screen.focusOn(lastEntryPop());
            }
            
            return true;
            */
        	
         case KEY_NUM1:
        	/*
            if (GLOBAL.sortOrder.equals("date")) {
               GLOBAL.sortOrder = "alphanumeric";
            } else {
               GLOBAL.sortOrder = "date";
            }
            screen.updateFileList(DIR);
            */
            return true;
         case KEY_ENTER:
        	 pleaseWait.setVisible(true);
        	 cacheMetadata();
        	 lastEntryPush(list.getFocusObject());
        	 doRefresh();
        	 break;
      }
      
      return super.handleKeyPress(code, rawcode);
   }
   
   public void cacheMetadata() {
	   MetaData m = new MetaData();
	   for (int i=0;i<list.size();i++) {
		   list.get(i).getMetadata(m);
	   }
   }
   
   public void lastEntryPush(Object o) {
      Log.verbose("name=" + o.toString()+ ", level:" + level);
      //lastEntry.push(name);
      sapp.lastEntry.setSize(Math.max(sapp.lastEntry.size(), level+1));
      sapp.lastEntry.setElementAt(o, level);
   }
   public Object getLastEntry() {
	   Object e = null;
	   if (level < sapp.lastEntry.size())
		   e = sapp.lastEntry.elementAt(level);
	   Log.verbose("getLastEntry: level: " + level + ", name: " + (e == null ? "null" : e.toString()));
	   return e;
   }
   /*
   public String lastEntryPop() {
      String name = lastEntry.pop();
      debug.print("name=" + name);
      return name;
   } 
   */  
   
   static class StandardList extends BListPlus<DirEntry> 
   {
       /**
        * Constructor
        */
       public StandardList(BView parent, Layout layout)
       {
           super(parent, layout, Math.min(45, ViewUtils.getHeight(parent, H_BAR)), true);
           setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", H_RIGHT);
           BHighlights h = this.getHighlights();
           h.setPageHint(H_PAGEUP,   A_RIGHT+13, A_TOP    - 5);
           h.setPageHint(H_PAGEDOWN, A_RIGHT+13, A_BOTTOM + 5);           
       }

       /**
        * Create a row that has an icon and text.
        */
       protected void createRow(BView parent, int index)
       {
           BView icon = new BView(parent, 0, 0, 34, parent.getHeight());
           DirEntry e = (DirEntry)get(index); // ((ViewScreen)get(index)).de;
           String iconPath;
           MediaFileType ft = e.getFileType();
           if (ft != null)
        	   iconPath = ft.getIcon();
           else
        	   iconPath = StreamBabyConfig.cfgMovieIcon.getValue();
           icon.setResource(iconPath);
           BText text = new BText(parent, 40, 0, parent.getWidth(), parent.getHeight());
           text.setFlags(RSRC_HALIGN_LEFT);
           text.setShadow(true);
           text.setColor(getColor());
           text.setFont(getFont());
           
           //
           // set the value of the row to be the text that was 
           // passed in through add()
           //
           
           text.setValue(get(index));
       }        
   }

   /*
   // Pass key events up to parent class
   public boolean handleKeyPress(int code, long rawcode) {
      return super.handleKeyPress(code, rawcode);
   }
   */
}

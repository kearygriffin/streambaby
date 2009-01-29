package com.unwiredappeal.tivo.streambaby;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BListPlus;
import com.tivo.hme.bananas.BText;
import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.ViewUtils;
import com.tivo.hme.bananas.layout.Layout;
import com.tivo.hme.bananas.layout.LayoutManager;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.Log;

public class PlayScreen extends ScreenTemplate {

	private DirEntry de;
	private StandardList list;
	public static class ListEntry {
		public ListEntry(String t, int c) {
			txt = t;
			cmd = c;
		}
		String txt;
		int cmd;
		public String toString() {
			return txt;
		}
	}
	public static int CMD_RESUME = 1;
	public static int CMD_PLAYBEGIN = 2;
	public static int CMD_GOBACK = 3;
	
	
	public PlayScreen(BApplicationPlus app, DirEntry de) {
		super(app);
		this.de = de;
		

		/*
	    LayoutManager lm = new LayoutManager(getNormal());
	    Layout safeTitle = lm.safeTitle(this);
	    Layout layout = safeTitle;
	      
	      list = new StandardList(getNormal(), layout);

	      //setupList();
	      
	      setFocusDefault(list);
	      */
	      resetTitle();
	}

	public Layout calcListLayout(int rows) {
		
	    LayoutManager lm = new LayoutManager(getNormal());
	    Layout safeTitle = lm.safeTitle(this);
	    Layout layout = safeTitle;
	
	    layout = lm.relativeY(layout, true);
	    //layout = lm.stretchWidth(layout, 0.9f);
	    layout = lm.safeAction(layout, this, 0, 25);
	    layout = lm.stretchWidth(layout, GLOBAL.SELECT_STRETCH);
	    int height = ViewUtils.getHeight(this, H_BAR);
	    layout = lm.valign(layout, 295 - (rows * height));
	    return layout;
	}

	
	   public boolean handleEnter(Object arg, boolean isReturn) {
			setupList();
		   return true;
	   }
	   
	   private void setupList() {
		   if (list != null)
			   list.remove();
		   long pos = ViewScreen.getResetSavedPosition((StreamBabyStream)getBApp(), de.getUri(), de.getVideoInformation().getDuration());
		   Layout layout = calcListLayout(pos > 0 ? 3 : 2);
		   
		   list = new StandardList(getNormal(), layout);
		   //list.clear();
		   setFocusDefault(list);
		   //list.setBounds(layout, null);
	      if (pos != 0) {
	    	  list.add(new ListEntry("Resume playing " + de.getName(), CMD_RESUME));
	    	  list.add(new ListEntry("Play " + de.getName() + " from beginning", CMD_PLAYBEGIN));
	      } else
	    	  list.add(new ListEntry("Play " + de.getName(), CMD_PLAYBEGIN));
	      list.add(new ListEntry("Go back", CMD_GOBACK));
    	  list.setFocus(0, true);
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
	       ListEntry entry = list.get(list.getFocus());;
	 
	       if (entry.cmd == CMD_PLAYBEGIN|| entry.cmd == CMD_RESUME) {
	    	   if (entry.cmd == CMD_PLAYBEGIN) {
	    			((StreamBabyStream) getBApp()).cachePersistentData(ViewScreen.persistKey(de.getUri()),
	    					"0", true);
	    	   }
	    	   ViewScreen newScreen = new ViewScreen(getBApp(), de, StreamBabyConfig.inst.getDefaultQuality());
	    	   getBApp().push(newScreen, TRANSITION_LEFT);
	       } else if (entry.cmd == CMD_GOBACK)
	    	   moveLeft();

	   }
	   public void moveLeft() {
	      getBApp().pop();
	   }
	   
	   public boolean handleKeyPress(int code, long rawcode) {
		   Log.debug("code=" + code + " rawcode=" + rawcode);
		   switch (code) {
	       
		   case KEY_SELECT:
			   moveRight();
			   	break;
	         case KEY_LEFT:
	        	 moveLeft();
	        	 break;
	      }
	      
	      return super.handleKeyPress(code, rawcode);
	   }
	
	
   public void resetTitle() {
	   setTitle(this.toString());
   }

   public String toString() {
	   if (de != null)
		   return de.getName();
	   else
		   return "Play";
   }
   
   static class StandardList extends BListPlus<ListEntry> 
   {
       /**
        * Constructor
        */
       public StandardList(BView parent, Layout layout)
       {
           super(parent, layout);
           setBarAndArrows(BAR_HANG, BAR_DEFAULT, "pop", H_RIGHT);
       }

       /**
        * Create a row that has an icon and text.
        */
       protected void createRow(BView parent, int index)
       {
           //BView icon = new BView(parent, 0, 0, 34, parent.getHeight());
           BText text = new BText(parent, 10, 0, parent.getWidth()-50, parent.getHeight());
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

}

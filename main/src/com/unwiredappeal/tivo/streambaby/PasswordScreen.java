package com.unwiredappeal.tivo.streambaby;

import java.util.List;

import com.tivo.hme.bananas.BApplicationPlus;
import com.tivo.hme.bananas.BButtonPlus;
import com.tivo.hme.bananas.BKeyboardPlus;
import com.tivo.hme.bananas.BView;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;

public class PasswordScreen extends ScreenTemplate {

	public BKeyboardPlus kb;
	public PasswordScreen(BApplicationPlus app) {
		super(app);
        kb = new BKeyboardPlus(getNormal(), 100, 140, BKeyboardPlus.PLAIN_KEYBOARD, true);
        
        setFocus(kb);
        
        // create a "return to menu" button
        int safeTitleH = getBApp().getSafeTitleHorizontal();
        int safeTitleV = getBApp().getSafeTitleVertical();
        BButtonPlus<String> button = new BButtonPlus<String>(getNormal(), safeTitleH, A_BOTTOM-safeTitleV, 400);
        button.setBarAndArrows(BAR_HANG, BAR_DEFAULT, "left", null, H_UP, null, true);
        button.setValue("OK");
        button.setFocusable(true);
	}
	
    public boolean handleEnter(Object arg, boolean isReturn) {
    	resetTitle();
        if (arg != null && arg instanceof String) {
        	String s = arg.toString();
        	kb.setValue(s);
        }
        if (!isReturn)
        	done();
        
        return super.handleEnter(arg, isReturn);
    }
	
    public boolean handleAction(BView view, Object action) {
        if ("pop".equals(action) || "right".equals(action)) {
            done();
            return true;
        } else if ("left".equals(action)) {
            exit();
            return true;                
        }

        return super.handleAction(view, action);
    }
    
    /**
     * 
     */
    public boolean handleKeyPress(int code, long rawcode) {
        if (code == KEY_UP) {
            setFocus(kb);
            return true;
        } else if (code == KEY_SELECT) {
            done();
            return true;
        } else if (code == KEY_LEFT) {
            exit();
            return true;
        }
       
        return super.handleKeyPress(code, rawcode);
    }
    
    public void exit() {
    	getBApp().setActive(false);
    }
    
    public boolean isValidPassword(String pw) {
    	//List<DirEntry> entries = ((StreamBabyStream)getBApp()).getRootEntry().getEntryList(pw);
    	DirEntry de = StreamBabyConfig.inst.buildRootDirEntry();
    	// If there are no directories, then there are no passwords
    	if (de.entryList.isEmpty())
    		return true;
    	List<DirEntry> entries = de.getEntryList(pw);
    	return !entries.isEmpty();
    }
    
    public void enterMainScreen() {
		DirEntry curDir = ((StreamBabyStream)getBApp()).getRootEntry();
		if (StreamBabyConfig.cfgTopLevelSkip.getBool()) {
			List<DirEntry> entries = curDir.getEntryList(((StreamBabyStream)getBApp()).getPassword());
			if (entries.size() == 1 && entries.get(0).isFolder()) {
				curDir = entries.get(0);
			}
		}
	    SelectionScreen screen = new SelectionScreen(getBApp(), curDir, 0);
	    getBApp().push(screen, TRANSITION_NONE);

    }
    public boolean done() {
    	String pw = kb.getValue();
    	if (pw == null)
    		pw = "";
    	if (isValidPassword(pw)) {
    		((StreamBabyStream)getBApp()).setPassword(pw);
    		enterMainScreen();
    		return true;
    	} else  {
    		setTitle("Incorrect password");
    		return false;
    	}
    }
    
    public void resetTitle() {
    	setTitle(this.toString());
    }
    
    
    /**
     * Title of the screen
     */
    public String toString() 
    {
        return "Enter Password";
    }

}

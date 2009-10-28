package com.unwiredappeal.tivo.streambaby;

import java.io.File;

import com.tivo.hme.bananas.BApplicationPlus;
import com.unwiredappeal.tivo.config.StreamBabyConfig;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.Log;

public class MoreScreen extends ButtonScreen {

	public String screenTitle = "More";
	private DirEntry de;
	

	public MoreScreen(BApplicationPlus app, DirEntry de) {
		super(app);
		this.de = de;
	}
	
	@Override
	public void render() {
	}

	@Override
	protected int setupButtons(boolean isReturn) {
		
		if (StreamBabyConfig.cfgShowDelete.getBool() && de.isFile()){
	
			addSimpleTextButton("Delete from Hard Drive", new ButtonHandler() { 
				public boolean left() {
					popBack();
					return true;
				}
				public boolean right() {
					doDelete();
					return true;
				}
				public boolean select() {
					doDelete();
					return true;
				}			
			}, false);
		}
		
		addSimpleTextButton("Go back", new ButtonHandler() 
		{ 
				public boolean left() 
				{
					popBack();
					return true;
				}
				public boolean right() 
				{
					popBack();
					return true;
				}
				public boolean select() {
					popBack();
					return true;
				}			
			}, true);		
			
		return 0;
	}

	private void doDelete() {
    	DeleteScreen delScreen = new DeleteScreen(getBApp(), de, 3);
        getBApp().push(delScreen, TRANSITION_LEFT);					
	}
	
	public String toString() {
		if (de != null)
			return de.getStrippedFilename();
		else
			return screenTitle;
	}

}

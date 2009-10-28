package com.unwiredappeal.tivo.streambaby;

import java.io.File;

import com.tivo.hme.bananas.BApplicationPlus;
import com.unwiredappeal.tivo.dir.DirEntry;
import com.unwiredappeal.tivo.utils.Log;

public class DeleteScreen extends ButtonScreen {

	public String screenTitle = "Delete";
	private DirEntry de;
	int pops = 1;
	

	public DeleteScreen(BApplicationPlus app, DirEntry de, int pops) {
		super(app);
		this.pops = pops;
		this.de = de;
	}
	
	@Override
	public void render() {
	}

	@Override
	protected int setupButtons(boolean isReturn) {
		
		addSimpleTextButton("Don't do anything", new ButtonHandler() { 
			public boolean left() {
				popBack();
				return true;
			}
			public boolean right() {
				return popBack();
			}
			public boolean select() {
				return popBack();
			}			
		}, false);
		
		addSimpleTextButton("Permanently delete", new ButtonHandler() 
		{ 
				public boolean left() 
				{
					popBack();
					return true;
				}
				public boolean right() 
				{
					SingleActionScreen.Action action = new SingleActionScreen.Action() 
					{
						public String go() 
						{
							File dFile = new File(de.uri);
							if (dFile.delete()) 
							{
								Log.info("Delete succeeded: " + de.fileName);
								return "File Deleted.";
							} 
							else 
							{
								Log.info("Delete Failed: " + de.fileName);				
								return "Deletion Failed!";
							}
						}						
					};					
					for (int i=0;i<pops;i++)
						popBack();
					getBApp().push(new SingleActionScreen(app, action, de.getStrippedFilename()), TRANSITION_LEFT);
					return true;
				}
				public boolean select() {
					SingleActionScreen.Action action = new SingleActionScreen.Action() 
					{
						public String go() 
						{
							File dFile = new File(de.uri);
							if (dFile.delete()) 
							{
								Log.info("Delete succeeded: " + de.fileName);
								return "File Deleted.";
							} 
							else 
							{
								Log.info("Delete Failed: " + de.fileName);				
								return "Deletion Failed!";
							}
						}						
					};					
					for (int i=0;i<pops;i++)
						popBack();
					getBApp().push(new SingleActionScreen(app, action, de.getStrippedFilename()), TRANSITION_LEFT);
					return true;
				}			
			}, true);		
			
		return 0;
	}

	public String toString() {
		return screenTitle;
	}

}

package com.unwiredappeal.tivo.streambaby;

import com.tivo.hme.bananas.BApplicationPlus;

public class ContinueScreen extends ButtonScreen {

	public String screenTitle = "Continue";
	public ContinueScreen(BApplicationPlus app) {
		super(app);
	}

	@Override
	public void render() {
	}

	@Override
	protected int setupButtons(boolean isReturn) {
		addSimpleTextButton("Continue", new ButtonHandler() { 
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
		}, true);		
		return 0;
	}

	public String toString() {
		return screenTitle;
	}

}

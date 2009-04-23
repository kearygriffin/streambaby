package com.unwiredappeal.tivo.streambaby;

import com.tivo.hme.bananas.BApplicationPlus;
import com.unwiredappeal.tivo.views.VText;

public class SingleActionScreen extends ContinueScreen {

	protected Action act;
	String title;
	
	public interface Action {
		public String go();
	}
	public SingleActionScreen(BApplicationPlus app, Action act, String title) {
		super(app);
		this.title = title;
		this.act = act;
	}
	@Override
	public void render() {
		String msg = act.go();
		VText message;
        message = new VText(
   	         getNormal(), SAFE_TITLE_H, (int)getHeight()/2, 1, ""
   	      );
   	      message.setFlags(RSRC_VALIGN_TOP | RSRC_HALIGN_CENTER);
   	      message.setValue(msg);
   	      message.setVisible(true);
		
	}
	
	public String toString() {
		if (title == null)
			return super.toString();
		else
			return title;
	}

}

package com.unwiredappeal.tivo.views;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.tivo.hme.bananas.BView;
import com.tivo.hme.bananas.BViewPlus;
import com.tivo.hme.sdk.Resource;
import com.unwiredappeal.tivo.views.util.ViewImageGenerator;

public class BRoundedPanel extends BViewPlus {
	List<BView> innerViews = new ArrayList<BView>();
	Rectangle innerRect;
	public BRoundedPanel(BView parent, int x, int y, int width, int height, int arc, float strokeWidth, Color borderColor, float transparency) {
		super(parent, x, y, width, height, false);
		if (transparency < 1.0f) {
			int darkoff = (int)strokeWidth/2;
			BViewPlus darkView = new BViewPlus(this, darkoff, darkoff, width-(darkoff*2), height-(darkoff*2), true);
			darkView.setResource(Color.black);
			darkView.setTransparency(transparency);
		}
		ViewImageGenerator vig = new ViewImageGenerator(getBApp());
		vig.createRoundedCorners(width, height, arc, strokeWidth, borderColor);
		innerRect = vig.getOffsetRectangle(5, 5);
		vig.drawImages(this);
	}
	
	public void remove(Resource anim) {
		clearResource();
		clearInnerView();
	}
	
	public Rectangle getInnerBounds() {
		return innerRect;
	}
	public void clearInnerView() {
		for (BView v : innerViews) {
			v.remove(null);
		}
		innerViews.clear();
	}
	
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		for (BView v : innerViews) {
			v.setVisible(b);
		}
	}

	public void addInner(BView v) {
		innerViews.add(v);
	}

}

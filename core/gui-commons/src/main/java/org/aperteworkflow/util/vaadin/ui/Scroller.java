package org.aperteworkflow.util.vaadin.ui;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ChameleonTheme;

public abstract class Scroller extends Panel{
	public Scroller(ComponentContainer container) {
		setStyleName(ChameleonTheme.PANEL_BORDERLESS);
		setScrollable(true);
		container.setSizeUndefined();
		setContent(container);
	}
	
	public void updateHeight(){
		setHeight(calculateHeight(), Sizeable.UNITS_PIXELS);
	}
	
	public abstract int calculateHeight();
}

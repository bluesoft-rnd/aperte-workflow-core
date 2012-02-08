package org.aperteworkflow.editor.stepeditor;


import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;


public class DummyApplication extends Application {

	private static final long		serialVersionUID	= 2136349026207825110L;

	@Override
	public void init() {
		Window mainWindow = new Window("");
		setMainWindow(mainWindow);
		mainWindow.removeAllComponents();
		HorizontalLayout main = new HorizontalLayout();
		main.setSpacing(true);
		main.setMargin(true);
		main.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		mainWindow.setContent(main);
	}


	
	
}

package org.aperteworkflow.widgets.refresherwrapper;

import java.util.Date;

import org.aperteworkflow.widgets.refresherwrapper.RefresherWrapper.LazyLoadComponentProvider;

import com.vaadin.Application;
import com.vaadin.ui.*;

public class LazyrefresherwrapperApplication extends Application implements LazyLoadComponentProvider {
	@Override
	public void init() {
		Window mainWindow = new Window("Lazyrefresherwrapper Application");
		Label label = new Label("Hello Vaadin user");
		mainWindow.addComponent(label);
		RefresherWrapper lrw = new RefresherWrapper(7000,this);
		mainWindow.addComponent(lrw);
		setMainWindow(mainWindow);
	}

	@Override
	public Component onComponentVisible() {
		System.out.println("Loading component at: " + new Date().toString());
		return new Label(new Date().toString());
	}

}

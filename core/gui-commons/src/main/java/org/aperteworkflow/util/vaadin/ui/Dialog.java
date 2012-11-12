package org.aperteworkflow.util.vaadin.ui;

import com.vaadin.Application;
import com.vaadin.ui.*;
import pl.net.bluesoft.util.lang.Pair;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2012-05-11
 * Time: 14:32
 */
public class Dialog extends Window {
	protected VerticalLayout contentLayout = new VerticalLayout();
	protected HorizontalLayout buttonLayout = new HorizontalLayout();

	public interface ActionListener {
		void handleAction(String action);
	}
	
	public Dialog(String title) {
		if (title != null) {
			setCaption(title);
		}
		buildLayout();
	}

	protected void buildLayout() {
		setModal(true);
		setResizable(true);
		center();

		contentLayout.setWidth("100%");
		contentLayout.setSpacing(true);
		contentLayout.setMargin(true);

		buttonLayout.setWidth("100%");
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(contentLayout);
		layout.addComponent(buttonLayout);
		setContent(layout);
		layout.setSizeUndefined();
		layout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
	}
	
	public void addDialogContent(Component component) {
		contentLayout.addComponent(component);
	}

	public void addDialogAction(String name, ActionListener actionListener) {
		addDialogAction(name, null, actionListener);
	}

	public void addDialogAction(String name, boolean closeWindow, ActionListener actionListener) {
		addDialogAction(name, closeWindow, null, actionListener);
	}

	public void addDialogAction(String name, String action, ActionListener actionListener) {
		addDialogAction(name, true, action, actionListener);
	}

	public void addDialogAction(String name, final boolean closeWindow, final String action, final ActionListener actionListener) {
		Button button = new Button(name, new Button.ClickListener() {
			public void buttonClick(Button.ClickEvent clickEvent) {
				if (actionListener != null) {
					actionListener.handleAction(action);
				}
				if (closeWindow) {
					closeWindow();
				}
			}
		});
		buttonLayout.addComponent(button);
		buttonLayout.setComponentAlignment(button, Alignment.BOTTOM_CENTER);
	}
	
	public void show(Application application) {
		application.getMainWindow().addWindow(this);
	}

	protected void closeWindow() {
		getApplication().getMainWindow().removeWindow(this);
	}
}

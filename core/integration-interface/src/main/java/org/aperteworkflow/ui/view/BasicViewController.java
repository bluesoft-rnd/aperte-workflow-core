package org.aperteworkflow.ui.view;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;

public class BasicViewController implements IViewController
{
	protected final AbstractOrderedLayout viewContainer = new VerticalLayout();
	
	private Component blankView;
	
	public BasicViewController()
	{
		viewContainer.setWidth(100, Sizeable.UNITS_PERCENTAGE);
	}
	
	public ComponentContainer getViewContainer() {
		return viewContainer;
	}

	@Override
	public void displayPreviousView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayCurrentView() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshCurrentView() {
		// TODO Auto-generated method stub
		
	}
	
	public void setBlankView(Component blankView)
	{
		this.blankView = blankView;
	}

	@Override
	public void displayBlankView() 
	{
		viewContainer.removeAllComponents();
		viewContainer.addComponent(blankView);
		
	}

}

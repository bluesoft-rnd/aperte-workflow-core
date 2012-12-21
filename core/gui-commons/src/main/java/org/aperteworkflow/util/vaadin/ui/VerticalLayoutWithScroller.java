package org.aperteworkflow.util.vaadin.ui;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class VerticalLayoutWithScroller extends Scroller{
	private int rowHeight;

	public VerticalLayoutWithScroller(int rowHeight) {
		super(new VerticalLayout());
		this.rowHeight = rowHeight;
	}
	
	@Override
	public void addComponent(Component c) {
		super.addComponent(c);
		c.setHeight(rowHeight, Sizeable.UNITS_PIXELS);
		updateHeight();
	}
	
	@Override 
	public void removeComponent(Component c) {
		super.removeComponent(c);
		updateHeight();
	}
	
	@Override
	public void updateHeight(){
		setHeight(calculateHeight(), Sizeable.UNITS_PIXELS);
	}
	
	@Override
	public int calculateHeight(){
		return (((VerticalLayout)getContent()).getComponentCount() * (rowHeight + 5) + 15);
	}
}
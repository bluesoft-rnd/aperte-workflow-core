package org.aperteworkflow.ui.view;

import pl.net.bluesoft.rnd.processtool.event.IEvent;

public class ViewEvent implements IEvent {
	
	public enum Type implements TypeMarker {
		ACTION_COMPLETE
	}
	
	private Type type;

	public ViewEvent(Type type) {
		super();
		this.type = type;
	}

	public Type getEventType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}	
}

package org.aperteworkflow.webapi.main.processes.domain;

import java.util.HashMap;
import java.util.Map;

public class HtmlWidgetData 
{
	private String widgetId;
	
	private Map<String, String> attributes = new HashMap<String, String>();

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	

}

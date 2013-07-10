package org.aperteworkflow.webapi.main.processes.domain;

import java.util.Map;

public class HtmlWidget 
{
	private Long widgetId;
	private String widgetName;
	private Map<String, String> data;
	


	public Long getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(Long widgetId) {
		this.widgetId = widgetId;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public String getWidgetName() {
		return widgetName;
	}

	public void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}

	

}

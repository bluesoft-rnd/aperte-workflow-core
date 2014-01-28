package org.aperteworkflow.webapi.main.processes.domain;

import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetDataEntry;

import java.util.Collection;

public class HtmlWidget 
{
	private Long widgetId;
	private String widgetName;
	private Collection<WidgetDataEntry> data;
	


	public Long getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(Long widgetId) {
		this.widgetId = widgetId;
	}

    public Collection<WidgetDataEntry> getData() {
        return data;
    }

    public void setData(Collection<WidgetDataEntry> data) {
        this.data = data;
    }

    public String getWidgetName() {
		return widgetName;
	}

	public void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}

	

}

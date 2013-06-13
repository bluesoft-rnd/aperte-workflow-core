package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.ui.IWidgetContentProvider;

/**
 * Widget in-memory model
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ProcessHtmlWidget 
{
	private String widgetName;
	private IWidgetDataHandler dataHandler;
	private IWidgetValidator validator;
	private IWidgetContentProvider contentProvider;
	
	public String getWidgetName() {
		return widgetName;
	}
	public void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}
	public IWidgetDataHandler getDataHandler() {
		return dataHandler;
	}
	public void setDataHandler(IWidgetDataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}
	public IWidgetValidator getValidator() {
		return validator;
	}
	public void setValidator(IWidgetValidator validator) {
		this.validator = validator;
	}
	public IWidgetContentProvider getContentProvider() {
		return contentProvider;
	}
	public void setContentProvider(IWidgetContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((widgetName == null) ? 0 : widgetName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessHtmlWidget other = (ProcessHtmlWidget) obj;
		if (widgetName == null) {
			if (other.widgetName != null)
				return false;
		} else if (!widgetName.equals(other.widgetName))
			return false;
		return true;
	}
	
	
	
	

}

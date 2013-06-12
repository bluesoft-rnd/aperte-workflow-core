package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

/**
 * Widget data handler
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IWidgetDataHandler 
{
	/** Handle widget data change */
	void handleWidgetData(ProcessInstance processInstance, Map<String, String> data);

}

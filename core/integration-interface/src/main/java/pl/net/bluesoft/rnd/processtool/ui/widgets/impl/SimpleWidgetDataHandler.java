package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;

/**
 * Simple data handler mapping given data to simple attributes 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class SimpleWidgetDataHandler implements IWidgetDataHandler 
{

	@Override
	public void handleWidgetData(BpmTask bpmTask, Map<String, String> data) 
	{
		for(String key: data.keySet())
			bpmTask.getProcessInstance().setSimpleAttribute(key, data.get(key));
	}

}

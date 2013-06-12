package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

/**
 * Simple data handler mapping given data to simple attributes 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class SimpleWidgetDataHandler implements IWidgetDataHandler 
{

	@Override
	public void handleWidgetData(ProcessInstance processInstance, Map<String, String> data) 
	{
		for(String key: data.keySet())
			processInstance.setSimpleAttribute(key, data.get(key));
	}

}

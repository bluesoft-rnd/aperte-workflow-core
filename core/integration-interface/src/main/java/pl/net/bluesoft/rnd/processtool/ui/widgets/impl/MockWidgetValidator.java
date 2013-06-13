package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetValidator;

public class MockWidgetValidator implements IWidgetValidator {

	@Override
	public Collection<String> validate(BpmTask task,Map<String, String> data) 
	{
		//NOP
		return new ArrayList<String>();
	}

}

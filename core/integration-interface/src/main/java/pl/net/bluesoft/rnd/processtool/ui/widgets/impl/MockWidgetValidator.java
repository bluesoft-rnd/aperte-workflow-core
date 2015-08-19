package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetValidator;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetData;

public class MockWidgetValidator implements IWidgetValidator {


    @Override
    public Collection<String> validate(IAttributesProvider task, WidgetData widgetData) {
        return new ArrayList<String>();
    }
}

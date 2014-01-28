package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.*;

/**
 * Simple data handler mapping given data to simple attributes 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class SimpleWidgetDataHandler implements IWidgetDataHandler
{
    private static final String TYPE_SIMPLE = "simple";
    private static final String TYPE_SIMPLE_LARGE = "large";

    private IKeysToIgnoreProvider keysToIgnoreProvider = null;

    @Override
    public Collection<HandlingResult> handleWidgetData(BpmTask task, WidgetData data)
    {
        ProcessInstance process = task.getProcessInstance();
        Collection<HandlingResult> results = new LinkedList<HandlingResult>();

        for(WidgetDataEntry widgetData: data.getWidgetDataEntries())
        {
            String key = widgetData.getKey();
            String type = widgetData.getType();


            if(keysToIgnoreProvider == null || !keysToIgnoreProvider.getKeysToIgnore().contains(key))
            {
                String oldValue = getOldValue(process, widgetData);

                String newValue = widgetData.getValue();
                if(oldValue != null && !oldValue.equals(newValue)) {
                    results.add(new HandlingResult(new Date(), key, oldValue, newValue));
                }
            }

            setNewValue(process, widgetData);
        }
        return results;
    }

    private String getOldValue(ProcessInstance process, WidgetDataEntry data)
    {
        if(TYPE_SIMPLE.equals(data.getType()))
            return process.getSimpleAttributeValue(data.getKey());
        else if(TYPE_SIMPLE_LARGE.equals(data.getType()))
            return process.getSimpleLargeAttributeValue(data.getKey());

        return null;
    }

    private void setNewValue(ProcessInstance process, WidgetDataEntry data)
    {
        if(TYPE_SIMPLE.equals(data.getType()))
            process.setSimpleAttribute(data.getKey(), data.getValue());
        else if(TYPE_SIMPLE_LARGE.equals(data.getType()))
            process.setSimpleLargeAttribute(data.getKey(), data.getValue());
    }


    public void setKeysToIgnore(IKeysToIgnoreProvider provider) {
        this.keysToIgnoreProvider = provider;
    }

}

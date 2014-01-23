package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.ui.widgets.HandlingResult;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IKeysToIgnoreProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;

/**
 * Simple data handler mapping given data to simple attributes 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class SimpleWidgetDataHandler implements IWidgetDataHandler
{
    private IKeysToIgnoreProvider keysToIgnoreProvider = null;

    @Override
    public Collection<HandlingResult> handleWidgetData(BpmTask task, Map<String, String> data)
    {
        ProcessInstance process = task.getProcessInstance();
        Collection<HandlingResult> results = new LinkedList<HandlingResult>();

        for(String key : data.keySet()) {
            if(keysToIgnoreProvider == null || !keysToIgnoreProvider.getKeysToIgnore().contains(key)) {
                String oldValue = process.getSimpleAttributeValue(key);
                if(oldValue == null) {
                    oldValue = "";
                }
                String newValue = data.get(key);
                if(!oldValue.equals(newValue)) {
                    results.add(new HandlingResult(new Date(), key, oldValue, newValue));
                }
            }
            process.setSimpleAttribute(key, data.get(key));
        }
        return results;
    }

    public void setKeysToIgnore(IKeysToIgnoreProvider provider) {
        this.keysToIgnoreProvider = provider;
    }

}

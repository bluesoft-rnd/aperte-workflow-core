package pl.net.bluesoft.rnd.processtool.ui.widgets;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class WidgetData
{
    private Collection<WidgetDataEntry> widgetDataEntries = new ArrayList<WidgetDataEntry>();

    public Collection<WidgetDataEntry> getWidgetDataEntries() {
        return widgetDataEntries;
    }

    public void addWidgetData(Collection<WidgetDataEntry> widgetData) {
        this.widgetDataEntries.addAll(widgetData);
    }

    public WidgetDataEntry getEntryByKey(String key)
    {
         for(WidgetDataEntry entry: widgetDataEntries)
             if(entry.getKey().equals(key))
                 return entry;

        return null;
    }

    public Collection<WidgetDataEntry> getEntriesByType(String type)
    {
        Collection<WidgetDataEntry> entries = new ArrayList<WidgetDataEntry>();
        for(WidgetDataEntry entry: widgetDataEntries)
            if(entry.getType().equals(type))
                entries.add(entry);

        return entries;
    }

    public void remove(String key)
    {
        WidgetDataEntry widgetDataEntry =  getEntryByKey(key);

        widgetDataEntries.remove(widgetDataEntry);
    }
}

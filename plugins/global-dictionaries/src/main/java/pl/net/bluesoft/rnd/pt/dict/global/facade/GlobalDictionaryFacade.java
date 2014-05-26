package pl.net.bluesoft.rnd.pt.dict.global.facade;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dict.IDictionaryFacade;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryItem;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryItemExt;

import java.util.*;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class GlobalDictionaryFacade implements IDictionaryFacade
{
    @Override
    public Collection<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale) {
        return getAllDictionaryItems(dictionaryName, locale, null);
    }

    @Override
    public Collection<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale,String filter)
    {
        Collection<DictionaryItem> dictionaryItems = new LinkedList<DictionaryItem>();

        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        if(ctx==null)
            throw new RuntimeException("There is no active context");

        ProcessDictionaryRegistry processDictionaryRegistry = ctx.getProcessDictionaryRegistry();
        if(processDictionaryRegistry==null)
            throw new RuntimeException("There is no dictionary registry");

        ProcessDictionary pd = processDictionaryRegistry.getDictionary(dictionaryName);
        if(pd==null )
            throw new RuntimeException("No dictionary found with name "+dictionaryName);

        String langCode = locale.getLanguage();
        List<ProcessDictionaryItem> list = pd.sortedItems(langCode);

        Collection<DictFilter> filters = parseFilters(filter);

        for(ProcessDictionaryItem pdi : list)
        {
            String desc = pdi.getDescription(locale);
            DictionaryItem dictionaryItem = new DictionaryItem();
            dictionaryItem.setKey(pdi.getKey());
            dictionaryItem.setValue(pdi.getValueForDate(new Date()).getValue(locale));
            dictionaryItem.setDescription(desc);

            for(ProcessDictionaryItemExtension extension: pdi.getValueForCurrentDate().getItemExtensions())
            {
                DictionaryItemExt dictionaryItemExt = new DictionaryItemExt();
                dictionaryItemExt.setKey(extension.getName());
                dictionaryItemExt.setValue(extension.getValue());

                dictionaryItem.getExtensions().add(dictionaryItemExt);
            }

            if(checkForFilters(dictionaryItem, filters))
                dictionaryItems.add(dictionaryItem);
        }

        return dictionaryItems;
    }


    private boolean checkForFilters(DictionaryItem item, Collection<DictFilter> filters)
    {
        for(DictFilter dictFilter: filters)
            if(!checkForFilter(item, dictFilter))
                return false;

        return true;
    }

    private boolean checkForFilter(DictionaryItem item, DictFilter filter)
    {
        for(DictionaryItemExt ext: item.getExtensions())
            if(ext.getKey().equals(filter.getKey()) && ext.getValue().equals(filter.getValue()))
                return true;

        return false;
    }

    private Collection<DictFilter> parseFilters(String query)
    {
        Collection<DictFilter> filters = new LinkedList<DictFilter>();
        if(query == null  || query.isEmpty())
            return filters;

        String[] parts = query.split("[,;]");
        for (String part : parts) {
            String[] assignment = part.split("[:=]");
            if (assignment.length != 2)
                continue;

            if (assignment[1].startsWith("\"") && assignment[1].endsWith("\""))
                assignment[1] = assignment[1].substring(1, assignment[1].length() - 1);

            String key = assignment[0];
            String value = assignment[1];

            DictFilter dictFilter = new DictFilter();
            dictFilter.setKey(key);
            dictFilter.setValue(value);

            filters.add(dictFilter);
        }

        return filters;
    }

    private class DictFilter
    {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

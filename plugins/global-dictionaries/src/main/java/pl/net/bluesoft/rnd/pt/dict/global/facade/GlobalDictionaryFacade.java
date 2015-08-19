package pl.net.bluesoft.rnd.pt.dict.global.facade;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryItem;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryItemExt;
import pl.net.bluesoft.rnd.processtool.dict.IDictionaryFacade;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;

import java.text.Collator;
import java.util.*;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class GlobalDictionaryFacade implements IDictionaryFacade
{
    private static final String KEY_FILTER = "key";
    private static final String VALUE_FILTER = "value";

    @Override
    public Collection<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale) {
        return getAllDictionaryItems(dictionaryName, locale, null, null);
    }

    @Override
    public Collection<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale, String filter){
        return getAllDictionaryItems(dictionaryName, locale, filter, null, null);
    }

    @Override
    public Collection<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale, String filter, Date date){
        return getAllDictionaryItems(dictionaryName, locale, filter, date, null);
    }

    public List<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale,String filter, Date date, String sortBy)
    {
        List<DictionaryItem> dictionaryItems = new LinkedList<DictionaryItem>();

        ProcessDictionary pd = fetchDictionary(dictionaryName);

        String langCode = locale.getLanguage();
        List<ProcessDictionaryItem> list = pd.sortedItems(langCode);

        Collection<DictFilter> filters = parseFilters(filter);

        for (ProcessDictionaryItem pdi : list)
        {
            String desc = pdi.getDescription(locale);
            DictionaryItem dictionaryItem = new DictionaryItem();
            dictionaryItem.setKey(pdi.getKey());
            ProcessDictionaryItemValue value = pdi.getValueForDate(date != null ? date : new Date());
            if (value != null)
                dictionaryItem.setValue(value.getValue(locale));
            else {
                dictionaryItem.setValue(pdi.getKey());
            }
            dictionaryItem.setDescription(desc);
			dictionaryItem.setValidFrom(value.getValidFrom());
			dictionaryItem.setValidTo(value.getValidTo());

            ProcessDictionaryItemValue valueForDate = pdi.getValueForDate(date);
            if (valueForDate == null || valueForDate instanceof ProcessDBDictionaryItem.EMPTY_VALUE) {
				dictionaryItem.setValid(false);
			}
            else {
				dictionaryItem.setValid(true);
			}

            if (pdi.getValueForCurrentDate() != null) {
				for (ProcessDictionaryItemExtension extension : pdi.getValueForCurrentDate().getItemExtensions()) {
					DictionaryItemExt dictionaryItemExt = new DictionaryItemExt();
					dictionaryItemExt.setKey(extension.getName());
					dictionaryItemExt.setValue(extension.getValue());

					dictionaryItem.getExtensions().add(dictionaryItemExt);
				}
			}

            if(checkForFilters(dictionaryItem, filters) && dictionaryItem.getisValid()) {
				dictionaryItems.add(dictionaryItem);
			}
        }

        /** Sorting order given, sort items by key, value or extenstion key */
        if(sortBy != null) {
            Comparator<DictionaryItem> comparator = createComparator(sortBy, locale);

            if (comparator != null)
                Collections.sort(dictionaryItems, comparator);
        }

        return dictionaryItems;
    }

    private ProcessDictionary fetchDictionary(String dictionaryName) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        if (ctx==null)
            throw new RuntimeException("There is no active context");

        ProcessDictionaryRegistry processDictionaryRegistry = ctx.getProcessDictionaryRegistry();
        if (processDictionaryRegistry==null)
            throw new RuntimeException("There is no dictionary registry");

        ProcessDictionary pd = processDictionaryRegistry.getDictionary(dictionaryName);
        if (pd==null) {
			throw new RuntimeException("No dictionary found with name " + dictionaryName);
		}
        return pd;
    }

    @Override
    public DictionaryItem getDictionaryItem(String dictionaryName, String key, Locale locale)
    {
        Collection<DictionaryItem> items = getAllDictionaryItems(dictionaryName, locale);
        for(DictionaryItem item: items)
            if(item.getKey().equals(key))
                return item;

        return null;
    }

    @Override
    public DictionaryItem getDictionaryItemForDate(String dictionaryName, String key, Locale locale, Date date) {
        Collection<DictionaryItem> items = getAllDictionaryItems(dictionaryName, locale, null, date);
        for(DictionaryItem item: items)
            if(item.getKey().equals(key))
                return item;

        return null;
    }

    @Override
    public Collection<DictionaryItem> getFlatDictionaryItemsList(String dictionaryName, Locale locale, String filter) {
        List<DictionaryItem> dictionaryItems = new LinkedList<DictionaryItem>();

        ProcessDictionary pd = fetchDictionary(dictionaryName);

        String langCode = locale.getLanguage();
        List<ProcessDictionaryItem> list = pd.sortedItems(langCode);

        Collection<DictFilter> filters = parseFilters(filter);

        for (ProcessDictionaryItem pdi : list)
        {
            for (ProcessDictionaryItemValue value : pdi.values()) {
                String desc = pdi.getDescription(locale);
                DictionaryItem dictionaryItem = new DictionaryItem();
                dictionaryItem.setKey(pdi.getKey());

                if (value != null)
                    dictionaryItem.setValue(value.getValue(locale));
                else {
                    dictionaryItem.setValue(pdi.getKey());
                }

                dictionaryItem.setDescription(desc);
                dictionaryItem.setValidFrom(value.getValidFrom());
                dictionaryItem.setValidTo(value.getValidTo());

                dictionaryItem.setValid(true);


                for (ProcessDictionaryItemExtension extension : value.getItemExtensions()) {
                    DictionaryItemExt dictionaryItemExt = new DictionaryItemExt();
                    dictionaryItemExt.setKey(extension.getName());
                    dictionaryItemExt.setValue(extension.getValue());

                    dictionaryItem.getExtensions().add(dictionaryItemExt);
                }


                if(checkForFilters(dictionaryItem, filters) && dictionaryItem.getisValid()) {
                    dictionaryItems.add(dictionaryItem);
                }
            }

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
        if(filter.getKey().equals(KEY_FILTER)) {
            String itemKey = item.getKey().toLowerCase();
            if (itemKey.contains(filter.getValue()))
                return true;
        }

        if(filter.getKey().equals(VALUE_FILTER))
            if(item.getValue().toLowerCase().contains(filter.getValue()))
                return true;

        for(DictionaryItemExt ext: item.getExtensions())
            if(ext.getKey().toLowerCase().equals(filter.getKey()) && ext.getValue().toLowerCase().equals(filter.getValue()))
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
            dictFilter.setKey(key.toLowerCase());
            dictFilter.setValue(value.toLowerCase());

            filters.add(dictFilter);
        }

        return filters;
    }

    private Comparator<DictionaryItem> createComparator(final String sortBy, final Locale locale)
    {
        if(StringUtils.isEmpty(sortBy))
            return null;

        final Collator collator = Collator.getInstance(locale);

        if(sortBy.equals(KEY_FILTER))
            return new Comparator<DictionaryItem>() {
                @Override
                public int compare(DictionaryItem o1, DictionaryItem o2) {
                    return collator.compare(o1.getKey(), o2.getKey());
                }
            };
        else if(sortBy.equals(VALUE_FILTER))

            return new Comparator<DictionaryItem>() {
                @Override
                public int compare(DictionaryItem o1, DictionaryItem o2) {
                    return collator.compare(o1.getValue(), o2.getValue());
                }
            };
        else
            return new Comparator<DictionaryItem>() {
                @Override
                public int compare(DictionaryItem o1, DictionaryItem o2)
                {
                    DictionaryItemExt o1Ext = o1.getExtensionByKey(sortBy);
                    DictionaryItemExt o2Ext = o2.getExtensionByKey(sortBy);
                    if(o1Ext == null && o2Ext == null)
                        return 0;
                    else if(o1Ext == null)
                        return -1;
                    else if(o2Ext == null)
                        return 1;
                    else
                        return collator.compare(o1Ext.getValue(), o2Ext.getValue());
                }
            };

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

    private class KeySorter implements Comparator<DictionaryItem>
    {

        @Override
        public int compare(DictionaryItem o1, DictionaryItem o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    private class ValueSorter implements Comparator<DictionaryItem>
    {

        @Override
        public int compare(DictionaryItem o1, DictionaryItem o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }
}

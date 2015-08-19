package pl.net.bluesoft.rnd.processtool.dict.mapping.providers;

import org.apache.commons.beanutils.ConvertUtilsBean;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.dict.exception.DictItemHasNoValueException;
import pl.net.bluesoft.rnd.processtool.dict.mapping.DictEntryFilter;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.PTDictDescription;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.entry.EntryInfo;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.entry.ExtInfo;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.math.BigDecimal;
import java.util.*;

import static java.lang.String.valueOf;
import static pl.net.bluesoft.util.lang.Classes.*;

/**
 * User: POlszewski
 * Date: 2012-01-03
 * Time: 23:34:50
 */
public abstract class PTDictEntryProvider implements DictEntryProvider {
	protected PTDictDescription dictDesc;

	private Map<String, ?> entries;
	private EntryInfo entryInfo;
	private I18NSource i18NSource;
	private Date entriesDate;
	
	private Collection<String> errorMessages = new HashSet<String>();
	
	private ProcessDictionary dict;

	protected PTDictEntryProvider(PTDictDescription dictDesc) {
		this.dictDesc = dictDesc;
	}

	@Override
	public Map getEntries() {
		return getEntries(null);
	}
	
	@Override
	public Collection<String> getErrorMessages()
	{
		return errorMessages;
	}

	@Override
	public Map<String, ?> getEntries(DictEntryFilter entryFilter) {
		if (entryFilter != null) {
			Map<String, Object> result = new HashMap<String, Object>();
			for (Map.Entry<String, ?> e : entries.entrySet()) {
				if (entryFilter.filter(e.getValue())) {
					result.put(e.getKey(), e.getValue());
				}
			}
			return result;
		}
		return entries;
	}

	@Override
	public void prepareEntries(DictEntryProviderParams params) {
		if (entries != null) {
			return;
		}

		if (dictDesc.getEntryClass() != null) {
			this.entryInfo = params.getDictMapper().getEntryInfo(dictDesc.getEntryClass());
		}
		this.i18NSource = params.getI18NSource();

		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		ProcessDictionaryRegistry processDictionaryRegistry = ctx.getProcessDictionaryRegistry();
		dict = getDictionary(processDictionaryRegistry, params);
		
		/* Save entries value date */
		entriesDate = params.getDate();

		if (dict != null) {
			if (dictDesc.getEntryClass() != null) {
				entries = getDictionaryItemMap(
						dict,
						entryInfo,
						entriesDate);
			}
			else {
				entries = getDictionaryItemMap(dict, entriesDate);
			}
		}
		else {
			entries = new HashMap<String, Object>();
		}
	}

	protected abstract ProcessDictionary getDictionary(ProcessDictionaryRegistry processDictionaryRegistry, DictEntryProviderParams params);

	@Override
	public Map<String, ?> getKeyValueMap() {
		return getKeyValueMap(null);
	}

	@Override
	public Map<String, ?> getKeyValueMap(DictEntryFilter entryFilter) {
		if (dictDesc.getCustomValueProvider() != null || dictDesc.getEntryClass() != null) {
			Map<String, Object> result = new HashMap<String, Object>();
			for (String key : entries.keySet()) {
				if (entryFilter == null || entryFilter.filter(entries.get(key))) {
					result.put(key, getMapValue(key));
				}
			}
			return result;
		}
		return getEntries(entryFilter);
	}

	@Override
	public Object getValue(String key) {
		return getKeyValueMap().get(key);
	}

	private Object getMapValue(String key) 
	{
		if(dictDesc.getCustomValueProvider() != null)
			return dictDesc.getCustomValueProvider().getValue(key, this, i18NSource);
		
		Object entry = entries.get(key);
		
		/* There is no entry value for dictionry item, throw exception */
		if(entry == null) {
			throw new DictItemHasNoValueException(i18NSource.getMessage("dictionary.novaluefor", "item", key, entriesDate));
		}

		if(entryInfo.getDescriptionProperty() != null)
			return getProperty(entry, entryInfo.getDescriptionProperty());
		
		if(entryInfo.getValueProperty() != null)
			return getProperty(entry, entryInfo.getValueProperty());
		
		return key;
	}	

	private Map<String, Object> getDictionaryItemMap(ProcessDictionary dict, EntryInfo entryInfo, Date date) {
		Map<String, Object> items = new HashMap<String, Object>();

        for (Object item : dict.items()) {
            ProcessDictionaryItem pdItem = (ProcessDictionaryItem)item; 
            ProcessDictionaryItemValue value = date != null ? pdItem.getValueForDate(date) : pdItem.getValueForCurrentDate();
            
            if(value != null)
            {
				Object mappedItem = mapTo(entryInfo, pdItem, value);
				items.put(getKey(pdItem), mappedItem);
            }
            else
            {
            	errorMessages.add(i18NSource.getMessage("dictionary.novaluefor", "item", pdItem.getKey(), entriesDate));
            }
        }
        return items;
    }

	private Map<String, String> getDictionaryItemMap(ProcessDictionary dict, Date date) {
		Map<String, String> items = new HashMap<String,String>();

        for (Object item : dict.items()) {
            ProcessDictionaryItem pdItem = (ProcessDictionaryItem)item;
            ProcessDictionaryItemValue value = date != null ? pdItem.getValueForDate(date) : pdItem.getValueForCurrentDate();
			items.put(getKey(pdItem), value != null ? valueOf(value.getValue(i18NSource.getLocale())) : null);
		}
		return items;
	}

	private Object mapTo(EntryInfo entryInfo, ProcessDictionaryItem pdItem, ProcessDictionaryItemValue value) {
		if (value == null) {
			return null;
		}
		Object item = newInstance(entryInfo.getEntryClass());

		if (entryInfo.getKeyProperty() != null) {
			setProperty(item, entryInfo.getKeyProperty(), convert(pdItem.getKey(), entryInfo.getKeyType(), null));
		}
		if (entryInfo.getValueProperty() != null) {
			setProperty(item, entryInfo.getValueProperty(), convert(getValue(value), entryInfo.getValueType(), null));
		}
		if (entryInfo.getDescriptionProperty() != null) {
			setProperty(item, entryInfo.getDescriptionProperty(), convert(pdItem.getDefaultDescription(), entryInfo.getDescriptionType(), null));
		}
		for (ExtInfo extInfo : entryInfo.getExtInfos()) {
			Object extValue;
			if (extInfo.getElementClass() != null) {
				extValue = convert(getExtValue(value, extInfo.getName()), extInfo.getType(), extInfo.getElementClass(), extInfo.getSeparator(), extInfo.isDefaultNull());
			}
			else {
				extValue = convert(getExtValue(value, extInfo.getName()), extInfo.getType(), extInfo.getDefaultValue());
			}
			setProperty(item, extInfo.getProperty(), extValue);
		}
		return item;
	}

	public static String getKey(ProcessDictionaryItem pdItem) {
		return  pdItem.getKey() != null ? valueOf(pdItem.getKey()) : null;
	}

	private Object getValue(ProcessDictionaryItemValue value) {
		return value != null ? value.getValue(i18NSource.getLocale()) : null;
	}

	private Object getExtValue(ProcessDictionaryItemValue value, String name) {
		if (value == null) {
			return null;
		}
		return value.getExtValue(name);
	}

	private Object convert(Object value, Class<?> type, String defaultValue) {
		if (value == null) {
			if (defaultValue == null) {
				return null;
			}
			value = defaultValue;
		}
		String val = valueOf(value);
		if (Arrays.asList(double.class, float.class, Double.class, Float.class, BigDecimal.class).contains(type)) {
			val = val.replace(',', '.').replaceAll(" ","").replaceAll("\\s", ""); // to nie jest zwykla spacja ^^
		}
		return new ConvertUtilsBean().convert(val, type);
	}

	private <T> Collection<T> convert(Object value, Class<? extends Collection> type, Class<T> elementType, String separator, boolean defaultNull) {
		if (value == null) {
			return defaultNull ? null : createCollection(type);
		}
		Collection coll = createCollection(type);
		for (String elem : valueOf(value).split(separator)) {
			coll.add(convert(elem, elementType, null));
		}
		return coll;
	}

	private static Collection createCollection(Class<? extends Collection> type) {
		if (type == Set.class) {
			return new HashSet();
		}
		if (type == List.class) {
			return new ArrayList();
		}
		throw new IllegalArgumentException("Could not create collection of type "+type);
	}

	@Override
	public Object getEntryForDate(String key, Date date) {
		if (dict != null) {
			ProcessDictionaryItem pdItem = dict.lookup(key);
			if (pdItem != null) {
				ProcessDictionaryItemValue value = date != null ? pdItem.getValueForDate(date) : pdItem.getValueForCurrentDate();
				if (dictDesc.getEntryClass() != null) {				
					return mapTo(entryInfo, pdItem, value);
				}
				else {
					return value != null ? valueOf(value.getValue(i18NSource.getLocale())) : null;
				}
			}
		}
		return null;
	}
}

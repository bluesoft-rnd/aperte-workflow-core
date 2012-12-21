package pl.net.bluesoft.rnd.processtool.dict.mapping;

import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Select;
import pl.net.bluesoft.rnd.processtool.dict.mapping.annotations.entry.*;
import pl.net.bluesoft.rnd.processtool.dict.mapping.annotations.key.DictKey;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.DictDescription;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.entry.EntryInfo;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.entry.ExtInfo;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.item.ItemInfo;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.item.PropertyInfo;
import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.DictEntryProvider;
import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.DictEntryProviderParams;
import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.LazyLoadDictEntryProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Pair;

import java.lang.reflect.Field;
import java.util.*;

import static pl.net.bluesoft.util.lang.Classes.getDeclaredFields;
import static pl.net.bluesoft.util.lang.Classes.getProperty;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 16:06:18
 */
public class DictMapper {
	private final Map<Class, ItemInfo> itemInfos = new HashMap<Class, ItemInfo>();
	private final Map<Class, EntryInfo> entryInfos = new HashMap<Class, EntryInfo>();
	private final Map<Pair<String, DictEntryFilter>, Container> dictContainers = new HashMap<Pair<String, DictEntryFilter>, Container>();
	private final Map<String, DictEntryProvider> dictEntryProviders = new HashMap<String, DictEntryProvider>();

	private final DictDescriptionBuilder dictDescr;

	private Set<String> usedDictNames = new HashSet<String>();

	public DictMapper(DictDescriptionProvider dictDescrProvider) {
		this.dictDescr = new DictDescriptionBuilder();
		dictDescrProvider.getDictionaryDescriptions(dictDescr);
	}

	public DictMapper use(Collection<String> dictNames) {
		usedDictNames.addAll(dictNames);
		return this;
	}

	public DictMapper use(String... dictNames) {
		usedDictNames.addAll(Arrays.asList(dictNames));
		return this;
	}

//	public DictMapper use(Collection<Class> itemClasses) {
//		usedDictNames.addAll(getDictNames(itemClasses));
//		return this;
//	}

	public DictMapper use(Class... itemClasses) {
		usedDictNames.addAll(getDictNames(Arrays.asList(itemClasses)));
		return this;
	}

	public DictMapper except(Collection<String> dictNames) {
		usedDictNames.removeAll(Arrays.asList(dictNames));
		return this;
	}

	public DictMapper except(String... dictNames) {
		usedDictNames.removeAll(Arrays.asList(dictNames));
		return this;
	}

	public Collection<String> getDictNames(Collection<Class> itemClasses) {
		Set<String> dictNames = new HashSet<String>();
		for (Class clazz : itemClasses) {
			for (PropertyInfo propInfo : getItemInfo(clazz).getPropertyInfos()) {
				dictNames.add(propInfo.getDictName());
			}
		}
		return dictNames;
	}

	public void prepareDictionaries(ProcessInstance processInstance, Date date, I18NSource i18NSource) {
		prepareDictionariesHelper(processInstance, date, i18NSource, getDictEntryProviders(topoSort(expandDependences(usedDictNames))));
	}

	private void prepareDictionariesHelper(ProcessInstance processInstance, Date date, I18NSource i18NSource, Collection<DictEntryProvider> dictEntryProviders) {
		DictEntryProviderParams params = new DictEntryProviderParams();
		params.setProcessInstance(processInstance);
		params.setDate(date);
		params.setI18NSource(i18NSource);
		params.setDictMapper(this);
		for (DictEntryProvider entryProvider : dictEntryProviders) {
			entryProvider.prepareEntries(params);
		}
	}

	private Collection<String> expandDependences(Collection<String> dictNames) {
		dictNames = new HashSet<String>(dictNames);
		while (true) {
			Set<String> toAdd = new HashSet<String>();
			for (String dictName : dictNames) {
				for (String dep : dictDescr.getDictDescription(dictName).getBaseDictionaries()) {
					if (!dictNames.contains(dep)) {
						toAdd.add(dep);
					}
				}
			}
			if (toAdd.isEmpty()) {
				break;
			}
			dictNames.addAll(toAdd);
		}
		return dictNames;
	}

	private List<String> topoSort(Collection<String> dictNames) {
		dictNames = new HashSet<String>(dictNames);
		List<String> result = new ArrayList<String>();
		while (!dictNames.isEmpty()) {
			boolean found = false;
			for (String dictName : dictNames) {
				if (result.containsAll(dictDescr.getDictDescription(dictName).getBaseDictionaries())) {
					result.add(dictName);
					dictNames.remove(dictName);
					found = true;
					break;
				}
			}
			if (!found) {
				throw new RuntimeException("Dictionaries contain cyclic dependency");
			}
		}
		return result;
	}

	private List<DictEntryProvider> getDictEntryProviders(Collection<String> dictNames) {
		List<DictEntryProvider> entryProviders = new ArrayList<DictEntryProvider>();
		for (String dictName : dictNames) {
			entryProviders.add(getDictEntryProvider(dictName));
		}
		return entryProviders;
	}

	public Select createSelect(Object item, String property) {
		return createSelect(new Select(), item, property, null);
	}

	public <S extends AbstractSelect> S createSelect(S select, Object item, String property) {
		return createSelect(select, item, property, null);
	}

	public Select createSelect(Object item, String property, DictEntryFilter entryFilter) {
		return createSelect(new Select(), item, property, entryFilter);
	}
	                                    
	public <S extends AbstractSelect> S createSelect(S select, Object item, String property, DictEntryFilter entryFilter) {
		select.setContainerDataSource(getContainer(item, property, entryFilter));
        select.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
        select.setItemCaptionPropertyId("name");
		return select;
	}

	public Select createSelect(String dictName) {
		return createSelect(new Select(), dictName, (DictEntryFilter)null);
	}

	public <S extends AbstractSelect> S createSelect(S select, String dictName, String property) {
		return createSelect(select, dictName, (DictEntryFilter)null);
	}

	public Select createSelect(String dictName, DictEntryFilter entryFilter) {
		return createSelect(new Select(), dictName, entryFilter);
	}

	public <S extends AbstractSelect> S createSelect(S select, String dictName, DictEntryFilter entryFilter) {
		select.setContainerDataSource(getContainer(dictName, entryFilter));
		select.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
		select.setItemCaptionPropertyId("name");
		return select;
	}

	public Map<String, ?> getEntries(String dictName) {
		return dictEntryProviders.get(dictName).getEntries();
	}

	public <T> Map<String, T> getEntries(String dictName, Class<T> clazz) {
		return dictEntryProviders.get(dictName).getEntries();
	}

	public Map<String, ?> getEntries(Object item, String property) {
		return getDictEntryProvider(item, property).getEntries();
	}

	public <T> Map<String, T> getEntries(Object item, String property, Class<T> clazz) {
		return getDictEntryProvider(item, property).getEntries();
	}
	
	public Object getEntryForDate(String dict, String key, Date date) {
		return getDictEntryProvider(dict).getEntryForDate(key, date);
	}

	public Object getEntryForDate(Object item, String property, String key, Date date) {
		return getDictEntryProvider(item, property).getEntryForDate(key, date);
	}

	public Object getValue(String dictName, String key) {
		return dictEntryProviders.get(dictName).getValue(key);
	}

	public Object getValue(Object item, String property) {
		Object key = getProperty(item, property);
		if (key != null) {
			return getDictEntryProvider(item, property).getValue(String.valueOf(key));
		}
		return null;
	}

	private PropertyInfo getPropertyInfo(Object item, String property) {
		PropertyInfo propInfo = getItemInfo(item.getClass()).getPropertyInfo(property);

		if (propInfo == null) {
			throw new RuntimeException(String.format("No dictionary info for property %s of class %s", property, item.getClass().getSimpleName()));
		}
		return propInfo;
	}

	public Container getContainer(Object item, String property) {
		return getContainer(item, property, null);
	}

	public Container getContainer(Object item, String property, DictEntryFilter entryFilter) {
		return getContainer(getPropertyInfo(item, property).getDictName(), entryFilter);
	}
	
	public Container getContainer(String dictName, DictEntryFilter entryFilter) {
		Pair<String, DictEntryFilter> key = new Pair<String, DictEntryFilter>(dictName, entryFilter);
		if (!dictContainers.containsKey(key)) {
			Map map = getKeyValueMap(dictName, entryFilter);
			dictContainers.put(key, createContainer(map));
		}
		return dictContainers.get(key);
	}

	public Map getKeyValueMap(String dictName) {
		return getDictEntryProvider(dictName).getKeyValueMap();
	}

	public Map getKeyValueMap(String dictName, DictEntryFilter entryFilter) {
		return getDictEntryProvider(dictName).getKeyValueMap(entryFilter);
	}

	private IndexedContainer createContainer(Map<?,?> elements) {
        IndexedContainer cont = new IndexedContainer();
        cont.addContainerProperty("name", String.class, "");
        for (Map.Entry<?,?> element : elements.entrySet()) {
            cont.addItem(element.getKey());
            cont.getItem(element.getKey()).getItemProperty("name").setValue(element.getValue());
        }
        cont.sort(new Object[]{ "name" }, new boolean[]{ true });
        return cont;
    }

	private ItemInfo getItemInfo(Class clazz) {
		if (!itemInfos.containsKey(clazz)) {
			itemInfos.put(clazz, createItemInfo(clazz));
		}
		return itemInfos.get(clazz);
	}

	private ItemInfo createItemInfo(Class clazz) {// 1 provider na slownik -> keszowanie
		ItemInfo itemInfo = new ItemInfo();
		for (Field field : getDeclaredFields(clazz)) {
			DictKey dictKey = field.getAnnotation(DictKey.class);
			if (dictKey != null) {
				PropertyInfo propInfo = new PropertyInfo();
				propInfo.setProperty(field.getName());
				propInfo.setDictName(dictKey.dict());
				propInfo.setDictEntryProvider(getDictEntryProvider(dictKey.dict()));
				itemInfo.addPropertyInfo(propInfo);
			}
		}
		return itemInfo;
	}

	private DictEntryProvider getDictEntryProvider(String dictName) {
		if (!dictEntryProviders.containsKey(dictName)) {
			DictDescription desc = dictDescr.getDictDescription(dictName);
			DictEntryProvider provider = desc.isLazyLoad()
					? new LazyLoadDictEntryProvider(desc)
					: desc.createDictEntryProvider();

			if (provider == null) {
				throw new RuntimeException("Unable to determine DictionaryEntryProvider from given parameters");
			}
			dictEntryProviders.put(desc.getName(), provider);
		}
		return dictEntryProviders.get(dictName);
	}

	private DictEntryProvider getDictEntryProvider(Object item, String property) {
		return getPropertyInfo(item, property).getDictEntryProvider();
	}
		                     
	public EntryInfo getEntryInfo(Class clazz) {
		if (!entryInfos.containsKey(clazz)) {
			EntryInfo entryInfo = new EntryInfo();
			entryInfo.setEntryClass(clazz);
			for (Field field : getDeclaredFields(clazz)) {
				if (field.getAnnotation(Key.class) != null) {
					entryInfo.setKeyProperty(field.getName());
					entryInfo.setKeyType(field.getType());
				}
				else if (field.getAnnotation(Value.class) != null) {
					entryInfo.setValueProperty(field.getName());
					entryInfo.setValueType(field.getType());
				}
				else if (field.getAnnotation(Description.class) != null) {
					entryInfo.setDescriptionProperty(field.getName());
					entryInfo.setDescriptionType(field.getType());
				}
				else {
					Ext ext = field.getAnnotation(Ext.class);
					if (ext != null) {
						ExtInfo extInfo = new ExtInfo();
						extInfo.setName(ext.name());
						extInfo.setProperty(field.getName());
						extInfo.setType(field.getType());
						Default def = field.getAnnotation(Default.class);
						if (def != null) {
							extInfo.setDefaultValue(def.value());
						}
						ComplexContent compl = field.getAnnotation(ComplexContent.class);
						if (compl != null) {
							extInfo.setElementClass(compl.elementClass());
							extInfo.setSeparator(compl.separator());
							extInfo.setDefaultNull(compl.defaultNull());
						}
						entryInfo.addExtInfo(extInfo);
					}
				}
			}
			entryInfos.put(clazz, entryInfo);
		}
		return entryInfos.get(clazz);
	}
}

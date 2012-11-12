package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.UniquePredicate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDictionaryDAO;
import pl.net.bluesoft.rnd.processtool.dict.GlobalDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.db.*;
import pl.net.bluesoft.util.cache.Caches;
import pl.net.bluesoft.util.lang.ExpiringCache;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class ProcessDictionaryDAOImpl extends SimpleHibernateBean<ProcessDBDictionary> implements ProcessDictionaryDAO,
        ProcessDictionaryProvider<ProcessDBDictionary>, GlobalDictionaryProvider<ProcessDBDictionary> {
    public ProcessDictionaryDAOImpl(Session session) {
        super(session);
    }

    private static final Logger logger = Logger.getLogger(ProcessDictionaryDAOImpl.class.getName());
    private static final ExpiringCache<DictionaryCacheKey, ProcessDBDictionary> cache = new ExpiringCache<DictionaryCacheKey, ProcessDBDictionary>(60 * 60 * 1000);

	private static class DictionaryCacheKey {
		private String definitionId;
		private String dictionaryId;
		private String languageCode;
		private Boolean defaultDictionary;

		public DictionaryCacheKey(ProcessDefinitionConfig definition, String dictionaryId, String languageCode) {
			this(definition, dictionaryId, languageCode, null);
		}

		public DictionaryCacheKey(ProcessDefinitionConfig definition, String dictionaryId, String languageCode,
								  Boolean defaultDictionary) {
			this(getCacheDefinitionId(definition), dictionaryId, languageCode, defaultDictionary);
		}

		public DictionaryCacheKey(String definitionId, String dictionaryId, String languageCode) {
			this(definitionId, dictionaryId, languageCode, null);
		}

		public DictionaryCacheKey(String definitionId, String dictionaryId, String languageCode, Boolean defaultDictionary) {
			this.definitionId = definitionId;
			this.dictionaryId = dictionaryId;
			this.languageCode = languageCode;
			this.defaultDictionary = defaultDictionary;
		}

		@Override
		public String toString() {
			if (defaultDictionary != null && defaultDictionary) {
				return Caches.cachedObjectId(definitionId, dictionaryId, getCacheDefaultToken(defaultDictionary));
			}
			return Caches.cachedObjectId(definitionId, dictionaryId, languageCode);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof DictionaryCacheKey) {
				return toString().equals(obj.toString());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}
	}

	private static String getCacheDefaultToken(Boolean flag) {
		return "default=" + flag;
	}

	private static String getCacheDefinitionId(ProcessDefinitionConfig config) {
		return "" + (config != null ? config.getId() : "global");
	}

    private void updateCache(Collection<ProcessDBDictionary> dictionaries) {
        for (ProcessDBDictionary dict : dictionaries) {
            updateCache(dict);
        }
    }

    private void updateCache(ProcessDBDictionary dict) {
		String definitionId = getCacheDefinitionId(dict.getProcessDefinition());
        String dictionaryId = dict.getDictionaryId();
        String languageCode = dict.getLanguageCode();
		DictionaryCacheKey key = new DictionaryCacheKey(definitionId, dictionaryId, languageCode);
		cache.put(key, dict);
        //        logger.info("Cached dictionary: " + objectId);
        if (dict.isDefaultDictionary() != null && dict.isDefaultDictionary()) {
            key = new DictionaryCacheKey(definitionId, dictionaryId, getCacheDefaultToken(dict.isDefaultDictionary()));
            cache.put(key, dict);
            //            logger.info("Cached dictionary: " + objectId);
        }
    }

    @Override
    public ProcessDBDictionary fetchProcessDictionary(final ProcessDefinitionConfig definition, final String dictionaryId,
                                                      final String languageCode) {
		DictionaryCacheKey key = new DictionaryCacheKey(definition, dictionaryId, languageCode);

		return cache.get(key, new ExpiringCache.NewValueCallback<DictionaryCacheKey, ProcessDBDictionary>() {
			@Override
			public ProcessDBDictionary getNewValue(DictionaryCacheKey key) {
                Criteria criteria = getSession().createCriteria(ProcessDBDictionary.class)
                        .add(Restrictions.eq("dictionaryId", dictionaryId))
                        .add(Restrictions.eq("languageCode", languageCode))
                        .add(definition == null ? Restrictions.isNull("processDefinition") : Restrictions.eq("processDefinition", definition));
                return (ProcessDBDictionary) criteria.uniqueResult();
            }
        });
    }

	@Override
    public ProcessDBDictionary fetchDefaultProcessDictionary(final ProcessDefinitionConfig definition, final String dictionaryId) {
		DictionaryCacheKey key = new DictionaryCacheKey(definition, dictionaryId, null, true);

		return cache.get(key, new ExpiringCache.NewValueCallback<DictionaryCacheKey, ProcessDBDictionary>() {
			@Override
			public ProcessDBDictionary getNewValue(DictionaryCacheKey key) {
				Criteria criteria = getSession().createCriteria(ProcessDBDictionary.class)
						.add(Restrictions.eq("dictionaryId", dictionaryId))
						.add(Restrictions.eq("defaultDictionary", Boolean.TRUE))
						.add(definition == null ? Restrictions.isNull("processDefinition") : Restrictions.eq("processDefinition", definition));
				return (ProcessDBDictionary)criteria.uniqueResult();
			}
		});
    }

    @Override
    public List<ProcessDBDictionary> fetchProcessDictionaries(ProcessDefinitionConfig definition) {
        Criteria criteria = getSession().createCriteria(ProcessDBDictionary.class)
                .add(definition == null ? Restrictions.isNull("processDefinition") : Restrictions.eq("processDefinition", definition))
                .addOrder(Order.desc("dictionaryName"));
        List<ProcessDBDictionary> dictionaries = criteria.list();
        updateCache(dictionaries);
        return dictionaries;
    }

    @Override
    public List<ProcessDBDictionary> fetchAllProcessDictionaries() {
        List<ProcessDBDictionary> dictionaries = getSession().createCriteria(ProcessDBDictionary.class)
                .add(Restrictions.isNotNull("processDefinition"))
                .addOrder(Order.desc("dictionaryName"))
                .list();
        updateCache(dictionaries);
        return dictionaries;
    }

    @Override
    public List<ProcessDBDictionary> fetchAllActiveProcessDictionaries() {
        List<ProcessDBDictionary> dictionaries = getSession().createCriteria(ProcessDBDictionary.class)
                .addOrder(Order.desc("dictionaryName"))
                .createCriteria("processDefinition")
                .add(Restrictions.eq("latest", Boolean.TRUE))
                .list();
        updateCache(dictionaries);
        return dictionaries;
    }

    @Override
    public List<ProcessDBDictionary> fetchAllGlobalDictionaries() {
        return fetchProcessDictionaries(null);
    }

    @Override
    public ProcessDBDictionary fetchDefaultGlobalDictionary(String dictionaryId) {
        return fetchDefaultProcessDictionary(null, dictionaryId);
    }

    @Override
    public ProcessDBDictionary fetchGlobalDictionary(String dictionaryId, String languageCode) {
        return fetchProcessDictionary(null, dictionaryId, languageCode);
    }

    @Override
    public void createOrUpdateDictionary(ProcessDefinitionConfig definition, ProcessDBDictionary dictionary, boolean overwrite) {
        createOrUpdateDictionaries(definition, Collections.singletonList(dictionary), overwrite);
    }

    public ProcessDBDictionary findDictionaryById(Long dictionaryId)
    {
        Criteria criteria = getSession().createCriteria(ProcessDBDictionary.class)
                .add(Restrictions.eq("id", dictionaryId));

        return (ProcessDBDictionary)criteria.uniqueResult();
    }

    public void createOrUpdateDictionaryItem(ProcessDBDictionary dictionary, String dictionaryItemKey, String dictionaryItemValue)
    {
    	Session session = getSession();

    	ProcessDBDictionaryItem dictionaryItem = null;

    	if(dictionary.getId() == null)
    	{
	        Criteria criteria = session.createCriteria(ProcessDBDictionaryItem.class)
	                .add(Restrictions.eq("key", dictionaryItemKey))
	                .add(Restrictions.eq("dictionary", dictionary));

	        dictionaryItem = (ProcessDBDictionaryItem)criteria.uniqueResult();
    	}
    	else
    	{
    		dictionaryItem = dictionary.getItems().get(dictionaryItemKey);
    	}

        if(dictionaryItem == null)
        {
        	dictionaryItem = new ProcessDBDictionaryItem();
        	dictionaryItem.setDictionary(dictionary);
        	dictionaryItem.setKey(dictionaryItemKey);

        	ProcessDBDictionaryItemValue itemValue = new ProcessDBDictionaryItemValue();
        	itemValue.setItem(dictionaryItem);
        	itemValue.setValue(dictionaryItemValue);
        	itemValue.setStringValue(dictionaryItemValue);
        	itemValue.setValidStartDate(new Date());

        	dictionaryItem.getValues().add(itemValue);

        	dictionary.addItem(dictionaryItem);

        	session.saveOrUpdate(dictionary);
        }
        else
        {
        	ProcessDBDictionaryItemValue currentValue = dictionaryItem.getValueForCurrentDate();
        	currentValue.setValue(dictionaryItemValue);

        	dictionaryItem.getValues().remove(currentValue);

        	ProcessDBDictionaryItemValue itemValue = new ProcessDBDictionaryItemValue();
        	itemValue.setItem(dictionaryItem);
        	itemValue.setValue(dictionaryItemValue);
        	itemValue.setStringValue(dictionaryItemValue);
        	itemValue.setValidStartDate(new Date());

        	dictionaryItem.getValues().add(itemValue);

        	session.saveOrUpdate(dictionary);
        }

        updateCache(dictionary);
    }

    @Override
    public void createOrUpdateDictionaries(ProcessDefinitionConfig definition, List<ProcessDBDictionary> newDictionaries, boolean overwrite) {
        List<ProcessDBDictionary> existingDBDictionaries = definition != null ? fetchProcessDictionaries(definition)
                : fetchAllGlobalDictionaries();
        Session session = getSession();
        for (ProcessDBDictionary newDict : newDictionaries) 
        {
        	ProcessDBDictionary existingDictionary = null;
        	for (ProcessDBDictionary existingDict : existingDBDictionaries)
        		if (existingDict.getDictionaryId().equals(newDict.getDictionaryId()) && existingDict.getLanguageCode().equals(newDict.getLanguageCode()))
        		{
        			existingDictionary = existingDict;
        			break;
        		}
        	
            boolean updated = false;
        	if(existingDictionary != null)
        	{
                if (overwrite) 
                {
                    session.delete(existingDictionary);
                } 
                else 
                {
                	existingDictionary.getPermissions().clear();
                	for(ProcessDBDictionaryPermission permission: newDict.getPermissions())
                	{
                		permission.setDictionary(existingDictionary);
                		existingDictionary.getPermissions().add(permission);
                	}
                	
                	existingDictionary.setDefaultDictionary(newDict.isDefaultDictionary());
                	existingDictionary.setDescription(newDict.getDescription());
                    for (ProcessDBDictionaryItem newItem : newDict.getItems().values()) {
                        if (!existingDictionary.getItems().containsKey(newItem.getKey())) {
                        	existingDictionary.addItem(newItem);
                        }
                    }
                    session.saveOrUpdate(existingDictionary);
                    updateCache(existingDictionary);
                    updated = true;
                }
        	}
        	
            if (!updated) {
                newDict.setProcessDefinition(definition);
                session.saveOrUpdate(newDict);
                updateCache(newDict);
            }
        }
    }

    @Override
    public void updateDictionary(ProcessDBDictionary dictionary) {
        Session session = getSession();
        session.update(dictionary);
        session.flush();
        updateCache(dictionary);
    }

//	@Override
//	public void copyDictionaries(ProcessDefinitionConfig oldDefinitionConfig,
//			ProcessDefinitionConfig newDefinitionConfig) {
//		// TODO Auto-generated method stub
//
//	}

    @Override
    public void copyDictionaries(ProcessDefinitionConfig oldDefinitionConfig,
                                 ProcessDefinitionConfig newDefinitionConfig) {
        List<ProcessDBDictionary> existingDBDictionaries = fetchProcessDictionaries(newDefinitionConfig);
        if (existingDBDictionaries != null && existingDBDictionaries.size() > 0)
            return; //słowniki już są
        List<ProcessDBDictionary> oldDBDictionariesList = fetchProcessDictionaries(oldDefinitionConfig);

        Collection<ProcessDBDictionary> oldDBDictionaries = CollectionUtils.select(oldDBDictionariesList, new UniquePredicate());

        //newDefinitionConfig.getBpmDefinitionKey()

//		PropertyFilter vetoer = new PropertyFilter() {
//		    public boolean propagate(String propertyName, Method readerMethod) {
//		        return readerMethod.getReturnType() != ProcessDefinitionConfig.class;
//		    }
//		};
//		Hibernate3BeanReplicator replicator = new Hibernate3BeanReplicator(null, null, vetoer);
//
//		for (ProcessDBDictionary existingDict : existingDBDictionaries) {
//			ProcessDBDictionary newDict = replicator.deepCopy(existingDict);
//			newDict.setProcessDefinition(newDefinitionConfig);
//			session.save(newDict);
//		}


        for (ProcessDBDictionary oldDict : oldDBDictionaries) {
            try {
                ProcessDBDictionary newDict = new ProcessDBDictionary();
                PropertyUtils.copyProperties(newDict, oldDict);
                newDict.setId(null);

                newDict.setProcessDefinition(newDefinitionConfig);

                Map<String, ProcessDBDictionaryItem> items = oldDict.getItems();
                newDict.setItems(new HashMap<String, ProcessDBDictionaryItem>());

                for (Entry<String, ProcessDBDictionaryItem> item : items.entrySet()) {
                    ProcessDBDictionaryItem newItem = new ProcessDBDictionaryItem();
                    PropertyUtils.copyProperties(newItem, item.getValue());
                    newItem.setId(null);

                    newItem.setDictionary(newDict);
                    newDict.getItems().put(item.getKey(), newItem);

                    Set<ProcessDBDictionaryItemValue> values = item.getValue().getValues();
                    newItem.setValues(new HashSet<ProcessDBDictionaryItemValue>());

                    for (ProcessDBDictionaryItemValue value : values) {
                        ProcessDBDictionaryItemValue newValue = new ProcessDBDictionaryItemValue();
                        PropertyUtils.copyProperties(newValue, value);
                        newValue.setId(null);

                        newValue.setItem(newItem);
                        newItem.getValues().add(newValue);

                        Map<String, ProcessDBDictionaryItemExtension> extensions = value.getExtensions();
                        newValue.setExtensions(new HashMap<String, ProcessDBDictionaryItemExtension>());

                        for (Entry<String, ProcessDBDictionaryItemExtension> extension : extensions.entrySet()) {
                            ProcessDBDictionaryItemExtension newExtension = new ProcessDBDictionaryItemExtension();
                            PropertyUtils.copyProperties(newExtension, extension.getValue());
                            newExtension.setId(null);

                            newExtension.setItemValue(newValue);
                            newValue.getExtensions().put(extension.getKey(), newExtension);
                        }
                    }
                }
                Set<ProcessDBDictionaryPermission> permissions = oldDict.getPermissions();
                newDict.setPermissions(new HashSet<ProcessDBDictionaryPermission>());

                for (ProcessDBDictionaryPermission permission : permissions) {
                    ProcessDBDictionaryPermission newPermission = new ProcessDBDictionaryPermission();
                    PropertyUtils.copyProperties(newPermission, permission);
                    newPermission.setId(null);

                    newPermission.setDictionary(newDict);
                    newDict.getPermissions().add(newPermission);
                }


                session.save(newDict);

            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

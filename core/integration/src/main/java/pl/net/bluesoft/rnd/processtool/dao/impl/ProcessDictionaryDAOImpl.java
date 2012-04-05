package pl.net.bluesoft.rnd.processtool.dao.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

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
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryPermission;
import pl.net.bluesoft.util.cache.Caches;
import pl.net.bluesoft.util.cache.Caches.CacheCallback;

public class ProcessDictionaryDAOImpl extends SimpleHibernateBean<ProcessDBDictionary> implements ProcessDictionaryDAO,
        ProcessDictionaryProvider<ProcessDBDictionary>, GlobalDictionaryProvider<ProcessDBDictionary> {
    public ProcessDictionaryDAOImpl(Session session) {
        super(session);
    }

    private static final Logger logger = Logger.getLogger(ProcessDictionaryDAOImpl.class.getName());
    private static final Map<String, ProcessDBDictionary> cache = Caches.synchronizedCache(100);

    private abstract class DictionaryCacheCallback extends CacheCallback<ProcessDBDictionary> {
        private ProcessDefinitionConfig definition;
        private String dictionaryId;
        private String languageCode;
        private Boolean defaultDictionary;

        public DictionaryCacheCallback(ProcessDefinitionConfig definition, String dictionaryId, String languageCode) {
            this(definition, dictionaryId, languageCode, null);
        }

        public DictionaryCacheCallback(ProcessDefinitionConfig definition, String dictionaryId, String languageCode,
                                       Boolean defaultDictionary) {
            this.definition = definition;
            this.dictionaryId = dictionaryId;
            this.languageCode = languageCode;
            this.defaultDictionary = defaultDictionary;
        }

        @Override
        protected void updateCache(Map<String, ProcessDBDictionary> cache, String objectId, ProcessDBDictionary result) {
            ProcessDictionaryDAOImpl.this.updateCache(result);
        }

        @Override
        protected String getCachedObjectId(Object... params) {
            String definitionId = getCacheDefinitionId(definition);
            if (defaultDictionary != null && defaultDictionary) {
                return Caches.cachedObjectId(definitionId, dictionaryId, getCacheDefaultToken(defaultDictionary));
            }
            return Caches.cachedObjectId(definitionId, dictionaryId, languageCode);
        }

        @Override
        protected void objectMissed(Map<String, ProcessDBDictionary> cache, String objectId) {
            logger.warning("Object missed: " + objectId + ".");
        }
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
        String objectId = Caches.cachedObjectId(definitionId, dictionaryId, languageCode);
        cache.put(objectId, dict);
        //        logger.info("Cached dictionary: " + objectId);
        if (dict.isDefaultDictionary() != null && dict.isDefaultDictionary()) {
            objectId = Caches.cachedObjectId(definitionId, dictionaryId, getCacheDefaultToken(dict.isDefaultDictionary()));
            cache.put(objectId, dict);
            //            logger.info("Cached dictionary: " + objectId);
        }
    }

    private String getCacheDefaultToken(Boolean flag) {
        return "default=" + flag;
    }

    private String getCacheDefinitionId(ProcessDefinitionConfig config) {
        return "" + (config != null ? config.getId() : "global");
    }

    @Override
    public ProcessDBDictionary fetchProcessDictionary(final ProcessDefinitionConfig definition, final String dictionaryId,
                                                      final String languageCode) {
        return new DictionaryCacheCallback(definition, dictionaryId, languageCode) {
            @Override
            protected ProcessDBDictionary fetchObject() {
                Criteria criteria = getSession().createCriteria(ProcessDBDictionary.class)
                        .add(Restrictions.eq("dictionaryId", dictionaryId))
                        .add(Restrictions.eq("languageCode", languageCode))
                        .add(definition == null ? Restrictions.isNull("processDefinition") : Restrictions.eq("processDefinition", definition));
                return (ProcessDBDictionary) criteria.uniqueResult();
            }
        }.run(cache);
    }

    @Override
    public ProcessDBDictionary fetchDefaultProcessDictionary(final ProcessDefinitionConfig definition, final String dictionaryId) {
        return new DictionaryCacheCallback(definition, dictionaryId, null, true) {
            @Override
            protected ProcessDBDictionary fetchObject() {
                Criteria criteria = getSession().createCriteria(ProcessDBDictionary.class)
                        .add(Restrictions.eq("dictionaryId", dictionaryId))
                        .add(Restrictions.eq("defaultDictionary", Boolean.TRUE))
                        .add(definition == null ? Restrictions.isNull("processDefinition") : Restrictions.eq("processDefinition", definition));
                return (ProcessDBDictionary) criteria.uniqueResult();
            }
        }.run(cache);
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

    @Override
    public void createOrUpdateDictionaries(ProcessDefinitionConfig definition, List<ProcessDBDictionary> newDictionaries, boolean overwrite) {
        List<ProcessDBDictionary> existingDBDictionaries = definition != null ? fetchProcessDictionaries(definition)
                : fetchAllGlobalDictionaries();
        Session session = getSession();
        for (ProcessDBDictionary newDict : newDictionaries) {
            boolean updated = false;
            for (ProcessDBDictionary existingDict : existingDBDictionaries) {
                if (existingDict.getDictionaryId().equals(newDict.getDictionaryId())
                        && existingDict.getLanguageCode().equals(newDict.getLanguageCode())) {
                    if (overwrite) {
                        session.delete(existingDict);
                    } else {
                        existingDict.getPermissions().clear();
                        existingDict.getPermissions().addAll(newDict.getPermissions());
                        existingDict.setDefaultDictionary(newDict.isDefaultDictionary());
                        existingDict.setDescription(newDict.getDescription());
                        for (ProcessDBDictionaryItem newItem : newDict.getItems().values()) {
                            if (!existingDict.getItems().containsKey(newItem.getKey())) {
                                existingDict.addItem(newItem);
                            }
                        }
                        session.saveOrUpdate(existingDict);
                        updateCache(existingDict);
                        updated = true;
                    }
                }
            }
            if (!updated) {
                newDict.setProcessDefinition(definition);
                session.save(newDict);
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

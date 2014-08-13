package pl.net.bluesoft.rnd.processtool.dao.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.dao.ProcessDictionaryDAO;
import pl.net.bluesoft.rnd.processtool.dict.GlobalDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryPermission;
import pl.net.bluesoft.util.lang.ExpiringCache;

import static pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary.*;

public class ProcessDictionaryDAOImpl extends SimpleHibernateBean<ProcessDBDictionary> implements ProcessDictionaryDAO,
        GlobalDictionaryProvider<ProcessDBDictionary> {
    public ProcessDictionaryDAOImpl(Session session) {
        super(session);
    }

    private static final ExpiringCache<DictionaryCacheKey, ProcessDBDictionary> cache = new ExpiringCache<DictionaryCacheKey, ProcessDBDictionary>(60 * 60 * 1000);

    private static class DictionaryCacheKey {
        private String dictionaryId;

        public DictionaryCacheKey(String dictionaryId) {
            this.dictionaryId = dictionaryId;
        }

        @Override
        public String toString() {
            return dictionaryId;
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

    private void updateCache(Collection<ProcessDBDictionary> dictionaries) {
        for (ProcessDBDictionary dict : dictionaries) {
            updateCache(dict);
        }
    }

    private void updateCache(ProcessDBDictionary dict) {
        cache.put(new DictionaryCacheKey(dict.getDictionaryId()), dict);
    }

    @Override
    public List<ProcessDBDictionary> fetchAllDictionaries() {
        Criteria criteria = getSession().createCriteria(ProcessDBDictionary.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.desc(_DEFAULT_NAME));
        List<ProcessDBDictionary> dictionaries = criteria.list();
        updateCache(dictionaries);
        return dictionaries;
    }

    @Override
    public ProcessDBDictionary fetchDictionary(final String dictionaryId) {
        DictionaryCacheKey key = new DictionaryCacheKey(dictionaryId);

        return cache.get(key, new ExpiringCache.NewValueCallback<DictionaryCacheKey, ProcessDBDictionary>() {
            @Override
            public ProcessDBDictionary getNewValue(DictionaryCacheKey key) {
                Criteria criteria = getSession().createCriteria(ProcessDBDictionary.class)
                        .add(Restrictions.eq(_DICTIONARY_ID, dictionaryId))
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                return (ProcessDBDictionary) criteria.uniqueResult();
            }
        });
    }

    @Override
    public void createOrUpdateDictionaryItem(ProcessDBDictionary dictionary, String languageCode, String dictionaryItemKey, String dictionaryItemValue) {
        Session session = getSession();

        ProcessDBDictionaryItem dictionaryItem;

        if (dictionary.getId() == null) {
            Criteria criteria = session.createCriteria(ProcessDBDictionaryItem.class)
                    .add(Restrictions.eq(ProcessDBDictionaryItem._KEY, dictionaryItemKey))
                    .add(Restrictions.eq(ProcessDBDictionaryItem._DICTIONARY, dictionary));

            dictionaryItem = (ProcessDBDictionaryItem) criteria.uniqueResult();
        } else {
            dictionaryItem = dictionary.getItems().get(dictionaryItemKey);
        }

        if (dictionaryItem == null) {
            dictionaryItem = new ProcessDBDictionaryItem();
            dictionaryItem.setDictionary(dictionary);
            dictionaryItem.setKey(dictionaryItemKey);

            ProcessDBDictionaryItemValue itemValue = new ProcessDBDictionaryItemValue();
            itemValue.setItem(dictionaryItem);
            itemValue.setValue(languageCode, dictionaryItemValue);
            itemValue.setValidFrom(new Date());

            dictionaryItem.addValue(itemValue);
            dictionary.addItem(dictionaryItem);

            session.saveOrUpdate(dictionary);
        } else {
            ProcessDBDictionaryItemValue currentValue = dictionaryItem.getValueForCurrentDate();
            currentValue.setDefaultValue(dictionaryItemValue);

            dictionaryItem.getValues().remove(currentValue);

            ProcessDBDictionaryItemValue itemValue = new ProcessDBDictionaryItemValue();
            itemValue.setItem(dictionaryItem);
            itemValue.setValue(languageCode, dictionaryItemValue);
            itemValue.setValidFrom(new Date());

            dictionaryItem.addValue(itemValue);

            session.saveOrUpdate(dictionary);
        }
        updateCache(dictionary);
    }

    /**
     * Create new global dicrionary. If there is already existing dictnioary, this method will delete it
     */

    @Override
    public void processDictionaries(Collection<ProcessDBDictionary> newDictionaries, boolean overwrite) {
        Collection<ProcessDBDictionary> existingDBDictionaries = fetchAllDictionaries();

        for (ProcessDBDictionary newDict : newDictionaries) {
            ProcessDBDictionary existingDictionary = getExistingProcessDictnioary(existingDBDictionaries, newDict.getDictionaryId());

        	/* There is no dictionary, create new one */
            if (existingDictionary == null) {
                saveDictionary(newDict);
                continue;
            }
        	
        	/* If there is already dictionary, delete it */
            if (overwrite) {
                deleteDictionary(existingDictionary);
            }

            if (overwrite) {
                existingDictionary = newDict; // new one replaces existing one since the latter is already deleted
            } else {
                existingDictionary = mergeDictnionaries(existingDictionary, newDict);
            }

            updateDictionary(existingDictionary);
        }
    }

    private void saveDictionary(ProcessDBDictionary dictionary) {
        Session session = getSession();
        session.saveOrUpdate(dictionary);
        updateCache(dictionary);
    }

    private void deleteDictionary(ProcessDBDictionary dictionary) {
        Session session = getSession();
        session.delete(dictionary);
    }

    private ProcessDBDictionary mergeDictnionaries(ProcessDBDictionary existingDictionary, ProcessDBDictionary newDict) {
        existingDictionary.getPermissions().clear();

        for (ProcessDBDictionaryPermission permission : newDict.getPermissions()) {
            existingDictionary.addPermission(permission);
        }

        for (ProcessDBDictionaryItem newItem : newDict.getItems().values()) {
            if (!existingDictionary.getItems().containsKey(newItem.getKey())) {
                existingDictionary.addItem(newItem);
            }
        }
        return existingDictionary;
    }

    private ProcessDBDictionary getExistingProcessDictnioary(Collection<ProcessDBDictionary> dictionaries, String dictionaryId) {
        for (ProcessDBDictionary existingDict : dictionaries) {
            if (existingDict.getDictionaryId().equals(dictionaryId)) {
                return existingDict;
            }
        }
        return null;
    }

    @Override
    public void updateDictionary(ProcessDBDictionary dictionary) {
        Session session = getSession();
        session.saveOrUpdate(dictionary);
        dictionary = (ProcessDBDictionary) session.merge(dictionary);
        session.flush();
        updateCache(dictionary);
    }

    @Override
    public ProcessDBDictionaryItem refresh(ProcessDBDictionaryItem item) {
        if (item == null || item.getId() == null) {
            return item;
        }

        return (ProcessDBDictionaryItem) getSession()
                .createCriteria(ProcessDBDictionaryItem.class)
                .add(Restrictions.eq(ProcessDBDictionaryItem._ID, item.getId()))
                .setFetchMode(ProcessDBDictionaryItem._DICTIONARY, FetchMode.JOIN)
                .uniqueResult();
    }

    @Override
    public Collection<ProcessDBDictionaryItem> getDictionaryItems(String dictionaryId, String sortColumnProperty, boolean sortAscending, final int pageLength, final int pageOffset) {
        Query query = this.session.createQuery("from ProcessDBDictionaryItem where dictionary.dictionaryId = :dictId order by " + sortColumnProperty + " " + (sortAscending ? "asc" : "desc"));
        query.setMaxResults(pageLength)
                .setFirstResult(pageOffset);
        setQueryParameters(query, dictionaryId);
        return query.list();
    }

    private void setQueryParameters(Query query, String dictionaryId) {
        query.setParameter("dictId", dictionaryId);
    }

    @Override
    public Long getDictionaryItemsCount(String dictionaryId) {
        Query query = this.session.createQuery("select count(*) from ProcessDBDictionaryItem where dictionary.dictionaryId = :dictId");
        setQueryParameters(query, dictionaryId);
        return (Long) query.uniqueResult();
    }
}

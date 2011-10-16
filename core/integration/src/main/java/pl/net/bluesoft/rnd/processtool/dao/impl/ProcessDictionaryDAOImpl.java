package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDictionaryDAO;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionaryItem;

import java.util.Collections;
import java.util.List;

public class ProcessDictionaryDAOImpl extends SimpleHibernateBean<ProcessDBDictionary> implements ProcessDictionaryDAO, ProcessDictionaryProvider<ProcessDBDictionary> {
    public ProcessDictionaryDAOImpl(Session session) {
        super(session);
    }

    @Override
    public ProcessDBDictionary fetchDictionary(ProcessDefinitionConfig definition, String dictionaryId, String languageCode) {
        Criteria criteria = session.createCriteria(ProcessDBDictionary.class)
                .add(Restrictions.eq("dictionaryId", dictionaryId))
                .add(Restrictions.eq("languageCode", languageCode))
                .add(Restrictions.eq("processDefinition", definition));
        return (ProcessDBDictionary) criteria.uniqueResult();
    }

    @Override
    public ProcessDBDictionary fetchDefaultDictionary(ProcessDefinitionConfig definition, String dictionaryId) {
        Criteria criteria = session.createCriteria(ProcessDBDictionary.class)
                .add(Restrictions.eq("dictionaryId", dictionaryId))
                .add(Restrictions.eq("defaultDictionary", Boolean.TRUE))
                .add(Restrictions.eq("processDefinition", definition));
        return (ProcessDBDictionary) criteria.uniqueResult();
    }

    @Override
    public List<ProcessDBDictionary> fetchProcessDictionaries(ProcessDefinitionConfig definition) {
        Criteria criteria = session.createCriteria(ProcessDBDictionary.class)
                .add(Restrictions.eq("processDefinition", definition)).addOrder(Order.desc("dictionaryName"));
        return criteria.list();
    }

    @Override
    public List<ProcessDBDictionary> fetchAllDictionaries() {
        return session.createCriteria(ProcessDBDictionary.class).addOrder(Order.desc("dictionaryName")).list();
    }

    @Override
    public List<ProcessDBDictionary> fetchAllActiveDictionaries() {
        return session.createCriteria(ProcessDBDictionary.class).addOrder(Order.desc("dictionaryName"))
                .createCriteria("processDefinition").add(Restrictions.eq("latest", Boolean.TRUE)).list();
    }

    @Override
    public void createOrUpdateProcessDictionary(ProcessDefinitionConfig definition, ProcessDBDictionary dictionary, boolean overwrite) {
        createOrUpdateProcessDictionaries(definition, Collections.singletonList(dictionary), overwrite);
    }

    @Override
    public void createOrUpdateProcessDictionaries(ProcessDefinitionConfig definition, List<ProcessDBDictionary> newDictionaries, boolean overwrite) {
        List<ProcessDBDictionary> existingDBDictionaries = fetchProcessDictionaries(definition);
        for (ProcessDBDictionary newDict : newDictionaries) {
            boolean updated = false;
            for (ProcessDBDictionary existingDict : existingDBDictionaries) {
                if (existingDict.getDictionaryId().equals(newDict.getDictionaryId())
                        && existingDict.getLanguageCode().equals(newDict.getLanguageCode())) {
                    existingDict.setDefaultDictionary(newDict.isDefaultDictionary());
                    updateItems(existingDict, newDict, overwrite);
                    session.update(existingDict);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                newDict.setProcessDefinition(definition);
                session.save(newDict);
            }
        }
    }

    @Override
    public void updateDictionary(ProcessDBDictionary dictionary) {
        session.update(dictionary);
        session.flush();
    }

    private void updateItems(ProcessDBDictionary existingDict, ProcessDBDictionary newDict, boolean overwrite) {
        if (overwrite) {
            for (ProcessDBDictionaryItem item : existingDict.getItems().values()) {
                item.setDictionary(null);
            }
            existingDict.setItems(newDict.getItems());
        }
        else {
            for (ProcessDBDictionaryItem item : newDict.getItems().values()) {
                if (!existingDict.getItems().keySet().contains(item.getKey())) {
                    existingDict.addItem(item);
                }
            }
        }
    }


}

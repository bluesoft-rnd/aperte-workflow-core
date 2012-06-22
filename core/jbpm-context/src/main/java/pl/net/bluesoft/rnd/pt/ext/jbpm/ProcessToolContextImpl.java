package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolException;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.dict.GlobalDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.hibernate.HibernateTransactionCallback;
import pl.net.bluesoft.rnd.processtool.model.BpmVariable;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolAutowire;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSequence;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSetting;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.eventbus.EventBusManager;
import pl.net.bluesoft.util.lang.Formats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.Lang.coalesce;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * Context replacement for Spring library
 *
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolContextImpl implements ProcessToolContext {
    private Session hibernateSession;
    private Transaction transaction;
    private ProcessToolJbpmSessionFactory processToolJbpmSessionFactory;
    private ProcessDictionaryRegistry processDictionaryRegistry;
    private ProcessEngine processEngine;
    private ProcessToolContextFactory factory;


    private Map<String, String> autowiringCache;
    private Map<Class<? extends HibernateBean>, HibernateBean> daoCache = new HashMap<Class<? extends HibernateBean>, HibernateBean>();

    private Boolean closed = false;

    public ProcessToolContextImpl(Session hibernateSession,
                                  ProcessToolContextFactory factory,
                                  ProcessEngine processEngine) {
        this.hibernateSession = hibernateSession;
        this.factory = factory;
        this.processEngine = processEngine;
        this.autowiringCache = getRegistry().getCache(ProcessToolAutowire.class.getName());
        processEngine.setHibernateSession(hibernateSession);

        transaction = hibernateSession.beginTransaction();
    }

    public void rollback() {
        transaction.rollback();
    }

    public void commit() {
        transaction.commit();

    }

    public synchronized void close() {
        try {
            processEngine.close();
        } finally {
            try {
                commit();
            } finally {
                hibernateSession.close();
                closed = true;
            }
        }
    }

    private synchronized void verifyContextOpen() {
        if (closed) {
            throw new ProcessToolException("Context already closed!");
        }
    }

    public void init() {

    }

    public boolean isActive() {
        return !closed;
    }

    @Override
    public void addTransactionCallback(HibernateTransactionCallback callback) {
        transaction.registerSynchronization(callback);
    }


//    public <T> T inTransaction(HibernateTransactionCallback<T> htc) {
//        return htc.doInTransaction(hibernateSession);
//
//     }
    @Override
    public ProcessDictionaryRegistry getProcessDictionaryRegistry() {
        if (processDictionaryRegistry == null) {
            processDictionaryRegistry = new ProcessDictionaryRegistry();
            processDictionaryRegistry.addProcessDictionaryProvider("db", (ProcessDictionaryProvider) getProcessDictionaryDAO());
            processDictionaryRegistry.addGlobalDictionaryProvider("db", (GlobalDictionaryProvider) getProcessDictionaryDAO());
        }
        return processDictionaryRegistry;
    }


    @Override
    public ProcessToolRegistry getRegistry() {
        return factory.getRegistry();
    }

    private <T extends HibernateBean> T getHibernateDAO(Class<T> daoClass) {
        verifyContextOpen();
        if (!daoCache.containsKey(daoClass)) {
            T dao = null;
            if (ProcessDictionaryDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessDictionaryDAO(hibernateSession);
            } else if (ProcessInstanceDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessInstanceDAO(hibernateSession);
            } else if (ProcessInstanceFilterDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessInstanceFilterDAO(hibernateSession);
            } else if (UserDataDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getUserDataDAO(hibernateSession);
            } else if (ProcessDefinitionDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessDefinitionDAO(hibernateSession);
            } else if (UserSubstitutionDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getUserSubstitutionDAO(hibernateSession);
            }
            if (dao != null) {
                daoCache.put(daoClass, dao);
            }
        }
        return (T) daoCache.get(daoClass);
    }

    public ProcessDictionaryDAO getProcessDictionaryDAO() {
        return getHibernateDAO(ProcessDictionaryDAO.class);
    }

    @Override
    public ProcessInstanceDAO getProcessInstanceDAO() {
        return getHibernateDAO(ProcessInstanceDAO.class);
    }

    @Override
    public ProcessInstanceFilterDAO getProcessInstanceFilterDAO() {
        return getHibernateDAO(ProcessInstanceFilterDAO.class);
    }

    @Override
    public UserDataDAO getUserDataDAO() {
        return getHibernateDAO(UserDataDAO.class);
    }

    @Override
    public ProcessDefinitionDAO getProcessDefinitionDAO() {
        return getHibernateDAO(ProcessDefinitionDAO.class);
    }

    @Override
    public UserSubstitutionDAO getUserSubstitutionDAO() {
        return getHibernateDAO(UserSubstitutionDAO.class);
    }

    @Override
    public Session getHibernateSession() {
        verifyContextOpen();
        return hibernateSession;
    }

    @Override
    public synchronized ProcessToolSessionFactory getProcessToolSessionFactory() {
        if (processToolJbpmSessionFactory == null) {
            processToolJbpmSessionFactory = new ProcessToolJbpmSessionFactory(this);
        }
        return processToolJbpmSessionFactory;
    }

    @Override
    public EventBusManager getEventBusManager() {
        return factory.getRegistry().getEventBusManager();
    }

    @Override
    public String getSetting(String key) {
        verifyContextOpen();
        ProcessToolSetting setting = (ProcessToolSetting) hibernateSession.createCriteria(ProcessToolSetting.class)
                .add(Restrictions.eq("key", key)).uniqueResult();
        return setting != null ? setting.getValue() : null;
    }

    @Override
    public void setSetting(String key, String value) {
        verifyContextOpen();
        List list = hibernateSession.createCriteria(ProcessToolSetting.class).add(Restrictions.eq("key", key)).list();
        ProcessToolSetting setting;
        if (list.isEmpty()) {
            setting = new ProcessToolSetting();
            setting.setKey(key);
        } else {
            setting = (ProcessToolSetting) list.get(0);
        }
        setting.setValue(value);
        hibernateSession.saveOrUpdate(setting);
    }

    @Override
    public String getAutowiredProperty(String key) {
        return autowiringCache.get(key);
    }

    @Override
    public void setAutowiredProperty(String key, String value) {
        String cachedValue = autowiringCache.get(key);
        if (cachedValue == null || !cachedValue.equals(value)) {
            verifyContextOpen();
            ProcessToolAutowire pta = (ProcessToolAutowire) hibernateSession.createCriteria(ProcessToolAutowire.class)
                    .add(Restrictions.eq("key", key)).uniqueResult();
            if (pta == null) {
                pta = new ProcessToolAutowire();
                pta.setKey(key);
            }
            pta.setValue(value);
            hibernateSession.saveOrUpdate(pta);
            autowiringCache.put(key, value);
        }
    }

    @Override
    public long getNextValue(String processDefinitionName, String sequenceName) {
        verifyContextOpen();
        List<ProcessToolSequence> seqList = hibernateSession.createCriteria(ProcessToolSequence.class)
                .add(Restrictions.eq("processDefinitionName", processDefinitionName))
                .add(Restrictions.eq("name", sequenceName))
                .list();

        ProcessToolSequence seq;

        if (seqList.isEmpty()) {
            seq = new ProcessToolSequence();
            seq.setProcessDefinitionName(processDefinitionName);
            seq.setName(sequenceName);
            seq.setValue(1);
        } else {
            seq = seqList.get(0);
            seq.setValue(seq.getValue() + 1);
        }
        hibernateSession.saveOrUpdate(seq);
        hibernateSession.flush();
        return seq.getValue();
    }

    @Override
    public long getNextValue(ProcessInstance processInstance, String sequenceName) {
        return getNextValue(processInstance.getDefinitionName(), sequenceName);
    }

    @Override
    public long getNextValue(String sequenceName) {
        return getNextValue((String) null, sequenceName);
    }

    @Override
    public UserData getAutoUser() {
        return new UserData(Formats.nvl(getSetting(AUTO_USER_LOGIN), "system"), Formats.nvl(getSetting(AUTO_USER_NAME), "System"),
                Formats.nvl(getSetting(AUTO_USER_EMAIL), "awf@bluesoft.net.pl"));
    }

    public ProcessToolContextFactory getFactory() {
        return factory;
    }


    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    @Override
    public void updateContext(ProcessInstance processInstance) {
        ExecutionService es = getProcessEngine().getExecutionService();
        for (ProcessInstanceAttribute pia : processInstance.getProcessAttributes()) {
            if (pia instanceof BpmVariable) {
                BpmVariable bpmVar = (BpmVariable) pia;
                if (hasText(bpmVar.getBpmVariableName())) {
                    es.setVariable(processInstance.getInternalId(), bpmVar.getBpmVariableName(), bpmVar.getBpmVariableValue());
                }
            }
        }
    }
    
    public Map<String, Object> getBpmVariables(ProcessInstance pi) {
        ExecutionService es = getProcessEngine().getExecutionService();
        return es.getVariables(pi.getInternalId(), es.getVariableNames(pi.getInternalId()));
    }
}

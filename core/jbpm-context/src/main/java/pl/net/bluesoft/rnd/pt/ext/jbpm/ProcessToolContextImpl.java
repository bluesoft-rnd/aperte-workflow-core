package pl.net.bluesoft.rnd.pt.ext.jbpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.BasicSettings;
import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolException;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDictionaryDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceFilterDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceSimpleAttributeDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessStateActionDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserDataDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserProcessQueueDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.dict.GlobalDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.hibernate.HibernateTransactionCallback;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolAutowire;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSequence;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSetting;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.userqueues.IUserProcessQueueManager;
import pl.net.bluesoft.util.eventbus.EventBusManager;
import pl.net.bluesoft.util.lang.Formats;

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
//    private ProcessEngine processEngine;
    private ProcessToolRegistry registry;
    private IUserProcessQueueManager userProcessQueueManager;

    private Map<String, String> autowiringCache;
    private Map<Class<? extends HibernateBean>, HibernateBean> daoCache = new HashMap<Class<? extends HibernateBean>, HibernateBean>();

    private Boolean closed = false;

    public ProcessToolContextImpl(Session hibernateSession,
    								ProcessToolRegistry registry) {
        this.hibernateSession = hibernateSession;
        this.registry = registry;
//        this.processEngine = processEngine;
        this.autowiringCache = registry.getCache(ProcessToolAutowire.class.getName());
        this.userProcessQueueManager = new UserProcessQueueManager(hibernateSession, getUserProcessQueueDAO());
//        processEngine.setHibernateSession(hibernateSession);

        transaction = hibernateSession.beginTransaction();
    }

    public void rollback() {
        transaction.rollback();
    }

    public void commit() {
        transaction.commit();

    }

    private synchronized void verifyContextOpen() {
        if (closed) {
            throw new ProcessToolException("Context already closed!");
        }
    }

    public void init() {

    }

    @Override
	public boolean isActive()
    {
    	/* Check hibernate session */
    	if(!hibernateSession.isOpen())
    		return false;
    	
        return !closed;
    }

    @Override
    public void addTransactionCallback(HibernateTransactionCallback callback) {
        transaction.registerSynchronization(callback);
    }


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
        return registry;
    }

    @SuppressWarnings("unchecked")
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
            } else if (UserProcessQueueDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getUserProcessQueueDAO(hibernateSession);
            } else if (UserSubstitutionDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getUserSubstitutionDAO(hibernateSession);
            }else if (ProcessInstanceSimpleAttributeDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessInstanceSimpleAttributeDAO(hibernateSession);
            }else if (ProcessStateActionDAO.class.equals(daoClass)) {
                dao = (T) getRegistry().getProcessStateAction(hibernateSession);
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
	public UserProcessQueueDAO getUserProcessQueueDAO() {
		return getHibernateDAO(UserProcessQueueDAO.class);
	}
    @Override
	public ProcessInstanceSimpleAttributeDAO getProcessInstanceSimpleAttributeDAO() {
		return getHibernateDAO(ProcessInstanceSimpleAttributeDAO.class);
	}
    
    @Override
	public ProcessStateActionDAO getProcessStateActionDAO() {
		return getHibernateDAO(ProcessStateActionDAO.class);
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
        return registry.getEventBusManager();
    }

    @Override
    public String getSetting(IProcessToolSettings key) {
        verifyContextOpen();
        ProcessToolSetting setting = (ProcessToolSetting) hibernateSession.createCriteria(ProcessToolSetting.class)
                .add(Restrictions.eq("key", key.toString())).uniqueResult();
        return setting != null ? setting.getValue() : null;
    }

    @Override
    public void setSetting(IProcessToolSettings key, String value) {
        verifyContextOpen();
        List list = hibernateSession.createCriteria(ProcessToolSetting.class).add(Restrictions.eq("key", key.toString())).list();
        ProcessToolSetting setting;
        if (list.isEmpty()) {
            setting = new ProcessToolSetting();
            setting.setKey(key.toString());
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
        return new UserData(Formats.nvl(getSetting(BasicSettings.AUTO_USER_LOGIN), "system"), Formats.nvl(getSetting(BasicSettings.AUTO_USER_NAME), "System"),
                Formats.nvl(getSetting(BasicSettings.AUTO_USER_EMAIL), "awf@bluesoft.net.pl"));
    }

    @Override
    public void updateContext(ProcessInstance processInstance) {
    }

	@Override
	public IUserProcessQueueManager getUserProcessQueueManager()
	{
		return userProcessQueueManager;
	}

	@Override
	public void close() {
		this.closed = true;
		
	}
}

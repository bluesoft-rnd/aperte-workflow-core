package org.aperteworkflow.ext.activiti;

import org.activiti.engine.ProcessEngine;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.hibernate.HibernateTransactionCallback;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSetting;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import java.util.List;

/**
 * Context replacement for Spring library
 * 
 * @author tlipski@bluesoft.net.pl
 */
public class ActivitiContextImpl implements ProcessToolContext {

	Session hibernateSession;
	Transaction transaction;
    ActivitiBpmSessionFactory processToolJbpmSessionFactory;

	ProcessDictionaryRegistry processDictionaryRegistry;
    ProcessDictionaryDAO processDictionaryDAO;
    ProcessInstanceDAO processInstanceDAO;
	ProcessDefinitionDAO processDefinitionDAO;
	ProcessEngine processEngine;

	private ProcessToolContextFactory factory;
	private UserDataDAO userDataDAO;
    private UserSubstitutionDAO userSubstitutionDAO;

    public ActivitiContextImpl(Session hibernateSession,
                               ProcessToolContextFactory factory,
                               ProcessEngine processEngine) {
		this.hibernateSession = hibernateSession;
		this.factory = factory;
		this.processEngine = processEngine;

		transaction = hibernateSession.beginTransaction();
	}

	public void rollback() {
		transaction.rollback();
	}
	public void commit() {
		transaction.commit();

	}
	public void close() {
		try {
			processEngine.close();
		}
		finally {
			try {
				commit();
			}
			finally {
				hibernateSession.close();
			}
		}
	}

	public void init() {

	}


	public<T> T inTransaction(HibernateTransactionCallback<T> htc) {
		return htc.doInTransaction(hibernateSession);

	}
	@Override
	public ProcessDictionaryRegistry getProcessDictionaryRegistry() {
		if (processDictionaryRegistry == null) {
			processDictionaryRegistry = new ProcessDictionaryRegistry();
            processDictionaryRegistry.addDictionaryProvider("db", (ProcessDictionaryProvider) getProcessDictionaryDAO());
		}
		return processDictionaryRegistry;
	}

	@Override
	public ProcessToolRegistry getRegistry() {
		return factory.getRegistry();
	}

    public synchronized ProcessDictionaryDAO getProcessDictionaryDAO() {
        if (processDictionaryDAO == null) {
            processDictionaryDAO = factory.getRegistry().getProcessDictionaryDAO(hibernateSession);
        }
        return processDictionaryDAO;
    }

	@Override
	public ProcessInstanceDAO getProcessInstanceDAO() {
		if (processInstanceDAO == null) {
			processInstanceDAO = factory.getRegistry().getProcessInstanceDAO(hibernateSession);

		}
		return processInstanceDAO;
	}

	@Override
	public Session getHibernateSession() {
		return hibernateSession;
	}

	@Override
	public UserDataDAO getUserDataDAO() {
		if (userDataDAO == null) {
			userDataDAO = factory.getRegistry().getUserDataDAO(hibernateSession);
		}
		return userDataDAO;
	}

    @Override
    public UserSubstitutionDAO getUserSubstitutionDAO() {
        if (userSubstitutionDAO == null) {
            userSubstitutionDAO = factory.getRegistry().getUserSubstitutionDAO(hibernateSession);
        }
        return userSubstitutionDAO;
    }

	@Override
	public synchronized ProcessToolSessionFactory getProcessToolSessionFactory() {
		if (processToolJbpmSessionFactory == null) {
			processToolJbpmSessionFactory = new ActivitiBpmSessionFactory();
		}
		return processToolJbpmSessionFactory;
	}

	@Override
	public ProcessDefinitionDAO getProcessDefinitionDAO() {
		if (processDefinitionDAO == null) {
			processDefinitionDAO = factory.getRegistry().getProcessDefinitionDAO(hibernateSession);
		}
		return processDefinitionDAO;
	}

	@Override
	public EventBusManager getEventBusManager() {
		return factory.getRegistry().getEventBusManager();
	}

	@Override
	public String getSetting(String key) {
		List list = hibernateSession.createCriteria(ProcessToolSetting.class).add(Restrictions.eq("key", key)).list();
		if (list.isEmpty()) return null;
		return ((ProcessToolSetting) list.get(0)).getValue();
	}

    public ProcessToolContextFactory getFactory() {
		return factory;
	}


	public ProcessEngine getProcessEngine() {
		return processEngine;
	}
}

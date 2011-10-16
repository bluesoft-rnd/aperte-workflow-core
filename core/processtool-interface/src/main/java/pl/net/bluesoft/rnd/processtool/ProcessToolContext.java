package pl.net.bluesoft.rnd.processtool;

import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolContext {
	ProcessDictionaryRegistry getProcessDictionaryRegistry();

	ProcessToolRegistry getRegistry();
	ProcessInstanceDAO getProcessInstanceDAO();

    ProcessDictionaryDAO getProcessDictionaryDAO();

	Session getHibernateSession();
	UserDataDAO getUserDataDAO();
    UserSubstitutionDAO getUserSubstitutionDAO();
	ProcessToolSessionFactory getProcessToolSessionFactory();

	ProcessDefinitionDAO getProcessDefinitionDAO();
	EventBusManager getEventBusManager();
	String getSetting(String key);

	void close();

	
	public static class Util {
		static Map<Thread, ProcessToolContext> CONTEXT_THREAD_HOLDER = new HashMap();

		public static synchronized void setProcessToolContextForThread(ProcessToolContext ctx) {
			if (getProcessToolContextFromThread() != null) {
				getProcessToolContextFromThread().close();
			}
			CONTEXT_THREAD_HOLDER.put(Thread.currentThread(), ctx);
		}

		public static ProcessToolContext getProcessToolContextFromThread() {
			return CONTEXT_THREAD_HOLDER.get(Thread.currentThread());
		}

		public static synchronized void removeProcessToolContextForThread(ProcessToolContext ctx) {
			CONTEXT_THREAD_HOLDER.remove(Thread.currentThread());
		}
	}

}

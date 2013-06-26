package pl.net.bluesoft.rnd.processtool;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

import java.util.Map;

import org.hibernate.Session;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDictionaryDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceFilterDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceSimpleAttributeDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessStateActionDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserDataDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserProcessQueueDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.hibernate.HibernateTransactionCallback;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.userqueues.IUserProcessQueueManager;
import pl.net.bluesoft.util.eventbus.EventBusManager;

/**
 *  Main application context
 *  
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public interface ProcessToolContext  extends ProcessToolBpmConstants 
{
	/** Get the user process queues manager */
	IUserProcessQueueManager getUserProcessQueueManager();
	
	ProcessDictionaryRegistry getProcessDictionaryRegistry();

	ProcessToolRegistry getRegistry();
	ProcessInstanceDAO getProcessInstanceDAO();
	
	UserProcessQueueDAO getUserProcessQueueDAO();

    ProcessInstanceFilterDAO getProcessInstanceFilterDAO();

    ProcessDictionaryDAO getProcessDictionaryDAO();

	Session getHibernateSession();
	UserDataDAO getUserDataDAO();
    UserSubstitutionDAO getUserSubstitutionDAO();
    ProcessInstanceSimpleAttributeDAO getProcessInstanceSimpleAttributeDAO();
    ProcessStateActionDAO getProcessStateActionDAO();
	ProcessToolSessionFactory getProcessToolSessionFactory();

	ProcessDefinitionDAO getProcessDefinitionDAO();
	EventBusManager getEventBusManager();
	String getSetting(IProcessToolSettings key);

    void setSetting(IProcessToolSettings key, String value);

    String getAutowiredProperty(String key);

    void setAutowiredProperty(String key, String value);

    long getNextValue(String processDefinitionName, String sequenceName);
    long getNextValue(ProcessInstance processInstance, String sequenceName);
    long getNextValue(String sequenceName);

    UserData getAutoUser();

    /** Close hibernate session and process engine */
	void close();
	
	/** Call when outer transaction had a rollback */
	void rollback();

    void updateContext(ProcessInstance processInstance);

    void addTransactionCallback(HibernateTransactionCallback callback);

    boolean isActive();

	class Util
	{
		/** We use {@link InheritableThreadLocal} because we want context to be provided for child worker threads */
        private static InheritableThreadLocal<ProcessToolContext> current = new InheritableThreadLocal<ProcessToolContext>();
        
		public static void setThreadProcessToolContext(ProcessToolContext ctx) 
		{
			current.set(ctx);
		}

		public static ProcessToolContext getThreadProcessToolContext() 
		{		
			return current.get();
		}

		public static void removeThreadProcessToolContext() 
		{
			current.remove();
		}

		public static <T> T withContext(ProcessToolContext ctx, ReturningProcessToolContextCallback<T> callback) {
			ProcessToolContext previousCtx = getThreadProcessToolContext();
			try {
				return callback.processWithContext(ctx);
			}
			finally {
				setThreadProcessToolContext(previousCtx);
			}
		}

        /**
         * The default implementation checks for several system properties, than defaults to current working directory.
         * The home directory is used to establish location of osgi-plugins, felix cache and other directories.
         */
        public static String getHomePath() {
            return nvl(
                    System.getProperty("aperte.workflow.home"),
                    System.getProperty("liferay.home"),
                    System.getProperty("catalina.home"),
                    ""
            );
        }
	}

}

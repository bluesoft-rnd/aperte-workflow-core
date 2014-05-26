package pl.net.bluesoft.rnd.processtool;

import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 *  Main application context
 *  
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public interface ProcessToolContext
{
	ProcessDictionaryRegistry getProcessDictionaryRegistry();

	ProcessInstanceDAO getProcessInstanceDAO();

    ProcessInstanceFilterDAO getProcessInstanceFilterDAO();
    OperationLockDAO getOperationLockDAO();
    ProcessDictionaryDAO getProcessDictionaryDAO();

	Session getHibernateSession();
    UserSubstitutionDAO getUserSubstitutionDAO();
    ProcessInstanceSimpleAttributeDAO getProcessInstanceSimpleAttributeDAO();
	ProcessToolSessionFactory getProcessToolSessionFactory();

	ProcessDefinitionDAO getProcessDefinitionDAO();
	EventBusManager getEventBusManager();
	String getSetting(IProcessToolSettings key);
    String getSetting(String key);

    void setSetting(IProcessToolSettings key, String value);

    long getNextValue(String processDefinitionName, String sequenceName);
    long getNextValue(ProcessInstance processInstance, String sequenceName);
    long getNextValue(String sequenceName);

    /** Close hibernate session and process engine */
	void close();

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

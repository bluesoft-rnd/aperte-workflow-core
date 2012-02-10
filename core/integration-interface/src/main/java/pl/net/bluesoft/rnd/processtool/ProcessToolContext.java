package pl.net.bluesoft.rnd.processtool;

import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

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
        private static ThreadLocal<ProcessToolContext> current = new ThreadLocal<ProcessToolContext>();

		public static synchronized void setThreadProcessToolContext(ProcessToolContext ctx) {
			current.set(ctx);
		}

		public static ProcessToolContext getThreadProcessToolContext() {
			return current.get();
		}

		public static synchronized void removeThreadProcessToolContext() {
			current.remove();
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

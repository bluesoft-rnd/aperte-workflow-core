package pl.net.bluesoft.rnd.processtool.plugins;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.dao.impl.*;
import pl.net.bluesoft.util.lang.FormatUtil;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 21:46
 */
@Component
@Scope(value = "singleton")
public class DataRegistryImpl implements DataRegistry {
	private static final Logger logger = Logger.getLogger(DataRegistryImpl.class.getSimpleName());

	private final Map<String, Class> annotatedClasses = new HashMap<String, Class>();
	private final Map<String, byte[]> hibernateResources = new HashMap<String, byte[]>();
	private final Map<String, ClassLoader> classLoaders = new HashMap<String, ClassLoader>();

	private SessionFactory sessionFactory;

    @Autowired
	private ProcessToolContextFactory processToolContextFactory;

	private boolean jta;

	private class ExtClassLoader extends ClassLoader {
		private ExtClassLoader(ClassLoader parent) {
			super(parent);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			Class aClass = annotatedClasses.get(name);
			if (aClass != null) {
				return aClass;
			}
			for (ClassLoader loader : classLoaders.values()) {
				try {
					Class<?> aClass1 = loader.loadClass(name);
					if (aClass1 != null) return aClass1;
				} catch (Exception e) {
					//do nothing
				}
			}
			return super.loadClass(name);
		}
	}

	@Override
	public synchronized void addClassLoader(String name, ClassLoader loader) {
		classLoaders.put(name, loader);
	}

	@Override
	public synchronized void removeClassLoader(String name) {
		classLoaders.remove(name);
	}

	@Override
	public synchronized ClassLoader getModelAwareClassLoader(ClassLoader parent) {
		return new ExtClassLoader(parent);
	}

	@Override
	public synchronized boolean registerModelExtension(Class<?>... cls) {
		logger.warning("Registered model extensions: " + FormatUtil.joinClassNames(cls));
		return addAnnotatedClass(cls);
	}

	@Override
	public synchronized boolean unregisterModelExtension(Class<?>... cls) {
		logger.warning("Unregistered model extensions: " + FormatUtil.joinClassNames(cls));
		return removeAnnotatedClass(cls);
	}

	@Override
	public synchronized void commitModelExtensions() {
		buildSessionFactory();
	}

	@Override
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public ProcessToolContextFactory getProcessToolContextFactory() {
		return processToolContextFactory;
	}

	@Override
	public boolean isJta() {
		return jta;
	}

	private boolean addAnnotatedClass(Class<?>... classes) {
		boolean needUpdate = false;
		for (Class cls : classes) {
			Class annotatedClass = annotatedClasses.get(cls.getName());
			if (annotatedClass == null || !annotatedClass.equals(cls)) {
				needUpdate = true;
				annotatedClasses.put(cls.getName(), cls);
			}
		}
		return needUpdate;
	}

	private boolean removeAnnotatedClass(Class... classes) {
		boolean needUpdate = false;
		for (Class cls : classes) {
			if (annotatedClasses.containsKey(cls.getName())) {
				needUpdate = true;
				annotatedClasses.remove(cls.getName());
			}
		}
		return needUpdate;
	}

	@Override
	public synchronized void addHibernateResource(String name, byte[] resource) {
		hibernateResources.put(name, resource);
	}

	@Override
	public synchronized void removeHibernateResource(String name) {
		hibernateResources.remove(name);
	}

	private void buildSessionFactory() {
		jta = false;
		boolean startJtaTransaction = true;
		String dataSourceName = checkForDataSource();
		UserTransaction ut = dataSourceName != null ? findUserTransaction() : null; //do not even try...

		Configuration configuration = new Configuration().configure();
		for (Class cls : annotatedClasses.values()) {
			configuration.addAnnotatedClass(cls);
		}

		for (String name : hibernateResources.keySet()) {
			byte[] b = hibernateResources.get(name);
			if (b != null && b.length > 0) {
				configuration.addInputStream(new ByteArrayInputStream(b));
			}
		}

		if (dataSourceName == null) {
			logger.severe("Aperte Workflow runs using embedded datasource. This approach is useful only for development and demoing purposes.");
                /*
                <!--<property name="hibernate.connection.driver_class">org.hsqldb.jdbcDriver</property>-->
                <!--<property name="hibernate.connection.url">jdbc:hsqldb:${liferay.home}/data/hsql/aperteworkflow</property>-->
                <!--<property name="hibernate.connection.username">sa</property>-->
                <!--<property name="hibernate.connection.password"></property>-->
                */
			configuration.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
			String url = "jdbc:hsqldb:" + ProcessToolContext.Util.getHomePath() + "/aperteworkflow-hsql";
			configuration.setProperty("hibernate.connection.url", url);
			configuration.setProperty("hibernate.connection.username", "sa");
			configuration.setProperty("hibernate.connection.password", "");
			logger.severe("Configured Aperte Workflow to use Hypersonic DB driver org.hsqldb.jdbcDriver, url: " + url);
		} else {
			logger.info("Configuring Aperte Workflow to use data source: " + dataSourceName);
			configuration.setProperty("hibernate.connection.datasource", dataSourceName);
		}
		String managerLookupClassName=null;
		if (ut != null) { //try to autodetect JTA settings
			logger.warning("UserTransaction found, attempting to autoconfigure Hibernate to use JTA");
			managerLookupClassName = System.getProperty("org.aperteworkflow.hibernate.transaction.manager_lookup_class");
			if (managerLookupClassName == null) {
				try {
					Class.forName("bitronix.tm.BitronixTransactionManager").getName();
					managerLookupClassName = "org.hibernate.transaction.BTMTransactionManagerLookup";
					logger.warning("Found class bitronix.tm.BitronixTransactionManager, Bitronix TM detected!");
				} catch (ClassNotFoundException e) {
					//nothing, go on.
				}
			}
			if (managerLookupClassName == null) {
				if (System.getProperty("jboss.home.dir") != null) {
					logger.warning("Found JBoss AS environment, using JBoss Arjuna TM");
					managerLookupClassName = "org.hibernate.transaction.JBossTransactionManagerLookup";
					startJtaTransaction = false; //hibernate forces autocommit on transaction update, which throws exception on jboss.
				}
			}
			logger.warning("Configured hibernate.transaction.manager_lookup_class to " + managerLookupClassName);
		}
		if (managerLookupClassName != null) {
			configuration.setProperty("hibernate.transaction.factory_class",
					nvl(System.getProperty("org.aperteworkflow.hibernate.transaction.factory_class"),
							"org.hibernate.transaction.JTATransactionFactory"));
			configuration.setProperty("hibernate.transaction.manager_lookup_class", managerLookupClassName);
			configuration.setProperty("current_session_context_class", "jta");
			jta = true;
		} else {
			logger.warning("UserTransaction or factory class not found, attempting to autoconfigure Hibernate to use per-Thread session context");
			configuration.setProperty("current_session_context_class", "thread");
		}

		if (startJtaTransaction && ut != null && jta) { //needed for tomcat/bitronix
			try {
				ut.begin();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(new ExtClassLoader(cl));
			sessionFactory = configuration.buildSessionFactory();
		} finally {
			Thread.currentThread().setContextClassLoader(cl);
		}
		if (processToolContextFactory != null) {
			processToolContextFactory.updateSessionFactory(sessionFactory);
		}
		if (startJtaTransaction && ut != null && jta) { //needed for tomcat/bitronix
			try {
				ut.commit();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (dataSourceName == null) {
			logger.severe("Aperte Workflow runs using embedded datasource. This approach is useful only for development and demoing purposes.");
		}
	}

	/*
		<!--<property name="hibernate.connection.datasource">java:comp/env/jdbc/aperte-workflow-ds</property>-->
	 */
	private String checkForDataSource() {
		for (String dsName : getPotentialDatasourceNames()) {
			if (dsName != null) {
				if (isValidDatasource(dsName)) {
					return dsName;
				}
				else {
					logger.info("Aperte Workflow datasource bound to name " + dsName +
							" not found or is badly configured. Looking for another one");
				}
			}
		}
		logger.log(Level.SEVERE,
				"Aperte Workflow datasource not found or is badly configured, falling back to preconfigured HSQLDB. " +
						" DO NOT USE THAT IN PRODUCTION ENVIRONMENT!");
		return null;
	}

	private String[] getPotentialDatasourceNames() {
		return new String[] {
				System.getProperty("org.aperteworkflow.datasource"),
				"java:comp/env/jdbc/aperte-workflow-ds",
				"jdbc/aperte-workflow-ds",
		};
	}

	private boolean isValidDatasource(String dsName) {
		try {
			DataSource lookup = (DataSource) new InitialContext().lookup(dsName);
			lookup.getConnection().close();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	private UserTransaction findUserTransaction() {
		UserTransaction ut=null;
		if (!"true".equalsIgnoreCase(System.getProperty("org.aperteworkflow.nojta"))) {
			try {
				ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
			} catch (Exception e) {
				logger.warning("java:comp/UserTransaction not found, looking for UserTransaction");
				try {
					ut = (UserTransaction) new InitialContext().lookup("UserTransaction");
				} catch (Exception e1) {
					logger.warning("UserTransaction not found in JNDI, JTA not available!");
				}
			}
		} else {
			logger.warning("User transaction lookup disabled via org.aperteworkflow.nojta setting");
		}
		return ut;
	}

	@Override
	public ProcessDictionaryDAO getProcessDictionaryDAO(Session hibernateSession) {
		return new ProcessDictionaryDAOImpl(hibernateSession);
	}

	@Override
	public ProcessInstanceDAO getProcessInstanceDAO(Session hibernateSession) {
		return new ProcessInstanceDAOImpl(hibernateSession);
	}

	@Override
	public ProcessInstanceSimpleAttributeDAO getProcessInstanceSimpleAttributeDAO(
			Session hibernateSession) {
		return new ProcessInstanceSimpleAttributeDAOImpl(hibernateSession);
	}

	@Override
	public ProcessInstanceFilterDAO getProcessInstanceFilterDAO(Session hibernateSession) {
		return new ProcessInstanceFilterDAOImpl(hibernateSession);
	}

	@Override
	public UserSubstitutionDAO getUserSubstitutionDAO(Session hibernateSession) {
		return new UserSubstitutionDAOImpl(hibernateSession);
	}

	@Override
	public ProcessDefinitionDAO getProcessDefinitionDAO(Session hibernateSession) {
		return new ProcessDefinitionDAOImpl(hibernateSession);
	}

    @Override
    public OperationLockDAO getOperationLockDAO(Session hibernateSession) {
        return new OperationLockDAOImpl(hibernateSession);
    }
}

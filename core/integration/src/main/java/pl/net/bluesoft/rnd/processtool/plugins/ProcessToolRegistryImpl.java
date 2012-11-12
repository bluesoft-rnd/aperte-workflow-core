package pl.net.bluesoft.rnd.processtool.plugins;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.Strings.hasText;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.aperteworkflow.search.SearchProvider;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDictionaryDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceFilterDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserDataDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserProcessQueueDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.dao.impl.ProcessDefinitionDAOImpl;
import pl.net.bluesoft.rnd.processtool.dao.impl.ProcessDictionaryDAOImpl;
import pl.net.bluesoft.rnd.processtool.dao.impl.ProcessInstanceDAOImpl;
import pl.net.bluesoft.rnd.processtool.dao.impl.ProcessInstanceFilterDAOImpl;
import pl.net.bluesoft.rnd.processtool.dao.impl.UserDataDAOImpl;
import pl.net.bluesoft.rnd.processtool.dao.impl.UserProcessQueueDAOImpl;
import pl.net.bluesoft.rnd.processtool.dao.impl.UserSubstitutionDAOImpl;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryLoader;
import pl.net.bluesoft.rnd.processtool.dict.exception.DictionaryLoadingException;
import pl.net.bluesoft.rnd.processtool.dict.xml.ProcessDictionaries;
import pl.net.bluesoft.rnd.processtool.event.ProcessToolEventBusManager;
import pl.net.bluesoft.rnd.processtool.model.Cacheable;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolAutowire;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryPermission;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProvider;
import pl.net.bluesoft.rnd.util.func.Func;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;
import pl.net.bluesoft.util.cache.Caches;
import pl.net.bluesoft.util.eventbus.EventBusManager;
import pl.net.bluesoft.util.lang.FormatUtil;
import pl.net.bluesoft.util.lang.Strings;

/**
 * @author tlipski@bluesoft.net.pl
 * @author amichalak@bluesoft.net.pl
 */
public class ProcessToolRegistryImpl implements ProcessToolRegistry {

	private static final Logger logger = Logger.getLogger(ProcessToolRegistryImpl.class.getName());

    private final List<ProcessToolServiceBridge> SERVICE_BRIDGE_REGISTRY = new LinkedList<ProcessToolServiceBridge>();

	private final Map<String, Class<? extends ProcessToolWidget>> WIDGET_REGISTRY = new HashMap<String, Class<? extends ProcessToolWidget>>();
	private final Map<String, Class<? extends ProcessToolActionButton>> BUTTON_REGISTRY = new HashMap<String, Class<? extends ProcessToolActionButton>>();
    private final Map<String, List<String>> RESOURCE_REGISTRY = new HashMap<String, List<String>>();
    private final Map<String, I18NProvider> I18N_PROVIDER_REGISTRY = new HashMap<String, I18NProvider>();
    private final Map<String, Func<? extends ProcessToolProcessStep>> STEP_REGISTRY = new HashMap<String, Func<? extends ProcessToolProcessStep>>();
    private final Map<String, Class<? extends TaskItemProvider>> TASK_ITEM_REGISTRY = new HashMap<String, Class<? extends TaskItemProvider>>();

    private ExecutorService executorService = Executors.newCachedThreadPool();
	private EventBusManager eventBusManager = new ProcessToolEventBusManager(this, executorService);

    private Map<String, Class> annotatedClasses = new HashMap<String, Class>();
    private Map<String, byte[]> hibernateResources = new HashMap<String, byte[]>();
    private Map<String, ClassLoader> classLoaders = new HashMap<String, ClassLoader>();

    private Map<String, Map> caches = new HashMap<String, Map>();

    private ProcessToolContextFactory processToolContextFactory;
	private SessionFactory sessionFactory;
    private PluginManager pluginManager;
    private SearchProvider searchProvider;
    private boolean jta;
    private BundleContext bundleContext;
    private String bpmDefinitionLanguage;

    {
        //init default provider, regardless of OSGi stuff
        final ClassLoader classloader = getClass().getClassLoader();
        I18N_PROVIDER_REGISTRY.put("", new PropertiesBasedI18NProvider(new PropertyLoader() {
            @Override
            public InputStream loadProperty(String path) throws IOException {
                return classloader.getResourceAsStream(path);
            }
        }, "messages"));
    }

	public synchronized void unregisterWidget(String name) {
		WIDGET_REGISTRY.remove(name);
	}

	public synchronized void registerWidget(String name, Class<? extends ProcessToolWidget> cls) {
		WIDGET_REGISTRY.put(name, cls);
	}

	public <T extends ProcessToolWidget> T makeWidget(String name) throws IllegalAccessException, InstantiationException {
		Class<? extends ProcessToolWidget> aClass = WIDGET_REGISTRY.get(name);
		if (aClass == null) {
			throw new IllegalAccessException("No class nicknamed by: " + name);
		}
		return (T) aClass.newInstance();

	}

	public <T extends ProcessToolActionButton> T makeButton(String name) throws IllegalAccessException, InstantiationException {
		Class<? extends ProcessToolActionButton> aClass = BUTTON_REGISTRY.get(name);
		if (aClass == null) {
			throw new IllegalAccessException("No class nicknamed by: " + name);
		}
		return (T) aClass.newInstance();

	}

	public ProcessToolRegistryImpl() {
		this.processToolContextFactory = null;
		buildSessionFactory();
        updateCaches();
	}


	public ClassLoader getModelAwareClassLoader(ClassLoader parent) {
		return new ExtClassLoader(parent);
	}

    public void setOsgiBundleContext(BundleContext context) {
        this.bundleContext = context;
    }

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

	public synchronized void addClassLoader(String name, ClassLoader loader) {
		classLoaders.put(name, loader);
	}
	public synchronized void removeClassLoader(String name) {
		classLoaders.remove(name);
	}

	@Override
	public EventBusManager getEventBusManager() {
		return eventBusManager;
	}

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public <T> T lookupService(String name) {
        ServiceReference serviceReference = bundleContext.getServiceReference(name);
        if (serviceReference == null) return null;
        return (T) bundleContext.getService(serviceReference);
    }

    public void setBpmDefinitionLanguage(String bpmDefinitionLanguage) {
        this.bpmDefinitionLanguage = bpmDefinitionLanguage;
    }

    @Override
    public String getBpmDefinitionLanguage() {
        return bpmDefinitionLanguage;
    }


    public synchronized boolean addAnnotatedClass(Class<?>... classes) {
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

    public synchronized boolean removeAnnotatedClass(Class... classes) {
        boolean needUpdate = false;
        for (Class cls : classes) {
            if (annotatedClasses.containsKey(cls.getName())) {
                needUpdate = true;
                annotatedClasses.remove(cls.getName());
            }
        }
        return needUpdate;
    }

	public synchronized void addHibernateResource(String name, byte[] resource) {
	  	hibernateResources.put(name, resource);
	}

	public synchronized void removeHibernateResource(String name) {
	  	hibernateResources.remove(name);
	}

	public void buildSessionFactory() {

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
        String dsName = nvl(System.getProperty("org.aperteworkflow.datasource"), "java:comp/env/jdbc/aperte-workflow-ds");
        try {
            DataSource lookup = (DataSource) new InitialContext().lookup(dsName);
            lookup.getConnection().close();
            return dsName;
        } catch (Exception e) {
            dsName = nvl(System.getProperty("org.aperteworkflow.datasource"), "jdbc/aperte-workflow-ds");
            try {
                DataSource lookup = (DataSource) new InitialContext().lookup(dsName);
                lookup.getConnection().close();
                return dsName;
            } catch (Exception e1) {
                logger.log(Level.SEVERE, "Aperte Workflow datasource bound to name " + dsName +
                        " not found or is badly configured, falling back to preconfigured HSQLDB." +
                        " DO NOT USE THAT IN PRODUCTION ENVIRONMENT!", e);
            }
        }
        return null;
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

    public <T extends ProcessToolWidget> T makeWidget(Class<? extends ProcessToolWidget> aClass) throws IllegalAccessException, InstantiationException {
		return (T) aClass.newInstance();
	}

	public void registerI18NProvider(I18NProvider i18Provider, String providerId) {
		I18N_PROVIDER_REGISTRY.put(providerId, i18Provider);
		I18NSourceFactory.invalidateCache();
        logger.warning("Registered I18NProvider: " + providerId);
    }

    public void unregisterI18NProvider(String providerId) {
		I18N_PROVIDER_REGISTRY.remove(providerId);
		I18NSourceFactory.invalidateCache();
		logger.warning("Unregistered I18NProvider: " + providerId);
	}

	public Collection<I18NProvider> getI18NProviders() {
		return I18N_PROVIDER_REGISTRY.values();
	}

    @Override
    public boolean hasI18NProvider(String providerId) {
        return I18N_PROVIDER_REGISTRY.containsKey(providerId);
    }

    @Override
    public <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback) {
		if (processToolContextFactory == null) {
			throw new RuntimeException("No process tool context factory implementation registered");
		}
		return processToolContextFactory.withProcessToolContext(callback);
	}

    @Override
       public <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback) {
           if (processToolContextFactory == null) {
               throw new RuntimeException("No process tool context factory implementation registered");
           }
           return processToolContextFactory.withExistingOrNewContext(callback);
       }

    @Override
    public ProcessDictionaryDAO getProcessDictionaryDAO(Session hibernateSession) {
        return new ProcessDictionaryDAOImpl(hibernateSession);
    }

    @Override
	public ProcessInstanceDAO getProcessInstanceDAO(Session hibernateSession) {
		return new ProcessInstanceDAOImpl(hibernateSession, searchProvider);
	}

	@Override
	public ProcessInstanceFilterDAO getProcessInstanceFilterDAO(Session hibernateSession) {
        return new ProcessInstanceFilterDAOImpl(hibernateSession);
	}

	@Override
	public UserDataDAO getUserDataDAO(Session hibernateSession) {
		return new UserDataDAOImpl(hibernateSession);
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
	public UserProcessQueueDAO getUserProcessQueueDAO(Session hibernateSession)
	{
		return new UserProcessQueueDAOImpl(hibernateSession);
	} 

	@Override
	public boolean registerModelExtension(Class<?>... cls) {
        logger.warning("Registered model extensions: " + FormatUtil.joinClassNames(cls));
		return addAnnotatedClass(cls);
	}

	@Override
	public void commitModelExtensions() {
		buildSessionFactory();
	}

	public ProcessToolContextFactory getProcessToolContextFactory() {
		return processToolContextFactory;
	}

	public void setProcessToolContextFactory(ProcessToolContextFactory processToolContextFactory) {
		this.processToolContextFactory = processToolContextFactory;
	}

	@Override
	public void unregisterProcessToolContextFactory(Class<?> cls) {
		if (processToolContextFactory == null ||
				processToolContextFactory.getClass().getName().equals(cls.getName())) {
			processToolContextFactory = null;
		}
	}

	@Override
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

    @Override
	public boolean unregisterModelExtension(Class<?>... cls) {
        logger.warning("Unregistered model extensions: " + FormatUtil.joinClassNames(cls));
        return removeAnnotatedClass(cls);
	}

    @Override
    public Map<String, Class<? extends ProcessToolWidget>> getAvailableWidgets() {
        return new HashMap(WIDGET_REGISTRY);
    }
    
    
	@Override
	public void registerWidget(Class<?> cls) {
		registerWidget(cls.getName(), (Class<? extends ProcessToolWidget>) cls);
        logger.info("Registered widget extension: " + cls.getName());
        AliasName annotation = (AliasName) cls.getAnnotation(AliasName.class);
		if (annotation != null) {
			registerWidget(annotation.name(), (Class<? extends ProcessToolWidget>) cls);
            logger.info("Registered widget alias: " + annotation.name() + " -> " + cls.getName());
        }
	}

	@Override
	public void unregisterWidget(Class<?> cls) {
		unregisterWidget(cls.getName());
        logger.info("Unregistered widget extension: " + cls.getName());
		AliasName annotation = (AliasName) cls.getAnnotation(AliasName.class);
		if (annotation != null) {
			unregisterWidget(annotation.name());
            logger.info("Unregistered widget alias: " + annotation.name() + " -> " + cls.getName());
		}
	}


	@Override
	public void registerButton(Class<?> cls) {
		AliasName annotation = cls.getAnnotation(AliasName.class);
		if (annotation != null) {
			BUTTON_REGISTRY.put(annotation.name(), (Class<? extends ProcessToolActionButton>) cls);
            logger.info("Registered button alias: " + annotation.name() + " -> " + cls.getName());
		}
	}

	@Override
	public void unregisterButton(Class<?> cls) {
        AliasName annotation = cls.getAnnotation(AliasName.class);
		if (annotation != null) {
			BUTTON_REGISTRY.remove(annotation.name());
            logger.info("Unregistered button alias: " + annotation.name() + " -> " + cls.getName());
		}
	}

    @Override
    public Map<String,Class<? extends ProcessToolActionButton>> getAvailableButtons() {
        return new HashMap<String, Class<? extends ProcessToolActionButton>>(BUTTON_REGISTRY);
    }

    private class StepClassFunc implements Func<ProcessToolProcessStep> {

        private Class<? extends ProcessToolProcessStep> cls;

        private StepClassFunc(Class<? extends ProcessToolProcessStep> cls) {
            this.cls = cls;
        }

        @Override
        public ProcessToolProcessStep invoke() {
            try {
                return cls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void registerStep(String name, Func<? extends ProcessToolProcessStep> f) {
        STEP_REGISTRY.put(name, f);
        logger.info("Registered step extension: " + name);
    }


	public void registerStep(Class<? extends ProcessToolProcessStep> cls) {
        registerStep(cls.getName(), new StepClassFunc(cls));
        AliasName annotation = cls.getAnnotation(AliasName.class);
        if (annotation != null) {
            registerStep(annotation.name(), new StepClassFunc(cls));
        }
	}

	public void unregisterStep(String name) {
        STEP_REGISTRY.remove(name);
        logger.info("Unregistered step extension: " + name);
    }

	public void unregisterStep(Class<? extends ProcessToolProcessStep> cls) {
        unregisterStep(cls.getName());
        AliasName annotation = cls.getAnnotation(AliasName.class);
        if (annotation != null) {
            unregisterStep(annotation.name());
        }
	}

    public Map<String,ProcessToolProcessStep> getAvailableSteps() {
        Map<String,ProcessToolProcessStep> steps = new HashMap<String,ProcessToolProcessStep>();
        for (Map.Entry<String, Func<? extends ProcessToolProcessStep>> e : STEP_REGISTRY.entrySet()) {
            steps.put(e.getKey(), e.getValue().invoke());
        }
        return steps;
    }

	public ProcessToolProcessStep getStep(String name) {
        Func<? extends ProcessToolProcessStep> func = STEP_REGISTRY.get(name);
        if (func != null) return func.invoke();
        return null;
	}

    @Override
	public void registerTaskItemProvider(Class<?> cls) {
		AliasName annotation = cls.getAnnotation(AliasName.class);
		if (annotation != null) {
			TASK_ITEM_REGISTRY.put(annotation.name(), (Class<? extends TaskItemProvider>) cls);
			logger.warning("Registered task item alias: " + annotation.name() + " -> " + cls.getName());
		}
	}

	@Override
	public void unregisterTaskItemProvider(Class<?> cls) {
		unregisterTaskItemProvider(cls.getName());
		AliasName annotation = cls.getAnnotation(AliasName.class);
		if (annotation != null) {
			unregisterTaskItemProvider(annotation.name());
		}
	}

	public void unregisterTaskItemProvider(String name) {
		TASK_ITEM_REGISTRY.remove(name);
	}

	@Override
	public TaskItemProvider makeTaskItemProvider(String name) throws IllegalAccessException, InstantiationException {
		Class<? extends TaskItemProvider> aClass = TASK_ITEM_REGISTRY.get(name);
		if (aClass == null) {
			throw new IllegalAccessException("No class nicknamed by: " + name);
		}
		return aClass.newInstance();
	}

       @Override
       public void registerGlobalDictionaries(InputStream is) {
           if (is != null) {
               ProcessDictionaries dictionaries = (ProcessDictionaries) DictionaryLoader.getInstance().unmarshall(is);
               String processBpmKey = dictionaries.getProcessBpmDefinitionKey();
               if (Strings.hasText(processBpmKey)) {
                   logger.warning("Global process dictionary should not be defined for a specific process: "
                           + dictionaries.getProcessBpmDefinitionKey());
               }
               Session session = sessionFactory.openSession();
               try {
                   Transaction tx = session.beginTransaction();
                   saveDictionaryInternal(session, null, dictionaries);
                   tx.commit();
                   logger.warning("Registered global dictionaries");
               }
               finally {
                   session.close();
               }
           }
       }

       @Override
       public void registerProcessDictionaries(InputStream is) {
           if (is != null) {
               ProcessDictionaries dictionaries = (ProcessDictionaries) DictionaryLoader.getInstance().unmarshall(is);
               String processBpmKey = dictionaries.getProcessBpmDefinitionKey();
               if (!Strings.hasText(processBpmKey)) {
                   throw new DictionaryLoadingException("No process name specified in the dictionaries XML");
               }
               Session session = sessionFactory.openSession();
               try {
                   Transaction tx = session.beginTransaction();
                   ProcessDefinitionConfig definitionConfig = getProcessDefinitionDAO(session).getActiveConfigurationByKey(processBpmKey);
                   if (definitionConfig == null) {
                       throw new DictionaryLoadingException("No active definition config with BPM key: " + processBpmKey);
                   }
                   saveDictionaryInternal(session, definitionConfig, dictionaries);
                   tx.commit();
                   logger.warning("Registered dictionaries for process: " + processBpmKey);
               }
               finally {
                   session.close();
               }
           }
       }

       private void saveDictionaryInternal(Session session, ProcessDefinitionConfig definitionConfig, ProcessDictionaries dictionaries) {
           ProcessDictionaryDAO dao = getProcessDictionaryDAO(session);
           List<ProcessDBDictionary> processDBDictionaries = DictionaryLoader.getDictionariesFromXML(dictionaries);
           for (ProcessDBDictionary dict : processDBDictionaries) {
               for (ProcessDBDictionaryPermission perm : dict.getPermissions()) {
                   if (!Strings.hasText(perm.getRoleName())) {
                       perm.setRoleName(PATTERN_MATCH_ALL);
                   }
                   if (!Strings.hasText(perm.getPrivilegeName())) {
                       perm.setPrivilegeName(PRIVILEGE_EDIT);
                   }
               }
           }
           DictionaryLoader.validateDictionaries(processDBDictionaries);
           dao.createOrUpdateDictionaries(definitionConfig, processDBDictionaries,
                   dictionaries.getOverwrite() != null && dictionaries.getOverwrite());
       }
    @Override
	public void deployOrUpdateProcessDefinition(final InputStream jpdlStream,
	                                            final ProcessDefinitionConfig cfg,
	                                            final ProcessQueueConfig[] queues,
	                                            final InputStream imageStream,
	                                            InputStream logoStream) {
		if (processToolContextFactory == null) {
			throw new RuntimeException("No process tool context factory implementation registered");
		}
		processToolContextFactory.deployOrUpdateProcessDefinition(jpdlStream, cfg, queues, imageStream, logoStream);
	}


    @Override
    public void deployOrUpdateProcessDefinition(InputStream jpdlStream,
                                                InputStream processToolConfigStream,
                                                InputStream queueConfigStream,
                                                InputStream imageStream,
                                                InputStream logoStream) {
        if (processToolContextFactory == null) {
            throw new RuntimeException("No process tool context factory implementation registered");
        }
        processToolContextFactory.deployOrUpdateProcessDefinition(jpdlStream, processToolConfigStream, queueConfigStream, imageStream, logoStream);
    }

    @Override
    public void addServiceLoader(ProcessToolServiceBridge serviceBridge) {
        if (serviceBridge != null) {
            SERVICE_BRIDGE_REGISTRY.add(serviceBridge);
            logger.warning("Registered service bridge: " + serviceBridge.getClass().getName());
        }
    }

    @Override
    public void removeServiceLoader(ProcessToolServiceBridge serviceBridge) {
        if (serviceBridge != null) {
            SERVICE_BRIDGE_REGISTRY.remove(serviceBridge);
            logger.warning("Removed service bridge: " + serviceBridge.getClass().getName());
        }
    }

    @Override
    public List<ProcessToolServiceBridge> getServiceLoaders() {
        return SERVICE_BRIDGE_REGISTRY;
    }

    @Override
    public void removeRegisteredService(Class<?> serviceClass) {
        boolean result = false;
        for (ProcessToolServiceBridge bridge : SERVICE_BRIDGE_REGISTRY) {
            if (result = bridge.removeService(serviceClass)) {
                break;
            }
        }
        logger.warning((result ? "Succeeded to" : "Failed to") + " remove registered service: " + serviceClass.getName());
    }

    @Override
    public <T> void registerService(Class<T> serviceClass, T instance, Properties properties) {
        boolean result = false;
        for (ProcessToolServiceBridge bridge : SERVICE_BRIDGE_REGISTRY) {
            if (result = bridge.registerService(serviceClass, instance, properties)) {
                break;
            }
        }
        logger.warning((result ? "Succeeded to" : "Failed to") + " register service: " + serviceClass.getName());
    }

    @Override
    public <T> T getRegisteredService(Class<T> serviceClass) {
        Object service = null;
        for (ProcessToolServiceBridge bridge : SERVICE_BRIDGE_REGISTRY) {
            service = bridge.loadService(serviceClass);
            if (service != null) {
                break;
            }
        }
        if (service == null) {
            throw new NoSuchServiceException("Service " + serviceClass.getName() + " not found!");
        }
        return (T) service;
    }


    @Override
    public boolean isJta() {
        return jta;
    }
    
    public SearchProvider getSearchProvider() {
        return searchProvider;
    }

    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @Override
    public void registerResource(String bundleSymbolicName, String path) {
        List<String> resources = RESOURCE_REGISTRY.get(bundleSymbolicName);
        if (resources == null) {
            resources = new ArrayList<String>();
            RESOURCE_REGISTRY.put(bundleSymbolicName, resources);
        }
        resources.add(path);
    }

    @Override
    public void removeRegisteredResources(String bundleSymbolicName) {
        RESOURCE_REGISTRY.remove(bundleSymbolicName);
        logger.warning("Removed resources for bundle: " + bundleSymbolicName);
    }

    @Override
    public InputStream loadResource(String bundleSymbolicName, String path) {
        boolean searchResource = false;
        if (hasText(bundleSymbolicName)) {
            if (RESOURCE_REGISTRY.containsKey(bundleSymbolicName)) {
                List<String> resources = RESOURCE_REGISTRY.get(bundleSymbolicName);
                if (resources.contains(path)) {
                    searchResource = true;
                }
            }
        }
        else {
            searchResource = true;
        }
        if (searchResource) {
            for (ProcessToolServiceBridge bridge : SERVICE_BRIDGE_REGISTRY) {
                try {
                    InputStream stream = bridge.loadResource(bundleSymbolicName, path);
                    if (stream != null) {
                        return stream;
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    @Override
        public InputStream loadResource(String path) {
            return loadResource(null, path);
        }

        private void updateCaches() {
            Class<? extends Cacheable<String, String>>[] cachedEntities = new Class[] {ProcessToolAutowire.class};

            Session session = sessionFactory.openSession();
            try {
                Transaction tx = session.beginTransaction();
                for (Class<? extends Cacheable<String, String>> entityClass : cachedEntities) {
                    Map<String, String> cache = Caches.synchronizedCache(100);
                    List<Cacheable<String, String>> list = session.createCriteria(entityClass).list();
                    for (Cacheable<String, String> obj : list) {
                        cache.put(obj.getKey(), obj.getValue());
                    }
                    registerCache(entityClass.getName(), cache);
                }
                tx.commit();
            }
            finally {
                session.close();
            }
        }

        @Override
        public <K, V> Map<K, V> getCache(String cacheName) {
            return caches.get(cacheName);
        }

        public <K, V> void registerCache(String cacheName, Map<K, V> cache) {
            caches.put(cacheName, cache);
            logger.warning("Registered cache named: " + cacheName);
        }


}

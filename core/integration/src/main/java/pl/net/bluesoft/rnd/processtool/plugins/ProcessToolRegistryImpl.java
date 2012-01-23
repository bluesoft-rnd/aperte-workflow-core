package pl.net.bluesoft.rnd.processtool.plugins;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.dao.impl.*;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryLoader;
import pl.net.bluesoft.rnd.processtool.dict.xml.ProcessDictionaries;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.util.func.Func;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;
import pl.net.bluesoft.util.eventbus.EventBusManager;
import pl.net.bluesoft.util.lang.FormatUtil;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolRegistryImpl implements ProcessToolRegistry {

	private static Logger logger = Logger.getLogger(ProcessToolRegistryImpl.class.getName());

    private final List<ProcessToolServiceBridge> SERVICE_BRIDGE_REGISTRY = new LinkedList<ProcessToolServiceBridge>();
	private final Map<String, Class<? extends ProcessToolWidget>> WIDGET_REGISTRY = new HashMap();
	private final Map<String, Class<? extends ProcessToolActionButton>> BUTTON_REGISTRY = new HashMap();
	private SessionFactory sessionFactory;
	private EventBusManager eventBusManager = new EventBusManager();
    private PluginManager pluginManager;
    private boolean jta;

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
	}

    private Map<String,Class> annotatedClasses = new HashMap<String, Class>();
	private Map<String,byte[]> hibernateResources = new HashMap<String, byte[]>();
	private Map<String,ClassLoader> classloaders = new HashMap();

	public ClassLoader getModelAwareClassLoader(ClassLoader parent) {
		return new ExtClassLoader(parent);
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
			for (ClassLoader loader : classloaders.values()) {
				try {
					Class<?> aClass1 = loader.loadClass(name);
					if (aClass1 != null) return aClass1;
				}
				catch (Exception e) { //do nothing
				}
			}
			return super.loadClass(name);    //To change body of overridden methods use File | Settings | File Templates.
		}
	}

	public synchronized void addClassLoader(String name, ClassLoader loader) {
		classloaders.put(name, loader);
	}
	public synchronized void removeClassLoader(String name) {
		classloaders.remove(name);
	}

	@Override
	public EventBusManager getEventBusManager() {
		return eventBusManager;
	}

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
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
        UserTransaction ut=null;
        try {
            ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        } catch (Exception e) {
            logger.warning("java:comp/UserTransaction not found, looking for UserTransaction");
            try {
                ut = (UserTransaction) new InitialContext().lookup("UserTransaction");
            }
            catch (Exception e1) {
                logger.warning("UserTransaction not found in JNDI, JTA not available!");
            }
        }

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

        String managerLookupClassName=null;
        if (ut != null && !"true".equalsIgnoreCase(System.getProperty("org.aperteworkflow.nojta"))) { //try to autodetect JTA settings
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
		}
		finally {
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

    }

	public <T extends ProcessToolWidget> T makeWidget(Class<? extends ProcessToolWidget> aClass) throws IllegalAccessException, InstantiationException {
		return (T) aClass.newInstance();
	}


	private final Map<String, I18NProvider> registeredI18NProviders = new HashMap();

    {   //init default provider, regardless of OSGi stuff
		final ClassLoader classloader = getClass().getClassLoader();
		registeredI18NProviders.put("", new PropertiesBasedI18NProvider(new PropertyLoader() {
			@Override
			public InputStream loadProperty(String path) throws IOException {
				return classloader.getResourceAsStream(path);
			}
		}, "messages"));
	}

	public void registerI18NProvider(I18NProvider i18Provider, String providerId) {
		registeredI18NProviders.put(providerId, i18Provider);
	}

	public void unregisterI18NProvider(String providerId) {
		registeredI18NProviders.remove(providerId);
	}

	public Collection<I18NProvider> getI18NProviders() {
		return registeredI18NProviders.values();
	}

	@Override
	public void withProcessToolContext(ProcessToolContextCallback callback) {
		if (processToolContextFactory == null) {
			throw new RuntimeException("No process tool context factory implementation registered");
		}
		processToolContextFactory.withProcessToolContext(callback);
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

	ProcessToolContextFactory processToolContextFactory;

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

	private final Map<String, Func<? extends ProcessToolProcessStep>> STEP_REGISTRY = new HashMap<String, Func<? extends ProcessToolProcessStep>>();

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
    public void registerDictionaries(InputStream dictionariesStream) {
        if (dictionariesStream == null) {
            return;
        }

        ProcessDictionaries dictionaries = (ProcessDictionaries) DictionaryLoader.getInstance().unmarshall(dictionariesStream);
        String processBpmKey = dictionaries.getProcessBpmDefinitionKey();
        if (!StringUtil.hasText(processBpmKey)) {
            throw new RuntimeException("No process name specified in the dictionaries XML");
        }
        Session session = sessionFactory.openSession();
        try {
            Transaction tx = session.beginTransaction();
            ProcessDefinitionConfig definitionConfig = getProcessDefinitionDAO(session).getActiveConfigurationByKey(processBpmKey);
            if (definitionConfig == null) {
                throw new RuntimeException("No active definition config with BPM key: " + processBpmKey);
            }
            ProcessDictionaryDAO dao = getProcessDictionaryDAO(session);
            List<ProcessDBDictionary> processDBDictionaries = DictionaryLoader.getDictionariesFromXML(dictionaries);
            dao.createOrUpdateProcessDictionaries(definitionConfig, processDBDictionaries, dictionaries.getOverwrite() != null
                    && dictionaries.getOverwrite());
            tx.commit();
        }
        finally {
            session.close();
        }
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
}

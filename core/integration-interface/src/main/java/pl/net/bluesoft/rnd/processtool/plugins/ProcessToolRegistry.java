package pl.net.bluesoft.rnd.processtool.plugins;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProvider;
import pl.net.bluesoft.rnd.util.func.Func;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;


/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolRegistry extends ProcessToolBpmConstants {

    void registerResource(String bundleSymbolicName, String path);

    void removeRegisteredResources(String bundleSymbolicName);

    InputStream loadResource(String bundleSymbolicName, String path);

    InputStream loadResource(String path);

	boolean registerModelExtension(Class<?>... cls);

	boolean unregisterModelExtension(Class<?>... cls);

	void commitModelExtensions();

	void registerWidget(Class<?> cls);

	void unregisterWidget(Class<?> cls);

	void registerButton(Class<?> cls);

	void unregisterButton(Class<?> cls);

    void registerStep(String name, Func<? extends ProcessToolProcessStep> f);

	void registerStep(Class<? extends ProcessToolProcessStep> cls);

    void unregisterStep(String name);

	void unregisterStep(Class<? extends ProcessToolProcessStep> cls);

    Map<String,ProcessToolProcessStep> getAvailableSteps();

	ProcessToolProcessStep getStep(String name);

    void registerProcessDictionaries(InputStream dictionariesStream);

    void registerGlobalDictionaries(InputStream dictionariesStream);

	void deployOrUpdateProcessDefinition(InputStream jpdlStream,
	                                     ProcessDefinitionConfig cfg,
	                                     ProcessQueueConfig[] queues,
	                                     final InputStream imageStream,
	                                     InputStream logoStream);

	void deployOrUpdateProcessDefinition(InputStream jpdlStream,
	                                     InputStream processToolConfigStream,
	                                     InputStream queueConfigStream,
	                                     InputStream imageStream,
	                                     InputStream logoStream);

	<T extends ProcessToolWidget> T makeWidget(String name)
			throws IllegalAccessException, InstantiationException;

	<T extends ProcessToolActionButton> T makeButton(String name) throws IllegalAccessException, InstantiationException;

	<T extends ProcessToolWidget> T makeWidget(Class<? extends ProcessToolWidget> aClass)
			throws IllegalAccessException, InstantiationException;

	void registerI18NProvider(I18NProvider p, String providerId);

	void unregisterI18NProvider(String providerId);

	Collection<I18NProvider> getI18NProviders();

    boolean hasI18NProvider(String providerId);

    <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback);

    <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback);

//	void withProcessToolContext(ProcessToolContextCallback callback);

    ProcessDictionaryDAO getProcessDictionaryDAO(Session hibernateSession);

	ProcessInstanceDAO getProcessInstanceDAO(Session hibernateSession);

    ProcessInstanceFilterDAO getProcessInstanceFilterDAO(Session hibernateSession);

	UserDataDAO getUserDataDAO(Session hibernateSession);

    UserSubstitutionDAO getUserSubstitutionDAO(Session hibernateSession);

	ProcessDefinitionDAO getProcessDefinitionDAO(Session hibernateSession);
	
	UserProcessQueueDAO getUserProcessQueueDAO(Session hibernateSession);

	ProcessToolContextFactory getProcessToolContextFactory();
	

	void setProcessToolContextFactory(ProcessToolContextFactory processToolContextFactory);

	void unregisterProcessToolContextFactory(Class<?> cls);

	SessionFactory getSessionFactory();

	void addHibernateResource(String name, byte[] resource);

	void removeHibernateResource(String name);

	void addClassLoader(String name, ClassLoader loader);

	ClassLoader getModelAwareClassLoader(ClassLoader parent);

	void removeClassLoader(String name);

	EventBusManager getEventBusManager();
    
    PluginManager getPluginManager();

    void setPluginManager(PluginManager pluginManager);

    void addServiceLoader(ProcessToolServiceBridge serviceBridge);

    void removeServiceLoader(ProcessToolServiceBridge serviceBridge);
    
    List<ProcessToolServiceBridge> getServiceLoaders();

    void removeRegisteredService(Class<?> serviceClass);

    <T> void registerService(Class<T> serviceClass, T instance, Properties properties);

    <T> T getRegisteredService(Class<T> serviceClass);

    boolean isJta();

    Map<String, Class<? extends ProcessToolWidget>> getAvailableWidgets();

    Map<String,Class<? extends ProcessToolActionButton>> getAvailableButtons();
    
    <T> T lookupService(String name);

    String getBpmDefinitionLanguage();

    ExecutorService getExecutorService();

    <K, V> void registerCache(String cacheName, Map<K, V> cache);

    <K, V> Map<K, V> getCache(String cacheName);

    void registerTaskItemProvider(Class<?> cls);

    void unregisterTaskItemProvider(Class<?> cls);

    TaskItemProvider makeTaskItemProvider(String name) throws IllegalAccessException, InstantiationException;

    //no way!
//    public boolean createRoleIfNotExists(String roleName, String description);

	public class ThreadUtil {
		private static final ThreadLocal<ProcessToolRegistry> processToolRegistry = new ThreadLocal<ProcessToolRegistry>();

		public static void setThreadRegistry(ProcessToolRegistry registry) {
			processToolRegistry.set(registry);
		}

		public static ProcessToolRegistry getThreadRegistry() {
			return processToolRegistry.get();
		}

		public static void removeThreadRegistry() {
			processToolRegistry.remove();
		}
	}
}

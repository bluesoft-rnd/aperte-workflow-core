package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
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
import pl.net.bluesoft.rnd.processtool.dao.UserRoleDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.domain.IWidgetScriptProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem.TaskItemProvider;
import pl.net.bluesoft.rnd.util.func.Func;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.util.eventbus.EventBusManager;


/**
 * Registry which stores all configuration parameters from osgi bundles and
 * hibernate configuration context. All content from registered bundles is
 * stored here
 * 
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
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
	
	UserRoleDAO getUserRoleDao(Session hibernateSession);

    Map<String,ProcessToolProcessStep> getAvailableSteps();
    
    /** Get widget class by given name */
    Class<? extends ProcessToolWidget> getWidgetClassName(String widgetName);

	ProcessToolProcessStep getStep(String name);

    void registerProcessDictionaries(InputStream dictionariesStream, ProcessDefinitionConfig newConfig, ProcessDefinitionConfig oldConfig);

    void registerGlobalDictionaries(InputStream dictionariesStream);

	<T extends ProcessToolActionButton> T makeButton(String name) throws IllegalAccessException, InstantiationException;

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
    
    ProcessInstanceSimpleAttributeDAO getProcessInstanceSimpleAttributeDAO(Session hibernateSession);
    
    ProcessStateActionDAO getProcessStateAction(Session hibernateSession);

	ProcessDefinitionDAO getProcessDefinitionDAO(Session hibernateSession);
	
	UserProcessQueueDAO getUserProcessQueueDAO(Session hibernateSession);

	ProcessToolContextFactory getProcessToolContextFactory();
	

	void setProcessToolContextFactory(ProcessToolContextFactory processToolContextFactory);

	void unregisterProcessToolContextFactory(Class<?> cls);

	SessionFactory getSessionFactory();

	ProcessToolSessionFactory getProcessToolSessionFactory();

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

	Map<String, Class<? extends TaskItemProvider>> getAvailableTaskItemProviders();

    /** Get plugin controller for web invocation */
    IOsgiWebController getWebController(String controllerName);

    /** register new plugin contorller */
    void registerWebController(String controllerName, IOsgiWebController controller);

    /** Unregister plugin web controller */
    void unregisterWebController(String controllerName);

	class Util {
		private static ProcessToolRegistry instance;
		private static ClassLoader awfClassLoader;

		public static ProcessToolRegistry getRegistry() {
			return instance;
		}

		public static void setInstance(ProcessToolRegistry instance) {
			Util.instance = instance;
		}

		public static ClassLoader getAwfClassLoader() {
			return awfClassLoader;
		}

		public static void setAwfClassLoader(ClassLoader awfClassLoader) {
			Util.awfClassLoader = awfClassLoader;
		}
	}

	/** Register new javaScript file for html widgets */
	void registerJavaScript(String fileName, IWidgetScriptProvider scriptProvider);

	/** Unregister new javaScript file for html widgets */
	void unregisterJavaScript(String fileName);

	/** Register new html view for widgets */
	void registerHtmlView(String widgetName, ProcessHtmlWidget scriptProvider);
	
	/** Unregister new html view for widgets */
	void unregisterHtmlView(String widgetName);
	
	/** Get Html Widget definition */
	ProcessHtmlWidget getHtmlWidget(String widgetName);

    Collection<ProcessHtmlWidget> getHtmlWidgets();
	
	/** Get Scripts */
	String getJavaScripts();
}

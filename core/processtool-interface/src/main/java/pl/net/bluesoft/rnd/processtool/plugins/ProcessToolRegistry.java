package pl.net.bluesoft.rnd.processtool.plugins;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.dao.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.util.func.Func;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolRegistry {

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

	ProcessToolProcessStep getStep(String name);

    void registerDictionaries(InputStream dictionariesStream);

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

	void withProcessToolContext(ProcessToolContextCallback callback);

    ProcessDictionaryDAO getProcessDictionaryDAO(Session hibernateSession);

	ProcessInstanceDAO getProcessInstanceDAO(Session hibernateSession);

	UserDataDAO getUserDataDAO(Session hibernateSession);

    UserSubstitutionDAO getUserSubstitutionDAO(Session hibernateSession);

	ProcessDefinitionDAO getProcessDefinitionDAO(Session hibernateSession);

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

}

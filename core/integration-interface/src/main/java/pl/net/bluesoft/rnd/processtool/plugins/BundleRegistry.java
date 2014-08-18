package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.util.i18n.I18NProvider;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 21:36
 */
public interface BundleRegistry {
	void registerResource(String bundleSymbolicName, String path);
	void removeRegisteredResources(String bundleSymbolicName);
	InputStream loadResource(String bundleSymbolicName, String path);
	InputStream loadResource(String path);

	void registerI18NProvider(I18NProvider p, String providerId);
	void unregisterI18NProvider(String providerId);
	Collection<I18NProvider> getI18NProviders();
	boolean hasI18NProvider(String providerId);

	void addServiceLoader(ProcessToolServiceBridge serviceBridge);
	void removeServiceLoader(ProcessToolServiceBridge serviceBridge);
	List<ProcessToolServiceBridge> getServiceLoaders();

	<T> void registerService(Class<T> serviceClass, T instance, Properties properties);
	void removeRegisteredService(Class<?> serviceClass);
	<T> T getRegisteredService(Class<T> serviceClass);

	PluginManager getPluginManager();
	void setPluginManager(PluginManager pluginManager);

	<T> T lookupService(String name);

	void registerBundleExtensionHandler(BundleExtensionHandler handler);
	void unregisterBundleExtensionHandler(BundleExtensionHandler handler);
	List<BundleExtensionHandler> getBundleExtensionHandlers();
}

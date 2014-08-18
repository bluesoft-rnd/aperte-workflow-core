package pl.net.bluesoft.rnd.processtool.plugins;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 21:36
 */
@Component
@Scope(value = "singleton")
public class BundleRegistryImpl implements BundleRegistry {
	private static final Logger logger = Logger.getLogger(BundleRegistryImpl.class.getSimpleName());

	private final Map<String, List<String>> resources = new HashMap<String, List<String>>();
	private final Map<String, I18NProvider> i18NProviders = new HashMap<String, I18NProvider>();
	private final List<ProcessToolServiceBridge> serviceBridges = new ArrayList<ProcessToolServiceBridge>();
	private final List<BundleExtensionHandler> bundleExtensionHandlers = new ArrayList<BundleExtensionHandler>();

	private PluginManager pluginManager;
	private BundleContext bundleContext;

	public BundleRegistryImpl() {
		//init default provider, regardless of OSGi stuff
		final ClassLoader classloader = getClass().getClassLoader();
		i18NProviders.put("", new PropertiesBasedI18NProvider(new PropertyLoader() {
			@Override
			public InputStream loadProperty(String path) throws IOException {
				return classloader.getResourceAsStream(path);
			}
		}, "messages"));
	}

	@Override
	public synchronized void registerResource(String bundleSymbolicName, String path) {
		List<String> resources = this.resources.get(bundleSymbolicName);
		if (resources == null) {
			resources = new ArrayList<String>();
			this.resources.put(bundleSymbolicName, resources);
		}
		resources.add(path);
	}

	@Override
	public synchronized void removeRegisteredResources(String bundleSymbolicName) {
		resources.remove(bundleSymbolicName);
		logger.warning("Removed resources for bundle: " + bundleSymbolicName);
	}

	@Override
	public synchronized InputStream loadResource(String bundleSymbolicName, String path) {
		boolean searchResource = false;
		if (hasText(bundleSymbolicName)) {
			if (resources.containsKey(bundleSymbolicName)) {
				List<String> resources = this.resources.get(bundleSymbolicName);
				if (resources.contains(path)) {
					searchResource = true;
				}
			}
		}
		else {
			searchResource = true;
		}
		if (searchResource) {
			for (ProcessToolServiceBridge bridge : serviceBridges) {
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

	@Override
	public synchronized void registerI18NProvider(I18NProvider i18Provider, String providerId) {
		i18NProviders.put(providerId, i18Provider);
		I18NSourceFactory.invalidateCache();
		logger.warning("Registered I18NProvider: " + providerId);
	}

	@Override
	public synchronized void unregisterI18NProvider(String providerId) {
		i18NProviders.remove(providerId);
		I18NSourceFactory.invalidateCache();
		logger.warning("Unregistered I18NProvider: " + providerId);
	}

	@Override
	public synchronized Collection<I18NProvider> getI18NProviders() {
		return new ArrayList<I18NProvider>(i18NProviders.values());
	}

	@Override
	public synchronized boolean hasI18NProvider(String providerId) {
		return i18NProviders.containsKey(providerId);
	}

	@Override
	public synchronized void addServiceLoader(ProcessToolServiceBridge serviceBridge) {
		if (serviceBridge != null) {
			serviceBridges.add(serviceBridge);
			logger.warning("Registered service bridge: " + serviceBridge.getClass().getName());
		}
	}

	@Override
	public synchronized void removeServiceLoader(ProcessToolServiceBridge serviceBridge) {
		if (serviceBridge != null) {
			serviceBridges.remove(serviceBridge);
			logger.warning("Removed service bridge: " + serviceBridge.getClass().getName());
		}
	}

	@Override
	public synchronized List<ProcessToolServiceBridge> getServiceLoaders() {
		return serviceBridges;
	}

	@Override
	public synchronized void removeRegisteredService(Class<?> serviceClass) {
		boolean result = false;
		for (ProcessToolServiceBridge bridge : serviceBridges) {
			if (result = bridge.removeService(serviceClass)) {
				break;
			}
		}
		logger.warning((result ? "Succeeded to" : "Failed to") + " remove registered service: " + serviceClass.getName());
	}

	@Override
	public synchronized <T> void registerService(Class<T> serviceClass, T instance, Properties properties) {
		boolean result = false;
		for (ProcessToolServiceBridge bridge : serviceBridges) {
			if (result = bridge.registerService(serviceClass, instance, properties)) {
				break;
			}
		}
		logger.warning((result ? "Succeeded to" : "Failed to") + " register service: " + serviceClass.getName());
	}

	@Override
	public synchronized <T> T getRegisteredService(Class<T> serviceClass) {
		Object service = null;
		for (ProcessToolServiceBridge bridge : serviceBridges) {
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
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	@Override
	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void setOsgiBundleContext(BundleContext context) {
		this.bundleContext = context;
	}

	@Override
	public synchronized <T> T lookupService(String name) {
		ServiceReference serviceReference = bundleContext.getServiceReference(name);
		if (serviceReference == null) {
			return null;
		}
		return (T) bundleContext.getService(serviceReference);
	}

	@Override
	public void registerBundleExtensionHandler(BundleExtensionHandler handler) {
		bundleExtensionHandlers.add(handler);
	}

	@Override
	public void unregisterBundleExtensionHandler(BundleExtensionHandler handler) {
		bundleExtensionHandlers.remove(handler);
	}

	@Override
	public List<BundleExtensionHandler> getBundleExtensionHandlers() {
		return Collections.unmodifiableList(bundleExtensionHandlers);
	}
}

package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory.ExecutionType;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.cache.CacheProvider;
import pl.net.bluesoft.rnd.processtool.hibernate.lock.OperationWithLock;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;


/**
 * Registry which stores all configuration parameters from osgi bundles and
 * hibernate configuration context. All content from registered bundles is
 * stored here
 * 
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public interface ProcessToolRegistry {
	GuiRegistry getGuiRegistry();
	BundleRegistry getBundleRegistry();
	DataRegistry getDataRegistry();

    void registerGlobalDictionaries(InputStream dictionariesStream);

	IUserSource getUserSource();
	void setUserSource(IUserSource userSource);

    <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback);
    <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback, ExecutionType type);
    <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback);
    <T> T withOperationLock(final OperationWithLock<T> operation,
                            final String lockName,
                            final OperationLockMode mode,
                            final  Integer expireAfterMinutes);

	ProcessToolSessionFactory getProcessToolSessionFactory();

	EventBusManager getEventBusManager();

    <T> T lookupService(String name);
	<T> T getRegisteredService(Class<T> serviceClass);

    ExecutorService getExecutorService();

	void registerCacheProvider(String cacheId, CacheProvider cacheProvider);
	void unregisterCacheProvider(String cacheId);
	Map<String, CacheProvider> getCacheProviders();

	UserData getAutoUser();

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
}

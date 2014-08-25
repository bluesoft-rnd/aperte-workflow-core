package pl.net.bluesoft.rnd.processtool.plugins;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.cache.CacheProvider;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDictionaryDAO;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryLoader;
import pl.net.bluesoft.rnd.processtool.dict.xml.ProcessDictionaries;
import pl.net.bluesoft.rnd.processtool.event.ProcessToolEventBusManager;
import pl.net.bluesoft.rnd.processtool.hibernate.lock.ILockFacade;
import pl.net.bluesoft.rnd.processtool.hibernate.lock.OperationLockFacade;
import pl.net.bluesoft.rnd.processtool.hibernate.lock.OperationOptions;
import pl.net.bluesoft.rnd.processtool.hibernate.lock.OperationWithLock;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryPermission;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.util.eventbus.EventBusManager;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.PATTERN_MATCH_ALL;
import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.PRIVILEGE_EDIT;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/** 
 * @author tlipski@bluesoft.net.pl
 * @author amichalak@bluesoft.net.pl
 * @author kkolodziej@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
@Component
@Scope(value = "singleton")
public class ProcessToolRegistryImpl implements ProcessToolRegistry {
	private static final Logger logger = Logger.getLogger(ProcessToolRegistryImpl.class.getName());

	@Autowired
	private GuiRegistry guiRegistry;

	@Autowired
	private BundleRegistry bundleRegistry;

	@Autowired
	private DataRegistry dataRegistry;

    @Autowired
    private ProcessToolSessionFactory processToolSessionFactory;

    @Autowired
    private IUserSource userSource;

	private ExecutorService executorService = Executors.newCachedThreadPool();
	private EventBusManager eventBusManager = new ProcessToolEventBusManager(this, executorService);

	private final Map<String, CacheProvider> cacheProviders = new HashMap<String, CacheProvider>();



	public ProcessToolRegistryImpl() {
		Util.setInstance(this);
	}

	@PostConstruct
	public void completeInit()
    {
        logger.log(Level.INFO, "Pre construct ProcessToolRegistry");
		dataRegistry.commitModelExtensions();

        ProcessToolRegistry.Util.setInstance(this);
        ProcessToolRegistry.Util.setAwfClassLoader(Thread.currentThread().getContextClassLoader());
	}

	@Override
	public EventBusManager getEventBusManager() {
		return eventBusManager;
	}

	@Override
	public ExecutorService getExecutorService() {
		return executorService;
	}

	@Override
	public <T> T lookupService(String name) {
		return (T)bundleRegistry.lookupService(name);
	}

	@Override
	public <T> T getRegisteredService(Class<T> serviceClass) {
		return (T)bundleRegistry.getRegisteredService(serviceClass);
	}

	@Override
	public IUserSource getUserSource() {
		return userSource;
	}

	@Override
	public void setUserSource(IUserSource userSource) {
		this.userSource = userSource;
	}

	@Override
	public <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback) {
		assertContextFactoryExists();
		return dataRegistry.getProcessToolContextFactory().withProcessToolContext(callback);
	}

    @Override
    public <T> T withOperationLock(final OperationWithLock<T> operation,
                                   final String lockName,
                                   final OperationLockMode mode,
                                   final  Integer expireAfterMinutes)
    {

        ILockFacade lockFacade = new OperationLockFacade();
        OperationOptions options = new OperationOptions(lockName, mode, expireAfterMinutes);
        return lockFacade.performWithLock(operation, options);

    }

	@Override
	public <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback, ProcessToolContextFactory.ExecutionType type) {
		assertContextFactoryExists();
		return dataRegistry.getProcessToolContextFactory().withProcessToolContext(callback, type);
	}
	
	@Override
	public <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback) {
		assertContextFactoryExists();
		return dataRegistry.getProcessToolContextFactory().withExistingOrNewContext(callback);
	}

	private void assertContextFactoryExists() {
		if (dataRegistry.getProcessToolContextFactory() == null) {
			throw new RuntimeException("No process tool context factory implementation registered");
		}
	}

	@Override
	public GuiRegistry getGuiRegistry() {
		return guiRegistry;
	}

	@Override
	public BundleRegistry getBundleRegistry() {
		return bundleRegistry;
	}

	@Override
	public DataRegistry getDataRegistry() {
		return dataRegistry;
	}

	@Override
	public ProcessToolSessionFactory getProcessToolSessionFactory() {
		return processToolSessionFactory;
	}

	@Override
	public UserData getAutoUser() {
		return userSource.getUserByLogin(ProcessToolBpmConstants.SYSTEM_USER.getLogin());
	}

	@Override
	public void registerGlobalDictionaries(InputStream is) {
		if (is != null) {
			ProcessDictionaries dictionaries = (ProcessDictionaries) DictionaryLoader.getInstance().unmarshall(is);
			Session session = dataRegistry.getSessionFactory().openSession();

			try {
				Transaction tx = session.beginTransaction();

				Collection<ProcessDBDictionary> processDBDictionaries = saveDictionaryInternal(dictionaries);

				ProcessDictionaryDAO dao = dataRegistry.getProcessDictionaryDAO(session);
				dao.processDictionaries(processDBDictionaries, dictionaries.isOverwrite());

				tx.commit();
				logger.warning("Registered global dictionaries");
			}
			finally {
				session.close();
			}
		}
	}

	private Collection<ProcessDBDictionary> saveDictionaryInternal(ProcessDictionaries dictionaries) {
		List<ProcessDBDictionary> processDBDictionaries = DictionaryLoader.getDictionariesFromXML(dictionaries);
		for (ProcessDBDictionary dict : processDBDictionaries) {
			for (ProcessDBDictionaryPermission perm : dict.getPermissions()) {
				if (!hasText(perm.getRoleName())) {
					perm.setRoleName(PATTERN_MATCH_ALL);
				}
				if (!hasText(perm.getPrivilegeName())) {
					perm.setPrivilegeName(PRIVILEGE_EDIT);
				}
			}
		}
		DictionaryLoader.validateDictionaries(processDBDictionaries);

		return processDBDictionaries;
	}



	@Override
	public void registerCacheProvider(String cacheId, CacheProvider cacheProvider) {
		cacheProviders.put(cacheId, cacheProvider);
	}

	@Override
	public void unregisterCacheProvider(String cacheId) {
		cacheProviders.remove(cacheId);
	}

	@Override
	public Map<String, CacheProvider> getCacheProviders() {
		return Collections.unmodifiableMap(cacheProviders);
	}
}

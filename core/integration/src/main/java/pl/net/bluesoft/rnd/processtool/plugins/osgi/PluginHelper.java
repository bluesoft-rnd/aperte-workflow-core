package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.aperteworkflow.search.ProcessInstanceSearchData;
import org.aperteworkflow.search.SearchProvider;
import org.osgi.framework.BundleException;
import pl.net.bluesoft.rnd.processtool.plugins.PluginManager;
import pl.net.bluesoft.rnd.processtool.plugins.PluginMetadata;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.newfelix.NewFelixBundleService;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.oldfelix.OldFelixBundleService;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.aperteworkflow.search.ProcessInstanceSearchAttribute;
import org.aperteworkflow.search.ProcessInstanceSearchData;
import org.aperteworkflow.search.SearchProvider;
import org.aperteworkflow.ui.view.IViewRegistry;
import org.aperteworkflow.ui.view.impl.DefaultViewRegistryImpl;
import org.aperteworkflow.util.liferay.LiferayBridge;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import pl.net.bluesoft.rnd.processtool.plugins.PluginManagementException;
import pl.net.bluesoft.rnd.processtool.plugins.PluginManager;
import pl.net.bluesoft.rnd.processtool.plugins.PluginMetadata;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolServiceBridge;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.util.ConfigurationResult;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;
import pl.net.bluesoft.util.lang.cquery.func.F;

import com.thoughtworks.xstream.XStream;

public class PluginHelper implements PluginManager, SearchProvider {
	public enum State {
        STOPPED, INITIALIZING, ACTIVE
    }

    private State state = State.STOPPED;

    private ScheduledExecutorService executor;

	private ErrorMonitor errorMonitor = new ErrorMonitor();
    private static Logger LOGGER = Logger.getLogger(PluginHelper.class.getName());

	private FelixBundleService felixService;

	private LuceneSearchService searchService = new LuceneSearchService(LOGGER);

	public synchronized void initialize(String pluginsDir,
                                        String felixDir,
                                        String luceneDir,
                                        ProcessToolRegistryImpl registry) throws BundleException {
		felixService = createFelixBundleService(pluginsDir);
		felixService.setPluginsDir(pluginsDir.replace('/', File.separatorChar));
		searchService.setLuceneDir(luceneDir.replace('/', File.separatorChar));

		registry.setPluginManager(this);
        registry.setSearchProvider(this);
        state = State.INITIALIZING;
        LOGGER.fine("initialize.start!");
        initializeFelix(felixDir, registry);
        LOGGER.fine("initializeCheckerThread!");
        initCheckerThread();
        LOGGER.fine("initializeSearchService!");
        initializeSearchService();
        LOGGER.fine("initialize.end!");
        state = State.ACTIVE;
    }

	private FelixBundleService createFelixBundleService(String pluginsDir) {
		if (new File(pluginsDir + File.separator + "use-new-felix").exists()) {
			return new NewFelixBundleService(errorMonitor, LOGGER);
		}
		return new OldFelixBundleService(errorMonitor, LOGGER);
	}

	private void initializeFelix(String felixDir, ProcessToolRegistryImpl registry) throws BundleException {
        felixService.initialize(felixDir, registry);
    }

    /**
     * Sets Felix storage properties
     *
     * @param storageDir
     * @param configMap
     */
    private void putStorageConfig(String storageDir, Map<String, Object> configMap) {
        configMap.put(FelixConstants.FRAMEWORK_STORAGE, storageDir);
        configMap.put(FelixConstants.FRAMEWORK_STORAGE_CLEAN, FelixConstants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
    }

    /**
     * Sets basic Felix properties
     *
     * @param configMap
     */
    private void putBasicConfig(String pluginsDir, Map<String, Object> configMap) {
        configMap.put(FelixConstants.LOG_LEVEL_PROP, "4");
        configMap.put(FelixConstants.LOG_LOGGER_PROP, new Logger() {
            @Override
            protected void doLog(Bundle bundle, ServiceReference sr, int level,
                                 String msg, Throwable throwable) {
                if (throwable != null) {
                    LOGGER.log(Level.SEVERE, "Felix: " + msg + ", Throwable: " + throwable.getMessage(), throwable);
                } else {
                    LOGGER.log(Level.FINE, "Felix: " + msg);
                }
            }
        });

        configMap.put(FelixConstants.SERVICE_URLHANDLERS_PROP, true);
        configMap.put(FelixConstants.FRAMEWORK_BUNDLE_PARENT, FelixConstants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
        configMap.put("felix.auto.deploy.action", "install,update,start");
        configMap.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, getSystemPackages(pluginsDir));
    }

    /**
     * Sets Felix activator properties
     *
     * @param registry
     * @param configMap
     */
    private void putActivatorConfig(final ProcessToolRegistryImpl registry, Map<String, Object> configMap) {
        ArrayList<BundleActivator> activators = new ArrayList<BundleActivator>();
        activators.add(new BundleActivator() {
            private ProcessToolServiceBridge serviceBridge;

            @Override
            public void start(BundleContext context) throws Exception {
                if (registry != null) {
                    registry.setOsgiBundleContext(context);
                    serviceBridge = new FelixServiceBridge(felix);
                    registry.addServiceLoader(serviceBridge);
                    context.registerService(ProcessToolRegistry.class.getName(), registry, new Hashtable());
                    context.registerService(IViewRegistry.class.getName(), new DefaultViewRegistryImpl(),
                            new Hashtable<String, Object>());
                }
            }

            @Override
            public void stop(BundleContext context) throws Exception {
                registry.removeServiceLoader(serviceBridge);
            }
        });

        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators);
    }

    private void initCheckerThread() {
        LOGGER.info("Starting OSGi checker thread");
		shutdownExecutor();
        scheduleBundleInstallAfter(1);
        LOGGER.info("Started OSGi checker thread");
    }

	private void shutdownExecutor() {
		if (executor != null && !executor.isShutdown()) {
			executor.shutdown();
			executor = null;
		}
	}

	private Runnable createBundleInstallTask() {
        return new Runnable() {
            @Override
            public void run() {
                scheduledBundleInstall();
            }
        };
    }

	private synchronized void scheduledBundleInstall() {
		try {
			felixService.scheduledBundleInstall();
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Bundle install interrupted", e);
			forwardErrorInfoToMonitor("Bundle install interrupted", e);
		}
		finally {
			scheduleBundleInstallAfter(5);
		}
	}

	private void forwardErrorInfoToMonitor(String path, Exception e) {
		errorMonitor.forwardErrorInfoToMonitor(path, e);
	}

	private void scheduleBundleInstallAfter(long seconds) {
		if (executor == null) {
			executor = Executors.newSingleThreadScheduledExecutor();
		}
		executor.schedule(createBundleInstallTask(), seconds, TimeUnit.SECONDS);
	}

	private void initializeSearchService() {
		searchService.initialize();
	}

	public synchronized void stopPluginSystem() throws BundleException {
        state = State.STOPPED;
        shutdownExecutor();
        felixService.stopFelix();
    }

    public State getState() {
        return state;
    }

    @Override
    public void registerPlugin(String filename, InputStream is) {
        felixService.registerPlugin(filename, is);
    }

    @Override
    public Collection<PluginMetadata> getRegisteredPlugins() {
        return felixService.getRegisteredPlugins();
    }

    @Override
    public void enablePlugin(PluginMetadata pluginMetadata) {
        felixService.enablePlugin(pluginMetadata);
    }

    @Override
    public void disablePlugin(PluginMetadata pluginMetadata) {
        felixService.disablePlugin(pluginMetadata);
    }

    @Override
    public void uninstallPlugin(PluginMetadata pluginMetadata) {
        felixService.uninstallPlugin(pluginMetadata);
    }

    @Override
    public void updateIndex(ProcessInstanceSearchData processInstanceSearchData) {
        searchService.updateIndex(processInstanceSearchData);
    }

	@Override
	public List<Long> searchProcesses(String query, Integer offset,
			Integer limit, boolean onlyRunning, String[] userRoles,
			String assignee, String... queues) {
		return searchService.searchProcesses(query, offset, limit, onlyRunning, userRoles, assignee, queues);
	}

    public List<Document> search(String query, int offset, int limit, Query... addQueries) {
        return searchService.search(query, offset, limit, addQueries);
    }

    public String getMonitorInfo() {
        return errorMonitor.getMonitorInfo();
    }
}
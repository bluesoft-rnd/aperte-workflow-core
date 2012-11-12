package pl.net.bluesoft.rnd.processtool.plugins.osgi;

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
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.aperteworkflow.search.ProcessInstanceSearchAttribute;
import org.aperteworkflow.search.ProcessInstanceSearchData;
import org.aperteworkflow.search.SearchProvider;
import org.aperteworkflow.ui.view.ViewRegistry;
import org.aperteworkflow.ui.view.impl.DefaultViewRegistryImpl;
import org.aperteworkflow.util.liferay.LiferayBridge;
import org.osgi.framework.*;

import com.thoughtworks.xstream.XStream;

import pl.net.bluesoft.rnd.processtool.plugins.*;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.net.bluesoft.rnd.processtool.plugins.osgi.OSGiBundleHelper.*;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

public class PluginHelper implements PluginManager, SearchProvider {

    public static final String AWF__ID = "__AWF__ID";
    public static final String AWF__TYPE = "__AWF__TYPE";
    public static final String AWF__ROLE = "__AWF__ROLE";

    public static final String PROCESS_INSTANCE = "PROCESS_INSTANCE";
    private String luceneDir;
    private Directory index;
    private IndexSearcher indexSearcher;
    private IndexReader indexReader;
    private static final String AWF_RUNNING = "__AWF__running";
    private static final String AWF__ASSIGNEE = "__AWF__assignee";
    private static final String AWF__QUEUE = "__AWF__queue";
    
    private static final String SEPARATOR = "/";

    private StringBuffer monitorInfo = new StringBuffer();

    private static class BundleInfo {
        private Long lastModified;
        private Long installDuration;
        private Set<String> exportedPackages = new HashSet<String>();
        private Set<String> importedPackages = new HashSet<String>();

        public Long getLastModified() {
            return lastModified;
        }

        public void setLastModified(Long lastModified) {
            this.lastModified = lastModified;
        }

        public Long getInstallDuration() {
            return installDuration;
        }

        public void setInstallDuration(Long installDuration) {
            this.installDuration = installDuration;
        }

        public Set<String> getExportedPackages() {
            return exportedPackages;
        }

        public void setExportedPackages(Set<String> exportedPackages) {
            this.exportedPackages = exportedPackages;
        }

        public Set<String> getImportedPackages() {
            return importedPackages;
        }

        public void setImportedPackages(Set<String> importedPackages) {
            this.importedPackages = importedPackages;
        }
    }

    private static class ExportParser {
        private final String s;
        private int pos;

        public ExportParser(String s) {
            this.s = s;
            this.pos = 0;
        }

        private void eatWhiteSpaces() {
            while (!eot() && Character.isWhitespace(curChar())) {
                ++pos;
            }
        }

        private boolean eot() {
            return pos >= s.length();
        }

        private char curChar() {
            return s.charAt(pos);
        }

        public Map<String, Map<String, String>> parse() {
            Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
            eatWhiteSpaces();
            while (!eot()) {
                String pack = packageName();
                result.put(pack, new HashMap<String, String>());
                if (eot()) {
                    break;
                }
                if (curChar() == ',') {
                    ++pos;
                }
                else if (curChar() == ';') {
                    ++pos;
                    result.put(pack, additionalArgs());
                }
            }
            return result;
        }

        private Map<String, String> additionalArgs() {
            Map<String, String> result = new HashMap<String, String>();

            while (!eot()) {
                eatWhiteSpaces();
                String arg = additionalArg().trim();
                Pattern p1 = Pattern.compile("^(.*?)\\:?=(.*)$");
                Matcher m = p1.matcher(arg);
                if (m.find()) {
                    String r = m.group(2).toString().trim();
                    if (r.startsWith("\"") && r.endsWith("\"")) {
                        r = r.substring(1, r.length() - 1).trim();
                    }
                    result.put(m.group(1).toString().trim(), r);
                }
                if (eot()) {
                    break;
                }
                else if (curChar() == ',') {
                    ++pos;
                    break;
                }
                else if (curChar() == ';') {
                    ++pos;
                }
            }
            return result;
        }

        private String additionalArg() {
            int start = pos;
            while (!eot()) {
                char c = curChar();
                if (c == ',' || c == ';') {
                    break;
                }
                ++pos;
                if (c == '\"') {
                    while (!eot() && curChar() != '\"') {
                        ++pos;
                    }
                    if (!eot() && curChar() == '\"') {
                        ++pos;
                    }
                }
            }
            return s.substring(start, pos);
        }

        private String packageName() {
            int start = pos;
            while (!eot() && curChar() != ',' && curChar() != ';') {
                ++pos;
            }
            return s.substring(start, pos);
        }
    }

    public enum State {
        STOPPED, INITIALIZING, ACTIVE
    }

    private State state = State.STOPPED;
    private Felix felix;

    private ScheduledExecutorService executor;
    private ProcessToolRegistry registry;
    private Map<String, BundleInfo> bundleInfos;
    private String pluginsDir;

    private static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PluginHelper.class.getName());

    synchronized private void processBundleExtensions(final Bundle bundle, int eventType) throws ClassNotFoundException {

        if (registry.getProcessToolContextFactory() == null) {
            LOGGER.severe("No default process tool context registered! - skipping process tool context-based processing of this OSGI bundle");
            return;
        }

        OSGiBundleHelper bundleHelper = new OSGiBundleHelper(bundle);

        if (bundleHelper.hasHeaderValues(MODEL_ENHANCEMENT)) {
            handleModelEnhancement(eventType, bundleHelper, registry);
        }

        if (bundleHelper.hasHeaderValues(WIDGET_ENHANCEMENT)) {
            handleWidgetEnhancement(eventType, bundleHelper, registry);
        }

        if (bundleHelper.hasHeaderValues(BUTTON_ENHANCEMENT)) {
            handleButtonEnhancement(eventType, bundleHelper, registry);
        }

        if (bundleHelper.hasHeaderValues(STEP_ENHANCEMENT)) {
            handleStepEnhancement(eventType, bundleHelper, registry);
        }

        if (bundleHelper.hasHeaderValues(I18N_PROPERTY)) {
            handleMessageSources(eventType, bundleHelper, registry);
        }

        if (bundleHelper.hasHeaderValues(PROCESS_DEPLOYMENT)) {
            handleProcessRoles(eventType, bundleHelper, registry);
            handleProcessDeployment(eventType, bundleHelper, registry);
        }

        if (bundleHelper.hasHeaderValues(GLOBAL_DICTIONARY)) {
            handleGlobalDictionaries(eventType, bundleHelper, registry);
        }

        if (bundleHelper.hasHeaderValues(RESOURCES)) {
            handleBundleResources(eventType, bundleHelper, registry);
        }

        if (bundleHelper.hasHeaderValues(TASK_ITEM_ENHANCEMENT)) {
           handleTaskItemEnhancement(eventType, bundleHelper, registry);
       }
    }

    private void handleMessageSources(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) {
        final Bundle bundle = bundleHelper.getBundle();
        String[] properties = bundleHelper.getHeaderValues(I18N_PROPERTY);
        for (final String propertyFileName : properties) {
            String providerId = bundle.getBundleId() + File.separator + propertyFileName;
            if (eventType == Bundle.ACTIVE) {
                toolRegistry.registerI18NProvider(new PropertiesBasedI18NProvider(new PropertyLoader() {
                    @Override
                    public InputStream loadProperty(String path) throws IOException {
                        return getBundleResourceStream(bundle, path);
                    }
                }, propertyFileName), providerId);
            }
            else {
                toolRegistry.unregisterI18NProvider(providerId);
            }
        }
    }

    private void handleStepEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(STEP_ENHANCEMENT);
        for (String cls : classes) {
            if (eventType == Bundle.ACTIVE) {
                toolRegistry.registerStep((Class<? extends ProcessToolProcessStep>) bundle.loadClass(cls));
            }
            else {
                toolRegistry.unregisterStep((Class<? extends ProcessToolProcessStep>) bundle.loadClass(cls));
            }
        }
    }

    private void handleButtonEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(BUTTON_ENHANCEMENT);
        for (String cls : classes) {
            if (eventType == Bundle.ACTIVE) {
                toolRegistry.registerButton(bundle.loadClass(cls));
            }
            else {
                toolRegistry.unregisterButton(bundle.loadClass(cls));
            }
        }
    }

    private void handleWidgetEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(WIDGET_ENHANCEMENT);
        for (String cls : classes) {
            if (eventType == Bundle.ACTIVE) {
                toolRegistry.registerWidget(bundle.loadClass(cls));
            }
            else {
                toolRegistry.unregisterWidget(bundle.loadClass(cls));
            }
        }
    }

    private void handleModelEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
        Bundle bundle = bundleHelper.getBundle();
        String[] classes = bundleHelper.getHeaderValues(MODEL_ENHANCEMENT);
        Collection<Class> classSet = new HashSet<Class>();
        for (String cls : classes) {
            classSet.add(bundle.loadClass(cls));
        }
        if (!classSet.isEmpty()) {
            Class<?>[] extensions = classSet.toArray(new Class<?>[classSet.size()]);
            boolean needUpdate = eventType == Bundle.ACTIVE
                    ? toolRegistry.registerModelExtension(extensions)
                    : toolRegistry.unregisterModelExtension(extensions);
            if (needUpdate) {
                LOGGER.fine("Rebuilding Hibernate session factory...");
                try {
                    toolRegistry.commitModelExtensions();
                }
                catch (Exception e) {
                    LOGGER.severe("Encountered problem while updating Hibernate mappings");
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    toolRegistry.unregisterModelExtension(extensions);
                }
            }
            else {
                LOGGER.warning("Skipping Hibernate session factory rebuild. Classes already processed.");
            }
        }
    }

    private void handleProcessDeployment(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) {
        final Bundle bundle = bundleHelper.getBundle();
        String[] properties = bundleHelper.getHeaderValues(PROCESS_DEPLOYMENT);
        for (String processPackage : properties) {
            String providerId = bundle.getBundleId() + SEPARATOR + processPackage.replace(".", SEPARATOR) + "/messages";
            if (eventType == Bundle.ACTIVE) {
                try {
                    String basePath = SEPARATOR + processPackage.replace(".", SEPARATOR) + SEPARATOR;
                    toolRegistry.deployOrUpdateProcessDefinition(
                            bundleHelper.getBundleResourceStream(basePath + "processdefinition." +
                                    toolRegistry.getBpmDefinitionLanguage() + ".xml"),
                            bundleHelper.getBundleResourceStream(basePath + "processtool-config.xml"),
                            bundleHelper.getBundleResourceStream(basePath + "queues-config.xml"),
                            bundleHelper.getBundleResourceStream(basePath + "processdefinition.png"),
                            bundleHelper.getBundleResourceStream(basePath + "processdefinition-logo.png"));

                    toolRegistry.registerI18NProvider(new PropertiesBasedI18NProvider(new PropertyLoader() {
                        @Override
                        public InputStream loadProperty(String path) throws IOException {
                            return getBundleResourceStream(bundle, path);
                        }
                    }, "/" + processPackage.replace(".", SEPARATOR) + "/messages"), providerId);

                    toolRegistry.registerProcessDictionaries(bundleHelper.getBundleResourceStream(basePath + "process-dictionaries.xml"));

                }
                catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
                }
            }
            else { // ignore
                toolRegistry.unregisterI18NProvider(providerId);
            }
        }
    }
    
	private void handleProcessRoles(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry registry) {
		if (eventType != Bundle.ACTIVE) {
			return;
		}
		
		final Bundle bundle = bundleHelper.getBundle();

		if (bundleHelper.hasHeaderValues(ROLE_FILES)) {
			String[] files = bundleHelper.getHeaderValues(ROLE_FILES);
			for (String file : files) {
				try {
					InputStream input = bundleHelper.getBundleResourceStream(file);
					Collection<ProcessRoleConfig> roles = getRoles(input);
					createRoles(roles,registry);
				}
				catch (Exception e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
				}
			}
		}

		if (bundleHelper.hasHeaderValues(PROCESS_DEPLOYMENT)) {
			String[] properties = bundleHelper.getHeaderValues(PROCESS_DEPLOYMENT);
			for (String processPackage : properties) {
				String basePath = SEPARATOR + processPackage.replace(".", SEPARATOR) + SEPARATOR;
				try {
					InputStream input = bundleHelper.getBundleResourceStream(basePath + "roles-config.xml");
					Collection<ProcessRoleConfig> roles = getRoles(input);
					createRoles(roles,registry);
				}
				catch (Exception e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
				}
			}
		}
	}

	private Collection<ProcessRoleConfig> getRoles(InputStream input) {
		if (input == null) {
			return null;
		}
		XStream xstream = new XStream();
		xstream.aliasPackage("config", ProcessRoleConfig.class.getPackage().getName());
		xstream.useAttributeFor(String.class);
		xstream.useAttributeFor(Boolean.class);
		xstream.useAttributeFor(Integer.class);
		return (Collection<ProcessRoleConfig>) xstream.fromXML(input);
	}

	private void createRoles(Collection<ProcessRoleConfig> roles,ProcessToolRegistry registry) {
		if (roles != null) {
			for (ProcessRoleConfig role : roles) {
				try 
				{
					boolean roleCreated = LiferayBridge.createRoleIfNotExists(role.getName(), role.getDescription());
					if (roleCreated) 
						LOGGER.log(Level.INFO, "Created role " + role.getName());

				} catch (RuntimeException e) {
					forwardErrorInfoToMonitor("adding role " + role.getName(), e);
					throw e;
				}
			}
		}
	}


    private void handleBundleResources(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) {
        Bundle bundle = bundleHelper.getBundle();
        String[] resources = bundleHelper.getHeaderValues(RESOURCES);
        for (String pack : resources) {
            if (eventType == Bundle.ACTIVE) 
            {
                String basePath = SEPARATOR + pack.replace(".", SEPARATOR);
                if (!basePath.endsWith(SEPARATOR)) {
                    basePath += SEPARATOR;
                }
                Enumeration<URL> urls = bundle.findEntries(basePath, null, true);
                while (urls.hasMoreElements()) {
                    String path = urls.nextElement().getPath();
                    toolRegistry.registerResource(bundle.getSymbolicName(), path);
                }
            }
            else {
                toolRegistry.removeRegisteredResources(bundle.getSymbolicName());
            }
        }
    }

    private void handleGlobalDictionaries(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) {
        String[] properties = bundleHelper.getHeaderValues(GLOBAL_DICTIONARY);
        if (eventType == Bundle.ACTIVE) {
            for (String pack : properties) {
                try {
                    String basePath = SEPARATOR + pack.replace(".", SEPARATOR) + SEPARATOR;
                    InputStream is = bundleHelper.getBundleResourceStream(basePath + "global-dictionaries.xml");
                    if (is != null) {
                        toolRegistry.registerGlobalDictionaries(is);
                    }
                    else {
                        LOGGER.log(Level.SEVERE, "No global dictionary stream found in package: " + pack);
                    }
                }
                catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    forwardErrorInfoToMonitor(bundleHelper.getBundleMetadata().getDescription() + " global-dictionary", e);
                }
            }
        }
    }

    private void handleTaskItemEnhancement(int eventType, OSGiBundleHelper bundleHelper, ProcessToolRegistry toolRegistry) throws ClassNotFoundException {
   		Bundle bundle = bundleHelper.getBundle();
   		String[] classes = bundleHelper.getHeaderValues(TASK_ITEM_ENHANCEMENT);
   		for (String cls : classes) {
   			if (eventType == Bundle.ACTIVE) {
   				toolRegistry.registerTaskItemProvider(bundle.loadClass(cls));
   			}
   			else {
   				toolRegistry.unregisterTaskItemProvider(bundle.loadClass(cls));
   			}
   		}
   	}


    public synchronized void initialize(String pluginsDir, 
                                        String storageDir, 
                                        String luceneDir,
                                        ProcessToolRegistryImpl registry)
            throws BundleException {
        this.pluginsDir =  pluginsDir.replace('/', File.separatorChar);
        this.luceneDir = luceneDir.replace('/', File.separatorChar);
        this.registry = registry;

        registry.setPluginManager(this);
        registry.setSearchProvider(this);
        state = State.INITIALIZING;
        LOGGER.fine("initialize.start!");
        initializeFelix(storageDir, registry);
        LOGGER.fine("initializeCheckerThread!");
        initCheckerThread();
        LOGGER.fine("initializeSearchService!");
        initializeSearchService();
        LOGGER.fine("initialize.end!");
        state = State.ACTIVE;
    }

    private void initializeFelix(String storageDir, final ProcessToolRegistryImpl registry) throws BundleException {
        if (felix != null) {
            felix.stop();
            felix = null;
        }

        this.registry = registry;

        Map<String, Object> configMap = new HashMap<String, Object>();
        putBasicConfig(pluginsDir, configMap);
        putStorageConfig(storageDir, configMap);
        putActivatorConfig(registry, configMap);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Felix.class.getClassLoader());
            felix = new Felix(configMap);
            felix.init();
            felix.start();
        }
        finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
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
                    context.registerService(ViewRegistry.class.getName(), new DefaultViewRegistryImpl(),
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
        bundleInfos = new HashMap<String, BundleInfo>();
        LOGGER.info("Starting OSGi checker thread");
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(createBundleInstallTask(pluginsDir), 1, TimeUnit.SECONDS);
        LOGGER.info("Started OSGi checker thread");
    }

    private Runnable createBundleInstallTask(final String pluginsDir) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    scheduledBundleInstall(pluginsDir);
                }
                catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Bundle install interrupted", e);
                    forwardErrorInfoToMonitor("Bundle install interrupted", e);
                }
                finally {
                    executor.schedule(createBundleInstallTask(pluginsDir), 5, TimeUnit.SECONDS);
                }
            }
        };
    }

    private synchronized void scheduledBundleInstall(String pluginsDir) {
        if (felix == null) {
            LOGGER.warning("Felix not initialized yet");
            return;
        }

        Set<String> jarFilePathsInPluginsDir = new HashSet<String>();
        List<String> installableBundlePaths = getInstallableBundlePaths(pluginsDir, jarFilePathsInPluginsDir);
        removeBundles(jarFilePathsInPluginsDir, installableBundlePaths);
        if (installableBundlePaths == null || installableBundlePaths.isEmpty()) {
            return;
        }

        List<String> toInstall = new ArrayList<String>(installableBundlePaths);

        long start = new Date().getTime();
        installBundles(installableBundlePaths, pluginsDir, false);
        long end = new Date().getTime() - start;

        LOGGER.info(from(toInstall).select(new F<String, String>() {
            @Override
            public String invoke(String path) {
                return String.format("Bundle %s installed in %s seconds", path, nvl(getBundleInfo(path).getInstallDuration(), 0L)/1000.0);
            }
        }).toString("\n"));

        LOGGER.info(String.format("Bundles installed in %s seconds", end / 1000.0));

        if (!installableBundlePaths.isEmpty()) {
            LOGGER.warning("UNABLE TO INSTALL BUNDLES: " + installableBundlePaths.toString());
        }
    }

    private void installBundles(List<String> installableBundlePaths, String pluginsDir, boolean depedencyWiseInstall) {
        if (!depedencyWiseInstall) {
            installBundles(installableBundlePaths);
        } else {
            dependencyWiseInstallBundles(installableBundlePaths, pluginsDir);
        }
    }

    private List<String> getInstallableBundlePaths(String pluginsDir, Set<String> jarFilePathsInPluginsDir) {
        File f = new File(pluginsDir);
        if (!f.exists()) {
            LOGGER.warning("Plugins dir not found: " + pluginsDir + " attempting to create...");
            if (!f.mkdir()) {
                LOGGER.severe("Failed to create plugins directory: " + pluginsDir + ", please reconfigure!!!");
                return null;
            } else {
                LOGGER.info("Created plugins directory: " + pluginsDir);
            }
        }
        String[] list = f.list();
        Arrays.sort(list);
        List<String> installableBundlePaths = new ArrayList<String>();
        for (String filename : list) {
            File subFile = new File(f.getAbsolutePath() + File.separator + filename);
            String path = subFile.getAbsolutePath();
            if (!subFile.isDirectory() && path.matches(".*jar$")) {
                Long lastModified = getBundleInfo(path).getLastModified();
                if (lastModified == null || lastModified < subFile.lastModified()) {
                    installableBundlePaths.add(path);
                    getBundleInfo(path).setLastModified(subFile.lastModified());
                }
                jarFilePathsInPluginsDir.add(path);
            }
        }
        return installableBundlePaths;
    }

    private BundleInfo getBundleInfo(String filename) {
        BundleInfo info = bundleInfos.get(filename);
        if (info == null) {
            bundleInfos.put(filename, info = new BundleInfo());
        }
        return info;
    }

    private void removeBundles(Set<String> jarFilePathsInPluginsDir, List<String> installableBundlePaths) {
        Set<String> removablePaths = new HashSet<String>(bundleInfos.keySet());
        removablePaths.removeAll(jarFilePathsInPluginsDir);
        if (!removablePaths.isEmpty()) {
            for (String path : removablePaths) {
                bundleInfos.remove(path);
            }
            removablePaths.addAll(installableBundlePaths);
            Set<Bundle> removedBundles = uninstallBundles(removablePaths);
            for (Bundle bundle : removedBundles) {
                try 
                {
                    processBundleExtensions(bundle, Bundle.STOPPING);
                }
                catch (ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, "Exception processing bundle", e);
                    forwardErrorInfoToMonitor(bundle.getSymbolicName(), e);
                }
                /* Zabezpiecznie na wypadku bledu w kodzie, aby nie wywalal innych 
                 * pakietow przy starcie
                 */
                catch(Exception ex)
                {
                    LOGGER.log(Level.SEVERE, "Exception processing bundle", ex);
                    forwardErrorInfoToMonitor(bundle.getSymbolicName(), ex);
                }
            }
        }
    }

    private Set<Bundle> uninstallBundles(Set<String> removablePaths) {
        Set<Bundle> removedBundles = new HashSet<Bundle>();
        Bundle[] installedBundles = felix.getBundleContext().getBundles();
        for (Bundle bundle : installedBundles) {
            String path = bundle.getLocation().replaceFirst("file://", "");
            if (removablePaths.contains(path) && (bundle.getState() &
                    (Bundle.INSTALLED | Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0) {
                try {
                    LOGGER.info("STOPPING: " + path);
                    bundle.stop();
                    LOGGER.info("STOPPED: " + path);
                    removedBundles.add(bundle);
                }
                catch (Exception e) {
                    LOGGER.warning("UNABLE TO UNINSTALL BUNDLE: " + path);
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
        return removedBundles;
    }

    private void installBundles(List<String> installableBundlePaths) {
        boolean installed = true;
        while (!installableBundlePaths.isEmpty() && installed) {
            installed = false;
            for (Iterator<String> it = installableBundlePaths.iterator(); it.hasNext(); ) {
                Bundle bundle = installBundle(it.next());
                if (bundle != null) {
                    try {
                        processBundleExtensions(bundle, Bundle.ACTIVE);
                        
                        bundle.start();
                        LOGGER.info("STARTED: " + it);
                        
                        it.remove();
                        installed = true;
                    }
                    catch (ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE, "Exception processing bundle", e);
                    }
                    /* Zabezpiecznie na wypadku bledu w kodzie, aby nie wywalal innych 
                     * pakietow przy starcie
                     */
                    catch(Exception ex)
                    {
                        LOGGER.log(Level.SEVERE, "Exception processing bundle", ex);
                        forwardErrorInfoToMonitor(bundle.getSymbolicName(), ex);
                    }
                }
            }
        }
    }

    private synchronized Bundle installBundle(String path) {
        Bundle bundle;
        long start = new Date().getTime();
        try {
            LOGGER.info("INSTALLING: " + path);
            bundle = felix.getBundleContext().installBundle("file://" + path.replace('\\','/'), new FileInputStream(path));
            bundle.update(new FileInputStream(path));
            LOGGER.info("INSTALLED: " + path);
        }
        catch (Throwable e) {
            LOGGER.warning("BLOCKING: " + path);
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            bundle = null;
        }
        getBundleInfo(path).setInstallDuration(new Date().getTime() - start);
        return bundle;
    }

    private void dependencyWiseInstallBundles(List<String> installableBundlePaths, String pluginsDir) {
        for (String installBundlePath : installableBundlePaths) {
			JarFile jar = null;
            try {
                jar = new JarFile(installBundlePath);
                updatePackageInfo(getBundleInfo(installBundlePath), jar.getManifest());
            }
            catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
			finally {
				try {
					if (jar != null) {
						jar.close();
					}
				}
				catch (IOException e) {
				}
			}
        }

        Map<String, Set<String>> deps = getDependencyMap(pluginsDir);
        List<String> failedDeps = new ArrayList<String>();
        List<String> orderedDeps = topoSort(deps, failedDeps);

        for (String failedDep : failedDeps) {
            LOGGER.log(Level.SEVERE, String.format("Dependency analysis: Bundle %s has either missing dependency or introduces dependency cycle", failedDep));
        }

        for (String dep : orderedDeps) {
            if (installableBundlePaths.contains(dep)) {
                Bundle bundle = installBundle(dep);
                if (bundle != null) {
                    try {
                        processBundleExtensions(bundle, Bundle.ACTIVE);
                        
                        bundle.start();
                        LOGGER.info("STARTED: " + dep);
                        
                        installableBundlePaths.remove(dep);
                    }
                    catch (ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE, "Exception processing bundle", e);
                    }
                    /* Zabezpiecznie na wypadek bledu w kodzie, aby nie wywalal innych 
                     * pakietow przy starcie
                     */
                    catch(Exception ex)
                    {
                        LOGGER.log(Level.SEVERE, "Exception processing bundle", ex);
                        forwardErrorInfoToMonitor(bundle.getSymbolicName(), ex);
                    }
                }
            }
        }
    }

    private void updatePackageInfo(BundleInfo info, Manifest manifest) {
        String importPackageAttr = manifest.getMainAttributes().getValue("Import-Package");

        if (importPackageAttr != null) {
            Map<String, Map<String, String>> m = new ExportParser(importPackageAttr).parse();
            info.getImportedPackages().addAll(m.keySet());
            info.getImportedPackages().remove("*");
        }

        String exportPackageAttr = manifest.getMainAttributes().getValue("Export-Package");

        if (exportPackageAttr != null) {
            Map<String, Map<String, String>> m = new ExportParser(exportPackageAttr).parse();
            info.getExportedPackages().addAll(m.keySet());
            info.getImportedPackages().removeAll(info.getExportedPackages());
        }
    }

    private Map<String, Set<String>> getDependencyMap(String pluginsDir) {
        Map<String, Map<String, String>> sysExport = new ExportParser(getSystemPackages(pluginsDir)).parse();
        Set<String> systemPackages = sysExport.keySet();
        Map<String, Set<String>> dependencyMap = new HashMap<String, Set<String>>();
        Set<String> errors = new HashSet<String>();

        for (String bundleName : bundleInfos.keySet()) {
            BundleInfo info = getBundleInfo(bundleName);
            Set<String> deps = new HashSet<String>();
            dependencyMap.put(bundleName, deps);
            boolean error = false;
            for (String importedPack : info.getImportedPackages()) {
                if (systemPackages.contains(importedPack)) {
                    continue;
                }
                int cnt = 0;
                for (String potentialDependency : bundleInfos.keySet()) {
                    BundleInfo depInfo =getBundleInfo(potentialDependency);
                    if (depInfo.getExportedPackages().contains(importedPack)) {
                        deps.add(potentialDependency);
                        ++cnt;
                    }
                }
                if (cnt == 0) {
                    error = true;
                    LOGGER.log(Level.SEVERE, String.format("Dependency analysis: bundle %s imports unknown package %s", bundleName, importedPack));
                }
                else if (cnt > 1) {
                    error = true;
                    LOGGER.log(Level.SEVERE, String.format("Dependency analysis: bundle %s imports package %s beging exported by more than 1 plugin. It may cause problems", bundleName, importedPack));
                }
                if (error) {
                    errors.add(bundleName);
                }
            }
        }
        return dependencyMap;
    }

    public String getSystemPackages(String basedir) {
        try {
            InputStream is;
            try {
                is = new FileInputStream(pluginsDir + File.separatorChar + "packages.export");
            }
            catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error occurred while reading " + pluginsDir + File.separatorChar + "packages.export", e);
                LOGGER.log(Level.SEVERE, "Falling back to bundled version");
                is = getClass().getResourceAsStream("/packages.export");
            }
            try {
                int c = 0;
                StringBuffer sb = new StringBuffer();
                while ((c = is.read()) >= 0) {
                    if (c == 10 || c == 13 || (char) c == ' ' || (char) c == '\t') {
                        continue;
                    }
                    sb.append((char) c);
                }
                return sb.toString().replaceAll("\\s*", "").replaceAll(",+", ",");
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error occurred while reading " + pluginsDir + File.separatorChar + "packages.export", e);

        }
        return "";
    }

    public synchronized void stopPluginSystem() throws BundleException {
        state = State.STOPPED;
        if (executor != null) {
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
            executor = null;
        }
        if (felix != null) {
            felix.stop();
            felix = null;
        }
    }

    public State getState() {
        return state;
    }

    private static List<String> topoSort(Map<String, Set<String>> deps, List<String> failedDeps) {
        Set<String> toLoad = new HashSet<String>(deps.keySet());
        List<String> result = new ArrayList<String>();
        while (!toLoad.isEmpty()) {
            boolean loaded = false;
            for (String dep : toLoad) {
                if (result.containsAll(deps.get(dep))) {
                    result.add(dep);
                    toLoad.remove(dep);
                    loaded = true;
                    break;
                }
            }
            if (!loaded) {
                break;
            }
        }
        failedDeps.clear();
        failedDeps.addAll(toLoad);
        return result;
    }

    @Override
    public void registerPlugin(String filename, InputStream is) {
        File fileRef = null;
        try {
            //create temp file
            File tempFile = fileRef = File.createTempFile(filename, Long.toString(System.nanoTime()));
            tempFile.setReadable(true, true);
            tempFile.setWritable(true, true);

            is.reset();
            FileOutputStream fos = new FileOutputStream(tempFile);
            try {
				byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf)) >= 0) {
					fos.write(buf, 0, len);
				}
				fos.flush();
			}
			finally {
            	fos.close();
			}

            File dest = new File(pluginsDir, filename);
            if (!tempFile.renameTo(dest)) {
                throw new IOException("Failed to rename " + tempFile.getAbsolutePath() + " to " + dest.getAbsolutePath() +
                        ", as File.renameTo returns only boolean, the reason is unknown.");
            } else {
                LOGGER.fine("Renamed " + tempFile.getAbsolutePath() + " to " + dest.getAbsolutePath());
            }
            fileRef = dest;
            LOGGER.info("Installing bundle: " + dest.getAbsolutePath());
            Bundle bundle = installBundle(dest.getAbsolutePath());
            
            bundle.start();
            LOGGER.info("STARTED: " + dest.getAbsolutePath());
            
            fileRef = null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to deploy plugin " + filename, e);
            throw new PluginManagementException(e);
        } finally {
            if (fileRef != null) {
                LOGGER.fine("trying to remove leftover file " + fileRef.getAbsolutePath());
                fileRef.delete();
            }
        }
    }

    @Override
    public Collection<PluginMetadata> getRegisteredPlugins() {
        List<ProcessToolServiceBridge> serviceLoaders = registry.getServiceLoaders();
        List<PluginMetadata> registeredPlugins = new ArrayList<PluginMetadata>();
        for (ProcessToolServiceBridge serviceBridge : serviceLoaders) {
            try {
                registeredPlugins.addAll(serviceBridge.getInstalledPlugins());
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Failed to get registered plugins");
                throw new PluginManagementException(e);
            }
        }
        return registeredPlugins;
    }

    @Override
    public void enablePlugin(PluginMetadata pluginMetadata) {
        try {
            felix.getBundleContext().getBundle(pluginMetadata.getId()).start();
            LOGGER.warning("Started bundle " + pluginMetadata.getName());
        } catch (BundleException e) {
            LOGGER.log(Level.SEVERE, "Failed to start plugin " + pluginMetadata.getName(), e);
            throw new PluginManagementException(e);
        }
    }

    @Override
    public void disablePlugin(PluginMetadata pluginMetadata) {
        try {
            felix.getBundleContext().getBundle(pluginMetadata.getId()).stop();
            LOGGER.warning("Stopped bundle " + pluginMetadata.getName());
        } catch (BundleException e) {
            LOGGER.log(Level.SEVERE, "Failed to stop plugin " + pluginMetadata.getName(), e);
            throw new PluginManagementException(e);
        }
    }

    @Override
    public void uninstallPlugin(PluginMetadata pluginMetadata) {
        try {
            String file = pluginMetadata.getBundleLocation();
            file = file.replaceAll("file://", "");
            File f = new File(file);
            felix.getBundleContext().getBundle(pluginMetadata.getId()).uninstall();
            if (!f.delete()) {
                throw new PluginManagementException("Failed to remove file: " + file);
            } else {
                LOGGER.warning("Uninstalled bundle " + pluginMetadata.getName() + ", removed file: " + file);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to uninstall plugin " + pluginMetadata.getName(), e);
            throw new PluginManagementException(e);
        }
    }


    private void initializeSearchService() {
        try {
            File path = new File(luceneDir);
            if (!path.exists()) {
                LOGGER.severe("Default lucene index directory: " + luceneDir + " not found, attempting to create...");
                if (!path.mkdir()) {
                    LOGGER.severe("Failed to create Default lucene index directory: " + luceneDir);
                } else {
                    LOGGER.severe("Created Default lucene index directory: " + luceneDir);
                }
            }
            try { if (indexSearcher != null) {
                indexSearcher.close();
            } } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }
            try { if (indexReader != null) {
                indexReader.close();
            } } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }
            try { if (index != null) {
                index.close();
            } } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }
            index = FSDirectory.open(path);
            indexReader = IndexReader.open(index);
            indexSearcher = new IndexSearcher(indexReader);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateIndex(ProcessInstanceSearchData processInstanceSearchData) {
        Document doc = new Document();
        doc.add(new Field(AWF__ID,
                String.valueOf(processInstanceSearchData.getProcessInstanceId()),
                Field.Store.YES,Field.Index.NOT_ANALYZED));
        doc.add(new Field(AWF__TYPE, PROCESS_INSTANCE, Field.Store.YES, Field.Index.NOT_ANALYZED));
        for (ProcessInstanceSearchAttribute attr : processInstanceSearchData.getSearchAttributes()) {
            if (attr.getValue() != null && !attr.getValue().trim().isEmpty()) {
                Field field = new Field(attr.getName(),
                        attr.isKeyword() ? attr.getValue().toLowerCase() : attr.getValue(),
                        Field.Store.YES,
                        attr.isKeyword() ? Field.Index.NOT_ANALYZED : Field.Index.ANALYZED);
                doc.add(field);
            }
        }
        updateIndex(doc);
    }

    @Override
    public List<Long> searchProcesses(String query, int offset, int limit, boolean onlyRunning,
                                      String[] userRoles,
                                      String assignee, String... queues) {

        List<Document> results;
        List<Query> addQueries = new ArrayList<Query>();
        if (assignee != null) {
            addQueries.add(new TermQuery(new Term(AWF__ASSIGNEE, assignee)));
        }
        if (queues != null) for (String queue : queues) {
            addQueries.add(new TermQuery(new Term(AWF__QUEUE, queue)));
        }
        if (onlyRunning) {
            addQueries.add(new TermQuery(new Term(AWF_RUNNING, String.valueOf(true))));
        }

        if (userRoles != null) {
            BooleanQuery bq = new BooleanQuery();
            bq.add(new TermQuery(new Term(AWF__ROLE, "__AWF__ROLE_ALL".toLowerCase())), BooleanClause.Occur.SHOULD);
            for (String roleName : userRoles) {
                bq.add(new TermQuery(new Term(AWF__ROLE, roleName.replace(' ', '_').toLowerCase())),
                        BooleanClause.Occur.SHOULD);
            }
            addQueries.add(bq);
        }
        results = search(query, 0, 1000, addQueries.toArray(new Query[addQueries.size()]));
        //always check 1000 first results - larger limit means no sense and Lucene provides the results
        //with no sort guarantees

        List<Long> res = new ArrayList<Long>(results.size());
        for (Document doc : results) {
            Fieldable fieldable = doc.getFieldable(AWF__ID);
            if (fieldable != null) {
                String s = fieldable.stringValue();
                if (s != null) {
                    res.add(Long.parseLong(s));
                }
            }
        }
        Collections.sort(res);
        Collections.reverse(res);
        return res.subList(offset, Math.min(offset+limit, res.size()));
    }

    public List<Document> search(String query, int offset, int limit, Query... addQueries) {
        try {
            LOGGER.fine("Parsing lucene search query: " + query);
            QueryParser qp = new QueryParser(Version.LUCENE_35, "all", new StandardAnalyzer(Version.LUCENE_35));
            Query q = qp.parse(query);
            BooleanQuery bq = new BooleanQuery();
            bq.add(new TermQuery(new Term(AWF__TYPE, PROCESS_INSTANCE)), BooleanClause.Occur.MUST);
            for (Query qq : addQueries) {
                bq.add(qq, BooleanClause.Occur.MUST);
            }
            bq.add(q, BooleanClause.Occur.MUST);

            LOGGER.fine("Searching lucene index with query: " + bq.toString());
            TopDocs search = indexSearcher.search(bq, offset + limit);

            List<Document> results = new ArrayList<Document>(limit);
            LOGGER.fine("Total result count for query: " + bq.toString() + " is " + search.totalHits);
            for (int i = offset; i < offset+limit && i < search.totalHits; i++) {
                ScoreDoc scoreDoc = search.scoreDocs[i];
                results.add(indexSearcher.doc(scoreDoc.doc));
            }
            return results;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);

        }
    }
    public synchronized void updateIndex(Document... docs) {
        try {
            //how awesome to force programmer to hardcode library version with no reasonable default
            IndexWriterConfig cfg = new IndexWriterConfig(Version.LUCENE_35, new StandardAnalyzer(Version.LUCENE_35));
            IndexWriter indexWriter = new IndexWriter(index, cfg);
            for (Document doc : docs) {
                LOGGER.fine("Updating index for document: " + doc.getFieldable(AWF__ID));
                indexWriter.deleteDocuments(new Term(AWF__ID, doc.getFieldable(AWF__ID).stringValue()));
                StringBuilder all = new StringBuilder();
                for (Fieldable f : doc.getFields()) {
                    all.append(f.stringValue());
                    all.append(' ');
                }
                LOGGER.fine("Updated field all for "+ doc.getFieldable(AWF__ID) + " with value: " + all);
                doc.add(new Field("all", all.toString(), Field.Store.NO, Field.Index.ANALYZED));
            }
            indexWriter.addDocuments(Arrays.asList(docs));
            LOGGER.fine("reindexing Lucene...");
            indexWriter.commit();
            indexWriter.close();
            LOGGER.fine("reindexing Lucene... DONE!");

            try { if (indexSearcher != null) {
                indexSearcher.close();
            } } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }
            try { if (indexReader != null) {
                indexReader.close();
            } } catch (Exception e) { LOGGER.log(Level.SEVERE, e.getMessage(), e); }

            indexReader = IndexReader.open(index);
            indexSearcher = new IndexSearcher(indexReader);
            LOGGER.fine("reopened Lucene index handles");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    protected void forwardErrorInfoToMonitor(String path, Exception e) 
	{
   		monitorInfo.append("\nSEVERE EXCEPTION: " + path);
   		monitorInfo.append("\n" + e.getMessage());
   	}

    public StringBuffer getMonitorInfo() {
        return monitorInfo;
    }
}
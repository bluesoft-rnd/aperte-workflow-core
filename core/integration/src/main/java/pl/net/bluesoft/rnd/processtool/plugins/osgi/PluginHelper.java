package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import static pl.net.bluesoft.util.lang.StringUtil.hasText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;

public class PluginHelper {
    public enum State {
        STOPPED, INITIALIZING, ACTIVE
    }

    private State state = State.STOPPED;
    private Felix felix;

    private Map<String, Long> fileTimes;
    private ScheduledExecutorService executor;

    private static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PluginHelper.class.getName());

    synchronized private void processBundleExtensions(final Bundle bundle, int eventType, final ProcessToolContextFactory ctx) throws ClassNotFoundException {
        if (ctx == null) {
            LOGGER.severe("No default process tool context registered! - skipping process tool context-based processing of this OSGI bundle");
            return;
        }

        String hibernateClasses = (String) bundle.getHeaders().get("ProcessTool-Model-Enhancement");
        String widgetClasses = (String) bundle.getHeaders().get("ProcessTool-Widget-Enhancement");
        String buttonClasses = (String) bundle.getHeaders().get("ProcessTool-Button-Enhancement");
        String stepClasses = (String) bundle.getHeaders().get("ProcessTool-Step-Enhancement");
        String i18nproperties = (String) bundle.getHeaders().get("ProcessTool-I18N-Property");
        String processProperties = (String) bundle.getHeaders().get("ProcessTool-Process-Deployment");

        ProcessToolRegistry toolRegistry = ctx.getRegistry();
        boolean hasModelChanges = false;
        if (hasText(hibernateClasses)) {
            String[] classes = hibernateClasses.replaceAll("\\s*", "").split(",");
            for (String cls : classes) {
                if (eventType == Bundle.ACTIVE) {
                    toolRegistry.registerModelExtension(bundle.loadClass(cls));
                }
                else {
                    toolRegistry.unregisterModelExtension(bundle.loadClass(cls));
                }
                hasModelChanges = true;
            }
        }
        if (hasModelChanges) {
            toolRegistry.commitModelExtensions();
        }
        if (hasText(widgetClasses)) {
            String[] classes = widgetClasses.replaceAll("\\s*", "").split(",");
            for (String cls : classes) {
                if (eventType == Bundle.ACTIVE) {
                    toolRegistry.registerWidget(bundle.loadClass(cls));
                }
                else {
                    toolRegistry.unregisterWidget(bundle.loadClass(cls));
                }
            }
        }
        if (hasText(buttonClasses)) {
            String[] classes = buttonClasses.replaceAll("\\s*", "").split(",");
            for (String cls : classes) {
                if (eventType == Bundle.ACTIVE) {
                    toolRegistry.registerButton(bundle.loadClass(cls));
                }
                else {
                    toolRegistry.unregisterButton(bundle.loadClass(cls));
                }
            }
        }

        if (hasText(stepClasses)) {
            String[] classes = stepClasses.replaceAll("\\s*", "").split(",");
            for (String cls : classes) {
                if (eventType == Bundle.ACTIVE) {
                    toolRegistry.registerStep(bundle.loadClass(cls));
                }
                else {
                    toolRegistry.unregisterStep(bundle.loadClass(cls));
                }
            }
        }

        if (hasText(i18nproperties)) {
            String[] properties = i18nproperties.replaceAll("\\s*", "").split(",");
            for (final String propertyFileName : properties) {
                String providerId = bundle.getBundleId() + File.separator + propertyFileName;
                if (eventType == Bundle.ACTIVE) {
                    toolRegistry.registerI18NProvider(new PropertiesBasedI18NProvider(new PropertyLoader() {
                        @Override
                        public InputStream loadProperty(String path) throws IOException {
                            URL cl = bundle.getResource(path);
                            if (cl == null) {
                                return null;
                            }
                            return cl.openStream();
                        }
                    }, propertyFileName), providerId);
                }
                else {
                    toolRegistry.unregisterI18NProvider(providerId);
                }
            }
        }

        if (hasText(processProperties)) {
            String[] properties = processProperties.replaceAll("\\s*", "").split(",");
            // Don't use File.separator here, we are dealing with the 
            // content inside bundle not on the file system 
            final String sep = "/";
            for (String processPackage : properties) {
                String providerId = bundle.getBundleId() + sep + processPackage.replace(".", sep) + sep + "messages";
                if (eventType == Bundle.ACTIVE) {
                    try {

                        String basepath = sep + processPackage.replace(".", sep) + sep;
                        InputStream imageStream = null;
                        if (bundle.getResource(basepath + "processdefinition.png") != null) {
                            imageStream = bundle.getResource(basepath + "processdefinition.png").openStream();
                        }
                        URL logoUrl = bundle.getResource(basepath + "processdefinition-logo.png");
                        toolRegistry.deployOrUpdateProcessDefinition(
                                bundle.getResource(basepath + "processdefinition.jpdl.xml").openStream(),
                                bundle.getResource(basepath + "processtool-config.xml").openStream(),
                                bundle.getResource(basepath + "queues-config.xml").openStream(),
                                logoUrl != null ? logoUrl.openStream() : null,
                                imageStream);
                        toolRegistry.registerI18NProvider(new PropertiesBasedI18NProvider(new PropertyLoader() {
                            @Override
                            public InputStream loadProperty(String path) throws IOException {
                                URL cl = bundle.getResource(path);
                                if (cl == null) {
                                    return null;
                                }
                                return cl.openStream();
                            }
                        }, sep + processPackage.replace(".", sep) + sep + "messages"), providerId);
                        URL dictUrl = bundle.getResource(basepath + "process-dictionaries.xml");
                        if (dictUrl != null) {
                            toolRegistry.registerDictionaries(dictUrl.openStream());
                        }
                    }
                    catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
                else { //ignore
                    toolRegistry.unregisterI18NProvider(providerId);
                }
            }
        }
    }

    public synchronized void initializePluginSystem(String pluginsDir, String storageDir, ProcessToolRegistry registry)
            throws BundleException {
        pluginsDir = pluginsDir.replace('/', File.separatorChar);
        state = State.INITIALIZING;
        LOGGER.fine("initializePluginSystem.start!");
        initializeFelix(pluginsDir, storageDir, registry);
        LOGGER.fine("initializeCheckerThread!");
        initCheckerThread(pluginsDir);
        LOGGER.fine("initializePluginSystem.end!");
        state = State.ACTIVE;
    }

    private void initializeFelix(String pluginsDir, String storageDir, final ProcessToolRegistry registry) throws BundleException {
        if (felix != null) {
            felix.stop();
            felix = null;
        }

        Map<String, Object> configMap = new HashMap<String, Object>();
        putBasicConfig(configMap);
        putStorageConfig(storageDir, configMap);
        putPackageConfig(pluginsDir, configMap);
        putActivatorConfig(registry, configMap);

        felix = new Felix(configMap);
        felix.init();
        felix.start();
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
     * Sets {@link Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA} property
     *
     * @param pluginsDir
     * @param configMap
     */
    private void putPackageConfig(String pluginsDir, Map<String, Object> configMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSystemPackages(pluginsDir));

        configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, sb.toString());
    }

    /**
     * Sets basic Felix properties
     *
     * @param configMap
     */
    private void putBasicConfig(Map<String, Object> configMap) {
        configMap.put(FelixConstants.LOG_LEVEL_PROP, "4");
        configMap.put(FelixConstants.LOG_LOGGER_PROP, new Logger() {
            @Override
            protected void doLog(Bundle bundle, ServiceReference sr, int level,
                                 String msg, Throwable throwable) {
                if (throwable != null) {
                    LOGGER.log(Level.SEVERE, "Felix: " + msg + ", Throwable: " + throwable.getMessage(), throwable);
                } else {
                    LOGGER.log(Level.WARNING, "Felix: " + msg);
                }
            }
        });

        configMap.put(FelixConstants.SERVICE_URLHANDLERS_PROP, true);
        configMap.put(FelixConstants.FRAMEWORK_BUNDLE_PARENT, FelixConstants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
        configMap.put("felix.auto.deploy.action", "install,update,start");
    }

    /**
     * Sets {@link FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP} property
     *
     * @param registry
     * @param configMap
     */
    private void putActivatorConfig(final ProcessToolRegistry registry, Map<String, Object> configMap) {
        ArrayList<BundleActivator> activators = new ArrayList<BundleActivator>();
        activators.add(new BundleActivator() {
            public void start(BundleContext context) throws Exception {
                if (registry != null) {
                    context.registerService(
                            ProcessToolRegistry.class.getName(),
                            registry,
                            new Hashtable<Object, Object>());
                }
            }

            public void stop(BundleContext context) throws Exception {
            }
        });

        activators.add(new BundleActivator() {
            BundleListener listener = new BundleListener() {
                public void bundleChanged(BundleEvent event) {
                    try {
                        if (((BundleEvent.STARTED | BundleEvent.STOPPED) & event.getType()) != 0) {
                            processBundleExtensions(
                                    event.getBundle(),
                                    event.getBundle().getState(),
                                    registry.getProcessToolContextFactory());
                        }
                    } catch (ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE, "Exception processing bundle", e);
                        throw new RuntimeException(e);
                    }
                }
            };

            public void start(BundleContext context) throws Exception {
                for (Bundle b : context.getBundles()) {
                    processBundleExtensions(b, b.getState(), registry.getProcessToolContextFactory());
                }
                context.addBundleListener(listener);
            }

            public void stop(BundleContext context) throws Exception {
                context.removeBundleListener(listener);
            }
        });

        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators);
    }

    private void initCheckerThread(final String pluginsDir) {
        fileTimes = new HashMap<String, Long>();
        LOGGER.info("Starting OSGi checker thread");
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        executor = Executors.newScheduledThreadPool(2);
        executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                synchronized (PluginHelper.class) {
                    executor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            scheduleBundleInstall(pluginsDir);
                        }
                    }, 0, TimeUnit.SECONDS);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
        LOGGER.info("Started OSGi checker thread");
    }

    private void scheduleBundleInstall(String pluginsDir) {
        if (felix == null) {
            LOGGER.warning("Felix not initialized yet");
            return;
        }
        File f = new File(pluginsDir);
        if (!f.exists()) {
            LOGGER.warning("Plugins dir not found: " + pluginsDir);
            return;
        }
        String[] list = f.list();
        Arrays.sort(list);
        Set<String> visitedPaths = new HashSet<String>();
        List<String> installableBundlePaths = new ArrayList<String>();
        for (String filename : list) {
            File subFile = new File(f.getAbsolutePath() + File.separator + filename);
            String path = subFile.getAbsolutePath();
            if (!subFile.isDirectory() && path.matches(".*jar$")) {
                Long lastModified = fileTimes.get(path);
                if (lastModified == null || lastModified < subFile.lastModified()) {
                    installableBundlePaths.add(path);
                    fileTimes.put(path, subFile.lastModified());
                }
                visitedPaths.add(path);
            }
        }
        Set<String> fileTimesKeys = new HashSet<String>(fileTimes.keySet());
        fileTimesKeys.removeAll(visitedPaths);
        for (String path : fileTimesKeys) {
            fileTimes.remove(path);
        }

        boolean installed = true;
        while (!installableBundlePaths.isEmpty() && installed) {
            installed = false;
            for (Iterator<String> it = installableBundlePaths.iterator(); it.hasNext(); ) {
                if (installBundle(it.next())) {
                    it.remove();
                    installed = true;
                }
            }
        }
        if (!installableBundlePaths.isEmpty()) {
            LOGGER.warning("UNABLE TO INSTALL BUNDLES: " + installableBundlePaths.toString());
        }

    }

    synchronized private boolean installBundle(String path) {
        boolean result = false;
        try {
            // Create the location using new URL object. That way path is formatted
            // and Felix is getting proper URL which prevents MalformedUrlException
            // on Windows
            String location = new URL("file://" + path).toString();

            LOGGER.warning("INSTALLING: " + path);
            Bundle bundle = felix.getBundleContext().installBundle(location, new FileInputStream(path));
            bundle.update(new FileInputStream(path));
            LOGGER.warning("INSTALLED: " + path);
            bundle.start();
            LOGGER.warning("STARTED: " + path);
            result = true;
        }
        catch (Exception e) {
            LOGGER.warning("BLOCKING: " + path);
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
        return result;
    }

    public String getSystemPackages(String basedir) {
        try {
            FileInputStream fis = new FileInputStream(basedir + File.separatorChar + "packages.export");
            try {
                int c = 0;
                StringBuffer sb = new StringBuffer();
                while ((c = fis.read()) >= 0) {
                    if (c == 10 || c == 13 || (char) c == ' ' || (char) c == '\t') {
                        continue;
                    }
                    sb.append((char) c);
                }
                return sb.toString().replaceAll("\\s*", "");
            }
            finally {
                if (fis != null) {
                    fis.close();
                }
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error occurred while reading " + basedir + File.separatorChar + "packages.export", e);
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
}
package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class PluginHelper {
    private Felix felix;

    private Map<String, Long> fileTimes = new HashMap<String, Long>();
    private ScheduledExecutorService executor;

    private static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PluginHelper.class.getName());

    void processBundleExtensions(final Bundle bundle, int eventType, final ProcessToolContextFactory ctx) throws ClassNotFoundException {

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
                String providerId = bundle.getBundleId() + "/" + propertyFileName;

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
            for (String processPackage : properties) {
                String providerId = bundle.getBundleId() + "/" + processPackage.replace(".", "/") + "/messages";
                if (eventType == Bundle.ACTIVE) {
                    try {
                        String basepath = "/" + processPackage.replace(".", "/") + "/";
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
                        }, "/" + processPackage.replace(".", "/") + "/messages"), providerId);
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

    public synchronized void initializePluginSystem(final String pluginsDir,
                                                    final ProcessToolRegistry registry) throws BundleException {
        LOGGER.fine("initializePluginSystem.start!");
        initializeFelix(pluginsDir, registry);
        LOGGER.fine("initializeCheckerThread!");
        initCheckerThread(pluginsDir);
        LOGGER.fine("initializePluginSystem.end!");
    }

    private void initializeFelix(String pluginsDir, final ProcessToolRegistry registry) throws BundleException {
        if (felix != null) {
            felix.stop();
            felix = null;
        }

        Map<String, Object> configMap = new HashMap();
        ArrayList<BundleActivator> activators = new ArrayList<BundleActivator>();
        activators.add(new BundleActivator() {
            public void start(BundleContext context) throws Exception {
                if (registry != null) {
                    context.registerService(ProcessToolRegistry.class.getName(),
                            registry,
                            new Hashtable());
                }
            }

            public void stop(BundleContext context) throws Exception {
            }
        });

        activators.add(new BundleActivator() {
            BundleListener listener = new BundleListener() {
                public void bundleChanged(BundleEvent event) {
                    try {
                        int state = event.getType();
                        if (state == BundleEvent.STARTED || state == BundleEvent.STOPPED) { //minimalize the number of bundle deployments in Aperte Workflow
                            processBundleExtensions(event.getBundle(), event.getBundle().getState(),
                                    registry.getProcessToolContextFactory());
                        }
                    }
                    catch (ClassNotFoundException e) {
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

        LOGGER.fine("initializePluginSystem.middle!");
        configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                "pl.net.bluesoft.rnd.processtool.plugins.osgi," +
                        "pl.net.bluesoft.rnd.processtool.plugins," +
                        "pl.net.bluesoft.rnd.processtool.model," +
                        "pl.net.bluesoft.rnd.processtool.model.config," +
                        "pl.net.bluesoft.rnd.processtool.model.processdata," +
                        "pl.net.bluesoft.rnd.processtool.bpm," +
                        "pl.net.bluesoft.rnd.processtool.ui.widgets," +
                        "pl.net.bluesoft.rnd.processtool," +
                        "pl.net.bluesoft.rnd.processtool.steps," +
                        "pl.net.bluesoft.rnd.processtool.ui.widgets," +
                        "pl.net.bluesoft.rnd.util.i18n," +
                        "javax.persistence," +
                        "com.vaadin.data," +
                        "com.vaadin.ui," +
                        "com.vaadin," +
                        "com.vaadin.data.util," +
                        "org.hibernate.proxy," +
                        "org.hibernate," +
                        "javassist.util.proxy," +
                        "bsh," +
                        "pl.net.bluesoft.rnd.util.i18n," +
                        "pl.net.bluesoft.rnd.util.xml," +
                        "pl.net.bluesoft.rnd.util.xml.jaxb," +
                        "pl.net.bluesoft.rnd.util.xml.validation," +
                        "javax.crypto,javax.crypto.spec,javax.mail,javax.mail.internet,javax.net,javax.net.ssl,javax.security.auth.callback,javax.servlet,javax.servlet.http,javax.swing,javax.swing.border,javax.swing.event,javax.swing.table,javax.swing.text,javax.swing.tree,javax.xml.parsers,org.apache.commons.collections.comparators,org.apache.commons.collections.keyvalue,org.apache.commons.collections.list,org.apache.commons.collections.set,org.apache.log,org.apache.log4j,org.apache.soap,org.apache.soap.rpc,org.apache.soap.transport,org.apache.soap.util,org.apache.soap.util.net,org.w3c.dom,org.xml.sax,org.xml.sax.helpers" +
                        "pl.net.bluesoft.rnd.processtool.ui.widgets," +
                        "pl.net.bluesoft.rnd.processtool.ui.widgets.impl," + getSystemPackages(pluginsDir)
        );
        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators);
        configMap.put(FelixConstants.LOG_LEVEL_PROP, "4");
        configMap.put(FelixConstants.SERVICE_URLHANDLERS_PROP, true);
        configMap.put(FelixConstants.FRAMEWORK_STORAGE_CLEAN, FelixConstants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        configMap.put("felix.auto.deploy.action", "install,update,start");
        configMap.put(FelixConstants.LOG_LOGGER_PROP, new Logger() {
            @Override
            protected void doLog(Bundle bundle, ServiceReference sr, int level, String msg, Throwable throwable) {
                LOGGER.warning("Felix: " + msg);
                if (throwable != null) {
                    LOGGER.log(Level.SEVERE, throwable.getMessage(), throwable);
                }
            }
        });
        felix = new Felix(configMap);
        felix.init();
        felix.start();
    }

    private void initCheckerThread(final String pluginsDir) {
        LOGGER.info("Starting OSGi checker thread");
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        executor = Executors.newScheduledThreadPool(2);
        Runnable runnable = new Runnable() {
            public void run() {
                if (felix == null) {
                    LOGGER.warning("Felix not initialized yet");
                    return;
                }
                File f = new File(pluginsDir);
                if (f == null || !f.exists()) {
                    LOGGER.warning("Plugins dir not found: " + pluginsDir);
                    return;
                }
                String[] list = f.list();
                Arrays.sort(list);
                for (String filename : list) {
                    File subFile = new File(f.getAbsolutePath() + File.separator + filename);
                    String path = subFile.getAbsolutePath();
                    if (!subFile.isDirectory() && path.matches(".*jar$")) {
                        Long aLong = fileTimes.get(path);
                        if (aLong == null || aLong < subFile.lastModified()) {
                            aLong = null;
                        }
                        if (aLong == null) {
                            try {
                                LOGGER.warning("installing: " + path);
                                Bundle bundle = felix.getBundleContext().installBundle("file://" + path, new FileInputStream(path));
                                bundle.update(new FileInputStream(path));
                                LOGGER.warning("installed: " + path);
                                bundle.start();
                                LOGGER.warning("started: " + path);
                                fileTimes.put(path, subFile.lastModified());
                            }
                            catch (Exception e) {
                                LOGGER.warning("blocking " + path);
                                LOGGER.warning(e.getMessage());
                                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                                fileTimes.put(path, subFile.lastModified());
                            }
                        }
                    }
                }
            }
        };
        executor.scheduleAtFixedRate(runnable, 3, 3, TimeUnit.SECONDS);
        LOGGER.info("Started OSGi checker thread");
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
            LOGGER.log(Level.SEVERE, "Error occured while reading " + basedir + File.separatorChar + "packages.export", e);
        }
        return "";
    }

    public synchronized void stopPluginSystem() throws BundleException {
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
}

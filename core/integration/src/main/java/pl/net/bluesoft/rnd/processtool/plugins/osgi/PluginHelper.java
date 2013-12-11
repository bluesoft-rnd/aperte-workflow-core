package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import org.osgi.framework.BundleException;
import pl.net.bluesoft.rnd.processtool.plugins.PluginManager;
import pl.net.bluesoft.rnd.processtool.plugins.PluginMetadata;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.newfelix.NewFelixBundleService;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginHelper implements PluginManager {
    public enum State {
        STOPPED, INITIALIZING, ACTIVE
    }

    private State state = State.STOPPED;

    private ScheduledExecutorService executor;

    private ErrorMonitor errorMonitor = new ErrorMonitor();
    private static Logger LOGGER = Logger.getLogger(PluginHelper.class.getName());

    private FelixBundleService felixService;

    public synchronized void initialize(String pluginsDir,
										String felixDir,
										ProcessToolRegistryImpl registry) throws BundleException {
        felixService = createFelixBundleService();
        felixService.setPluginsDir(pluginsDir.replace('/', File.separatorChar));

        registry.getBundleRegistry().setPluginManager(this);
        state = State.INITIALIZING;
        LOGGER.fine("initialize.start!");
        initializeFelix(felixDir, registry);
        LOGGER.fine("initializeCheckerThread!");
        initCheckerThread();
        LOGGER.fine("initialize.end!");
        state = State.ACTIVE;
    }

    private FelixBundleService createFelixBundleService() {
		return new NewFelixBundleService(errorMonitor, LOGGER);
    }

    private void initializeFelix(String felixDir, ProcessToolRegistryImpl registry) throws BundleException {
        felixService.initialize(felixDir, registry);
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
        catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "Bundle install interrupted", e);
            forwardErrorInfoToMonitor("Bundle install interrupted", e);
        }
        finally {
            scheduleBundleInstallAfter(5);
        }
    }

    private void forwardErrorInfoToMonitor(String path, Throwable e) {
        errorMonitor.forwardErrorInfoToMonitor(path, e);
    }

    private void scheduleBundleInstallAfter(long seconds) {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
        executor.schedule(createBundleInstallTask(), seconds, TimeUnit.SECONDS);
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
}
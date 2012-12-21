package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.lang.Classes;

import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractPluginActivator implements BundleActivator {
    protected BundleContext context;
    protected ProcessToolRegistry registry;

    protected ProcessToolRegistry getRegistry() {
        return FelixServiceBridge.getServiceByReference(ProcessToolRegistry.class, context);
    }

    protected Properties loadProperties(String path) {
        return Classes.loadProperties(getClass(), path);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        this.registry = getRegistry();
        init();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        this.context = context;
        destroy();
    }

    protected InputStream loadResourceAsStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }

    protected abstract void init() throws Exception;
    protected abstract void destroy() throws Exception;
}

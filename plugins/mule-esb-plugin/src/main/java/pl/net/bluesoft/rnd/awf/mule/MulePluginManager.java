package pl.net.bluesoft.rnd.awf.mule;

import org.mule.MuleServer;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.context.MuleContextBuilder;
import org.mule.config.ConfigResource;
import org.mule.config.DefaultMuleConfiguration;
import org.mule.config.PropertiesMuleConfigurationFactory;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.util.ClassUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class MulePluginManager {
    
    private static final Logger logger = Logger.getLogger(MulePluginManager.class.getName());
    
    private MuleContext muleContext = null;
    private Map<String, SpringXmlConfigurationBuilder> builderMap = new HashMap<String, SpringXmlConfigurationBuilder>();

    public MulePluginManager() {

    }

    public synchronized void initialize() throws MuleException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            DefaultMuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
            if (muleContext != null && muleContext.isStarted()) {
                muleContext.stop();
                muleContext.dispose();
            }
            while (muleContext != null) {
                if (muleContext.isDisposed()) {
                    logger.warning("Mule already disposed!");
                    muleContext = null;
                    break;
                }
                logger.info("Waiting 1s for mule to stop and dispose itself");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }

            }

            DefaultMuleConfiguration muleConfiguration = new
                    PropertiesMuleConfigurationFactory(PropertiesMuleConfigurationFactory.getMuleAppConfiguration(MuleServer.DEFAULT_CONFIGURATION))
                    .createConfiguration();
            muleConfiguration.setId(""+Math.random()*System.currentTimeMillis());
            muleConfiguration.setContainerMode(true);
            muleConfiguration.setShutdownTimeout(3);

            muleConfiguration.setWorkingDirectory(ProcessToolContext.Util.getHomePath() + File.separator + ".mule");

            MuleContextBuilder muleContextBuilder = new DefaultMuleContextBuilder();
            muleContextBuilder.setMuleConfiguration(muleConfiguration);

            muleContext = muleContextFactory.createMuleContext(new ArrayList(builderMap.values()), muleContextBuilder);
            muleContext.start();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public void shutdown() throws MuleException {
        if (muleContext != null) {
            muleContext.stop();
            muleContext.dispose();
        }
    }

    public synchronized void unregisterEntry(String name) {
        SpringXmlConfigurationBuilder builder = builderMap.get(name);
        if (builder == null) {
            throw new IllegalArgumentException("Entry with name: " + name + " is not registered!");
        }
        try {
            builder.unconfigure(muleContext);
        } finally {
            builderMap.remove(name);
        }

    }

    public synchronized void registerEntry(String name, InputStream is, ClassLoader cl) {

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            if (builderMap.containsKey(name)) {
                throw new IllegalArgumentException("Entry with name: " + name + " is already registered!");
            }
            ConfigResource[] resources = new ConfigResource[]{new ConfigResource(name, is)};
//            Class<?> aClass = cl.loadClass(SpringXmlConfigurationBuilder.class.getName());
//            Constructor<?> constructor = aClass.getConstructor(ConfigResource[].class);
            SpringXmlConfigurationBuilder builder = (SpringXmlConfigurationBuilder)
                    ClassUtils.instanciateClass(SpringXmlConfigurationBuilder.class.getName(), new Object[]{resources}, cl);
//            SpringXmlConfigurationBuilder builder = (SpringXmlConfigurationBuilder) constructor.newInstance(resources);
//            builder.configure(muleContext);
//            muleContext.start();
            builderMap.put(name, builder);
            initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public MuleContext getMuleContext() {
        return muleContext;
    }
}
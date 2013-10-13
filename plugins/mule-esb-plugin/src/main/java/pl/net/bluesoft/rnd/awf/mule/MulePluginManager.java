package pl.net.bluesoft.rnd.awf.mule;

import org.apache.commons.io.IOUtils;
import org.mule.MuleServer;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.config.ConfigurationBuilder;
import org.mule.api.context.MuleContextBuilder;
import org.mule.config.ConfigResource;
import org.mule.config.DefaultMuleConfiguration;
import org.mule.config.PropertiesMuleConfigurationFactory;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.util.ClassUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private Map<String, PluginConfiguration> configMap = new HashMap<String, PluginConfiguration>();
    private Map<String, ConfigurationBuilder> builderMap = new HashMap<String, ConfigurationBuilder>();

    private static final class PluginConfiguration {
        private String name;
        private InputStream is;
        private ClassLoader cl;

        private PluginConfiguration(String name, InputStream is, ClassLoader cl) {
            this.name = name;
            this.is = is;
            this.cl = cl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public InputStream getIs() {
            return is;
        }

        public void setIs(InputStream is) {
            this.is = is;
        }

        public ClassLoader getCl() {
            return cl;
        }

        public void setCl(ClassLoader cl) {
            this.cl = cl;
        }
    }

    private MulePluginManager() {
    }

	private static MulePluginManager instance;

	public static MulePluginManager instance() {
		if (instance == null) {
			instance = new MulePluginManager();
		}
		return instance;
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

            List<PluginConfiguration> configurationBuilders = new ArrayList<PluginConfiguration>(configMap.values());
            List<ConfigurationBuilder> builders = new ArrayList<ConfigurationBuilder>();
            List<ConfigResource> resources = new ArrayList<ConfigResource>();
            for (PluginConfiguration c : configurationBuilders) {
                InputStream is = c.getIs();
                try {
                    is.reset();
                    resources.add(new ConfigResource(c.getName(), new ByteArrayInputStream(IOUtils.toByteArray(is))));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
            try {
                SpringXmlConfigurationBuilder builder = (SpringXmlConfigurationBuilder)
                                ClassUtils.instanciateClass(SpringXmlConfigurationBuilder.class.getName(),
                                        new Object[]{resources.toArray(new ConfigResource[resources.size()])},
                                        getClass().getClassLoader());
                builders.add(builder);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }

            muleContext = muleContextFactory.createMuleContext(builders, muleContextBuilder);
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
        configMap.remove(name);
        try {
            initialize();
        } catch (MuleException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    public synchronized void registerEntry(String name, InputStream is, ClassLoader cl) {

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            if (configMap.containsKey(name)) {
                throw new IllegalArgumentException("Entry with name: " + name + " is already registered!");
            }
            configMap.put(name, new PluginConfiguration(name, is, cl));
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
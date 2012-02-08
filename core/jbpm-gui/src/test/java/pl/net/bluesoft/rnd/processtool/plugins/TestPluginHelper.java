package pl.net.bluesoft.rnd.processtool.plugins;

import org.junit.Test;
import org.osgi.framework.BundleException;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.PluginHelper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class TestPluginHelper {

    private static final Logger logger = Logger.getLogger(TestPluginHelper.class.getName());
    
    @Test
    public void testScanner() throws BundleException, IOException {
        new PluginHelper().initialize("osgi", "felix-cache", "lucene", new ProcessToolRegistryImpl());
        if (System.getProperty("test.osgi.wait") != null) {
            while (true) try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}
package pl.net.bluesoft.rnd.processtool.plugins;

import org.junit.Test;
import org.osgi.framework.BundleException;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.PluginHelper;

import java.io.IOException;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class TestPluginHelper {

    @Test
    public void testScanner() throws BundleException, IOException {
        new PluginHelper().initializePluginSystem("osgi", "felix-cache", new ProcessToolRegistryImpl());
        if (System.getProperty("test.osgi.wait") != null) {
            while (true) try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
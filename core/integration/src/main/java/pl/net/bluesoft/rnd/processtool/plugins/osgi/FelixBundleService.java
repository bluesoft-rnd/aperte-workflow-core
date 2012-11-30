package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import org.osgi.framework.BundleException;
import pl.net.bluesoft.rnd.processtool.plugins.PluginManager;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;

/**
 * User: POlszewski
 * Date: 2012-11-29
 * Time: 20:50
 */
public interface FelixBundleService extends PluginManager {
	void initialize(String felixDir, ProcessToolRegistryImpl registry) throws BundleException;
	void setPluginsDir(String pluginsDir);
	void stopFelix() throws BundleException;
	void scheduledBundleInstall();
}

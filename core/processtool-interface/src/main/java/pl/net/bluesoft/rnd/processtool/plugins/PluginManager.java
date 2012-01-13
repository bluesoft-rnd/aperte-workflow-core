package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface PluginManager {
    
    void registerPlugin(String filename, InputStream is);

    Collection<PluginInformation> getRegisteredPlugins();

    void enablePlugin(PluginInformation pi);
    void disablePlugin(PluginInformation pi);
    void uninstallPlugin(PluginInformation pi);
}

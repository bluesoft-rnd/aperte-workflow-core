package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface PluginManager {
    Collection<PluginMetadata> getRegisteredPlugins();
    void registerPlugin(String filename, InputStream is);
    void enablePlugin(PluginMetadata pi);
    void disablePlugin(PluginMetadata pi);
    void uninstallPlugin(PluginMetadata pi);
}

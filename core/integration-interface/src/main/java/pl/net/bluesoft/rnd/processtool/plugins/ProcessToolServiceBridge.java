package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public interface ProcessToolServiceBridge {
    <T> boolean registerService(Class<T> serviceClass, T instance, Properties properties);

    <T> T loadService(Class<T> serviceClass);

    <T> boolean removeService(Class<T> serviceClass);

    InputStream loadResource(String bundleSymbolicName, String resourcePath) throws IOException;

    List<PluginMetadata> getInstalledPlugins() throws ClassNotFoundException;
}

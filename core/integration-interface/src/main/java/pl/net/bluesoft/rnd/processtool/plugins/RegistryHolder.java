package pl.net.bluesoft.rnd.processtool.plugins;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class RegistryHolder {

    private static ProcessToolRegistry registry = null;

    public static synchronized void setRegistry(ProcessToolRegistry registry) {
        RegistryHolder.registry = registry;
    }
    public static synchronized ProcessToolRegistry getRegistry() {
        return RegistryHolder.registry;
    }
}

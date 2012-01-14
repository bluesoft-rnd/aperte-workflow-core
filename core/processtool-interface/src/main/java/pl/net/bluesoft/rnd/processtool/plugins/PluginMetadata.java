package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.util.i18n.I18NProvider;

import java.net.URL;
import java.util.List;

public interface PluginMetadata extends Comparable<PluginMetadata> {

	String getBundleLocation();
	List<Class<?>> getStepClasses();
	List<Class<?>> getWidgetClasses();
	List<I18NProvider> getI18NProviders();
	String getHumanNameKey();
	String getDescriptionKey();
	List<URL> getIconResources();
	String getImplementationBuild();
	long getLastModifiedAt();

    long getId();
    String getName();
    String getSymbolicName();
    String getVersion();
    String getDocumentationUrl();
    String getHomepageUrl();
    String getDescription();
    long getState();
    String getStateDescription();
    boolean isCanEnable();
    boolean isCanDisable();
    boolean isCanUninstall();
}

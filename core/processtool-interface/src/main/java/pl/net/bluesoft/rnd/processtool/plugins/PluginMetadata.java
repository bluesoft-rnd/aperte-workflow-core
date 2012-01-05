package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.util.i18n.I18NProvider;

import java.net.URL;
import java.util.List;

public interface PluginMetadata {

	public String getBundleLocation();
	public String getBundleName();
	public List<Class<?>> getStepClasses();
	public List<Class<?>> getWidgetClasses();
	public List<I18NProvider> getI18NProviders();
	public String getHumanNameKey();
	public String getDescriptionKey();
	public List<URL> getIconResources();
	public String getImplementationBuild();
	public int getState();
	public long getLastModifiedAt();

}

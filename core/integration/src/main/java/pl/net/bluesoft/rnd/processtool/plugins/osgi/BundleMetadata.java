package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import pl.net.bluesoft.rnd.processtool.plugins.PluginMetadata;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class BundleMetadata implements PluginMetadata {
	private String				bundleLocation;
	private String				bundleName;
	private String				humanNameKey;
	private String				descriptionKey;
	private String				implementationBuild;
	private List<Class<?>>		widgetClasses	= new LinkedList<Class<?>>();
	private List<Class<?>>		stepClasses		= new LinkedList<Class<?>>();
	private List<URL>			iconResources	= new LinkedList<URL>();
	private List<I18NProvider>	i18NProviders	= new LinkedList<I18NProvider>();
	private int state;
	private long lastModifiedAt;

	public BundleMetadata() {
	}

	public BundleMetadata(String bundleLocation, String bundleName, long lastModifiedAt, int state) {
		this.bundleLocation = bundleLocation;
		this.bundleName = bundleName;
		this.lastModifiedAt = lastModifiedAt;
		this.state = state;
	}

	public String getBundleLocation() {
		return bundleLocation;
	}

	public void setBundleLocation(String bundleLocation) {
		this.bundleLocation = bundleLocation;
	}

	public String getBundleName() {
		return bundleName;
	}

	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}

	public List<Class<?>> getStepClasses() {
		return stepClasses;
	}

	public void setStepClasses(List<Class<?>> stepClasses) {
		this.stepClasses = stepClasses;
	}

	public List<Class<?>> getWidgetClasses() {
		return widgetClasses;
	}

	public void setWidgetClasses(List<Class<?>> widgetClasses) {
		this.widgetClasses = widgetClasses;
	}

	public List<I18NProvider> getI18NProviders() {
		return i18NProviders;
	}

	public void setI18NProviders(List<I18NProvider> i18NProviders) {
		this.i18NProviders = i18NProviders;
	}

	public void addWidgetClass(Class<?> widgetClass) {
		widgetClasses.add(widgetClass);
	}

	public void addStepClass(Class<?> stepClass) {
		stepClasses.add(stepClass);
	}

	public void addI18NProvider(I18NProvider provider) {
		i18NProviders.add(provider);
	}

	public void addIconResource(URL url) {
		iconResources.add(url);
	}

	public String getHumanNameKey() {
		return humanNameKey;
	}

	public void setHumanNameKey(String humanNameKey) {
		this.humanNameKey = humanNameKey;
	}

	public String getDescriptionKey() {
		return descriptionKey;
	}

	public void setDescriptionKey(String descriptionKey) {
		this.descriptionKey = descriptionKey;
	}

	public List<URL> getIconResources() {
		return iconResources;
	}

	public void setIconResources(List<URL> iconResources) {
		this.iconResources = iconResources;
	}

	public String getImplementationBuild() {
		return implementationBuild;
	}

	public void setImplementationBuild(String implementationBuild) {
		this.implementationBuild = implementationBuild;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(long lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}
}

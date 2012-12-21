package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import pl.net.bluesoft.rnd.processtool.plugins.PluginMetadata;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class BundleMetadata implements PluginMetadata {

	private String				bundleLocation;
	private String				humanNameKey;
	private String				descriptionKey;
	private String				implementationBuild;
	private List<Class<?>>		widgetClasses	= new LinkedList<Class<?>>();
	private List<Class<?>>		stepClasses		= new LinkedList<Class<?>>();
	private List<URL>			iconResources	= new LinkedList<URL>();
	private List<I18NProvider>	i18NProviders	= new LinkedList<I18NProvider>();

    private long id;
    private String name;
    private String description;
	private long state;
    private String stateDescription;
	private long lastModifiedAt;
    private boolean canUninstall;
    private boolean canDisable;
    private boolean canEnable;
    private String homepageUrl;
    private String version;
    private String documentationUrl;
    private String symbolicName;

    public BundleMetadata() {
	}

	public BundleMetadata(String bundleLocation, String name, long lastModifiedAt, long state) {
		this.bundleLocation = bundleLocation;
		this.name = name;
		this.lastModifiedAt = lastModifiedAt;
		this.state = state;
	}

	public String getBundleLocation() {
		return bundleLocation;
	}

	public void setBundleLocation(String bundleLocation) {
		this.bundleLocation = bundleLocation;
	}

	public String getName() {
		return name;
	}

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDocumentationUrl() {
        return documentationUrl; 
    }

    @Override
    public String getHomepageUrl() {
        return homepageUrl;
    }

    public void setName(String name) {
		this.name = name;
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

    public long getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateDescription() {
        return stateDescription;
    }

    public void setStateDescription(String stateDescription) {
        this.stateDescription = stateDescription;
    }

    public long getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(long lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCanUninstall() {
        return canUninstall;
    }

    public void setCanUninstall(boolean canUninstall) {
        this.canUninstall = canUninstall;
    }

    public boolean isCanEnable() {
        return canEnable;
    }

    public void setCanEnable(boolean canEnable) {
        this.canEnable = canEnable;
    }

    public boolean isCanDisable() {
        return canDisable;
    }

    public void setCanDisable(boolean canDisable) {
        this.canDisable = canDisable;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStatus(long status) {
        this.state = status;
    }

    public void setHomepageUrl(String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public int compareTo(PluginMetadata o) {
        return Long.valueOf(getId()).compareTo(Long.valueOf(o.getId()));
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BundleMetadata that = (BundleMetadata)o;

		if (id != that.id) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (int)(id ^ (id >>> 32));
	}
}

package org.aperteworkflow.editor.stepeditor.user;

import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class BundleItem {

	private String				bundleName;
	private String				bundleDescription;
	private List<I18NProvider>	i18NProviders;
	private List<URL>			iconResources;

	public BundleItem(String bundleName, String bundleDescription, List<I18NProvider> i18NProviders, List<URL> iconResources) {
		super();
		this.bundleName = bundleName;
		this.bundleDescription = bundleDescription;
		this.i18NProviders = i18NProviders;
		this.iconResources = iconResources;
	}

	public String getBundleName() {
		return bundleName;
	}

	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}

	public String getBundleDescription() {
		return bundleDescription;
	}

	public void setBundleDescription(String bundleDescription) {
		this.bundleDescription = bundleDescription;
	}

	public List<I18NProvider> getI18NProviders() {
		return i18NProviders;
	}

	public void setI18NProviders(List<I18NProvider> i18NProviders) {
		this.i18NProviders = i18NProviders;
	}

	public List<URL> getIconResources() {
		return iconResources;
	}

	public void setIconResources(List<URL> iconResources) {
		this.iconResources = iconResources;
	}

	public InputStream getIconStream(String icon) throws IOException {
		if (StringUtils.isEmpty(icon))
			return null;
		for (URL url : iconResources) {
			if (url.getPath().endsWith(icon)) {
				return url.openStream();
			}
		}
		return null;
	}

}

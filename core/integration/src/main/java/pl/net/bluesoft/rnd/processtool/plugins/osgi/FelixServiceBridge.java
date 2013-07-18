package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import pl.net.bluesoft.rnd.processtool.plugins.PluginMetadata;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolServiceBridge;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertiesBasedI18NProvider;
import pl.net.bluesoft.rnd.util.i18n.impl.PropertyLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static pl.net.bluesoft.rnd.processtool.plugins.osgi.OSGiBundleHelper.*;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

public class FelixServiceBridge implements ProcessToolServiceBridge {
    private Felix felix;

    public FelixServiceBridge(Felix felix) {
        this.felix = felix;
    }

    private Map<String, ServiceRegistration> serviceRegistrationMap = new HashMap<String, ServiceRegistration>();

    @Override
    public <T> boolean registerService(Class<T> serviceClass, T instance, Properties properties) {
        String serviceClassName = serviceClass.getName();
        ServiceRegistration sr = serviceRegistrationMap.get(serviceClassName);
        if (sr != null) {
            sr.unregister();
            serviceRegistrationMap.remove(serviceClassName);
        }
        sr = felix.getBundleContext().registerService(serviceClassName, instance, null);
        if (sr != null) {
            serviceRegistrationMap.put(serviceClassName, sr);
        }
        return sr != null;
    }

    @Override
    public <T> T loadService(Class<T> serviceClass) {
        return getServiceByReference(serviceClass, felix.getBundleContext());
    }

    @Override
    public <T> boolean removeService(Class<T> serviceClass) {
        String serviceClassName = serviceClass.getName();
        ServiceRegistration sr = serviceRegistrationMap.get(serviceClassName);
        if (sr != null) {
            sr.unregister();
            serviceRegistrationMap.remove(serviceClassName);
            return true;
        }
        return false;
    }

    public static <T> T getServiceByReference(Class<T> serviceClass, BundleContext context) {
        ServiceReference ref = context.getServiceReference(serviceClass.getName());
        return ref != null ? (T) context.getService(ref) : null;
    }

    @Override
    public InputStream loadResource(String bundleSymbolicName, String resourcePath) throws IOException {
        for (Bundle bundle : felix.getBundleContext().getBundles()) {
            if (!hasText(bundleSymbolicName) || !hasText(bundle.getSymbolicName()) || bundle.getSymbolicName().equals(bundleSymbolicName)) {
                InputStream is = OSGiBundleHelper.getBundleResourceStream(bundle, resourcePath);
                if (is != null) {
                    return is;
                }
            }
        }
        return null;
    }
    
    public synchronized List<PluginMetadata> getInstalledPlugins() throws ClassNotFoundException {
		Bundle[] bundles = felix.getBundleContext().getBundles();
		List<PluginMetadata> metadata = new ArrayList<PluginMetadata>();
		for (final Bundle bundle : bundles) {
			OSGiBundleHelper headerHelper = new OSGiBundleHelper(bundle);
			BundleMetadata bm = headerHelper.getBundleMetadata();

            bm.setId(bundle.getBundleId());
            bm.setState(bundle.getState());
            bm.setStateDescription("osgi.plugin.status." + getStatusDescription(bundle.getState()));
            bm.setSymbolicName(bundle.getSymbolicName());
            bm.setCanEnable(bundle.getBundleId() > 0 && bundle.getState() == Bundle.RESOLVED);
            bm.setCanDisable(bundle.getBundleId() > 0 && bundle.getState() == Bundle.ACTIVE);
            bm.setCanUninstall(bundle.getBundleId() > 0 && (bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.INSTALLED));
            bm.setVersion(bundle.getVersion().toString());

			if (headerHelper.hasHeaderValues(HUMAN_NAME)) {
				bm.setHumanNameKey(headerHelper.getHeaderValues(HUMAN_NAME)[0]);
			}
			if (headerHelper.hasHeaderValues(DESCRIPTION_KEY)) {
				bm.setDescriptionKey(headerHelper.getHeaderValues(DESCRIPTION_KEY)[0]);
			}
			if (headerHelper.hasHeaderValues(IMPLEMENTATION_BUILD)) {
				bm.setImplementationBuild(headerHelper.getHeaderValues(IMPLEMENTATION_BUILD)[0]);
			}
            if (headerHelper.hasHeaderValues(DESCRIPTION)) {
                bm.setDescription(headerHelper.getHeaderValues(DESCRIPTION)[0]);
            }
            if (headerHelper.hasHeaderValues(DOCUMENTATION_URL)) {
                bm.setDocumentationUrl(headerHelper.getHeaderValues(DOCUMENTATION_URL)[0]);
            }
            if (headerHelper.hasHeaderValues(HOMEPAGE_URL)) {
                bm.setHomepageUrl(headerHelper.getHeaderValues(HOMEPAGE_URL)[0]);
            }

            if (bundle.getState() == Bundle.ACTIVE) {
                if (headerHelper.hasHeaderValues(WIDGET_ENHANCEMENT)) {
                    for (String className : headerHelper.getHeaderValues(WIDGET_ENHANCEMENT)) {
                        bm.addWidgetClass(bundle.loadClass(className));
                    }
                }
                if (headerHelper.hasHeaderValues(STEP_ENHANCEMENT)) {
                    for (String className : headerHelper.getHeaderValues(STEP_ENHANCEMENT)) {
                        bm.addStepClass(bundle.loadClass(className));
                    }
                }
                if (headerHelper.hasHeaderValues(I18N_PROPERTY)) {
                    for (String property : headerHelper.getHeaderValues(I18N_PROPERTY)) {
                        PropertiesBasedI18NProvider provider = new PropertiesBasedI18NProvider(new PropertyLoader() {
                            @Override
                            public InputStream loadProperty(String path) throws IOException {
                                return getBundleResourceStream(bundle, path);
                            }
                        }, property);
                        bm.addI18NProvider(provider);
                    }
                }
                if (headerHelper.hasHeaderValues(ICON_RESOURCES)) {
                    String[] resources = headerHelper.getHeaderValues(ICON_RESOURCES);
                    for (String pack : resources) {
                        String basePath = File.separator + pack.replace(".", File.separator);
                        if (!basePath.endsWith(File.separator)) {
                            basePath += File.separator;
                        }
                        Enumeration<URL> urls = bundle.findEntries(basePath, null, true);
                        if (urls != null) {
                            while (urls.hasMoreElements()) {
                                bm.addIconResource(urls.nextElement());// .substring(basePath.length()));
                            }
                        }
                    }
                }
            }

			metadata.add(bm);
		}
		return metadata;
	}

    private String getStatusDescription(int state) {
        switch (state) {
            case Bundle.ACTIVE:
                return "active";
            case Bundle.INSTALLED:
                return "installed";
            case Bundle.RESOLVED:
                return "resolved";
            case Bundle.STARTING:
                return "starting";
            case Bundle.STOPPING:
                return "stopping";
            case Bundle.UNINSTALLED:
                return "uninstalled";
            default:
                return String.valueOf(state);
        }
    }
}

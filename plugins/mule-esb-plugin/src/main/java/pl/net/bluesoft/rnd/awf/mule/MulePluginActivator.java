package pl.net.bluesoft.rnd.awf.mule;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.*;
import pl.net.bluesoft.rnd.awf.mule.step.MuleStep;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * Created by IntelliJ IDEA.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class MulePluginActivator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(MulePluginActivator.class.getName());
    
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        try {
//            mulePluginManager.initialize();
            bundleContext.registerService(MulePluginManager.class.getName(),
                    MulePluginManager.instance(),
                    new Hashtable());

            getRegistry(bundleContext).getGuiRegistry().registerStep(MuleStep.class);
            bundleContext.addBundleListener(new BundleListener() {
                @Override
                public void bundleChanged(BundleEvent bundleEvent) {
                    processBundleExtensions(bundleEvent.getBundle(), bundleEvent.getType());
                }

                private void processBundleExtensions(final Bundle bundle, int state) {
                    String muleCfgs = (String) bundle.getHeaders().get("Mule-Config-Files");
                    if (hasText(muleCfgs)) {
                        String[] names = muleCfgs.split(",");
                        for (String name : names) {
                            name = name.trim();
                            if (state == BundleEvent.STARTED) {
                                try {
                                    InputStream is = bundle.getResource(name).openStream();

									MulePluginManager.instance().registerEntry(name, new ByteArrayInputStream(IOUtils.toByteArray(is)),
                                            new ClassLoader() {
                                                @Override
                                                public URL getResource(String s) {
                                                    return nvl(bundle.getResource(s),
                                                            MulePluginActivator.class.getClassLoader().getResource(s),
                                                            super.getResource(s)
                                                            );
                                                }

                                                @Override
                                                public Enumeration<URL> getResources(String s) throws IOException {
                                                    Enumeration resources = bundle.getResources(s);
                                                    Enumeration<URL> resources1 = MulePluginActivator.class.getClassLoader().getResources(s);
                                                    Enumeration<URL> resources2 = super.getResources(s);
                                                    Vector<URL> v = new Vector<URL>();
                                                    if (resources != null) while (resources.hasMoreElements()) {
                                                        v.add((URL) resources.nextElement());
                                                    }
                                                    if (resources1 != null) while (resources1.hasMoreElements()) {
                                                        v.add((URL) resources1.nextElement());
                                                    }
                                                    if (resources2 != null) while (resources2.hasMoreElements()) {
                                                        v.add((URL) resources2.nextElement());
                                                    }
                                                    return v.elements();
                                                }

                                                @Override
                                                public InputStream getResourceAsStream(String s) {
                                                    try {
                                                        URL resource = getResource(s);
                                                        if (resource != null) return resource.openStream();
                                                        return super.getResourceAsStream(s);
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }


                                                @Override
                                                public Class<?> loadClass(String s) throws ClassNotFoundException {
                                                    try {
                                                        Class aClass = bundle.loadClass(s);
                                                        if (aClass != null) return aClass;
                                                    } catch (Exception e) {
                                                        //ignore
                                                    }
                                                    try {
                                                        return MulePluginActivator.class.getClassLoader().loadClass(s);
                                                    } catch (Exception e) {
                                                        //ignore
                                                    }
                                                    return super.loadClass(s);
                                                }
                                            });
                                } catch (IOException e) {
                                    logger.log(Level.SEVERE, "Error registering entry in mule plugin manager", e);
                                }
                            } else if (state == BundleEvent.STOPPED) {
								MulePluginManager.instance().unregisterEntry(name);
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error starting bundle", e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        try {
			MulePluginManager.instance().shutdown();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error shutting down mule plugin manager", e);
        }

        getRegistry(bundleContext).getGuiRegistry().unregisterStep(MuleStep.class);
    }

    private ProcessToolRegistry getRegistry(BundleContext context) {
		ServiceReference ref = context.getServiceReference(ProcessToolRegistry.class.getName());
		return (ProcessToolRegistry) context.getService(ref);
	}

}

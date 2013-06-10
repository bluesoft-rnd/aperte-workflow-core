package org.aperteworkflow.scripting;

import static pl.net.bluesoft.util.lang.StringUtil.hasText;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;

import pl.net.bluesoft.rnd.util.func.Func;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/23/12
 * Time: 5:32 PM
 */
public class ScriptingActivator implements BundleActivator {

    private ScriptProcessorRegistry registry;
    private ServiceRegistration serviceRegistration;
    private static final Logger logger = Logger.getLogger(ScriptingActivator.class.getName());

    @Override
    public void start(BundleContext context) throws Exception {
        registry = new ScriptProcessorRegistry();
        serviceRegistration = context.registerService(ScriptProcessorRegistry.class.getName(), registry, null);
        context.addBundleListener(new BundleListener() {

            @Override
            public void bundleChanged(BundleEvent event) {
                processBundleExtensions(event.getBundle(),
                        event.getType(),
                        registry);
            }

            private void processBundleExtensions(Bundle bundle, int state, ScriptProcessorRegistry registry) {
                String scriptCfgs = (String) bundle.getHeaders().get("Script-Processor-Classes");
                if (hasText(scriptCfgs)) {
                    String[] names = scriptCfgs.split(",");
                    for (String name : names) {
                        name = name.trim();
                        if (state == BundleEvent.STARTED) {
                            try {

                                final Class<? extends ScriptProcessor> scriptProcessorClass
                                        = (Class<? extends ScriptProcessor>) bundle.loadClass(name);
                                final String finalName = name;
                                registry.registerProcessor(scriptProcessorClass.getSimpleName(), new Func<ScriptProcessor>() {
                                    @Override
                                    public ScriptProcessor invoke() {
                                        try {
                                            return scriptProcessorClass.newInstance();
                                        } catch (InstantiationException e) {
                                            logger.log(Level.SEVERE, e.getMessage(), e);
                                        } catch (IllegalAccessException e) {
                                            logger.log(Level.SEVERE, e.getMessage(), e);
                                        }
                                        logger.severe("Cannot register script processor: " + finalName);
                                        return null;
                                    }
                                });


                            } catch (Exception e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                            }


                        } else if (state == BundleEvent.UNINSTALLED) {
                            final Class<? extends ScriptProcessor> scriptProcessorClass;
                            try {
                                scriptProcessorClass = (Class<? extends ScriptProcessor>) bundle.loadClass(name);
                                registry.unregisterProcessor(scriptProcessorClass.getSimpleName());
                            } catch (ClassNotFoundException e) {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                                logger.severe("Cannot unregister script processor: " + name);
                            }

                        }
                    }
                }
            }

        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (serviceRegistration != null)
            serviceRegistration.unregister();
    }
}

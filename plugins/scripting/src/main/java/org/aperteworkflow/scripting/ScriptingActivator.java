package org.aperteworkflow.scripting;

import org.osgi.framework.*;
import pl.net.bluesoft.rnd.util.func.Func;

import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.StringUtil.hasText;

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
                String scriptCfgs = (String) bundle.getHeaders().get("Script-Processor-Config-Files");
                if (hasText(scriptCfgs)) {
                    String[] names = scriptCfgs.split(",");
                    for (String name : names) {
                        name = name.trim();
                        if (state == BundleEvent.STARTED) {
                            try {
                                final Class<? extends ScriptProcessor> scriptProcessorClass = (Class<? extends ScriptProcessor>) Class.forName(name);
                                final String finalName = name;
                                registry.registerProcessor(scriptProcessorClass.getSimpleName(), new Func<ScriptProcessor>() {
                                    @Override
                                    public ScriptProcessor invoke() {
                                        try {
                                            return scriptProcessorClass.newInstance();
                                        } catch (InstantiationException e) {
                                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                        }
                                        logger.severe("Cannot register script processor: " + finalName);
                                        return null;
                                    }
                                });


                            } catch (Exception e) {
//                          TODO: exception
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

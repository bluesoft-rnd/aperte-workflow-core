package pl.net.bluesoft.rnd.pt.ext.filescapture;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.filescapture.model.FilesCheckerConfiguration;
import pl.net.bluesoft.rnd.pt.ext.filescapture.model.FilesCheckerRuleConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Agata Taraszkiewicz
 */
public class Activator implements BundleActivator {
    boolean run = true;

    private final Logger logger = Logger.getLogger(Activator.class.getName());

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        final ProcessToolRegistry toolRegistry = getRegistry(bundleContext);
        toolRegistry.registerModelExtension(FilesCheckerConfiguration.class);
        toolRegistry.registerModelExtension(FilesCheckerRuleConfiguration.class);

		toolRegistry.commitModelExtensions();
//
//        toolRegistry.commitModelExtensions();
        new Thread(new Runnable() {

            @Override
            public void run() {
                run = true;
                while (run) {
                    try {
                        Thread.sleep(10000);
                        try {
                            toolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
                                @Override
                                public void withContext(ProcessToolContext ctx) 
                                {
                                   new FilesChecker(ctx).run();
                                }
                            });
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                    } catch (InterruptedException e) {
                        logger.log(Level.INFO, e.getMessage(), e);
                    }

                }
            }
        }).start();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        run = false;
    }

    private ProcessToolRegistry getRegistry(BundleContext context) {
		ServiceReference ref = context.getServiceReference(ProcessToolRegistry.class.getName());
		return (ProcessToolRegistry) context.getService(ref);
	}
}

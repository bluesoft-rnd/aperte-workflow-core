package pl.net.bluesoft.rnd.pt.ext.filescapture;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.filescapture.model.FilesCheckerConfiguration;
import pl.net.bluesoft.rnd.pt.ext.filescapture.model.FilesCheckerRuleConfiguration;

/**
 * Created by Agata Taraszkiewicz
 */
public class Activator implements BundleActivator 
{
	@Autowired
	private ProcessToolRegistry processToolRegistry;
	
    boolean run = true;

    private final Logger logger = Logger.getLogger(Activator.class.getName());

    @Override
    public void start(BundleContext bundleContext) throws Exception 
    {
    	SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    	
    	
    	processToolRegistry.registerModelExtension(FilesCheckerConfiguration.class);
    	processToolRegistry.registerModelExtension(FilesCheckerRuleConfiguration.class);

    	processToolRegistry.commitModelExtensions();
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
                        	processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
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
}

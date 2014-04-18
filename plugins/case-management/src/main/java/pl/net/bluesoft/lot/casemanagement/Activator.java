package pl.net.bluesoft.lot.casemanagement;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-04-18.
 */
public class Activator implements BundleActivator {
    @Autowired
    private ProcessToolRegistry processToolRegistry;

    private final Logger logger = Logger.getLogger(Activator.class.getName());

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        logger.info("Activating the case-management plugin");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        logger.info("Deactivating the case-management plugin");
    }
}

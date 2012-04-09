package org.aperteworkflow.view.impl.history;

import org.aperteworkflow.ui.view.ViewRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;


/**
 * @author tlipski@bluesoft.net.pl
 */
public class HistoryViewBundleActivator implements BundleActivator {


    protected ProcessToolRegistry getRegistry(BundleContext bundleContext) {
        ServiceReference ref = bundleContext.getServiceReference(ProcessToolRegistry.class.getName());
        return ref != null ? (ProcessToolRegistry) bundleContext.getService(ref) : null;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        ViewRegistry registeredService = getRegistry(bundleContext).getRegisteredService(ViewRegistry.class);
        if (registeredService != null) {
            registeredService.registerView(new ViewGeneratingFunction());
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}

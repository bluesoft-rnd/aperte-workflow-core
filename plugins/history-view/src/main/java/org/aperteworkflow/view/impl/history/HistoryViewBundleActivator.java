package org.aperteworkflow.view.impl.history;

import org.aperteworkflow.ui.view.ViewRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.HashMap;


/**
 * @author tlipski@bluesoft.net.pl
 */
public class HistoryViewBundleActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        ViewRegistry registeredService = (ViewRegistry)
                bundleContext.getService(bundleContext.getServiceReference(ViewRegistry.class.getName()));
        if (registeredService != null) {
            registeredService.registerView(new ViewGeneratingFunction());
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}

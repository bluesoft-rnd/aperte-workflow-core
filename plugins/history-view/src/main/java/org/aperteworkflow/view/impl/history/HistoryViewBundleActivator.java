package org.aperteworkflow.view.impl.history;

import org.aperteworkflow.ui.view.IViewRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class HistoryViewBundleActivator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
		IViewRegistry registeredService = getViewRegistry(bundleContext);
		if (registeredService != null) {
            registeredService.registerView(ViewGeneratingFunction.INSTANCE);
        }
    }

	@Override
    public void stop(BundleContext bundleContext) throws Exception {
		IViewRegistry registeredService = getViewRegistry(bundleContext);
		if (registeredService != null) {
			registeredService.unregisterView(ViewGeneratingFunction.INSTANCE);
		}
    }

	private IViewRegistry getViewRegistry(BundleContext bundleContext) {
		return (IViewRegistry)bundleContext.getService(bundleContext.getServiceReference(IViewRegistry.class.getName()));
	}
}

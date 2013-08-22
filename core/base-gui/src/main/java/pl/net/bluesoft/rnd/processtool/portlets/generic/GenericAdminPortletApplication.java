package pl.net.bluesoft.rnd.processtool.portlets.generic;

import org.aperteworkflow.ui.view.IViewRegistry;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.portlets.caches.CachesPortletRenderer;
import pl.net.bluesoft.rnd.processtool.portlets.report.ReportPortletRenderer;
import pl.net.bluesoft.rnd.processtool.ui.generic.GenericAdminPortletPanel;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 09:22
 */
public class GenericAdminPortletApplication extends GenericVaadinPortlet2BpmApplication {
	@Override
	protected void initializePortlet() {
		registerDefaultAdminPortlets();
	}

	@Override
	protected void renderPortlet() {
		getMainWindow().setContent(new GenericAdminPortletPanel(this, this, bpmSession, PortletKeys.ADMIN));
	}

	private synchronized void registerDefaultAdminPortlets() {
		getViewRegistry().registerGenericPortletViewRenderer(PortletKeys.ADMIN, CachesPortletRenderer.INSTANCE);
		getViewRegistry().registerGenericPortletViewRenderer(PortletKeys.ADMIN, ReportPortletRenderer.INSTANCE);
	}

	private IViewRegistry getViewRegistry() {
		return getRegistry().getRegisteredService(IViewRegistry.class);
	}
}

package pl.net.bluesoft.rnd.processtool.portlets.generic;

import org.aperteworkflow.ui.view.IViewRegistry;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.portlets.caches.CachesPortletRenderer;
import pl.net.bluesoft.rnd.processtool.ui.generic.GenericAdminPortletPanel;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

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
		getMainWindow().setContent(new GenericAdminPortletPanel(this, this, bpmSession, this, PortletKeys.ADMIN));
	}

	private synchronized void registerDefaultAdminPortlets() {
		getViewRegistry().registerGenericPortletViewRenderer(PortletKeys.ADMIN, CachesPortletRenderer.INSTANCE);
	}

	private IViewRegistry getViewRegistry() {
		return getThreadProcessToolContext().getRegistry().getRegisteredService(IViewRegistry.class);
	}
}

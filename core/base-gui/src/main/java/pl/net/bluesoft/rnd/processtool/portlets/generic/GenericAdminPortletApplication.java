package pl.net.bluesoft.rnd.processtool.portlets.generic;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.ui.generic.GenericAdminPortletPanel;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 09:22
 */
public class GenericAdminPortletApplication extends GenericVaadinPortlet2BpmApplication {
	@Override
	protected void initializePortlet() {
	}

	@Override
	protected void renderPortlet() {
		getMainWindow().setContent(new GenericAdminPortletPanel(this, this, bpmSession, this, PortletKeys.ADMIN));
	}
}

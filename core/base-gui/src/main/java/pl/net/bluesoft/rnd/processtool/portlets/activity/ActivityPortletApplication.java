package pl.net.bluesoft.rnd.processtool.portlets.activity;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.ui.activity.ActivityMainPane;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ActivityPortletApplication extends GenericVaadinPortlet2BpmApplication {

	ActivityMainPane amp;

	public ActivityPortletApplication() {
		loginRequired = true;
	}

	@Override
	protected void initializePortlet() {
		amp = new ActivityMainPane(this, this, bpmSession);
		getMainWindow().setContent(amp);
	}

	@Override
	protected void renderPortlet() {

	}
}

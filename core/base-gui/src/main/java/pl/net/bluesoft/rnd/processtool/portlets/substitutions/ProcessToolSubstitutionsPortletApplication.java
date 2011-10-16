package pl.net.bluesoft.rnd.processtool.portlets.substitutions;

import pl.net.bluesoft.rnd.processtool.ui.substitutions.SubstitutionsMainPane;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TasksMainPane;
import pl.net.bluesoft.rnd.util.vaadin.GenericVaadinPortlet2BpmApplication;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolSubstitutionsPortletApplication extends GenericVaadinPortlet2BpmApplication {
	public ProcessToolSubstitutionsPortletApplication() {
		loginRequired = true;
	}

	@Override
	protected void initializePortlet() {
		getMainWindow().setContent(new SubstitutionsMainPane(this, this, this));
	}

	@Override
	protected void renderPortlet() {
	}
}

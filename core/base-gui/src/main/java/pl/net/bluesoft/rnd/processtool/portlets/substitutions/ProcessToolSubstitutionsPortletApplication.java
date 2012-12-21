package pl.net.bluesoft.rnd.processtool.portlets.substitutions;

import com.vaadin.ui.Label;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.ui.substitutions.SubstitutionsMainPane;

/**
 * @author POlszewski
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

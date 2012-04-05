package pl.net.bluesoft.rnd.processtool.portlets.substitutions;

import com.vaadin.ui.Label;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.ui.substitutions.SubstitutionsMainPane;

/**
 * @author POlszewski
 */
public class ProcessToolSubstitutionsPortletApplication extends GenericVaadinPortlet2BpmApplication {
    private static final String SUBSTITUTION_ADMIN = "SUBSTITUTION_ADMIN";

	public ProcessToolSubstitutionsPortletApplication() {
		loginRequired = true;
	}

	@Override
	protected void initializePortlet() {
        if (userRoles.contains(SUBSTITUTION_ADMIN)) {
		getMainWindow().setContent(new SubstitutionsMainPane(this, this, this));
        }
        else {
            getMainWindow().addComponent(new Label(getMessage("substitutions.no.required.roles")));
        }
	}

	@Override
	protected void renderPortlet() {
	}
}

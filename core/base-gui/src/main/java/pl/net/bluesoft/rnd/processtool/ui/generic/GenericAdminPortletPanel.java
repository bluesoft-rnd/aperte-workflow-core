package pl.net.bluesoft.rnd.processtool.ui.generic;

import com.vaadin.Application;
import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.aperteworkflow.util.vaadin.TransactionProvider;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 20:47
 */
public class GenericAdminPortletPanel extends GenericPortletPanel {
	public GenericAdminPortletPanel(Application application, I18NSource i18NSource, ProcessToolBpmSession bpmSession,
									TransactionProvider transactionProvider, String portletKey) {
		super(application, i18NSource, bpmSession, transactionProvider, portletKey);
		buildView();
	}

	protected void buildView() {
		List<GenericPortletViewRenderer> permittedRenderers = getPermittedRenderers();

		if (permittedRenderers.size() > 1) {
			addComponent(renderTabSheet(permittedRenderers));
		}
		else {
			addComponent(renderVerticalLayout(permittedRenderers));
		}
	}
}

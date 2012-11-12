package pl.net.bluesoft.rnd.processtool.ui.generic;

import com.vaadin.Application;
import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.aperteworkflow.util.vaadin.TransactionProvider;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.List;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 20:48
 */
public class GenericUserPortletPanel extends GenericPortletPanel {
	private String[] viewKeys;

	public GenericUserPortletPanel(Application application, I18NSource i18NSource, ProcessToolBpmSession bpmSession,
								   TransactionProvider transactionProvider, String portletKey, String[] viewKeys) {
		super(application, i18NSource, bpmSession, transactionProvider, portletKey);
		this.viewKeys = Lang2.noCopy(viewKeys);
		buildView();
	}

	@Override
	protected void buildView() {
		List<GenericPortletViewRenderer> permittedRenderers = getPermittedRenderers();

		addComponent(renderVerticalLayout(permittedRenderers));
	}

	@Override
	protected boolean isPermitted(GenericPortletViewRenderer renderer) {
		return super.isPermitted(renderer) && from(viewKeys).contains(renderer.getKey());
	}
}

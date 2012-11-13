package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet;

import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.aperteworkflow.ui.view.RenderParams;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-07-31
 * Time: 13:44
 */
public class BpmAdminPortletRender implements GenericPortletViewRenderer {
	public static final BpmAdminPortletRender INSTANCE = new BpmAdminPortletRender();

	@Override
	public String getKey() {
		return "bpm-notifications";
	}

	@Override
	public String getName(I18NSource i18NSource) {
		return i18NSource.getMessage("bpmnot.bpmnotifications");
	}

	@Override
	public int getPosition() {
		return 100;
	}

	@Override
	public String[] getRequiredRoles() {
		return new String[] {};
	}

	@Override
	public Object render(RenderParams params) {
		return new BpmNotificationsAdminPanel(params.getI18NSource(), params.getContext().getRegistry());
	}
}

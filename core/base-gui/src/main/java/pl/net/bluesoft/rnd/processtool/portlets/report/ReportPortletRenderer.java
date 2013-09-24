package pl.net.bluesoft.rnd.processtool.portlets.report;

import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.aperteworkflow.ui.view.RenderParams;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2013-08-21
 * Time: 14:05
 */
public class ReportPortletRenderer implements GenericPortletViewRenderer {
	public static final ReportPortletRenderer INSTANCE = new ReportPortletRenderer();

	@Override
	public String getKey() {
		return "awf-reports";
	}

	@Override
	public String getName(I18NSource i18NSource) {
		return i18NSource.getMessage("AWF Reports");
	}

	@Override
	public int getPosition() {
		return 100;
	}

	@Override
	public String[] getRequiredRoles() {
		return new String[]{};
	}

	@Override
	public Object render(RenderParams params) {
		return new ReportsPanel(params.getI18NSource());
	}
}

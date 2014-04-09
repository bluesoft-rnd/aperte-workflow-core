package pl.net.bluesoft.rnd.processtool.portlets.report;

import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.aperteworkflow.ui.view.RenderParams;

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
	public String getName() {
		return "AWF Reports";
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

	@Override
	public String render() {
		return "ReportPortlet";
	}
}

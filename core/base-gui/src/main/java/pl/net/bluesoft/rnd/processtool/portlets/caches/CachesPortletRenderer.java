package pl.net.bluesoft.rnd.processtool.portlets.caches;

import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.aperteworkflow.ui.view.RenderParams;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 13:33
 */
public class CachesPortletRenderer implements GenericPortletViewRenderer {
	public static final CachesPortletRenderer INSTANCE = new CachesPortletRenderer();

	@Override
	public String getKey() {
		return "awf-caches";
	}

	@Override
	public String getName(I18NSource i18NSource) {
		return i18NSource.getMessage("AWF Caches");
	}

	@Override
	public int getPosition() {
		return 500;
	}

	@Override
	public String[] getRequiredRoles() {
		return new String[]{};
	}

	@Override
	public Object render(RenderParams params) {
		return new CachesPanel(params.getI18NSource());
	}
}

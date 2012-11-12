package org.aperteworkflow.ui.view;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 10:16
 */
public interface GenericPortletViewRenderer {
	String getKey();
	String getName(I18NSource i18NSource);
	int getPosition();
	String[] getRequiredRoles();
	Object render(RenderParams params);
}

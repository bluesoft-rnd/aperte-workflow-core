package org.aperteworkflow.ui.view;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 10:16
 */
public interface GenericPortletViewRenderer {
	String getKey();
	String getName();
	int getPosition();
	String[] getRequiredRoles();
	Object render(RenderParams params);
	String getCode();
}

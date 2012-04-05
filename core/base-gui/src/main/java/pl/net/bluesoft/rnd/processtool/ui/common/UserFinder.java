package pl.net.bluesoft.rnd.processtool.ui.common;

import pl.net.bluesoft.rnd.processtool.model.UserData;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;

/**
 * User: POlszewski
 * Date: 2012-01-16
 * Time: 10:25:29
 */
public interface UserFinder {
	UserData getUserByLogin(String login);
	UserData getUserByEmail(String email);
}

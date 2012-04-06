package pl.net.bluesoft.rnd.processtool.service;

import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * User: POlszewski
 * Date: 2012-01-16
 * Time: 10:25:29
 */
public interface UserFinder {
	UserData getUserByLogin(String login);
	UserData getUserByEmail(String email);
}

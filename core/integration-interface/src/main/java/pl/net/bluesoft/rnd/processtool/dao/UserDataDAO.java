package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface UserDataDAO extends HibernateBean<UserData> {

	UserData loadOrCreateUserByLogin(UserData ud);
	UserData loadUserByLogin(String login);
    Map<String, UserData> loadUsersByLogin(Collection<String> logins);
}

package pl.net.bluesoft.rnd.pt.ext.user.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.pt.ext.user.model.PersistentUserData;

import java.util.Collection;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface UserDataDAO extends HibernateBean<PersistentUserData> {

	PersistentUserData loadOrCreateUserByLogin(PersistentUserData ud);
	PersistentUserData loadUserByLogin(String login);
    Map<String, PersistentUserData> loadUsersByLogin(Collection<String> logins);
	
	/** Load user by its e-mail */
	PersistentUserData loadUserByEmail(String email);
}

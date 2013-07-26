package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2011-08-31
 * Time: 10:17:50
 */
public interface UserSubstitutionDAO extends HibernateBean<UserSubstitution> {
	List<String> getCurrentSubstitutedUserLogins(String userLogin);
	boolean isSubstitutedBy(String userLogin, String userSubstituteLogin);
	void deleteById(Long id);
}

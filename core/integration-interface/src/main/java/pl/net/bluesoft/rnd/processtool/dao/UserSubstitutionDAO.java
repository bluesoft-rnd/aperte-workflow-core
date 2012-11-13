package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;

import java.util.Date;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2011-08-31
 * Time: 10:17:50
 */
public interface UserSubstitutionDAO extends HibernateBean<UserSubstitution> {
    List<UserSubstitution> getActiveSubstitutions(UserData user, Date date);
	List<UserData> getSubstitutedUsers(UserData user, Date date);
	void deleteById(Long id);
	List<UserSubstitution> findAllEagerUserFetch();
}

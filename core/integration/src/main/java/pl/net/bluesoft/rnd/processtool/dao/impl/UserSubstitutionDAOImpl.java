package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;

import java.util.Date;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2011-08-31
 * Time: 10:18:37
 */
public class UserSubstitutionDAOImpl extends SimpleHibernateBean<UserSubstitution> implements UserSubstitutionDAO {
    public UserSubstitutionDAOImpl(Session hibernateSession) {
        super(hibernateSession);
    }

    @Override
    public List<String> getCurrentSubstitutedUserLogins(String userLogin)
    {
		String query = "select distinct us.userLogin from UserSubstitution us where us.userSubstituteLogin = :userLogin and :date between us.dateFrom and us.dateTo";
		return getSession().createQuery(query)
				.setParameter("userLogin", userLogin)
				.setParameter("date", new Date())
				.list();
    }

    @Override
	public void deleteById(Long id) {
		if (id == null) {
			return;
		}
		Session session = getSession();
		Object item = session.createQuery("from UserSubstitution where id = :id")
				.setParameter("id", id)
				.uniqueResult();
		if (item != null) {
			delete((UserSubstitution)item);
		}
	}
}

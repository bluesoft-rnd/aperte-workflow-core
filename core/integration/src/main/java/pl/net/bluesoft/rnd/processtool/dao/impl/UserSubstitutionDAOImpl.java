package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.FetchMode;
import org.hibernate.SQLQuery;
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
	public List<String> getSubstitutedUserLogins(String userLogin, Date date) {
		Session session = getSession();
		return session.createQuery("select distinct us.userLogin from UserSubstitution us where us.userSubstituteLogin = :userLogin and :date between us.dateFrom and us.dateTo")
				.setParameter("userLogin", userLogin)
				.setParameter("date", date)
				.list();
	}

    @Override
    public List<String> getCurrentSubstitutedUserLogins(String userLogin)
    {
        SQLQuery query = getSession().createSQLQuery("select ud.login from pt_user_substitution sub left join pt_user_data ud on " +
                "ud.id = sub.user_id " +
                "where sub.user_substitute_id = (select id from  pt_user_data where login = '"+userLogin+"') and " +
                "now() between sub.dateFrom and sub.dateTo") ;

        return query.list();
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

	@Override
	public List<UserSubstitution> findAllEagerUserFetch() {
		return findByCriteria(getDetachedCriteria());
	}
}

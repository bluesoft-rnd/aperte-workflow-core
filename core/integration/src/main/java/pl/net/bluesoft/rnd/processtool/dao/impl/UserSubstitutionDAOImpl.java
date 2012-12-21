package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserData;
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
    public List<UserSubstitution> getActiveSubstitutions(UserData user, Date date) {
        Session session = getSession();
        return session.createQuery("from UserSubstitution where userSubstitute = :user and :date between dateFrom and dateTo")
            .setParameter("user", user)
            .setParameter("date", date)
            .list();                       
//        return session.createCriteria(UserSubstitution.class)
//                .add(eq("userSubstitute", user))
//                .add(ge("dateFrom", date))
//                .add(le("dateTo", date))
//                .list();
    }

	@Override
	public List<UserData> getSubstitutedUsers(UserData user, Date date) {
		Session session = getSession();
		return session.createQuery("select distinct us.user from UserSubstitution us where us.userSubstitute = :user and :date between us.dateFrom and us.dateTo")
				.setParameter("user", user)
				.setParameter("date", date)
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

	@Override
	public List<UserSubstitution> findAllEagerUserFetch() {
		DetachedCriteria criteria = getDetachedCriteria()
				.setFetchMode(UserSubstitution._USER, FetchMode.JOIN)
				.setFetchMode(UserSubstitution._USER_SUBSTITUTE, FetchMode.JOIN);
		return findByCriteria(criteria);
	}
}

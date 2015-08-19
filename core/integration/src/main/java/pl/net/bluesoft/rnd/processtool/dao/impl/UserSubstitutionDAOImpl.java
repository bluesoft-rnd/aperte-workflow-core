package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;
import pl.net.bluesoft.util.lang.ExpiringCache;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2011-08-31
 * Time: 10:18:37
 */
public class UserSubstitutionDAOImpl extends SimpleHibernateBean<UserSubstitution> implements UserSubstitutionDAO {
	private static final ExpiringCache<String, List<String>> currentSubstitutedUserLogins = new ExpiringCache<String, List<String>>(10 * 60 * 1000);

    public UserSubstitutionDAOImpl(Session hibernateSession) {
        super(hibernateSession);
    }

    @Override
    public List<String> getCurrentSubstitutedUserLogins(final String userLogin)
    {
		return currentSubstitutedUserLogins.get(userLogin, new ExpiringCache.NewValueCallback<String, List<String>>() {
			@Override
			public List<String> getNewValue(String key) {
				String query = "select distinct us.userLogin from UserSubstitution us where us.userSubstituteLogin = :userSubstituteLogin and :date between us.dateFrom and us.dateTo";
				return getSession().createQuery(query)
						.setParameter("userSubstituteLogin", userLogin)
						.setParameter("date", new Date())
						.list();
			}
		});
    }

	@Override
	public boolean isSubstitutedBy(String userLogin, String userSubstituteLogin) {
		return getCurrentSubstitutedUserLogins(userSubstituteLogin).contains(userLogin);
	}

	@Override
	public void saveOrUpdate(UserSubstitution object) {
		super.saveOrUpdate(object);
		currentSubstitutedUserLogins.clear();
	}

	@Override
	public void saveOrUpdate(Collection<UserSubstitution> objects) {
		super.saveOrUpdate(objects);
		currentSubstitutedUserLogins.clear();
	}

	@Override
	public void delete(Collection<UserSubstitution> objects) {
		super.delete(objects);
		currentSubstitutedUserLogins.clear();
	}

	@Override
	public void delete(UserSubstitution object) {
		super.delete(object);
		currentSubstitutedUserLogins.clear();
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
			currentSubstitutedUserLogins.clear();
		}
	}
}

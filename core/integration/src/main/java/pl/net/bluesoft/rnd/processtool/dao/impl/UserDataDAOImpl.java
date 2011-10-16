package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.dao.UserDataDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.List;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class UserDataDAOImpl extends SimpleHibernateBean<UserData> implements UserDataDAO {

	public UserDataDAOImpl(Session session) {
		super(session);
	}

	@Override
	public UserData loadOrCreateUserByLogin(UserData ud) {
		if (session.contains(ud))
			return ud;
		
		if (ud.getId() != null) {
			return (UserData) session.get(UserData.class, ud.getId());
		} else {
			List users = session.createCriteria(UserData.class).add(eq("login", ud.getLogin())).list();
			if (users.isEmpty()) {
				session.saveOrUpdate(ud);
				return ud;
			} else {
				return (UserData) users.get(0);
			}

		}

	}

	@Override
	public UserData loadUserByLogin(String login) {
		List users = session.createCriteria(UserData.class).add(eq("login", login)).list();
		if (users.isEmpty()) return null;
		else return (UserData) users.get(0);
	}
}

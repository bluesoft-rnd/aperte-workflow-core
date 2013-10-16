package pl.net.bluesoft.rnd.pt.ext.user.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.PagedCollection;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.pt.ext.user.model.PersistentUserData;

import java.util.*;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class UserDataDAOImpl extends SimpleHibernateBean<PersistentUserData> implements UserDataDAO
{
	private static final int PAGE_LENGTH = 500;

	public UserDataDAOImpl(Session session) {
		super(session);
	}

	@Override
	public PersistentUserData loadOrCreateUserByLogin(PersistentUserData ud) {
		Session session = getSession();

		if (session.contains(ud)) {
			return ud;
		}
		if (ud.getId() != null) {
			return (PersistentUserData) session.get(PersistentUserData.class, ud.getId());
		}
		else {
			PersistentUserData user = loadUserByLogin(ud.getLogin());

			if (user == null) {
				session.saveOrUpdate(ud);
				return ud;
			}
			else {
				return user;
			}
		}
	}

	@Override
	public PersistentUserData loadUserByLogin(String login) {
		DetachedCriteria criteria = getDetachedCriteria().setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return findUnique(criteria, eq("login", login));
	}

	@Override
	public Map<String, PersistentUserData> loadUsersByLogin(Collection<String> logins)
	{
		/* Logins size is smaller then max page size, no need to advanced processing */
		if(logins.size() <= PAGE_LENGTH)
			return loadUsersPageByLogin(logins);

		Map<String, PersistentUserData> users = new HashMap<String, PersistentUserData>(logins.size());

		PagedCollection<String> pagedCollection = new PagedCollection<String>(logins);
    	  
    	/* Iterate through pages */
		while(pagedCollection.hasMoreElements())
		{
			Collection<String> loginsPage = pagedCollection.getNextPage();
			Map<String, PersistentUserData> usersPage = loadUsersPageByLogin(loginsPage);

			users.putAll(usersPage);
		}
		return users;
	}

	private Map<String, PersistentUserData> loadUsersPageByLogin(Collection<String> logins)
	{
		if (logins == null || logins.isEmpty()) {
			return Collections.emptyMap();
		}

		final List<PersistentUserData> users = findByCriteria(getDetachedCriteria()
				.add(Restrictions.in("login", logins))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));

		return new HashMap<String, PersistentUserData>(users.size()) {{
			for (PersistentUserData user : users) {
				put(user.getLogin(), user);
			}
		}};
	}

	@Override
	public PersistentUserData loadUserByEmail(String userEmail)
	{
		DetachedCriteria criteria = getDetachedCriteria().setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return findUnique(criteria, eq("email", userEmail));
	}
}

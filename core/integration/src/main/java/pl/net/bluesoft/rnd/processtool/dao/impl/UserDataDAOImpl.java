package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.UserDataDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.*;

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
          Session session = getSession();
          if (session.contains(ud)) {
              return ud;
          }

          if (ud.getId() != null) {
              return (UserData) session.get(UserData.class, ud.getId());
          }
          else {
              List users = session.createCriteria(UserData.class).add(eq("login", ud.getLogin())).list();
              if (users.isEmpty()) {
                  session.saveOrUpdate(ud);
                  return ud;
              }
              else {
                  return (UserData) users.get(0);
              }
          }
      }

      @Override
      public UserData loadUserByLogin(String login) {
          DetachedCriteria criteria = getDetachedCriteria().setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
          return findUnique(criteria, eq("login", login));
      }

      @Override
      public void saveOrUpdate(UserData object) {
          super.saveOrUpdate(object);
          for (UserAttribute a : object.getOrphans()) {
              session.delete(a);
          }
          object.removeAllOrphans();
      }

      @Override
      public Map<String, UserData> loadUsersByLogin(Collection<String> logins) {
          final List<UserData> users = findByCriteria(getDetachedCriteria()
                  .add(Restrictions.in("login", logins))
                  .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
          return new HashMap<String, UserData>(users.size()) {{
              for (UserData user : users) {
                  put(user.getLogin(), user);
              }
          }};
      }
}

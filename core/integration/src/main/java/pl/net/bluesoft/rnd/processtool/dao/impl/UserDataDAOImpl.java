package pl.net.bluesoft.rnd.processtool.dao.impl;

import static org.hibernate.criterion.Restrictions.eq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.dao.PagedCollection;
import pl.net.bluesoft.rnd.processtool.dao.UserDataDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class UserDataDAOImpl extends SimpleHibernateBean<UserData> implements UserDataDAO 
{
	private static final int PAGE_LENGTH = 500;

    public UserDataDAOImpl(Session session) {
          super(session);
      }
    
    @Override
    public UserData findOrCreateUser(UserData ud) {
    	
//    	 long start = System.currentTimeMillis();
        UserData user = loadUserByLogin(ud.getLogin());
        if (user == null) {
        	Session session = getSession();
            session.saveOrUpdate(ud);
            user = ud;
        }
        
//        long duration = System.currentTimeMillis() - start;
//		logger.severe("findOrCreateUser: " +  duration);
        
        
        return user;
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
    	  
//    	  long start = System.currentTimeMillis();
          DetachedCriteria criteria = getDetachedCriteria().setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
           UserData findUnique = findUnique(criteria, eq("login", login));
//           long duration = System.currentTimeMillis() - start;
//			logger.severe("loadUserByLogin: " +  duration);
			
			return findUnique;
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
      public Map<String, UserData> loadUsersByLogin(Collection<String> logins) 
      {
    	  /* Logins size is smaller then max page size, no need to advanced 
    	   * processing */
    	  if(logins.size() <= PAGE_LENGTH)
    		  return loadUsersPageByLogin(logins);
    	  
    	  Map<String, UserData> users = new HashMap<String, UserData>(logins.size());
    	  
    	  PagedCollection<String> pagedCollection = new PagedCollection<String>(logins);
    	  
    	  /* Iterate through pages */
    	  while(pagedCollection.hasMoreElements())
    	  {
    		  Collection<String> loginsPage = pagedCollection.getNextPage();
    		  Map<String, UserData> usersPage = loadUsersPageByLogin(loginsPage);
    		  
    		  users.putAll(usersPage);
    	  }
    	  
    	  return users;
    	      	      	 
      }
      
      private Map<String, UserData> loadUsersPageByLogin(Collection<String> logins)
      {
          final List<UserData> users = findByCriteria(getDetachedCriteria()
                  .add(Restrictions.in("login", logins))
                  .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));
          
          return new HashMap<String, UserData>(users.size()) {{
              for (UserData user : users) {
                  put(user.getLogin(), user);
              }
          }};
      }

	@Override
	public UserData loadUserByEmail(String userEmail) 
	{
        DetachedCriteria criteria = getDetachedCriteria().setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return findUnique(criteria, eq("email", userEmail));
	}
}

package pl.net.bluesoft.rnd.pt.ext.user.dao;

import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.pt.ext.user.model.UserRole;

import java.util.List;


/**
 * 
 * User Role dao implementation
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class UserRoleDAOImpl extends SimpleHibernateBean<UserRole> implements UserRoleDAO
{

	public UserRoleDAOImpl(Session hibernateSession) {
		super(hibernateSession);
	}

	@Override
	public UserRole getUserRoleByName() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserRole> getAll() 
	{
		// TODO Auto-generated method stub
		return null;
	}
}

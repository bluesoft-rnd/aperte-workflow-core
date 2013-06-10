package pl.net.bluesoft.rnd.processtool.dao.impl;

import java.util.List;

import org.hibernate.Session;

import pl.net.bluesoft.rnd.processtool.dao.UserRoleDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserRole;


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

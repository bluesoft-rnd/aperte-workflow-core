package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserRole;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Dao for user role operations
 * 
 * @author mpawlak@bluesoft.net.pl
 */
public interface UserRoleDAO extends HibernateBean<UserRole> 
{
	UserRole getUserRoleByName();
	List<UserRole> getAll();
}

package pl.net.bluesoft.rnd.pt.ext.user.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.pt.ext.user.model.PersistentUserRole;

import java.util.List;

/**
 * Dao for user role operations
 * 
 * @author mpawlak@bluesoft.net.pl
 */
public interface UserRoleDAO extends HibernateBean<PersistentUserRole>
{
	PersistentUserRole getUserRoleByName();
	List<PersistentUserRole> getAll();
}

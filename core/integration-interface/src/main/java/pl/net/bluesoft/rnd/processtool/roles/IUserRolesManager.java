package pl.net.bluesoft.rnd.processtool.roles;

import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.roles.exception.RoleCreationExceptionException;
import pl.net.bluesoft.rnd.processtool.roles.exception.RoleNotFoundException;
import pl.net.bluesoft.rnd.processtool.roles.exception.UserWithRoleNotFoundException;

/**
 * Interface for user roles operations
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IUserRolesManager 
{
	/**
	 * Check if role with given name exists
	 * 
	 * @param roleName
	 * @return true if exists
	 */
	boolean isRoleExist(String roleName);
	
	/**
	 * Create new role 
	 * 
	 * @param roleName new role name
	 * @param description description for role
	 * @throws RoleCreationExceptionException thrown if role already exists
	 */
	void createRole(String roleName, String description) throws RoleCreationExceptionException;
	
	/** 
	 * Update role description
	 * 
	 * @param roleName
	 * @param description
	 */
	void updateRoleDescription(String roleName, String description) throws RoleNotFoundException;

	/** Get all role names for given company ID 
	 * 
	 * @param companyId id of the company
	 * @return
	 */
	Collection<String> getRoleNamesForCompanyId(Long companyId);
	
	/** 
	 * Get all users by given role name
	 * @param roleName
	 * @return
	 */
	Collection<UserData> getUsersByRole(String roleName) throws RoleNotFoundException;
	
	/**
	 * Get random first user with given role name
	 * @param roleName
	 * @return
	 */
	UserData getFirstUserWithRole(String roleName) throws RoleNotFoundException, UserWithRoleNotFoundException;

	/**
	 * Get all available role names
	 * 
	 * @return
	 */
	Collection<String> getAllRolesNames();
	
	
}

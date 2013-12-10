package pl.net.bluesoft.rnd.processtool.roles.impl;

import com.liferay.portal.NoSuchRoleException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import org.aperteworkflow.integration.liferay.utils.LiferayUserConverter;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.roles.exception.RoleCreationExceptionException;
import pl.net.bluesoft.rnd.processtool.roles.exception.RoleNotFoundException;
import pl.net.bluesoft.rnd.processtool.roles.exception.UserWithRoleNotFoundException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LiferayUserRolesManager implements IUserRolesManager 
{
    private static final Logger logger = Logger.getLogger(LiferayUserRolesManager.class.getName());

	@Override
	public boolean isRoleExist(String roleName) 
	{
		/* Look for the role with provided name */
    	Role role = null;
    	try
    	{
    		/* Look for the role with provided name */
    		role = getRoleByName(roleName);
    		
    		return role != null;
    	}
    	catch(RoleNotFoundException ex)
    	{
			return false;
    	}
	}
	
	@Override
	public void updateRoleDescription(String roleName, String description) 
	{
    	try 
    	{
    		/* Look for the role with provided name */
        	Role role = null;
        	try
        	{
        		/* Look for the role with provided name */
        		role = getRoleByName(roleName);
        	}
        	catch(RoleNotFoundException ex)
        	{
				throw new RoleNotFoundException("Role not found: "+roleName, ex);
        	}

			/* Role found, maybe there is need to update description */
			if(description != null && !description.equals(role.getDescription()))
			{
				role.setDescription(description);
				RoleLocalServiceUtil.updateRole(role);
			}
		}
		catch (Exception e) 
		{
			throw new RoleNotFoundException(e);
		}
		
	}

	@Override
	public void createRole(String roleName, String description) 
	{
		
		try 
		{
			Map<Locale, String> titles = new HashMap<Locale,  String>();
            Map<Locale, String> descriptionMap = new HashMap<Locale,  String>();
            descriptionMap.put(LocaleUtil.getDefault(), description);

            RoleLocalServiceUtil.addRole(0,PortalUtil.getDefaultCompanyId(), roleName,
                    titles, descriptionMap, RoleConstants.TYPE_REGULAR);
		} 
		catch (PortalException e) 
		{
			throw new RoleCreationExceptionException("Problem during new role creation", e);
		} 
		catch (SystemException e) 
		{
			throw new RoleCreationExceptionException("Problem during new role creation", e);
		}
	}

	@Override
	public Collection<String> getRoleNamesForCompanyId(Long companyId) 
	{
		List<String> roleNames = new ArrayList<String>();
		try {
			List<Role> roles = RoleLocalServiceUtil.getRoles(companyId);
			for (Role r : roles) {
				roleNames.add(r.getName());
			}
		} 
		catch (SystemException e) 
		{
			logger.log(Level.SEVERE, "Error getting liferay roles", e);
		}
		return roleNames;
	}

	@Override
	public Collection<UserData> getUsersByRole(String roleName) throws RoleNotFoundException
	{
        try 
        {
        	/* Search for role with given name */
        	Role role = getRoleByName(roleName);
        	
        	List<User> liferayUsers = new ArrayList<User>();
        	
        	/* Get users with directly assigned role */
        	liferayUsers.addAll(UserLocalServiceUtil.getRoleUsers(role.getRoleId()));
        	
        	/* Get user groups with assigned role */
        	List<Group> liferayGroups = GroupLocalServiceUtil.getRoleGroups(role.getRoleId());
        	
	
        	for(Group liferayGroup: liferayGroups)
        	{    		
        		/* Get all users from selected group. ClassPK is the id of the UserGroup instance
        		 * Group is something diffrent then UserGroup and there are no users in Group */
        		List<User> groupUsers = UserLocalServiceUtil.getUserGroupUsers(liferayGroup.getClassPK());
        		
        		/* If there is no user from group in liferay users list yet, add him */
        		for(User groupLiferayUser: groupUsers)
        			if(!liferayUsers.contains(groupLiferayUser))
        				liferayUsers.add(groupLiferayUser);
        	}
        	
        	
        	List<UserData> userDatas = new ArrayList<UserData>();
        	
        	/* Map all liferay users to aperte users */
        	for(User liferayUser: liferayUsers)
        		userDatas.add(LiferayUserConverter.convertLiferayUser(liferayUser));
        	
        	
        	return userDatas;
        }
        catch (SystemException e) 
        {
            throw new UserWithRoleNotFoundException(e);
        } 
	}

	@Override
	public Collection<String> getAllRolesNames() 
	{
		List<String> roleNames = new ArrayList<String>();
		try {
			List<Role> roles = RoleLocalServiceUtil.getRoles(PortalUtil.getDefaultCompanyId());
			for (Role r : roles) {
				if (r.getType() == RoleConstants.TYPE_REGULAR) {
					roleNames.add(r.getName());
				}
			}
		}
		catch (SystemException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return roleNames;
	}

    /** Find Liferay role by given name. The role is searched in all company ids */
    protected Role getRoleByName(String roleName) throws RoleNotFoundException
    {
    	try
    	{
    		/* Search for role in all companies. There is commonly only one company in system */
	        long[] companyIds = PortalUtil.getCompanyIds();
	        for (int i = 0; i < companyIds.length; ++i) 
	        {
	        	long companyId = companyIds[i];
	        	Role role = RoleLocalServiceUtil.getRole(companyId, roleName);
	        	
	        	if(role != null)
	        		return role;
	        }
	        
	        /* No role with given name found, throw exception */
	        throw new RoleNotFoundException("No role found with given name: "+roleName);
    	}
    	catch(NoSuchRoleException ex)
    	{
    		throw new RoleNotFoundException("No role found with given name: "+roleName);
    	}
    	catch (PortalException e) 
    	{
    		throw new RoleNotFoundException("Error during role ["+roleName+"]", e);
		} 
    	catch (SystemException e) 
    	{
    		throw new RoleNotFoundException("Error during role ["+roleName+"]", e);
		}
    }

	@Override
	public UserData getFirstUserWithRole(String roleName) throws RoleNotFoundException, UserWithRoleNotFoundException 
	{
		Collection<UserData> usersWithRole = getUsersByRole(roleName);
		for(UserData user: usersWithRole)
			return user;
		
		/* No users, throw an exception */
		throw new UserWithRoleNotFoundException("No user with role "+roleName+" was found");
	}

}

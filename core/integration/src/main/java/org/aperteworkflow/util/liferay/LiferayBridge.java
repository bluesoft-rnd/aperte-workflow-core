package org.aperteworkflow.util.liferay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.aperteworkflow.util.liferay.exceptions.RoleNotFoundException;

import pl.net.bluesoft.rnd.processtool.model.UserAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;

import com.liferay.portal.NoSuchRoleException;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserGroupLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

/**
 * Facade for all Liferay operations
 *
 * @author tlipski@bluesoft.net.pl, mpawlak@bluesoft.net.pl
 */
public class LiferayBridge {
    private static final Logger logger = Logger.getLogger(LiferayBridge.class.getName());

    public static class LiferayBridgeException extends RuntimeException {
        public LiferayBridgeException() {
        }
        public LiferayBridgeException(String message) {
            super(message);
        }
        public LiferayBridgeException(String message, Throwable cause) {
            super(message, cause);
        }
        public LiferayBridgeException(Throwable cause) {
            super(cause);
        }
    }

    public static UserData convertLiferayUser(User user) throws SystemException {
        if (user == null) {
            return null;
        }
        UserData ud = new UserData();
        ud.setEmail(user.getEmailAddress());
        ud.setLogin(user.getScreenName());
        ud.setFirstName(user.getFirstName());
        ud.setLastName(user.getLastName());
        ud.setJobTitle(user.getJobTitle());
        ud.setCompanyId(user.getCompanyId());
        ud.setLiferayUserId(user.getUserId());
        for (Role role : user.getRoles()) 
        {
            ud.addRoleName(role.getName(), role.getDescription());
        }
        setGroupRoles(ud, user);
        return ud;
    }
    
    private static void setGroupRoles(UserData ud, User user) {
        try {
            for (UserGroup userGroup : user.getUserGroups()) {
                List<Role> roles = RoleLocalServiceUtil.getGroupRoles(userGroup.getGroup().getGroupId());
                for (Role role : roles) {
                    ud.addRoleName(role.getName());
                }
            }
        }
        catch (Exception e) {
            throw new LiferayBridgeException(e);
        }
    }

    public static UserData getLiferayUser(String login, Long companyId) {
        try {
            User user = UserLocalServiceUtil.getUserByScreenName(companyId, login);
            return convertLiferayUser(user);
        }
        catch (Exception e) {
            throw new LiferayBridgeException(e);
        }
    }

    public static UserData getLiferayUser(final String login) {
        if (login == null) {
            return null;
        }
        try {
            long[] companyIds = PortalUtil.getCompanyIds();
            for (int i = 0; i < companyIds.length; ++i) {
                long ci = companyIds[i];
                try {
                    User u = UserLocalServiceUtil.getUserByScreenName(ci, login);
                    if (u != null) {
                        return convertLiferayUser(u);
                    }
                }
                catch (NoSuchUserException e) {
                    // continue
                }
            }
            return null;
        }
        catch (Exception e) {
            throw new LiferayBridgeException(e);
        }
    }

    public static UserData getLiferayUserByEmail(String email, Long companyId) {
        try {
            User user = UserLocalServiceUtil.getUserByEmailAddress(companyId, email);
            return convertLiferayUser(user);
        }
        catch (Exception e) {
            throw new LiferayBridgeException(e);
        }
    }

    public static UserData getLiferayUserByAttributeNoException(final UserAttribute attribute, Long companyId) {
        try {
            return getLiferayUserByAttribute(attribute, companyId);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static UserData getLiferayUserByAttribute(final UserAttribute attribute, Long companyId) {
        try {
            return Collections.firstMatching(getUsersByCompanyId(companyId), new Predicate<UserData>() {
                @Override
                public boolean apply(UserData input) {
                    UserAttribute userAttribute = input.findAttribute(attribute.getKey());
                    return userAttribute != null && userAttribute.getValue().equals(attribute.getValue());
                }
            });
        }
        catch (Exception e) {
            throw new LiferayBridgeException(e);
        }
    }

	public static UserData getLiferayUserByEmailNoException(String email, Long companyId) {
        try {
            User user = UserLocalServiceUtil.getUserByEmailAddress(companyId, email);
            return convertLiferayUser(user);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static List<UserData> getAllUsersByCurrentUser(UserData currentUser) {
        try {
            if (currentUser == null) {
                return new ArrayList<UserData>(0);
            }
            return convertLiferayUsers(UserLocalServiceUtil.getCompanyUsers(currentUser.getCompanyId(), 0, Integer.MAX_VALUE));
        }
        catch (SystemException e) {
            throw new LiferayBridgeException(e);
        }
    }

    public static List<UserData> getUsersByCompanyId(Long companyId) {
        try {
            if (companyId == null) {
                return new ArrayList<UserData>(0);
            }
            return convertLiferayUsers(UserLocalServiceUtil.getCompanyUsers(companyId, 0, Integer.MAX_VALUE));
        }
        catch (SystemException e) {
            throw new LiferayBridgeException(e);
        }
    }

    public static List<UserData> getAllUsers() {
        try {
            return convertLiferayUsers(UserLocalServiceUtil.getUsers(0, UserLocalServiceUtil.getUsersCount()));
        }
        catch (SystemException e) {
            throw new LiferayBridgeException(e);
        }
    }

    public static List<UserData> getLiferayUsers(final Collection<String> logins) {
        List<UserData> result = new LinkedList<UserData>();
        if (logins != null && !logins.isEmpty()) {
            for (String login : logins) {
                UserData user = getLiferayUser(login);
                if (user != null) {
                    result.add(user);
                }
            }
        }
        return result;
    }

    private static List<UserData> convertLiferayUsers(List<User> liferayUsers) throws SystemException {
        List<UserData> users = new ArrayList<UserData>(liferayUsers.size());
        for (User liferayUser : liferayUsers) {
            users.add(convertLiferayUser(liferayUser));
        }
        return users;
    }

    /** Get all users with given role name. The role must be assigned directly to user
     * or to user group which contains users
     */
    public static List<UserData> getUsersByRole(String roleName) 
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
        		userDatas.add(convertLiferayUser(liferayUser));
        	
        	
        	return userDatas;
        }
        catch (SystemException e) 
        {
            throw new LiferayBridgeException(e);
        } 
        catch (RoleNotFoundException ex) 
        {
        	throw new LiferayBridgeException(ex);
		} 
    }
    
    /** Find Liferay role by given name. The role is searched in all company ids */
    public static Role getRoleByName(String roleName) throws RoleNotFoundException
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
    
    public static boolean createRoleIfNotExists(String roleName, String description) 
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
				Map<Locale, String> titles = new HashMap<Locale,  String>();
				RoleLocalServiceUtil.addRole(0, PortalUtil.getDefaultCompanyId(), roleName, titles, description, RoleConstants.TYPE_REGULAR);
				return true;
        	}

			/* Role found, maybe there is need to update description */
			if(description != null && !description.equals(role.getDescription()))
			{
				role.setDescription(description);
				RoleLocalServiceUtil.updateRole(role);
			}
			return false;
		}
		catch (Exception e) 
		{
			throw new LiferayBridgeException(e);
		}
    }
}

package org.aperteworkflow.util.liferay;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import pl.net.bluesoft.rnd.processtool.model.UserAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Mapcar;
import pl.net.bluesoft.util.lang.Predicate;

import java.util.*;
import java.util.logging.Logger;

/**
 * Utility methods for integration with Liferay.
 *
 * @author tlipski@bluesoft.net.pl
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
        for (Role role : user.getRoles()) {
            ud.addRoleName(role.getName());
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

    public static List<UserData> getUsersByRole(final String roleName) {
        try {
            List<User> liferayUsers = UserLocalServiceUtil.getUsers(0, UserLocalServiceUtil.getUsersCount());
            return new Mapcar<User, UserData>(liferayUsers) {
                @Override
                public UserData lambda(User x) {
                    try {
                        for (Role role : x.getRoles()) {
                            if (role.getName().equals(roleName)) {
                                return convertLiferayUser(x);
                            }
                        }
                    }
                    catch (SystemException e) {
                        throw new LiferayBridgeException(e);
                    }
                    return null;
                }
            }.go();
        }
        catch (SystemException e) {
            throw new LiferayBridgeException(e);
        }
    }
    
    public boolean createRoleIfNotExists(String roleName, String description) {
    	try {
			int cnt = RoleLocalServiceUtil.searchCount(PortalUtil.getDefaultCompanyId(), roleName, null, null);
			if (cnt == 0) {
				Map<Locale, String> titles = new HashMap<Locale,  String>();
				RoleLocalServiceUtil.addRole(0, PortalUtil.getDefaultCompanyId(), roleName, titles, description, RoleConstants.TYPE_REGULAR);
				return true;
			}
			return false;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}

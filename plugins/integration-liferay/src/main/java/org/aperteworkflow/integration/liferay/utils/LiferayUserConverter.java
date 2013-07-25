package org.aperteworkflow.integration.liferay.utils;

import java.util.ArrayList;
import java.util.List;

import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserDataBean;
import pl.net.bluesoft.rnd.processtool.usersource.exception.UserSourceException;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.service.RoleLocalServiceUtil;

/**
 * Liferay {@link User} to {@link UserData} converter
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class LiferayUserConverter 
{
    public static UserData convertLiferayUser(User user) throws SystemException {
        if (user == null) {
            return null;
        }
		UserDataBean ud = new UserDataBean();
        ud.setEmail(user.getEmailAddress());
        ud.setLogin(user.getScreenName());
        ud.setFirstName(user.getFirstName());
        ud.setLastName(user.getLastName());
        ud.setJobTitle(user.getJobTitle());
        ud.setCompanyId(user.getCompanyId());
        for (Role role : user.getRoles()) 
        {
            ud.addRole(role.getName());
        }
        setGroupRoles(ud, user);
        return ud;
    }
    
    public static List<UserData> convertLiferayUsers(List<User> liferayUsers) throws SystemException
    {
    	List<UserData> users = new ArrayList<UserData>(liferayUsers.size());
        for (User liferayUser : liferayUsers) {
            users.add(convertLiferayUser(liferayUser));
        }
        return users;
    }
    
    private static void setGroupRoles(UserDataBean ud, User user) {
        try {
            for (UserGroup userGroup : user.getUserGroups()) {
                List<Role> roles = RoleLocalServiceUtil.getGroupRoles(userGroup.getGroup().getGroupId());
                for (Role role : roles) {
                    ud.addRole(role.getName());
                }
            }
        }
        catch (Exception e) {
            throw new UserSourceException(e);
        }
    }

}

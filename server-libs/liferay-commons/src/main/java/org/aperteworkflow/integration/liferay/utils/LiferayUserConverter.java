package org.aperteworkflow.integration.liferay.utils;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.service.RoleLocalServiceUtil;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserDataBean;
import pl.net.bluesoft.rnd.processtool.usersource.exception.UserSourceException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Liferay {@link User} to {@link UserData} converter
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class LiferayUserConverter 
{
    private static final String APERTE_PREFIX_ATTRIBUTE = "aperte";

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

        Map<String, Serializable> customAttributes = user.getExpandoBridge().getAttributes(false);
        Map<String, Object> attributes = new HashMap<String, Object>();

        for(Map.Entry<String, Serializable> entry:  customAttributes.entrySet())
        {
            String attributeKey = entry.getKey();
            Serializable value = entry.getValue();

            if(attributeKey.contains(APERTE_PREFIX_ATTRIBUTE))
                attributes.put(attributeKey, value);
        }

        ud.setAttributes(attributes);
        return ud;
    }
    
    public static List<UserData> convertLiferayUsers(List<User> liferayUsers) throws SystemException
    {
    	List<UserData> users = new ArrayList<UserData>(liferayUsers.size());
        for (User liferayUser : liferayUsers)
        {
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

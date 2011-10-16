package pl.net.bluesoft.rnd.util.liferay;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.util.lang.Mapcar;

import javax.portlet.PortletRequest;
import java.util.*;

/**
 * Utility methods for integration with Liferay.
 * 
 * @author tlipski@bluesoft.net.pl
 */
public class LiferayBridge {

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

	public static UserData getLiferayUser(PortletRequest request) {
		try {
			User user = PortalUtil.getUser(request);
			return convertLiferayUser(user);
		} catch (Exception e) {
			throw new LiferayBridgeException(e);
		}
	}

	public static UserData convertLiferayUser(User user) {
        if (user == null) {
            return null;
        }
        UserData ud = new UserData();
        ud.setEmail(user.getEmailAddress());
        ud.setLogin(user.getScreenName());
        ud.setRealName(user.getFullName());
        ud.setJobTitle(user.getJobTitle());
        ud.setCompanyId(user.getCompanyId());
        if (user.getExpandoBridge() != null && user.getExpandoBridge().hasAttribute("company")
                && user.getExpandoBridge().hasAttribute("department")
                && user.getExpandoBridge().hasAttribute("superior")) {
            ud.setCompany(user.getExpandoBridge().getAttribute("company").toString());
            ud.setDepartment(user.getExpandoBridge().getAttribute("department").toString());
            ud.setSuperior(user.getExpandoBridge().getAttribute("superior").toString());
        }
        return ud;
    }

	public static Collection<String> getLiferayUserRoles(PortletRequest request) {
		try {
			User user = PortalUtil.getUser(request);
			if (user == null) {
                return null;
            }
			return new Mapcar<Role, String>(user.getRoles()) {
				@Override
				public String lambda(Role x) {
					return x.getName();
				}
			}.go();
		} catch (Exception e) {
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

	public static List<UserData> getAllUsers(UserData user) {
		try {
			if (user == null) {
                return new ArrayList<UserData>(0);
            }
			List<User> liferayUsers = UserLocalServiceUtil.getCompanyUsers(user.getCompanyId(), 0, Integer.MAX_VALUE);
			List<UserData> users = new ArrayList<UserData>(liferayUsers.size());
			for(User liferayUser : liferayUsers){
				users.add(convertLiferayUser(liferayUser));
			}
			return users;
		} catch (SystemException e) {
			throw new LiferayBridgeException(e);
		}
	}

    public static Map<UserData, List<String>> getAllUsersWithRoles(UserData user) {
		try {
            if (user != null) {
                List<User> liferayUsers = UserLocalServiceUtil.getCompanyUsers(user.getCompanyId(), 0, Integer.MAX_VALUE);
                Map<UserData, List<String>> users = new HashMap<UserData, List<String>>(liferayUsers.size());
                for(User liferayUser : liferayUsers){
                    users.put(convertLiferayUser(liferayUser),
                            new Mapcar<Role, String>(liferayUser.getRoles()) {
                                @Override
                                public String lambda(Role x) {
                                    return x.getName();
                                }
                            }.go()
                    );
                }
                return users;
            }
		} catch (SystemException e) {
            throw new LiferayBridgeException(e);
		}
        return new HashMap<UserData, List<String>>(0);
	}

    public static List<String> getUserRoles(UserData userData) {
        try {
            User user = UserLocalServiceUtil.getUserByScreenName(userData.getCompanyId(), userData.getLogin());
            if (user != null) {
                return new Mapcar<Role, String>(user.getRoles()) {
                    @Override
                    public String lambda(Role x) {
                        return x.getName();
                    }
                }.go();
            }
        }
        catch (Exception e) {
            throw new LiferayBridgeException(e);
		}
        return new ArrayList<String>(0);
    }
}

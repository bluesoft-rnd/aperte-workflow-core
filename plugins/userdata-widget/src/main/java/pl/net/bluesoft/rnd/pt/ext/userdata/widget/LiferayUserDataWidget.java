package pl.net.bluesoft.rnd.pt.ext.userdata.widget;

import java.util.Collection;
import java.util.HashSet;

import javax.portlet.PortletRequest;

import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;

import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.RoleServiceUtil;
import com.liferay.portal.service.UserServiceUtil;
import com.liferay.portal.util.PortalUtil;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name="LiferayUserData")
@WidgetGroup("userdata-widget")
public class LiferayUserDataWidget extends UserDataWidget {

	@AutoWiredProperty
    @AperteDoc(
            humanNameKey = "userdata.widget.liferay.liferayRoleName",
            descriptionKey = "userdata.widget.liferay.liferayRoleName.description"
    )
	public String liferayRoleName = "User";

	@Override
	protected Collection<UserData> getUsers() {

		try {
			final PermissionChecker oldChecker = PermissionThreadLocal.getPermissionChecker();
			try {
				PermissionThreadLocal.setPermissionChecker(new PermissionChecker() { //TODO not the best way to do it?
					@Override
					public long getCompanyId() {
						return oldChecker.getCompanyId();
					}

					@Override
					public long getOwnerRoleId() {
						return oldChecker.getOwnerRoleId();
					}

					@Override
					public long[] getRoleIds(long userId, long groupId) {
						return oldChecker.getRoleIds(userId, groupId);
					}

					@Override
					public long getUserId() {
						return oldChecker.getUserId();
					}

					@Override
					public boolean hasOwnerPermission(long companyId, String name, long primKey, long ownerId, String actionId) {
						return true;
					}

					@Override
					public boolean hasOwnerPermission(long companyId, String name, String primKey, long ownerId, String actionId) {
						return true;
					}

					@Override
					public boolean hasPermission(long groupId, String name, long primKey, String actionId) {
						return true;
					}

					@Override
					public boolean hasPermission(long groupId, String name, String primKey, String actionId) {
						return true;
					}

					@Override
					public boolean hasUserPermission(long groupId, String name, String primKey, String actionId, boolean checkAdmin) {
						return true;
					}

					@Override
					public void init(User user, boolean checkGuest) {
						oldChecker.init(user, checkGuest);
					}

					@Override
					public boolean isCommunityAdmin(long groupId) {
						return true;
					}

					@Override
					public boolean isCommunityOwner(long groupId) {
						return true;
					}

					@Override
					public boolean isCompanyAdmin() {
						return true;
					}

					@Override
					public boolean isCompanyAdmin(long companyId) {
						return true;
					}

					@Override
					public boolean isOmniadmin() {
						return true;
					}

					@Override
					public void resetValues() {
						oldChecker.resetValues();
					}

					@Override
					public void setCheckGuest(boolean checkGuest) {
						oldChecker.setCheckGuest(true);
					}

					@Override
					public void setValues(PortletRequest portletRequest) {
						oldChecker.setValues(portletRequest);
					}
				});
				Role role = RoleServiceUtil.getRole(PortalUtil.getDefaultCompanyId(), liferayRoleName);
				if (role == null) return new HashSet<UserData>();
				long[] roleUserIds = UserServiceUtil.getRoleUserIds(role.getRoleId());
				HashSet<UserData> users = new HashSet<UserData>();
				for (long roleUserId : roleUserIds) {
					User userById = UserServiceUtil.getUserById(roleUserId);
					UserData ud = new UserData();
					ud.setLogin(userById.getLogin());
					ud.setDescription(userById.getFullName());
					ud.setBpmLogin(userById.getScreenName());
					users.add(ud);
				}
				return users;
			}
			finally {
				PermissionThreadLocal.setPermissionChecker(oldChecker);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public String getLiferayRoleName() {
		return liferayRoleName;
	}

	public void setLiferayRoleName(String liferayRoleName) {
		this.liferayRoleName = liferayRoleName;
	}
}

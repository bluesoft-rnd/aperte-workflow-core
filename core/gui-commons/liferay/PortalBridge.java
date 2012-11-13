package org.aperteworkflow.util.liferay;

import java.util.Collection;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.util.lang.Mapcar;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.model.PortletPreferences;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PortletKeys;

public class PortalBridge {

    public static UserData getLiferayUser(PortletRequest request) {
        try {
            User user = PortalUtil.getUser(request);
            return LiferayBridge.convertLiferayUser(user);
        }
        catch (Exception e) {
            throw new LiferayBridge.LiferayBridgeException(e);
        }
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
        }
        catch (Exception e) {
            throw new LiferayBridge.LiferayBridgeException(e);
        }
    }

    public static String getCurrentRequestParameter(String param) {
        PortletRequest req = ProcessToolVaadinApplicationPortlet2.getPortletRequest();
        return req != null ? PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(req)).getParameter(param) : null;
    }

    public static Map getCurrentRequestParameterMap() {
        PortletRequest req = ProcessToolVaadinApplicationPortlet2.getPortletRequest();
        return req != null ? PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(req)).getParameterMap() : null;
    }

    public static boolean getBoolean(long companyId, String name, boolean defaultValue) throws Exception {
        String value = PrefsPropsUtil.getString(companyId, name);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public static boolean getBoolean(PortletPreferences preferences, String name, boolean defaultValue) {
        return GetterUtil.getBoolean(getString(preferences, name, "" + defaultValue));
    }

    public static String getString(long companyId, String name, String defaultValue) throws Exception {
        String value = PrefsPropsUtil.getString(companyId, name);
        return value != null ? value : defaultValue;
    }

    public static String getString(long companyId, String name) throws Exception {
        return getString(companyId, name, null);
    }

    public static String getString(PortletPreferences preferences, String name, String defaultValue) {
        return preferences.getValue(name, defaultValue);
    }

    public static PortletPreferences getPortalPreferences(long companyId) throws SystemException {
        return PortletPreferencesLocalServiceUtil.getPreferences(companyId, companyId, PortletKeys.PREFS_OWNER_TYPE_COMPANY,
                PortletKeys.PREFS_PLID_SHARED, PortletKeys.LIFERAY_PORTAL);
    }


}

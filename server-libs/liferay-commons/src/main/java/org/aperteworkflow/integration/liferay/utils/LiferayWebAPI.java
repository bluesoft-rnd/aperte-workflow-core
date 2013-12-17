package org.aperteworkflow.integration.liferay.utils;

import com.liferay.portal.kernel.util.CookieKeys;
import com.liferay.portal.kernel.util.CookieUtil;
import com.liferay.portal.util.PortalUtil;
import org.aperteworkflow.ui.view.IWebAPI;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class LiferayWebAPI implements IWebAPI {
    @Override
    public HttpServletRequest getServletRequest(PortletRequest portletRequest) {
        return PortalUtil.getHttpServletRequest(portletRequest);
    }

    @Override
    public String getCookieValue(PortletRequest portletRequest, String cookieKey) {
        return CookieKeys.getCookie(getServletRequest(portletRequest), cookieKey);
    }

    @Override
    public String getCookieValue(HttpServletRequest portletRequest, String cookieKey) {
        return CookieKeys.getCookie(portletRequest, cookieKey);
    }
}

package org.aperteworkflow.ui.view;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public interface IWebAPI
{
    HttpServletRequest getServletRequest(PortletRequest portletRequest);

    String getCookieValue(PortletRequest portletRequest, String cookieKey);
    String getCookieValue(HttpServletRequest portletRequest, String cookieKey);
}

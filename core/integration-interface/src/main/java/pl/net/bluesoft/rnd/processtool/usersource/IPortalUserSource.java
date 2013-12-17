package pl.net.bluesoft.rnd.processtool.usersource;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.net.bluesoft.rnd.processtool.model.UserData;


/**
 * User source interface witch extends {@link IUserSource} inteferace
 * with portal request authentication methods
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IPortalUserSource extends IUserSource
{
	UserData getUserByRequest(HttpServletRequest request);
	UserData getUserByRequest(PortletRequest request);

    HttpServletRequest getHttpServletRequest(PortletRequest request);
    HttpServletRequest getOriginalHttpServletRequest(HttpServletRequest request);

    HttpServletResponse getHttpServletResponse(PortletResponse response);
}

package pl.net.bluesoft.rnd.processtool.usersource;

import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;

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
	UserData getUserByRequest(RenderRequest request);
	
}

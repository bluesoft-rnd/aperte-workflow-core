package pl.net.bluesoft.rnd.processtool.authorization.impl;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.authorization.exception.AuthorizationException;
import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * This is mock authorization service. It simply look for user with provided
 * login and if one is found, it returns its instance. It is used only 
 * for demo purpose 
 * 
 * You should provide your own, portal-based authorization implementation
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class MockAuthorizationService implements IAuthorizationService 
{

	@Override
	public UserData getUserByRequest(HttpServletRequest servletRequest) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserData authenticateByLogin(String login, String password) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public UserData authenticateByLogin(String login, String password, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws AuthorizationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public UserData getUserByRequest(PortletRequest renderRequest)
			throws AuthorizationException {
		// TODO Auto-generated method stub
		return null;
	}

}

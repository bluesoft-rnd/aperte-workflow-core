package pl.net.bluesoft.rnd.processtool.authorization;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.net.bluesoft.rnd.processtool.authorization.exception.AuthorizationException;
import pl.net.bluesoft.rnd.processtool.model.UserData;

/** 
 * Authorization service interface 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IAuthorizationService 
{
	/**
	 * Check if user is already logged in and if it so, return {@link UserData} instance
	 * 
	 * @param servletRequest servlet http request
	 * @return {@link UserData} instance if client is already logged in
	 */
	UserData getUserByRequest(HttpServletRequest servletRequest) throws AuthorizationException;
	
	UserData getUserByRequest(PortletRequest renderRequest) throws AuthorizationException;
	
	/**
	 * Authenticate user by given login and password. If there is no user or password is 
	 * invalid, null is returned
	 * 
	 * @param login
	 * @param password
	 * @return
	 */
	UserData authenticateByLogin(String login, String password) throws AuthorizationException;
	
	/**
	 * Authenticate user by given login and password. If there is no user or password is 
	 * invalid, null is returned
	 * 
	 * Adds attibutes to portal session
	 * 
	 * @param login
	 * @param password
	 * @return
	 */
	UserData authenticateByLogin(String login, String password, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws AuthorizationException;
	
	
}

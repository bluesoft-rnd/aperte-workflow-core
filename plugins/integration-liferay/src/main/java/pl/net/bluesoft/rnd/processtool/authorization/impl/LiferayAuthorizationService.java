package pl.net.bluesoft.rnd.processtool.authorization.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aperteworkflow.integration.liferay.utils.LiferayUserConverter;

import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.authorization.exception.AuthorizationException;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.exception.InvalidCredentialsUserSourceException;
import pl.net.bluesoft.rnd.processtool.usersource.exception.UserSourceException;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

/**
 * This is authorization service based on Liferay Portal
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class LiferayAuthorizationService implements IAuthorizationService 
{
	/** Get default company id */
	public static Long getDefaultCompanyId()
	{
			return PortalUtil.getDefaultCompanyId();

	}

	@Override
	public UserData getUserByRequest(HttpServletRequest servletRequest) 
	{
		try 
		{
			User liferayUser = PortalUtil.getUser(servletRequest);
			
			/* No user logged in, return null */
			if(liferayUser == null)
				return null;
			
			return LiferayUserConverter.convertLiferayUser(liferayUser);
		} 
		catch (PortalException e) 
		{
			throw new AuthorizationException("Problem with authorization", e);
		} 
		catch (SystemException e) 
		{
			throw new AuthorizationException("Problem with authorization", e);
		}
	}

	@Override
	public UserData authenticateByLogin(String login, String password) 
	{
		try 
		{
			return LiferayUserConverter.convertLiferayUser(authenticateLiferayUser(login, password, null));
		} 
		catch (SystemException e) 
		{
			throw new UserSourceException(e);
		}
	}

	@Override
	public UserData authenticateByLogin(String login, String password, HttpServletRequest servletRequest) {
		HttpSession session = servletRequest.getSession();
		
		User loggedUser = authenticateLiferayUser(login, password, servletRequest);
		
		/* Invalidate old session and create new one */
		session.invalidate();
		session = servletRequest.getSession(true);
		
		session.setAttribute("j_username", String.valueOf(loggedUser.getUserId()));
		session.setAttribute("j_password", loggedUser.getPassword());
		session.setAttribute("j_remoteuser", String.valueOf(loggedUser.getUserId()));
		session.setAttribute(WebKeys.USER_PASSWORD, password);

		Cookie companyIdCookie = new Cookie("COMPANY_ID", String.valueOf(loggedUser.getCompanyId()));
		companyIdCookie.setDomain(servletRequest.getServerName());
		companyIdCookie.setPath(StringPool.SLASH); 
		
		try 
		{
			return LiferayUserConverter.convertLiferayUser(loggedUser);
		} 
		catch (SystemException e) 
		{
			throw new UserSourceException(e);
		}
	}
	
	private User authenticateLiferayUser(String login, String password,HttpServletRequest request)
	{
		Long defaultCompanyId = getDefaultCompanyId();
		
		if(defaultCompanyId == null)
			throw new UserSourceException("There is no default company id. Impossible!");
		
		try 
		{
			Map<String, String[]> parameterMap = new HashMap<String, String[]>();
			Map<String, String[]> headerMap = new HashMap<String, String[]>();
			
			if(request != null)
			{
				parameterMap = request.getParameterMap();		
			}
			
			/* Get userId by login and password using screen-name authentication type */
			int authResult = UserLocalServiceUtil.authenticateByScreenName(defaultCompanyId, login, password, headerMap, parameterMap);
			
			if(authResult == Authenticator.FAILURE)
				throw new InvalidCredentialsUserSourceException("Invalid credentials");
			
			long userId = UserLocalServiceUtil.getUserIdByScreenName(defaultCompanyId, login);
			
			User liferayUser = UserLocalServiceUtil.getUser(userId);
			
			return liferayUser;
		} 
		catch (PortalException e) 
		{
			throw new UserSourceException(e);
		} 
		catch (SystemException e) 
		{
			throw new UserSourceException(e);
		}
	}

	
}

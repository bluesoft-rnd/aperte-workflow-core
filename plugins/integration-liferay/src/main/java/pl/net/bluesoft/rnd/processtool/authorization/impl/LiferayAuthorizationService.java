package pl.net.bluesoft.rnd.processtool.authorization.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.liferay.portal.kernel.util.*;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.service.PasswordPolicyLocalServiceUtil;
import org.aperteworkflow.integration.liferay.utils.LiferayUserConverter;

import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.authorization.exception.AuthorizationException;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.exception.InvalidCredentialsUserSourceException;
import pl.net.bluesoft.rnd.processtool.usersource.exception.UserSourceException;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.persistence.UserUtil;
import com.liferay.portal.theme.ThemeDisplay;
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
	public UserData getUserByRequest(PortletRequest renderRequest)
	{
		HttpServletRequest servletRequest = PortalUtil.getHttpServletRequest(renderRequest);
		
		return getUserByRequest(servletRequest);
	}

	@Override
	public UserData getUserByRequest(HttpServletRequest servletRequest) 
	{
		try 
		{
           HttpSession session = servletRequest.getSession(false);
			/* Fix for wrong user in servlet request */
			User sessionUser = getLiferayUser(servletRequest);
			User liferayUser = PortalUtil.getUser(servletRequest);

           //Object test = servletRequest.getAttribute("USER");

			/* Why? Becouse you can be logged out and still have cookies in browser */
			if(liferayUser == null)
				return null;
			
			/* No cookies, use liferay user */
			if(sessionUser == null)
				sessionUser = liferayUser; 
			
			
			
			return LiferayUserConverter.convertLiferayUser(sessionUser);
		} 
		catch (PortalException e) 
		{
			throw new AuthorizationException("Problem with authorization", e);
		} 
		catch (SystemException e) 
		{
			throw new AuthorizationException("Problem with authorization", e);
		} catch (ServletException e) {
			throw new AuthorizationException("Problem with authorization", e);
		} 
	}
	
	/** Get Liferay user by given servlet request */
	protected User getLiferayUser(HttpServletRequest req) throws ServletException
	{
			User userByScreenName = null;
			
			/* Try to authorized user by given servlet request.
			 * We have to use cookies, otherwise authentication 
			 * won't work on WebSphere
			 */
			
			String userId = null;
			String password = null;
			String companyId = null;

            /* Safari fix */
            if(req.getCookies() == null)
                return null;
			
			for (Cookie c : req.getCookies()) 
			{
				if ("COMPANY_ID".equals(c.getName())) {
					companyId = c.getValue();
				} else if ("ID".equals(c.getName())) {
					userId = hexStringToStringByAscii(c.getValue());
				} else if ("PASSWORD".equals(c.getName())) {
					password = hexStringToStringByAscii(c.getValue());
				}
			}
			
			if (userId != null && password != null && companyId != null) {
				try {
					
					KeyValuePair kvp = UserLocalServiceUtil.decryptUserId(Long.parseLong(companyId), userId, password);

					userByScreenName = UserLocalServiceUtil.getUserById(Long.valueOf(kvp.getKey()));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PortalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SystemException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(userByScreenName == null)
			{
				return null;
			}
			
			return userByScreenName;
	}
	
	public String hexStringToStringByAscii(String hexString) {
		byte[] bytes = new byte[hexString.length()/2];
		for (int i = 0; i < hexString.length() / 2; i++) {
			String oneHexa = hexString.substring(i * 2, i * 2 + 2);
			bytes[i] = Byte.parseByte(oneHexa, 16);
		}
		try {
			return new String(bytes, "ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
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
	public UserData authenticateByLogin(String login, String password, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		HttpSession session = servletRequest.getSession();
		
		User loggedUser = authenticateLiferayUser(login, password, servletRequest);
		
		/* Invalidate old session and create new one */
		session.invalidate();
		session = servletRequest.getSession(true);

		session.setAttribute("j_username", String.valueOf(loggedUser.getUserId()));
		session.setAttribute("j_password", loggedUser.getPassword());
		session.setAttribute("j_remoteuser", String.valueOf(loggedUser.getUserId()));
		session.setAttribute(WebKeys.USER_PASSWORD, password);
        session.setAttribute(WebKeys.USER, loggedUser);
        session.setAttribute(WebKeys.USER_ID, loggedUser.getUserId());

        servletRequest.setAttribute(WebKeys.USER_ID, loggedUser.getUserId());
        servletRequest.setAttribute(WebKeys.USER, loggedUser);
        servletRequest.setAttribute(WebKeys.USER_PASSWORD, password);

		Cookie companyIdCookie = new Cookie("COMPANY_ID", String.valueOf(loggedUser.getCompanyId()));
		companyIdCookie.setDomain(servletRequest.getServerName());
		companyIdCookie.setPath(StringPool.SLASH);




        try
		{

            Cookie userIdCookie = new Cookie("ID", UserLocalServiceUtil.encryptUserId(((Long)loggedUser.getUserId()).toString()));
            userIdCookie.setDomain(servletRequest.getServerName());
            userIdCookie.setPath(StringPool.SLASH);

            Cookie userPasswordCookie = new Cookie("PASSWORD", loggedUser.getPassword());
            userPasswordCookie.setDomain(servletRequest.getServerName());
            userPasswordCookie.setPath(StringPool.SLASH);

            //MethodKey key = new MethodKey("com.liferay.portlet.login.util.LoginUtil", "login", HttpServletRequest.class, HttpServletResponse.class, String.class, String.class, boolean.class, String.class);
            //PortalClassInvoker.invoke(false, key, new Object[] { servletRequest, servletResponse, loggedUser.getEmailAddress(), password, false, CompanyConstants.AUTH_TYPE_EA});

			return LiferayUserConverter.convertLiferayUser(loggedUser);
		} 
		catch (SystemException e) 
		{
			throw new UserSourceException(e);
		} catch (Exception e)
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

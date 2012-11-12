package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

/**
 * Abstract servlet with integration with Liferay. It uses liferay authentication
 * and session sharing 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public abstract class AbstractLiferayServlet extends HttpServlet 
{
	private static final long serialVersionUID = -3306153687062512299L;
	private static Logger logger = Logger.getLogger(AbstractLiferayServlet.class.getName());
	
	public enum Format 
	{
		JSON, 
		XML
	}

	/** Collection of authorized roles. If no role is specified, any user
	 * can access this servlet
	 */
	public abstract Set<String> getAuthorizedRoles();
	
	/** Name of attribute which stores authorization attribute. It should be unique through 
	 * servlets
	 */
	public abstract String getSessionAuthorizationName();
	
	/** Is user authorization required? Should return true if it so, false otherwise */
	public abstract boolean isAuthorizationRequired();
	
	/**
	 * Authorize user by request. There must be active liferay session for logged user to do this, and
	 * user must have all roles, specifed by {@link getAuthorizedRoles()}
	 */
	protected boolean authorizeUserByRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
	{
		PrintWriter out = resp.getWriter();
		HttpSession session = req.getSession();
		
		/* Check if session is already authorized. If not, proceed with authorization */
		if (session.getAttribute(getSessionAuthorizationName()) == null) 
		{
			User liferayUser = getLiferayUser(req);
			
			/* No liferay user bound with current session, fail */
			if(liferayUser == null)
			{
				out.write("No Liferay user is bound to current sesssion, abort");
				return false;
			}
			
			/* There is user bound with current session, but it has not got all required roles */
			if(!hasHelpChangeRole(liferayUser))
			{
				out.write("User "+liferayUser.getScreenName()+" does not have all roles: "+getAuthorizedRoles());
				return false;
			}
			
			session.setAttribute(getSessionAuthorizationName(), liferayUser.getScreenName());

		}

		out.close();
		return true;
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
				logger.warning("Failed to authorize user");
				return null;
			}
			logger.info("Successfully authorized user: " + userByScreenName.getScreenName());
			
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
	
	/** Checks if there user has all roles specified by {@link getAuthorizedRoles()} */
	protected boolean hasHelpChangeRole(User liferayUser) throws ServletException
	{
		try 
		{
			List<Role> roles = liferayUser.getRoles();
			
			Collection<String> userRolesNames = new ArrayList<String>();
			for (Role role : roles) 
				userRolesNames.add(role.getName());
			
			return  userRolesNames.containsAll(getAuthorizedRoles()); 
		} 
		catch (SystemException e) 
		{
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new ServletException(e);
		}
	}
	
	/** Get request parameter from given request */
	protected String getRequestParamter(HttpServletRequest req, String parameterKey)
	{
		String parameterValue = req.getParameter(parameterKey);
		if (parameterValue == null) 
			throw new IllegalArgumentException("No "+parameterKey+" specified. Please run servlet with '"+parameterKey+"' parameter");
		
		return parameterValue;
	}


	@Override
	public void init() throws ServletException {
		super.init();
		logger.info(this.getClass().getSimpleName() + " INITIALIZED: "
				+ getServletContext().getContextPath());
	}

	@Override
	public void destroy() {
		super.destroy();
		logger.info(this.getClass().getSimpleName() + " DESTROYED");
	}
}

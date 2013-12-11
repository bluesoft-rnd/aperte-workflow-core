package pl.net.bluesoft.rnd.processtool.plugins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Abstract servlet with integration with portal
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
@Component
public abstract class AbstractPortalServlet extends HttpServlet
{
	private static final long serialVersionUID = -3306153687062512299L;
	private static Logger logger = Logger.getLogger(AbstractPortalServlet.class.getName());

    @Autowired
    private IUserRolesManager userRolesManager;

    @Autowired
    private IPortalUserSource portalUserSource;
	
	public enum Format 
	{
		JSON, 
		XML
	}

    public AbstractPortalServlet()
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
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
	 * Authorize user by request. There must be active portal session for logged user to do this, and
	 * user must have all roles
	 */
	protected boolean authorizeUserByRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
	{
		PrintWriter out = resp.getWriter();
		HttpSession session = req.getSession();
		
		/* Check if session is already authorized. If not, proceed with authorization */
		if (session.getAttribute(getSessionAuthorizationName()) == null) 
		{
			UserData portalUser = portalUserSource.getUserByRequest(req);
			
			/* No liferay user bound with current session, fail */
			if(portalUser == null)
			{
				out.write("No Liferay user is bound to current sesssion, abort");
				return false;
			}
			
			/* There is user bound with current session, but it has not got all required roles */
			if(!hasHelpChangeRole(portalUser))
			{
				out.write("User "+ portalUser.getLogin()+" does not have all roles: "+getAuthorizedRoles());
				return false;
			}
			
			session.setAttribute(getSessionAuthorizationName(), portalUser.getLogin());

		}

		out.close();
		return true;
	}

	
	/** Checks if there user has all roles specified by } */
	protected boolean hasHelpChangeRole(UserData portalUser) throws ServletException
	{
        Collection<String> roles = portalUser.getRoles();

        return  roles.containsAll(getAuthorizedRoles());
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

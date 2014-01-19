package pl.net.bluesoft.rnd.processtool.plugins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.roles.exception.RoleNotFoundException;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * <p>
 * This servlet authorizes user by screen name using Liferay API. Of course no SSO (CAS for example) is available
 * at this moment. Still, if such need arises, SSO could also be implemented.
 * </p>
 * We use basic authentication, therefore use of SSL is required in production environment.
 */
public class PermissionFilter implements Filter {

    private static final Logger logger = Logger.getLogger(PermissionFilter.class.getName());

    public static final String AUTHORIZED = "Aperte_Authorized";

    private static final Collection<String> ROLE_NAMES = Arrays.asList("ADMINISTRATOR", "MODELER_USER");

    @Autowired
    private IUserRolesManager userRolesManager;

    @Autowired
    private IPortalUserSource portalUserSource;

    public PermissionFilter()
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        if(portalUserSource == null)
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        HttpServletRequest req = Lang2.assumeType(request, HttpServletRequest.class);
        HttpServletResponse res = Lang2.assumeType(response, HttpServletResponse.class);

        HttpSession session = req.getSession();
        if (session.getAttribute(AUTHORIZED) != null) {
            chain.doFilter(request, response);
            return;
        }

        UserData userInRequest = portalUserSource.getUserByRequest(req);
        if(userInRequest == null)
        {
            logger.warning("Failed to authorize user");

            res.setHeader("WWW-Authenticate", "Basic realm=\"Aperte Modeler\"");
            res.setStatus(401);

            return;
        }

        logger.info("Successfully authorized user: " + userInRequest.getLogin());

        if(/*isRoleExistForUser(userInRequest)*/true)
        {
            logger.info("Matched role for user " + userInRequest.getLogin());

            session.setAttribute(AUTHORIZED, userInRequest.getLogin());
            chain.doFilter(request, response);
        }
        else
        {
            res.setHeader("WWW-Authenticate", "Basic realm=\"Aperte Modeler\"");
            res.setStatus(401);
        }
    }

    private boolean isRoleExistForUser(UserData userInRequest)
    {
        for(String roleName: ROLE_NAMES)
        {
            try
            {
                Collection<UserData> users = userRolesManager.getUsersByRole(roleName);
                if(users.contains(userInRequest))
                {
                    logger.info("Matched role " + roleName + " for user " + userInRequest.getLogin());

                    return true;
                }
            }
            catch(RoleNotFoundException ex)
            {

            }
        }

        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}

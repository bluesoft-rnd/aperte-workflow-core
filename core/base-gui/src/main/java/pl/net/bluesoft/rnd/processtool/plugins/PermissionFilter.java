package pl.net.bluesoft.rnd.processtool.plugins;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = Lang2.assumeType(request, HttpServletRequest.class);
        HttpServletResponse res = Lang2.assumeType(response, HttpServletResponse.class);

        HttpSession session = req.getSession();
        if (session.getAttribute(AUTHORIZED) != null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            //check for user session
            User userByScreenName = null;
            //try to authorize user using Liferay API
            long basicAuthUserId = PortalUtil.getBasicAuthUserId(req);
            if (basicAuthUserId != 0)
                userByScreenName  = UserLocalServiceUtil.getUserById(basicAuthUserId);
            if (userByScreenName != null) {
                String username = userByScreenName.getScreenName();
                logger.info("Successfully authorized user: " + username);
                List<Role> roles = userByScreenName.getRoles();
                boolean found = false;
                for (Role role : roles) {
                    if (!role.isTeam() && ROLE_NAMES.contains(role.getName().toUpperCase())) {
                        found = true;
                        logger.info("Matched role " + role.getName() + " for user " + username);
                        break;
                    }
                }
                if (!found) {
                    logger.info("User " + username + " has insufficient privileges.");
                } else {
                    session.setAttribute(AUTHORIZED, username);
                    chain.doFilter(request, response);
                    return;
                }

            } else {
                logger.warning("Failed to authorize user");
            }
        } catch (PortalException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        } catch (SystemException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        }
        //if we are here, then authentication has failed or no username/password has been supplied
        res.setHeader("WWW-Authenticate", "Basic realm=\"Aperte Modeler\"");
        res.setStatus(401);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}

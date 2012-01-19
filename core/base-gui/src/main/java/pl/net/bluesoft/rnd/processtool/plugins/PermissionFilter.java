package pl.net.bluesoft.rnd.processtool.plugins;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Company;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import java.io.IOException;
import java.net.Authenticator;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This servlet authorizes user by screen name using Liferay API. Of course no SSO (CAS for example) is available at this
 * moment. Still, if such need arises, SSO could also be implemented.
 * <p/>
 * We use basic authentication, therefore use of SSL is required in production environment.
 */
public class PermissionFilter implements Filter {

    private static final Logger logger = Logger.getLogger(PermissionFilter.class.getName());

    public static final String AUTHORIZED = "Aperte_Authorized";


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession();
        if (session.getAttribute(AUTHORIZED) != null) {
            chain.doFilter(request, response);
            return;
        }
        //try to authorize user using Liferay API            
        String authHeader = req.getHeader("authorization");
        if (authHeader != null && authHeader.toUpperCase().startsWith("BASIC ")) {
            String userpassEncoded = authHeader.substring(6);
            sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
            String userpassDecoded = new String(dec.decodeBuffer(userpassEncoded));
            String username = userpassDecoded.split(":")[0];
            String password = userpassDecoded.split(":")[1];
            logger.info("Attempting to authorize user: " + username);
            try {
                if (UserLocalServiceUtil.authenticateByScreenName(PortalUtil.getDefaultCompanyId(),
                        username,
                        password,
                        new HashMap(),
                        new HashMap()) == 1) {
                    logger.info("Successfully authorized user: " + username);
                    session.setAttribute(AUTHORIZED, username);
                    chain.doFilter(request, response);
                    return;
                } else {
                    logger.warning("Failed to authorize user: " + username);                    
                }
            } catch (PortalException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new ServletException(e);
            } catch (SystemException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new ServletException(e);
            }
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

package org.aperteworkflow.webapi;

import org.aperteworkflow.webapi.main.util.MappingJacksonJsonViewEx;
import org.springframework.web.portlet.ModelAndView;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.portlet.ResourceRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class PortletUtil
{
    private static Logger logger = Logger.getLogger(PortletUtil.class.getName());

    /**
     * Obtain http servlet request with additional attributes from ajax request
     */
    public static HttpServletRequest getOriginalHttpServletRequest(IPortalUserSource portalUserSource, ResourceRequest request) {
        try {
            HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
            HttpServletRequest originalHttpServletRequest = portalUserSource.getOriginalHttpServletRequest(httpServletRequest);

            /* Copy all attributes, because portlet attributes do not exist in original request */
            originalHttpServletRequest.getParameterMap().putAll(httpServletRequest.getParameterMap());

            return originalHttpServletRequest;
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "[PORTLET CONTROLLER] Error", ex);
            throw new RuntimeException(ex);
        }

    }

    /**
     * Translate DTO object to json in model and view, which is required for portlet resource serving
     */
    public static ModelAndView translate(String resultName, Object result) {
        ModelAndView mav = new ModelAndView();
        MappingJacksonJsonViewEx v = new MappingJacksonJsonViewEx();
        v.setBeanName(resultName);
        v.setContentType("application/json");

        mav.setView(v);
        mav.addObject(resultName, result);

        return mav;
    }

	public static ModelAndView translate(String resultName, Object result, String controller, String action) {
		ModelAndView mav = new ModelAndView();
		MappingJacksonJsonViewEx v = new MappingJacksonJsonViewEx();
		v.setBeanName(resultName);
		v.setContentType("application/json");
		if ("filescontroller".equals(controller) && "uploadFile".equals(action)){
			v.setContentType("text/plain");
		}

		mav.setView(v);
		mav.addObject(resultName, result);

		return mav;
	}
}

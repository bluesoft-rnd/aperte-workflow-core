package org.aperteworkflow.webapi.dictionary;

import org.aperteworkflow.webapi.PortletUtil;
import org.aperteworkflow.webapi.main.DispatcherController;
import org.aperteworkflow.webapi.tools.WebApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import pl.net.bluesoft.rnd.processtool.ISettingsProvider;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-05-30.
 */
@Controller(value = "DictionaryEditorPortletController")
@RequestMapping("VIEW")
public class DictionaryEditorPortletController {
    protected static final String PORTLET_JSON_RESULT_ROOT_NAME = "result";
    private static final String LANGUAGES_SETTING = "dictionary.editor.languages";
    private static final String LANGUAGES_ATTRIBUTE = "languages";
    private static final String DEFAULT_LANGUAGES = "{'default':'en_US', 'pl':'pl_PL'}";

    private static Logger logger = Logger.getLogger(DictionaryEditorPortletController.class.getName());

    @Autowired(required = false)
    protected IPortalUserSource portalUserSource;

    @Autowired(required = false)
    protected ProcessToolRegistry processToolRegistry;

    @Autowired(required = false)
    private DispatcherController mainDispatcher;

    @Autowired
    private ISettingsProvider settingsProvider;

    /**
     * main view handler for Portlet.
     */
    @RenderMapping()
    public ModelAndView handleMainRenderRequest(RenderRequest request, RenderResponse response, Model model) {
        logger.info("DictionaryEditorPortletController.handleMainRenderRequest... ");
        ModelAndView modelView = new ModelAndView();
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        UserData user = portalUserSource.getUserByRequest(request);
        modelView.addObject(WebApiConstants.USER_PARAMETER_NAME, user);
        if (user == null || user.getLogin() == null) {
            modelView.setViewName("login");
        } else {
            modelView.setViewName("dictionary-editor");
        }
        String languages = settingsProvider.getSetting(LANGUAGES_SETTING);
        if (languages == null)
            languages = DEFAULT_LANGUAGES;
        modelView.addObject(LANGUAGES_ATTRIBUTE, languages);
        return modelView;
    }

    @ResourceMapping("dispatcher")
    @ResponseBody
    public ModelAndView dispatcher(ResourceRequest request, ResourceResponse response) throws PortletException {
        HttpServletRequest originalHttpServletRequest = PortletUtil.getOriginalHttpServletRequest(portalUserSource, request);

        String controller = originalHttpServletRequest.getParameter("controller");
        String action = originalHttpServletRequest.getParameter("action");

        logger.log(Level.INFO, "controllerName: " + controller + ", action: " + action);

        if (controller == null || controller.isEmpty()) {
            logger.log(Level.SEVERE, "[ERROR] No controller paramter in dispatcher invocation!");
            throw new PortletException("No controller paramter!");
        } else if (action == null || action.isEmpty()) {
            logger.log(Level.SEVERE, "[ERROR] No action paramter in dispatcher invocation!");
            throw new PortletException("No action paramter!");
        }

        HttpServletResponse httpServletResponse = getHttpServletResponse(response);

        return PortletUtil.translate(PORTLET_JSON_RESULT_ROOT_NAME,
                mainDispatcher.invokeExternalController(controller, action, originalHttpServletRequest, httpServletResponse));
    }

    /**
     * Obtain http servlet response from ajax request
     */
    private HttpServletResponse getHttpServletResponse(ResourceResponse response) {
        try {
            return portalUserSource.getHttpServletResponse(response);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "[PORTLET CONTROLLER] Error", ex);
            throw new RuntimeException(ex);
        }
    }

}

package org.aperteworkflow.webapi.casemanagement.complaint;

import org.aperteworkflow.webapi.PortletUtil;
import org.aperteworkflow.webapi.casemanagement.CaseManagementPortletController;
import org.aperteworkflow.webapi.main.DispatcherController;
import org.aperteworkflow.webapi.main.processes.controller.ProcessesListController;
import org.aperteworkflow.webapi.main.processes.controller.TaskViewController;
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
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.portlet.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-05-19.
 */
@Controller(value = "ComplaintRegistryPortletController")
@RequestMapping("VIEW")
public class ComplaintRegistryPortletController extends CaseManagementPortletController {
    private static Logger logger = Logger.getLogger(ComplaintRegistryPortletController.class.getName());

    @Autowired(required = false)
    private TaskViewController taskViewController;
    @Autowired(required = false)
    private ProcessesListController processesListController;

    @RenderMapping()
    /**
     * main view handler for Portlet.
     */
    public ModelAndView handleMainRenderRequest(RenderRequest request, RenderResponse response, Model model) {
        logger.info("ComplaintRegistryPortletController.handleMainRenderRequest... ");
        ModelAndView modelView = new ModelAndView();
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        UserData user = portalUserSource.getUserByRequest(request);
        modelView.addObject(WebApiConstants.USER_PARAMETER_NAME, user);
        if (user == null || user.getLogin() == null) {
            modelView.setViewName("login");
        } else {
            modelView.setViewName("complaint-registry");
        }

        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        HttpServletRequest originalHttpServletRequest = portalUserSource.getOriginalHttpServletRequest(httpServletRequest);

        /* Start from case view */
        String showCaseId = originalHttpServletRequest.getParameter(PORTLET_CASE_ID_PARAMTER);
        if (showCaseId != null) {
            modelView.addObject(PORTLET_CASE_ID_PARAMTER, showCaseId);
        }

        return modelView;
    }

    @ResourceMapping("loadTask")
    public void loadTask(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = PortletUtil.getOriginalHttpServletRequest(portalUserSource, request);
        HttpServletResponse httpServletResponse = portalUserSource.getHttpServletResponse(response);
        taskViewController.loadTask(originalHttpServletRequest, httpServletResponse);
    }

    @ResourceMapping("performAction")
    @ResponseBody
    public ModelAndView performAction(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = PortletUtil.getOriginalHttpServletRequest(portalUserSource, request);
        return PortletUtil.translate(PORTLET_JSON_RESULT_ROOT_NAME, processesListController.performAction(originalHttpServletRequest));
    }

    @ResourceMapping("saveAction")
    @ResponseBody
    public ModelAndView saveAction(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = PortletUtil.getOriginalHttpServletRequest(portalUserSource, request);
        return PortletUtil.translate(PORTLET_JSON_RESULT_ROOT_NAME, processesListController.saveAction(originalHttpServletRequest));
    }

}

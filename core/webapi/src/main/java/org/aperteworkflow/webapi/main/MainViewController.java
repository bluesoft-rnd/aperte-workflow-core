package org.aperteworkflow.webapi.main;

import org.aperteworkflow.webapi.main.processes.controller.ProcessesListController;
import org.aperteworkflow.webapi.main.processes.controller.TaskViewController;
import org.aperteworkflow.webapi.main.queues.controller.QueuesController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.portlet.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller(value = "MainViewController")
@RequestMapping("VIEW")
/**
 * Portal portlet main controller class. In case of calling servlet request, use portlet resource
 * mapping to obtain portal specific attributes and cookies
 */
public class MainViewController extends AbstractMainController<ModelAndView, RenderRequest>

{
    private static final String PORTLET_JSON_RESULT_ROOT_NAME = "result";

    private static Logger logger = Logger.getLogger(MainViewController.class.getName());

    @Autowired
    private QueuesController queuesController;

    @Autowired
    private TaskViewController taskViewController;

    @Autowired
    private ProcessesListController processesListController;

    @Autowired(required = false)
    private IPortalUserSource portalUserSource;

	@RenderMapping ()
	public ModelAndView handleMainRenderRequest(RenderRequest request, RenderResponse response, Model model)
    {
		System.out.println("MainViewController.handleMainRenderRequest... ");
		
        ModelAndView modelView = new ModelAndView();
        modelView.setViewName("main");
        modelView.addObject(IS_STANDALONE, false);

        processRequest(modelView, request);


        return modelView;
    }



	@Override
	protected void addObject(ModelAndView modelView, String key, Object value) {
		modelView.addObject(key, value);
	}

	@Override
	protected UserData getUserByRequest(IPortalUserSource userSource, RenderRequest request)
    {
		return userSource.getUserByRequest(request);
	}

	@Override
	protected ProcessToolBpmSession getSession(RenderRequest request) {
		return (ProcessToolBpmSession)request.getAttribute(ProcessToolBpmSession.class.getName());
	}

	@Override
	protected void setSession(ProcessToolBpmSession bpmSession, RenderRequest request) {
		request.setAttribute(ProcessToolBpmSession.class.getName(), bpmSession);
	}

    @ResourceMapping( "getUserQueues")
    @ResponseBody
    public ModelAndView getUserQueues(ResourceRequest request, ResourceResponse response)
    {
        HttpServletRequest originalHttpServletRequest = getOriginalHttpServletRequest(request);
        return  translate(PORTLET_JSON_RESULT_ROOT_NAME, queuesController.getUserQueues(originalHttpServletRequest));
    }

    @ResourceMapping( "claimTaskFromQueue")
    @ResponseBody
    public ModelAndView claimTaskFromQueue(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = getOriginalHttpServletRequest(request);
        HttpServletResponse httpServletResponse = portalUserSource.getHttpServletResponse(response);
        return  translate(PORTLET_JSON_RESULT_ROOT_NAME, taskViewController.claimTaskFromQueue(originalHttpServletRequest, httpServletResponse));
    }

    @ResourceMapping( "loadTask")
    public void loadTask(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = getOriginalHttpServletRequest(request);
        HttpServletResponse httpServletResponse = portalUserSource.getHttpServletResponse(response);
        taskViewController.loadTask(originalHttpServletRequest, httpServletResponse);
    }

    @ResourceMapping( "performAction")
    @ResponseBody
    public ModelAndView performAction(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = getOriginalHttpServletRequest(request);
        return  translate(PORTLET_JSON_RESULT_ROOT_NAME, processesListController.performAction(originalHttpServletRequest));
    }

    @ResourceMapping( "saveAction")
    @ResponseBody
    public ModelAndView saveAction(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = getOriginalHttpServletRequest(request);
        return  translate(PORTLET_JSON_RESULT_ROOT_NAME, processesListController.saveAction(originalHttpServletRequest));
    }

    @ResourceMapping( "startNewProcess")
    @ResponseBody
    public ModelAndView startNewProcess(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = getOriginalHttpServletRequest(request);
        return  translate(PORTLET_JSON_RESULT_ROOT_NAME, processesListController.startNewProcess(originalHttpServletRequest));
    }

    @ResourceMapping( "searchTasks")
    @ResponseBody
    public ModelAndView searchTasks(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = getOriginalHttpServletRequest(request);
        return  translate(PORTLET_JSON_RESULT_ROOT_NAME, processesListController.searchTasks(originalHttpServletRequest));
    }


    @ResourceMapping( "loadProcessesList")
    @ResponseBody
    public ModelAndView loadProcessesList(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest originalHttpServletRequest = getOriginalHttpServletRequest(request);

        return  translate(PORTLET_JSON_RESULT_ROOT_NAME, processesListController.loadProcessesList(originalHttpServletRequest));
    }

    /** Obtain http servlet request with additional attributes from ajax request */
    private HttpServletRequest getOriginalHttpServletRequest(ResourceRequest request)
    {
        try {
            HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
            HttpServletRequest originalHttpServletRequest =  portalUserSource.getOriginalHttpServletRequest(httpServletRequest);

            /* Copy all attributes, because portlet attributes do not exist in original request */
            originalHttpServletRequest.getParameterMap().putAll(httpServletRequest.getParameterMap());

            return  originalHttpServletRequest;
        }
        catch(Throwable ex)
        {
            logger.log(Level.SEVERE, "[PORTLET CONTROLLER] Error", ex);
            throw new RuntimeException(ex);
        }

    }

    /** Translate DTO object to json in model and view, which is required for portlet resource serving */
    private ModelAndView translate(String resultName, Object result)
    {
        ModelAndView mav = new ModelAndView();
        MappingJacksonJsonView v = new MappingJacksonJsonView();
        v.setBeanName(resultName);

        mav.setView(v);
        mav.addObject(resultName, result);

        return mav;
    }

}

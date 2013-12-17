package org.aperteworkflow.webapi.main;

import org.aperteworkflow.webapi.main.processes.controller.ProcessesListController;
import org.aperteworkflow.webapi.main.processes.controller.TaskViewController;
import org.aperteworkflow.webapi.main.queues.controller.QueuesController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.portlet.*;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Controller(value = "MainViewController")
@RequestMapping("VIEW")
public class MainViewController extends AbstractMainController<ModelAndView, RenderRequest>

{

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
        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        return  translate("queues", queuesController.getUserQueues(httpServletRequest));
    }

    @ResourceMapping( "claimTaskFromQueue")
    @ResponseBody
    public ModelAndView claimTaskFromQueue(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        HttpServletResponse httpServletResponse = portalUserSource.getHttpServletResponse(response);
        return  translate("data", taskViewController.claimTaskFromQueue(httpServletRequest, httpServletResponse));
    }

    @ResourceMapping( "loadTask")
    public void loadTask(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        HttpServletResponse httpServletResponse = portalUserSource.getHttpServletResponse(response);
        taskViewController.loadTask(httpServletRequest, httpServletResponse);
    }

    @ResourceMapping( "performAction")
    @ResponseBody
    public ModelAndView performAction(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        return  translate("data", processesListController.performAction(httpServletRequest));
    }

    @ResourceMapping( "saveAction")
    @ResponseBody
    public ModelAndView saveAction(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        return  translate("data", processesListController.saveAction(httpServletRequest));
    }

    @ResourceMapping( "startNewProcess")
    @ResponseBody
    public ModelAndView startNewProcess(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        return  translate("data", processesListController.startNewProcess(httpServletRequest));
    }

    @ResourceMapping( "searchTasks")
    @ResponseBody
    public ModelAndView searchTasks(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        return  translate("data", processesListController.searchTasks(httpServletRequest));
    }


    @ResourceMapping( "loadProcessesList")
    @ResponseBody
    public ModelAndView loadProcessesList(ResourceRequest request, ResourceResponse response) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = portalUserSource.getHttpServletRequest(request);
        HttpServletRequest originalHttpServletRequest =  portalUserSource.getOriginalHttpServletRequest(httpServletRequest);
        return  translate("data", processesListController.loadProcessesList(originalHttpServletRequest));
    }

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

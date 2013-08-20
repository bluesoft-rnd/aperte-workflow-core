package org.aperteworkflow.webapi.main;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

@Controller(value = "MainViewController")
@RequestMapping("VIEW")
public class MainViewController extends AbstractMainController<ModelAndView, RenderRequest>
{
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
	protected UserData getUserByRequest(IPortalUserSource userSource, RenderRequest request) {
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
}

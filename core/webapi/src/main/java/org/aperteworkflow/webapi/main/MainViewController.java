package org.aperteworkflow.webapi.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;


@Controller(value = "MainViewController")
@RequestMapping("VIEW")
public class MainViewController extends AbstractMainController
{
	@Autowired
	private ProcessToolRegistry processToolRegistry;
	
	@RenderMapping ()
	public ModelAndView handleMainRenderRequest(RenderRequest request,RenderResponse response,Model model)
    {
        ModelAndView modelView = new ModelAndView();
        modelView.setViewName("main");
        modelView.addObject(IS_STANDALONE, false);

        processRequest(modelView, request);

        return modelView;
    }

    private void processRequest(final ModelAndView modelView, final RenderRequest request)
	{
		IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);
		final UserData user = authorizationService.getUserByRequest(request);
		
		/* No user to process, abort */
		if(user == null)
			return;

		modelView.addObject(USER_PARAMETER_NAME, user);
		
		processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				ProcessToolBpmSession bpmSession = (ProcessToolBpmSession)request.getAttribute(ProcessToolBpmSession.class.getName());
				if(bpmSession == null)
				{
					bpmSession = getRegistry().getProcessToolSessionFactory().createSession(user, user.getRoleNames());
					request.setAttribute(ProcessToolBpmSession.class.getName(), bpmSession);
				}
				
				I18NSource messageSource = I18NSourceFactory.createI18NSource(request.getLocale());
				
				//addUserQueues(modelView, user, ctx, messageSource);
                modelView.addObject(PROCESS_START_LIST, addProcessStartList(ctx, bpmSession));
				
			}
		});
	}
}

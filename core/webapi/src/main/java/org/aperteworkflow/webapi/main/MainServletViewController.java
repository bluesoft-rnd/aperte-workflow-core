package org.aperteworkflow.webapi.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import pl.net.bluesoft.rnd.processtool.BasicSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.DEFAULT_QUEUE_INTERVAL;

@Controller(value = "MainServletViewController")
@RequestMapping(value ="/view")
public class MainServletViewController extends AbstractMainController
{
	@Autowired
	private ProcessToolRegistry processToolRegistry;

    @RequestMapping()
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response)
	{
		ModelAndView modelView = new ModelAndView("index");
        modelView.addObject(IS_STANDALONE, true);

		processRequest(modelView, request);

	    return modelView;
	}

    private void processRequest(final ModelAndView modelView, final HttpServletRequest request)
	{
		processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {

			@Override
			public void withContext(ProcessToolContext ctx) 
			{

                IPortalUserSource userSource = ObjectFactory.create(IPortalUserSource.class);
                final UserData user = userSource.getUserByRequest(request);

		        /* No user to process, abort */
                if(user == null)
                    return;

                modelView.addObject(USER_PARAMETER_NAME, user);

                ProcessToolBpmSession bpmSession = (ProcessToolBpmSession)request.getAttribute(ProcessToolBpmSession.class.getName());
				if(bpmSession == null)
				{
					bpmSession = processToolRegistry.getProcessToolSessionFactory().createSession(user);
					request.setAttribute(ProcessToolBpmSession.class.getName(), bpmSession);
				}
				
                modelView.addObject(PROCESS_START_LIST, addProcessStartList(ctx, bpmSession));
                
				Integer interval = DEFAULT_QUEUE_INTERVAL;
                String refreshInterval = ctx.getSetting(BasicSettings.REFRESHER_INTERVAL_SETTINGS_KEY);
                if (refreshInterval!=null && !refreshInterval.trim().isEmpty()) {
    				try {
						interval = Integer.parseInt(refreshInterval+"000");
					} catch (NumberFormatException e) {}
                }
        		modelView.addObject(QUEUE_INTERVAL,interval);
                
			}
		});
	}
}

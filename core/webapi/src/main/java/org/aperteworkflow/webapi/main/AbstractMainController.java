package org.aperteworkflow.webapi.main;

import org.aperteworkflow.webapi.tools.WebApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.BasicSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;

import javax.servlet.http.HttpServletRequest;

import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.DEFAULT_QUEUE_INTERVAL;

/**
 * User: mpawlak
 * Date: 30.06.13
 * Time: 14:18
 */
public abstract class AbstractMainController<ModelAndViewType>
{


    public static final String PROCESS_START_LIST = "processStartList";
    public static final String QUEUES_PARAMETER_NAME = "queues";
    public static final String IS_STANDALONE = "isStandAlone";
    public static final String QUEUE_INTERVAL = "queueInterval";
    public static final String EXTERNAL_TASK_ID = "externalTaskId";
    public static final String EXTERNAL_QUEUE_ID = "externalQueueId";

	@Autowired(required = false)
    protected ProcessToolRegistry processToolRegistry;



    @Autowired(required = false)
    protected IPortalUserSource portalUserSource;


	protected void processRequest(final ModelAndViewType modelView, final HttpServletRequest request)
	{
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        final UserData user = portalUserSource.getUserByRequest(request);

        /* No user to process, abort */
        if(user == null) {
            return;
        }

		processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
			@Override
			public void withContext(ProcessToolContext ctx)
			{


				addObject(modelView, WebApiConstants.USER_PARAMETER_NAME, user);

				ProcessToolBpmSession bpmSession = getSession(request);

				if(bpmSession == null)
				{
					bpmSession = processToolRegistry.getProcessToolSessionFactory().createSession(user);
					setSession(bpmSession, request);
				}

				addObject(modelView, PROCESS_START_LIST, bpmSession.getAvailableConfigurations());

				Integer interval = DEFAULT_QUEUE_INTERVAL;
				String refreshInterval = ctx.getSetting(BasicSettings.REFRESHER_INTERVAL_SETTINGS_KEY);

				if (refreshInterval!=null && !refreshInterval.trim().isEmpty()) {
					try {
						interval = Integer.parseInt(refreshInterval+"000");
					} catch (NumberFormatException e) {}
				}
				addObject(modelView, QUEUE_INTERVAL, interval);
			}
		});
	}

	protected abstract void addObject(ModelAndViewType modelView, String key, Object value);


    protected ProcessToolBpmSession getSession(HttpServletRequest request) {
        return (ProcessToolBpmSession)request.getAttribute(ProcessToolBpmSession.class.getName());
    }


    protected void setSession(ProcessToolBpmSession bpmSession, HttpServletRequest request) {
        request.setAttribute(ProcessToolBpmSession.class.getName(), bpmSession);
    }
}

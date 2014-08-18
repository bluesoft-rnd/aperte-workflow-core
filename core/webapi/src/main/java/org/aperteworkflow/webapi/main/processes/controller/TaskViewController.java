package org.aperteworkflow.webapi.main.processes.controller;

import org.aperteworkflow.webapi.main.AbstractProcessToolServletController;
import org.aperteworkflow.webapi.main.processes.ActionPseudoTaskBean;
import pl.net.bluesoft.rnd.processtool.web.view.BpmTaskBean;
import org.aperteworkflow.webapi.main.ui.TaskViewBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory.ExecutionType;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

@Controller
public class TaskViewController extends AbstractProcessToolServletController
{
	private static Logger logger = Logger.getLogger(TaskViewController.class.getName());
	
	@RequestMapping(method = RequestMethod.POST, value = "/task/claimTaskFromQueue")
	@ResponseBody
	public BpmTaskBean claimTaskFromQueue(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		logger.info("claimTaskFromQueue ...");
		long t0 = System.currentTimeMillis();

        		/* Initilize request context */
        final IProcessToolRequestContext context = this.initilizeContext(request,getProcessToolRegistry().getProcessToolSessionFactory());
		
		final I18NSource messageSource = context.getMessageSource();

		final String queueName = request.getParameter("queueName");
		final String taskId = request.getParameter("taskId");
		final String userId = request.getParameter("userId");

        BpmTaskBean taskBean = new BpmTaskBean();

        if(isNull(taskId))
		{
            taskBean.addError(SYSTEM_SOURCE, messageSource.getMessage("request.performaction.error.notaskid"));
			return taskBean;
		}
		else if (isNull(userId))
		{
            taskBean.addError(SYSTEM_SOURCE, messageSource.getMessage("request.performaction.error.nouserid"));
            return taskBean;
		}
		

		
		if(!context.isUserAuthorized())
		{
            taskBean.addError(SYSTEM_SOURCE, messageSource.getMessage("request.handle.error.nouser"));
            return taskBean;
		}
		
		long t1 = System.currentTimeMillis();

		taskBean = getProcessToolRegistry().withProcessToolContext(new ReturningProcessToolContextCallback<BpmTaskBean>() {
			@Override
			public BpmTaskBean processWithContext(ProcessToolContext ctx) {
                BpmTask newTask;
                /* Task assigned from virtual queue */
                if(isNull(queueName))
                    newTask = getBpmSession(context, userId).assignTaskToUser(taskId, userId);
                else
                    newTask = getBpmSession(context, userId).assignTaskFromQueue(queueName, taskId);

				if (newTask != null) {
					return BpmTaskBean.createFrom(newTask, messageSource);
				}

				try {
					response.getWriter().print(messageSource.getMessage("request.performaction.error.notask"));
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				return null;
			}
		}, ExecutionType.TRANSACTION_SYNCH);
		
		long t2 = System.currentTimeMillis();

		logger.log(Level.INFO, "claimTaskFromQueue total: " + (t2-t0) + "ms, " +
				"[1]: " + (t1-t0) + "ms, " +
				"[2]: " + (t2-t1) + "ms " 
				);
		
		return taskBean;
	}

	public static ProcessToolBpmSession getBpmSession(IProcessToolRequestContext context, String userLogin) {
		ProcessToolBpmSession userSession = context.getBpmSession();

		if (userSession.getUserLogin().equals(userLogin)) {
			return userSession;
		}

		UserSubstitutionDAO userSubstitutionDAO = getThreadProcessToolContext().getUserSubstitutionDAO();

		if (userSubstitutionDAO.isSubstitutedBy(userLogin, userSession.getUserLogin())) {
			return userSession.createSession(userLogin);
		}
		throw new RuntimeException("Attempting to create session for nonsubstituted user: " + userLogin);
	}

    
	@RequestMapping(method = RequestMethod.POST, value = "/task/loadTask")
	@ResponseBody
	public void loadTask(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
            logger.info("loadTask ...");
            long t0 = System.currentTimeMillis();

            /* Get process state configuration db id */
            final String taskId = request.getParameter("taskId");

            /* Initilize request context */
            final IProcessToolRequestContext context = this.initilizeContext(request,getProcessToolRegistry().getProcessToolSessionFactory());

            if(isNull(taskId))
            {
                response.getWriter().print(context.getMessageSource().getMessage("request.performaction.error.notaskid"));
                return;
            }

            long t1 = System.currentTimeMillis();

            if(!context.isUserAuthorized())
            {
                response.getWriter().print(context.getMessageSource().getMessage("request.handle.error.nouser"));
                return;
            }

            long t2 = System.currentTimeMillis();

            final String output = buildTaskView(getProcessToolRegistry(), context, taskId);

            /* Write to output writer here, so there will be no invalid output
            for error in previous code with session
             */
            response.getWriter().print(output);

            long t3 = System.currentTimeMillis();

            logger.log(Level.INFO, "loadTask total: " + (t3-t0) + "ms, " +
                    "[1]: " + (t1-t0) + "ms, " +
                    "[2]: " + (t2-t1) + "ms, " +
                    "[3]: " + (t3-t2) + "ms, "
                    );
	}

    public static String buildTaskView(final ProcessToolRegistry processToolRegistry, final IProcessToolRequestContext context, final String taskId) {
        final StringBuilder builder = new StringBuilder();

        processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                long t0 = System.currentTimeMillis();

                // reset string buffer
                builder.setLength(0);

                BpmTask task = getBpmTask(context, taskId);

                long t1 = System.currentTimeMillis();

                ProcessStateConfiguration config = task.getCurrentProcessStateConfiguration();
                String processDescription = context.getMessageSource().getMessage(config.getDefinition().getDescription());
                String processVersion = String.valueOf(config.getDefinition().getBpmDefinitionVersion());

                long t2 = System.currentTimeMillis();

                // Load view widgets
                List<ProcessStateWidget> widgets = new ArrayList<ProcessStateWidget>(config.getWidgets());
                Collections.sort(widgets, BY_WIDGET_PRIORITY);

                long t3 = System.currentTimeMillis();

                // Load view actions
                List<ProcessStateAction> actions = new ArrayList<ProcessStateAction>(config.getActions());
                Collections.sort(actions, BY_ACTION_PRIORITY);

                long t4 = System.currentTimeMillis();

                TaskViewBuilder taskViewBuilder = new TaskViewBuilder()
                        .setWidgets(widgets)
                        .setActions(actions)
                        .setDescription(processDescription)
                        .setVersion(processVersion)
                        .setI18Source(context.getMessageSource())
                        .setUser(context.getUser())
                        .setCtx(ctx)
                        .setUserQueues(context.getUserQueues())
                        .setTask(task)
                        .setBpmSession(context.getBpmSession());

                long t5 = System.currentTimeMillis();

                try {
                    builder.append(taskViewBuilder.build());

                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Problem during task view generation. TaskId=" + taskId, ex);
                }
                long t6 = System.currentTimeMillis();

                logger.log(Level.INFO, "loadTask.withContext total: " + (t6-t0) + "ms, " +
                                "[1]: " + (t1-t0) + "ms, " +
                                "[2]: " + (t2-t1) + "ms, " +
                                "[3]: " + (t3-t2) + "ms, " +
                                "[4]: " + (t4-t3) + "ms, " +
                                "[5]: " + (t5-t4) + "ms, " +
                                "[6]: " + (t6-t5) + "ms, "
                );
            }
        }, ExecutionType.TRANSACTION_SYNCH);
        return builder.toString();
    }

	private static BpmTask getBpmTask(IProcessToolRequestContext context, String taskId) {
		String jbpmTaskId = taskId;
		boolean pseudoTask = false;

		if (ActionPseudoTaskBean.isActionPseudotask(taskId)) {
			jbpmTaskId = ActionPseudoTaskBean.extractJbpmTaskId(taskId);
			pseudoTask = true;
		}

		BpmTask task = context.getBpmSession().getTaskData(jbpmTaskId);

		if(task == null) {
			task = context.getBpmSession().getHistoryTask(jbpmTaskId);
		}

		if (pseudoTask) {
			task = ActionPseudoTaskBean.createBpmTask(task, taskId);
		}

		return task;
	}

	private static final Comparator<ProcessStateWidget> BY_WIDGET_PRIORITY = new Comparator<ProcessStateWidget>() {
		@Override
		public int compare(ProcessStateWidget widget1, ProcessStateWidget widget2) {
			return widget1.getPriority().compareTo(widget2.getPriority());
		}
	};

	private static Comparator<ProcessStateAction> BY_ACTION_PRIORITY = new Comparator<ProcessStateAction>() {
		@Override
		public int compare(ProcessStateAction action1, ProcessStateAction action2) {
			if(action1.getPriority() == null)
				return -1;
			if(action2.getPriority() == null)
				return 1;
			return action1.getPriority().compareTo(action2.getPriority());
		}
	};

	private static boolean isNull(String value) {
		return value == null || value.isEmpty() || "null".equals(value);
	}
}

package pl.net.bluesoft.rnd.pt.ext.deadline;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolException;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.*;
import pl.net.bluesoft.rnd.pt.ext.sched.service.ProcessToolSchedulerService;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;
import pl.net.bluesoft.util.lang.Strings;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

public class DeadlineEngine {
    private static final Logger logger = Logger.getLogger(DeadlineEngine.class.getName());

    @Autowired
    private ProcessToolRegistry registry;

    @Autowired
    private IUserRolesManager userRolesManager;

    @Autowired
    private I18NSourceFactory i18NSourceFactory;

    private Properties pluginProperties;

    public DeadlineEngine(Properties pluginProperties) throws SchedulerException
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        this.pluginProperties = pluginProperties;
    }

    public void init() {
        registry.getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                registry.withProcessToolContext(new ProcessToolContextCallback() {
                    @Override
                    public void withContext(ProcessToolContext ctx) 
                    {
                        ProcessToolBpmSession bpmSession = getRegistry().getProcessToolSessionFactory().createAutoSession();
                        Session session = ctx.getHibernateSession();
                        List<ProcessInstance> instances = loadProcessesWithDeadlines(session);

                        for (ProcessInstance pi : instances) {
                            if (pi.isProcessRunning()) {
                                Collection<BpmTask> tasks = bpmSession.findProcessTasks(pi);

								for (BpmTask task : tasks) {
									if (task.getAssignee() != null) {
										ProcessDeadline deadline = pi.getDeadline(task);

										if (deadline != null) {
											scheduleDeadline(pi.getInternalId(), deadline);
										}
									}
								}
                            }
                        }
                    }
                });
            }
        });
    }

    private List<ProcessInstance> loadProcessesWithDeadlines(Session session) {
        DetachedCriteria pdc = DetachedCriteria.forClass(ProcessDeadline.class)
                .add(Restrictions.or(Restrictions.eq("alreadyNotified", Boolean.FALSE), Restrictions.isNull("alreadyNotified")))
                .createAlias("processInstance", "pi")
                .setProjection(Projections.distinct(Projections.property("pi.id")));

        return session.createCriteria(ProcessInstance.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Subqueries.propertyIn("id", pdc)).list();
    }

    public void destroy() {
        ProcessToolSchedulerService service = getSchedulerService();
        service.cancelScheduledJobGroup(HandleDeadlineJob.class.getName());
    }

    private ProcessToolSchedulerService getSchedulerService() {
        return registry.getRegisteredService(ProcessToolSchedulerService.class);
    }

    private void scheduleDeadline(String processInternalId, ProcessDeadline pd) {
        ProcessToolSchedulerService service = getSchedulerService();
        Date currentDate = new Date(), dueDate = pd.getDueDate();
        if (DateUtils.isSameDay(dueDate, currentDate) || currentDate.before(dueDate) || !pd.isAlreadyNotified()) {
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("processInstanceId", processInternalId);
            dataMap.put("deadlineAttribute", pd);
            dataMap.put("deadlineEngine", this);

            String taskName = pd.getTaskName();
            String identity = "pi:" + processInternalId + ";task:" + taskName;

            JobDetail jobDetail = JobBuilder.newJob(HandleDeadlineJob.class)
                    .withIdentity(identity, HandleDeadlineJob.class.getName())
                    .usingJobData(dataMap)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(identity, HandleDeadlineJob.class.getName())
                    .startAt(dueDate)
                    .forJob(jobDetail)
                    .build();

            logger.finest("Scheduling deadline job handler at: " + dueDate + " for process instance: " + processInternalId
                    + " and task name: " + taskName);
            service.scheduleJob(jobDetail, trigger);
        }
    }

    public void onProcessStateChange(final BpmTask task, ProcessInstance pi, boolean processInitiated)
    {
        if (pi == null || pi.getId() == null || task == null) {
            logger.finest("Event contained no persistent process instance and task. Omitting.");
            return;
        }

        String internalId = pi.getInternalId();
        logger.finest("Processing deadlines for process: " + internalId);
        ProcessToolContext ctx = getThreadProcessToolContext();
        pi = ctx.getProcessInstanceDAO().getProcessInstance(pi.getId());
        if (pi == null) {
            throw new ProcessToolException("Unable to find process instance by internal id: " + internalId);
        }
		ProcessDeadline deadline = pi.getDeadline(task);

		if (deadline != null) {
			scheduleDeadline(pi.getInternalId(), deadline);
		}
    }

    public void handleDeadlineJob(final String processInstanceId, final ProcessDeadline processDeadline) 
    {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                try {
                    signalDeadline(processInstanceId, processDeadline);
                }
                catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception while sending deadline notification", e);
                }
            }
        }, ProcessToolContextFactory.ExecutionType.TRANSACTION);
    }

    private void signalDeadline(String processInstanceId, ProcessDeadline processDeadline) throws Exception {
		ProcessToolContext ctx = getThreadProcessToolContext();
		ProcessInstance pi = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(processInstanceId);
        ProcessToolBpmSession bpmSession = getRegistry().getProcessToolSessionFactory().createAutoSession();
        List<BpmTask> tasks = bpmSession.findProcessTasks(pi);
        
    	ITemplateDataProvider templateDataProvider = new TemplateDataProvider();
        
        for (BpmTask task : tasks) {
            if (task.getTaskName().equals(processDeadline.getTaskName())) {
                String assigneeLogin = task.getAssignee();
				Map<String, UserData> notifyUsers = prepareUsersForNotification(assigneeLogin, processDeadline);

                for (UserData user : notifyUsers.values()) {
                    if (processDeadline.isSkipAssignee() && user.getLogin().equals(assigneeLogin)) {
                        logger.info("Skipping deadline signal for assignee: " + assigneeLogin);
                        continue;
                    }
                    TemplateData templateData = templateDataProvider.createTemplateData(processDeadline.getTemplateName(), Locale.getDefault());
                    templateDataProvider
                    	.addProcessData(templateData, pi)
                    	.addTaskData(templateData, task)
                    	.addUserToNotifyData(templateData, user);
                    
                    templateData.addEntry("notifiedUser", user);
                    templateData.addEntry("assignedUser", notifyUsers.get(assigneeLogin));
                    
                    NotificationData notificationData = new NotificationData();
                    notificationData
                    	.setProfileName("Default")
                    	.setRecipient(user)
                    	.setTemplateData(templateData);

					logger.finest("Signaling deadline for task: " + task.getTaskName() + " owned by: " + assigneeLogin + ", mailed to: " + user.getLogin());

					EmailSender.sendEmail(getBpmNotifications(), notificationData);
                }
                processDeadline.setAlreadyNotified(true);
                ctx.getHibernateSession().saveOrUpdate(processDeadline);
            }
        }
    }

	private IBpmNotificationService getBpmNotifications() {
		return registry.getRegisteredService(IBpmNotificationService.class);
	}

	private I18NSource getI18NSource() {
		String defaultLocale = pluginProperties.getProperty("default.locale");
		Locale locale = Strings.hasText(defaultLocale) ? new Locale(defaultLocale) : Locale.getDefault();
		return i18NSourceFactory.createI18NSource(locale);
	}

	private Map<String, UserData> prepareUsersForNotification(String assigneeLogin, ProcessDeadline processDeadline) {
        Map<String, UserData> notifyUsers;
        List<String> userLogins = new ArrayList<String>();
        userLogins.add(assigneeLogin);
        if (Strings.hasText(processDeadline.getNotifyUsersWithLogin())) {
            for (String login : processDeadline.getNotifyUsersWithLogin().split(",")) {
                if (Strings.hasText(login)) {
                    userLogins.add(login);
                }
            }
        }
        notifyUsers = loadUsersAsMap(userLogins);
        if (!notifyUsers.containsKey(assigneeLogin)) {
            throw new ProcessToolException("Unable to find task assignee with login: " + assigneeLogin);
        }
        if (Strings.hasText(processDeadline.getNotifyUsersWithRole())) {
            for (String role : processDeadline.getNotifyUsersWithRole().split(",")) {
                if (Strings.hasText(role)) 
                {

                	
                    for (UserData userWithRole : userRolesManager.getUsersByRole(role)) {
                        notifyUsers.put(userWithRole.getLogin(), userWithRole);
                    }
                }
            }
        }
        return notifyUsers;
    }

    private Map<String, UserData> loadUsersAsMap(List<String> userLogins) {
        Map<String, UserData> users = new HashMap<String, UserData>();

        for (String login : userLogins) {
            if (users.get(login) == null) 
            {
                UserData user = getRegistry().getUserSource().getUserByLogin(login);
                
                if (user == null) {
                    logger.warning("Unable to find user by login: " + login);
                }
                else {
                    users.put(login, user);
                }
            }
        }
        return Collections.filterValues(users, new Predicate<UserData>() {
            @Override
            public boolean apply(UserData input) {
                return input != null;
            }
        });
    }
}

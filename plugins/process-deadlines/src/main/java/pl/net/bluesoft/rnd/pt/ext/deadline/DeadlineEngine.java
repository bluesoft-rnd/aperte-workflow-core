package pl.net.bluesoft.rnd.pt.ext.deadline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolException;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.EmailSender;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.ITemplateDataProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateDataProvider;
import pl.net.bluesoft.rnd.pt.ext.sched.service.ProcessToolSchedulerService;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.Transformer;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

public class DeadlineEngine {
    private static final Logger logger = Logger.getLogger(DeadlineEngine.class.getName());

    private ProcessToolRegistry registry;

    private Properties pluginProperties;

    public DeadlineEngine(ProcessToolRegistry registry, Properties pluginProperties) throws SchedulerException {
        this.registry = registry;
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
                            if (bpmSession.isProcessRunning(pi.getInternalId())) {
                                Set<ProcessDeadline> deadlines = pi.findAttributesByClass(ProcessDeadline.class);
                                Collection<BpmTask> tasks = bpmSession.findProcessTasks(pi);
                                for (ProcessDeadline pd : deadlines) {
                                    for (BpmTask task : tasks) {
                                        if (task.getTaskName().equals(pd.getTaskName()) && task.getAssignee() != null) {
                                            scheduleDeadline(pi.getInternalId(), pd);
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
        if (DateUtils.isSameDay(dueDate, currentDate) || currentDate.before(dueDate) || !pd.isAlreadyProcessed()) {
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

            logger.info("Scheduling deadline job handler at: " + dueDate + " for process instance: " + processInternalId
                    + " and task name: " + taskName);
            service.scheduleJob(jobDetail, trigger);
        }
    }

    public void onProcessStateChange(final BpmTask task, ProcessInstance pi, boolean processInitiated) {
        if (pi == null || pi.getId() == null) {
            logger.info("Event contained no persistent process instance. Omitting.");
            return;
        }
        String internalId = pi.getInternalId();
        logger.info("Processing deadlines for process: " + internalId);
        ProcessToolContext ctx = getThreadProcessToolContext();
        pi = ctx.getProcessInstanceDAO().getProcessInstance(pi.getId());
        if (pi == null) {
            throw new ProcessToolException("Unable to find process instance by internal id: " + internalId);
        }
        Set<ProcessDeadline> deadlines = pi.findAttributesByClass(ProcessDeadline.class);
        if (!deadlines.isEmpty()) {
            logger.info("Found deadline configurations for process: " + pi.getInternalId());
            ProcessToolBpmSession bpmSession = getRegistry().getProcessToolSessionFactory().createAutoSession();
            List<BpmTask> tasks = processInitiated || task == null ? bpmSession.findProcessTasks(pi) : new ArrayList<BpmTask>() {{
                add(task);
            }};
            if (!tasks.isEmpty()) {
                Set<String> taskNames = new HashSet<String>();
                Collections.collect(tasks, new Transformer<BpmTask, String>() {
                    @Override
                    public String transform(BpmTask obj) {
                        return obj.getTaskName();
                    }
                }, taskNames);
                logger.info("Found tasks for process: " + pi.getInternalId());
                for (ProcessDeadline da : deadlines) {
                    if (taskNames.contains(da.getTaskName())) {
                        scheduleDeadline(pi.getInternalId(), da);
                    }
                }
            }
            else {
                logger.info("No tasks found for process: " + pi.getInternalId());
            }
        }
        else {
            logger.info("No deadlines found for process: " + pi.getInternalId());
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
        });
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
				Map<String, UserData> notifyUsers = prepareUsersForNotification(ctx, assigneeLogin, processDeadline);

                // everything is good, unless it’s not
				I18NSource messageSource = getI18NSource();
                ProcessStateConfiguration st = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(task);
                String taskName = messageSource.getMessage(st.getDescription());

                for (UserData user : notifyUsers.values()) {
                    if (processDeadline.getSkipAssignee() != null && processDeadline.getSkipAssignee() && user.getLogin().equals(assigneeLogin)) {
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

					logger.info("Signaling deadline for task: " + task.getTaskName() + " owned by: " + assigneeLogin + ", mailed to: " + user.getLogin());

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
		return I18NSourceFactory.createI18NSource(locale);
	}

	private Map<String, UserData> prepareUsersForNotification(ProcessToolContext ctx, String assigneeLogin, ProcessDeadline processDeadline) {
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
        notifyUsers = loadUsersAsMap(ctx, userLogins);
        if (!notifyUsers.containsKey(assigneeLogin)) {
            throw new ProcessToolException("Unable to find task assignee with login: " + assigneeLogin);
        }
        if (Strings.hasText(processDeadline.getNotifyUsersWithRole())) {
            for (String role : processDeadline.getNotifyUsersWithRole().split(",")) {
                if (Strings.hasText(role)) 
                {
                	IUserRolesManager rolesManager = ObjectFactory.create(IUserRolesManager.class);
                	
                    for (UserData userWithRole : rolesManager.getUsersByRole(role)) {
                        notifyUsers.put(userWithRole.getLogin(), userWithRole);
                    }
                }
            }
        }
        return notifyUsers;
    }

    private Map<String, UserData> loadUsersAsMap(ProcessToolContext ctx, List<String> userLogins) {
        Map<String, UserData> users = ctx.getUserDataDAO().loadUsersByLogin(userLogins);
        for (String login : userLogins) {
            if (users.get(login) == null) 
            {
            	IUserSource userSource = ObjectFactory.create(IUserSource.class);
                UserData user = userSource.getUserByLogin(login);
                
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

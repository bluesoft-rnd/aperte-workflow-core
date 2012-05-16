package pl.net.bluesoft.rnd.pt.ext.deadline;

import org.hibernate.Session;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.HolidayCalendar;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.BpmTask;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolNotificationTemplate;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateLoader;
import pl.net.bluesoft.rnd.pt.ext.deadline.model.DeadlineNotificationTemplate;
import pl.net.bluesoft.rnd.pt.utils.template.TemplateProcessor;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeadlineEngine implements ProcessToolTemplateLoader {
    private static final Logger logger = Logger.getLogger(DeadlineEngine.class.getName());

    private ProcessToolRegistry registry;
    private Scheduler scheduler;
    private TemplateProcessor templateProcessor;

    private Properties mailProperties;
    private Properties schedulerProperties;

    public DeadlineEngine(ProcessToolRegistry registry) throws SchedulerException {
        this.registry = registry;
        this.mailProperties = loadProperties("/pl/net/bluesoft/rnd/pt/ext/deadline/mail.properties");
        this.schedulerProperties = loadProperties("/pl/net/bluesoft/rnd/pt/ext/deadline/quartz.properties");
        this.templateProcessor = new TemplateProcessor(this);
        startScheduler();
    }

    public void startScheduler() throws SchedulerException {
        scheduler = new StdSchedulerFactory(schedulerProperties).getScheduler();
        scheduler.addCalendar(DeadlineEngine.class.getName(), new HolidayCalendar(), true, true);
        scheduler.start();
    }

    public void stopScheduler() throws SchedulerException {
        scheduler.clear();
        scheduler.shutdown();
    }

    public void setupProcessDeadlines() {
        UserData autoUser = new UserData();
        autoUser.setLogin("deadlinenotify");
        autoUser.setEmail("none@none.none");
        autoUser.setRealName("Deadline Notify user");

        ProcessToolContext context = ProcessToolContext.Util.getThreadProcessToolContext();
        ProcessToolBpmSession bpmSession = context.getProcessToolSessionFactory().createSession(autoUser, new HashSet<String>());

        Session session = context.getHibernateSession();
        List<ProcessDeadline> deadlines = session.createCriteria(ProcessDeadline.class).list();
        for (ProcessDeadline attr : deadlines) {
            ProcessInstance pi = attr.getProcessInstance();
            if (bpmSession.isProcessRunning(pi.getInternalId(), context)) {
                Collection<BpmTask> tasks = bpmSession.getTaskList(pi, context);
                for (BpmTask task : tasks) {
                    if (pi.getState().equals(task.getTaskName()) && pi.getState().equals(attr.getTaskName()) && task.getOwner() != null) {
                        processDeadline(pi, task.getOwner(), task.getTaskName(), attr.getDueDate(), attr.getTemplateName());
                    }
                }
            }
        }
    }

    private Properties loadProperties(String path) {
        Properties p = new Properties();
        try {
            p.load(getClass().getResourceAsStream(path));
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return p;
    }

    private void processDeadline(ProcessInstance pi, UserData ud, String taskName, Date dueDate, String templateName) {
        Date currentDate = new Date();
        if (currentDate.before(dueDate)) {
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("processInstanceId", pi.getInternalId());
            dataMap.put("taskName", taskName);
            dataMap.put("templateName", templateName);
            dataMap.put("userLogin", ud.getLogin());
            dataMap.put("deadlineEngine", this);

            String identity = pi.getInternalId() + "_" + taskName;
            JobDetail handleDeadlineJob = JobBuilder.newJob(HandleDeadlineJob.class)
                    .withIdentity(identity)
                    .usingJobData(dataMap)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(identity)
                    .startAt(dueDate)
                    .forJob(handleDeadlineJob)
                    .build();

            logger.info("Scheduling deadline job handler at: " + dueDate + " for process instance: " + pi.getInternalId() + " and task name: " + taskName);
            try {
                scheduler.unscheduleJob(trigger.getKey());
                scheduler.scheduleJob(handleDeadlineJob, trigger);
            }
            catch (SchedulerException e) {
                logger.log(Level.SEVERE, "Error while scheduling job: " + e.getMessage(), e);
            }
        }
    }

    public void onProcessStateChange(ProcessInstance pi, UserData ud) {
        Set<ProcessDeadline> deadlines = pi.findAttributesByClass(ProcessDeadline.class);
        if (!deadlines.isEmpty()) {
            for (ProcessDeadline da : deadlines) {
                if (pi.getState().equals(da.getTaskName())) {
                    processDeadline(pi, ud, pi.getState(), da.getDueDate(), da.getTemplateName());
                }
            }
        }
    }

    public void handleDeadlineJob(final String processInstanceId, final String taskName, final String userLogin, final String templateName) {
        registry.withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                ProcessToolContext.Util.setThreadProcessToolContext(ctx);
                try {
                    processDeadlineJob(ctx, processInstanceId, userLogin, taskName, templateName);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception while sending deadline notification", e);
                } finally {
                    ProcessToolContext.Util.removeThreadProcessToolContext();
                }
            }
        });
    }

    private void processDeadlineJob(ProcessToolContext ctx, String processInstanceId, String userLogin, String taskName, String templateName)
            throws Exception {
        ProcessInstance pi = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(processInstanceId);
        if (pi.getState().equals(taskName)) {
            UserData ud = ctx.getUserDataDAO().loadUserByLogin(userLogin);
            Map dataModel = new HashMap();
            dataModel.put("process", pi);
            dataModel.put("user", ud);
            logger.info("Signaling deadline for user: " + ud.getLogin());
            templateProcessor.processEmail(templateName, dataModel, ud.getEmail(), mailProperties, true);
        }
    }

    @Override
    public List<ProcessToolNotificationTemplate> loadTemplates() {
        List<ProcessToolNotificationTemplate> templates = new ArrayList<ProcessToolNotificationTemplate>();
        Session session = ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
        templates.addAll(session.createCriteria(DeadlineNotificationTemplate.class).list());
        return templates;
    }
}

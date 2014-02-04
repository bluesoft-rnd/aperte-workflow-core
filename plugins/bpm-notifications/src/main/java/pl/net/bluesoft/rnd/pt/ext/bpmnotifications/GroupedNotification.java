package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.di.annotations.AutoInject;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.ITemplateDataProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationHistory;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationHistoryEntry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.ProcessedNotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentDescription;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.IMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.settings.NotificationsSettingsProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.templates.IMailTemplateLoader;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

public class GroupedNotification implements IBpmNotificationService {
	
    private static final long CONFIG_DEFAULT_CACHE_REFRESH_INTERVAL = 5* 1000;
    
    private static final String SUBJECT_TEMPLATE_SUFFIX = "_subject";
    private static final String SENDER_TEMPLATE_SUFFIX = "_sender";
    private static final String DEFAULT_PROFILE_NAME = "Default";
    private static final String REFRESH_INTERVAL = "mail.settings.refresh.interval";
    
    /** Mail body encoding */
    private static final String MAIL_ENCODING = "UTF-8";
    
    private static final Logger logger = Logger.getLogger(GroupedNotification.class.getName());

    private Collection<BpmNotificationConfig> configCache = new HashSet<BpmNotificationConfig>();
    
    private long cacheUpdateTime;
    private long refrshInterval;

    private ProcessToolBpmSession bpmSession;
    
    @Autowired
    private IUserSource userSource;
    @Autowired
    private ProcessToolRegistry processToolRegistry;

    /** Data provider for standard e-mail template */
    @AutoInject
    private ITemplateDataProvider templateDataProvider;
    
    private ProcessToolRegistry registry;
    

    /** Provider for mail main session and mail connection properties */
    @AutoInject
    private IMailSessionProvider mailSessionProvider;

    /** Provider for email templates */
    @AutoInject
    private IMailTemplateLoader templateProvider;

	private NotificationHistory history = new NotificationHistory(1000);
	
	final I18NSource messageSource = I18NSourceFactory.createI18NSource(Locale.getDefault());
    
    public GroupedNotification(ProcessToolRegistry registry)
    {
    	this.registry = registry;
    	
    	
    	try {init();}catch (Exception e){}
    }
    
    /** Initialize all providers and configurations */
    private void init()
    {
    	if(ProcessToolContext.Util.getThreadProcessToolContext() != null)
    	{
    		initComponents();
    	}
    		
    	else
    	{
	        registry.withProcessToolContext(new ProcessToolContextCallback() 
	        {
				@Override
				public void withContext(ProcessToolContext ctx)
				{	
					initComponents();
				}
	        });
    	}
    }
    
    private void initComponents()
    {

        /* Inject dependencies */
        ObjectFactory.inject(this);
    	
    	readRefreshIntervalFromSettings();
    	
        /* Refresh config for providers */
        templateProvider.refreshConfig();
        mailSessionProvider.refreshConfig();
        
        logger.info("[NOTIFICATIONS] Grouped notifications engine initialized");
    }
    
    /** The method check if there are any new notifications in database to be sent */
    public void handleNotifications(){
        registry.withProcessToolContext(new ProcessToolContextCallback() 
        {
			@Override
			public void withContext(ProcessToolContext ctx)
			{
				handleNotificationsWithContext();
			}
        });
    }
    
    /** The method check if there are any new notifications in database to be sent */
    public void handleNotificationsWithContext(){
    	
    	logger.info("[NOTIFICATIONS JOB] Checking assigned tasks for notify users... ");
    	
    	SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    	
    	List<UserData> users = userSource.getAllUsers();
    	String isGroup = "";
    	
    	for(UserData u: users){    		
    		try {
    			isGroup = u.getAttribute(messageSource.getMessage("bpmnot.notify.liferay.groupingCheckbox")).toString();
    			
    		} catch(Exception e){
    			logger.log(Level.SEVERE, "Add custom field true/false for grouping notifications. Property: bpmnot.notify.liferay.groupingCheckbox=key");
    		}
    		
    		if(isGroup.equals("true")){
    			Date d = (Date)u.getAttribute(messageSource.getMessage("bpmnot.notify.liferay.groupingSendHour"));

    			Calendar cal = Calendar.getInstance();
    			Calendar notificationTime = Calendar.getInstance();
    			notificationTime.setTime(d);
    			
        		cal.set(Calendar.SECOND, notificationTime.get(Calendar.SECOND));
        		cal.set(Calendar.HOUR_OF_DAY, notificationTime.get(Calendar.HOUR_OF_DAY));
        		cal.set(Calendar.MINUTE, notificationTime.get(Calendar.MINUTE));
        		d = cal.getTime();
        		
    			Long now = new Date().getTime();
    			Date nowPlus = new Date(now + (5 * 60000));
    			
    			if((new Date()).after(d) && d.before(nowPlus)){
    				BpmNotification notification = createNotificationFromTasks(u);
    	    		
    	    		try {
    	    			if(notification.getRecipient() != null){
    	    				sendNotification(notification);
    	    			}
    					
    				} catch (Exception e) {
    					// TODO Auto-generated catch block
    					logger.log(Level.SEVERE, "Unable to send notification." , e);
    				}
    			}
    		}
    	}
    }
    
    /** Read config refresh rate */
    private void readRefreshIntervalFromSettings()
    {
    	String refreshIntervalString = NotificationsSettingsProvider.getRefreshInterval();
    	
    	if(refreshIntervalString == null)
    	{
    		refrshInterval = CONFIG_DEFAULT_CACHE_REFRESH_INTERVAL;
    	}
    	else
    	{
    		refrshInterval = Long.parseLong(refreshIntervalString);
    	}
    		
    }
    
    public BpmNotification createNotificationFromTasks(UserData user){
		refreshConfigIfNecessary();
		
		BpmNotification notification = new BpmNotification();
		bpmSession = getRegistry().getProcessToolSessionFactory().createSession(user.getLogin());
		List<BpmTask> tasks = bpmSession.findUserTasks(0, 50);
		String body = messageSource.getMessage("bpmnot.grouped.notify.email.body.greeting") + "<br/>";
		
		for(BpmTask t: tasks){
			if(notification.getSender() == null){
				if( t.getProcessInstance().getExternalKey() !=null){
					body = body + messageSource.getMessage("bpmnot.grouped.notify.email.external.key")
							+ " " + t.getProcessInstance().getExternalKey();
				}	
				else{
					body = body + messageSource.getMessage("bpmnot.grouped.notify.email.new.process");
				}
				
				body = body + messageSource.getMessage("bpmnot.grouped.notify.email.step")
						+ messageSource.getMessage(t.getCurrentProcessStateConfiguration().getDescription())
						+ messageSource.getMessage("bpmnot.grouped.notify.email.created.by") 
						+ userSource.getUserByLogin(t.getProcessInstance().getCreatorLogin()).getRealName() + "</br></br>";
				
				notification.setBody(body);
				notification.setRecipient(user.getEmail());
				notification.setSendAsHtml(true);
				notification.setSender(messageSource.getMessage("bpmnot.grouped.notify.email.from"));
				notification.setProfileName(messageSource.getMessage("bpmnot.grouped.notify.email.profile.name"));
				notification.setSubject(messageSource.getMessage("bpmnot.grouped.notify.email.subject"));
				
			} 
			else {
				if( t.getProcessInstance().getExternalKey() !=null){
					body = body + messageSource.getMessage("bpmnot.grouped.notify.email.external.key") 
							+ " " + t.getProcessInstance().getExternalKey();
				}
				else {
					body = body + messageSource.getMessage("bpmnot.grouped.notify.email.new.process");
				}
				
				body = body + messageSource.getMessage("bpmnot.grouped.notify.email.step") 
						+ messageSource.getMessage(t.getCurrentProcessStateConfiguration().getDescription())
						+ messageSource.getMessage("bpmnot.grouped.notify.email.created.by") 
						+ userSource.getUserByLogin(t.getProcessInstance().getCreatorLogin()).getRealName() + "</br></br>";
				
				notification.setBody(body);
			}
		}
		return notification;
	}
	
   private void sendNotification(BpmNotification notification) throws Exception{
    	javax.mail.Session mailSession = mailSessionProvider.getSession(notification.getProfileName());
    	
    	/* Create javax mail message from notification bean */
        Message message = BpmNotificationEngine.createMessageFromNotification(notification, mailSession);
        
        try {
	    	/* If smtps is required, force diffrent transport properties */
	    	if(isSmtpsRequired(mailSession))
	    	{
	    		Properties emailPrtoperties = mailSession.getProperties();
	    		
	    		String secureHost = emailPrtoperties.getProperty("mail.smtp.host");
	    		String securePort = emailPrtoperties.getProperty("mail.smtp.port");
	    		String userName = emailPrtoperties.getProperty("mail.smtp.user");
	    		String userPassword = emailPrtoperties.getProperty("mail.smtp.password");
	    		

	            Transport transport = mailSession.getTransport("smtps");
	            transport.connect(secureHost, Integer.parseInt(securePort), userName, userPassword);
	            transport.sendMessage(message, message.getAllRecipients());
	           
	            transport.close();
	    	}
	    	/* Default transport mechanism */
	    	else
	    	{
	    		Transport.send(message);
	    	}

			//history.notificationSent(notification);

	    	logger.info("Emails sent");
        }
        catch (Exception e) {
			//history.errorWhileSendingNotification(notification, e);

            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
   
   private boolean isSmtpsRequired(javax.mail.Session mailSession){
		Properties emailPrtoperties = mailSession.getProperties();
		String transportProtocol = emailPrtoperties.getProperty("mail.transport.protocol");
		
		return "smtps".equals(transportProtocol);
		
   }
   @SuppressWarnings("unchecked")
 	public synchronized void refreshConfigIfNecessary(){
         if (cacheUpdateTime + refrshInterval < System.currentTimeMillis())
         {
             Session session = ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
             configCache = session
                     .createCriteria(BpmNotificationConfig.class)
                     .add(Restrictions.eq("active", true))
                     .addOrder(Order.asc("id"))
                     .list();

             cacheUpdateTime = System.currentTimeMillis();
             
             /* Update cache refresh rate 8 */
             readRefreshIntervalFromSettings();
             
             /* Inject dependencies */
             ObjectFactory.inject(this);
             
             /* Refresh config for providers */
             templateProvider.refreshConfig();
             mailSessionProvider.refreshConfig();

             bpmSession = getRegistry().getProcessToolSessionFactory().createAutoSession();
             
             logger.info("Mail configuration updated. Interval is set to "+refrshInterval);
         }

     }
    
	@Override
	public void addNotificationToSend(ProcessedNotificationData notificationData)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void addNotificationToSend(NotificationData notificationData)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public TemplateData createTemplateData(String templateName, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITemplateDataProvider getTemplateDataProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessedNotificationData processNotificationData(
			NotificationData notificationData) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findTemplate(String templateName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String processTemplate(String templateName, TemplateData templateData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerTemplateArgumentProvider(
			TemplateArgumentProvider provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterTemplateArgumentProvider(
			TemplateArgumentProvider provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<TemplateArgumentProvider> getTemplateArgumentProviders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TemplateArgumentDescription> getDefaultArgumentDescriptions(
			I18NSource i18nSource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<NotificationHistoryEntry> getNotificationHistoryEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invalidateCache() {
		// TODO Auto-generated method stub

	}
	
	
}

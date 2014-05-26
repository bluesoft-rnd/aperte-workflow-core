package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.Strings.hasText;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

import java.net.ConnectException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.LockAcquisitionException;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.di.annotations.AutoInject;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade.NotificationsFacade;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmAttachment;
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

/**
 * E-mail notification engine. 
 * 
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class BpmNotificationEngine implements IBpmNotificationService 
{
    private static final long CONFIG_DEFAULT_CACHE_REFRESH_INTERVAL = 5* 1000;
    
    private static final String SUBJECT_TEMPLATE_SUFFIX = "_subject";
    private static final String SENDER_TEMPLATE_SUFFIX = "_sender";
    private static final String DEFAULT_PROFILE_NAME = "Default";
    private static final String REFRESH_INTERVAL = "mail.settings.refresh.interval";
    
    /** Mail body encoding */
    private static final String MAIL_ENCODING = "UTF-8";
    
    private static final Logger logger = Logger.getLogger(BpmNotificationEngine.class.getName());

    private Collection<BpmNotificationConfig> configCache = new HashSet<BpmNotificationConfig>();
    
    private long cacheUpdateTime;
    private long refrshInterval;

    private ProcessToolBpmSession bpmSession;
    

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
    
    public BpmNotificationEngine(ProcessToolRegistry registry)
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
        
        logger.info("[NOTIFICATIONS] Notifications engine initialized");
    }
    
    /** The method check if there are any new notifications in database to be sent */
    public void handleNotifications()
    {
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
    public void handleNotificationsWithContext()
    {
    	logger.info("[NOTIFICATIONS JOB] Checking awaiting notifications... ");

		try
		{
	    	/* Get all notifications waiting to be sent */
	    	Collection<BpmNotification> notificationsToSend = NotificationsFacade.getNotificationsToSend();
	    	
	    	/* Get all notifications waiting to be sent */
	    	Collection<BpmNotification> notificationsToSendForGrouping = NotificationsFacade.getNotificationsForGrouping();
	    	notificationsToSend.addAll(notificationsToSendForGrouping);
	    	
	    	logger.info("[NOTIFICATIONS JOB] "+notificationsToSend.size()+" notifications waiting to be sent...");
	    	
	    	Map<String,BpmNotification> notificationsToSendMap = new HashMap<String, BpmNotification>();
	    	
	    	for(BpmNotification notification: notificationsToSend)
	    	{
	    		if (notification.isGroupNotifications()){
	    			BpmNotification groupedNotif = notificationsToSendMap.get(notification.getRecipient());
	    			
	    			if (groupedNotif != null ){
	    			
	    				notificationsToSendMap.remove(groupedNotif.getRecipient());
	    				String body = groupedNotif.getBody();
	    				groupedNotif.setSubject(messageSource.getMessage("bpmnot.notify.subject.for.grouped.email"));
		    			
		    			body += "</br></br>" + notification.getSubject();
		    			groupedNotif.setBody(body);
		    			
		    			notificationsToSendMap.put(groupedNotif.getRecipient(), groupedNotif);
		    			NotificationsFacade.removeNotification(notification);
	    			}
	    			else{
	    				notification.setBody(notification.getSubject());
	    				notificationsToSendMap.put(notification.getRecipient(), notification);
	    				NotificationsFacade.removeNotification(notification);
	    			}
	    		}
	    		else{
	    			notificationsToSendMap.put(Long.toString(notification.getNotificationCreated().getTime()), notification);
	    			NotificationsFacade.removeNotification(notification);
	    		}
	    	}
	    	
	    	for(BpmNotification notification: notificationsToSendMap.values())
	    	{	
	    		try
	    		{
	    			sendNotification(notification);
	    			
	    			/* Notification was sent, so remove it from te queue */
	    			//NotificationsFacade.removeNotification(notification);
	    		}
	    		catch(ConnectException ex)
	    		{
	    			logger.log(Level.SEVERE, "[NOTIFICATIONS JOB] Could not connect to server", ex);
	    			
	    			history.errorWhileSendingNotification(notification, ex);
	    			
	    			/* End loop, host is invalid or down */
	    			break;
	    		}
	    		catch(Exception ex)
	    		{
	    			logger.log(Level.SEVERE, "[NOTIFICATIONS JOB] Problem during notification sending", ex);
	    			
	    			history.errorWhileSendingNotification(notification, ex);
	    		}
	    	}
		}
		/* Table is locked, end transation */
		catch(LockAcquisitionException ex)
		{
		}
    }
    
    
    public void onProcessStateChange(BpmTask task, ProcessInstance pi, String userLogin, boolean processStarted,
									 boolean processEnded, boolean enteringStep) {
        refreshConfigIfNecessary();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

//		logger.log(Level.INFO, "BpmNotificationEngine processes " + configCache.size() + " rules");
		
        for (BpmNotificationConfig cfg : configCache) {
            try {
            	if(enteringStep != cfg.isOnEnteringStep()) 
            		continue;

            	if(processStarted != cfg.isNotifyOnProcessStart()) 
            		continue;
            	
				if (processEnded != cfg.isNotifyOnProcessEnd()) 
					continue;
				
				if (cfg.isNotifyOnProcessEnd() && task.getProcessInstance().getParent() != null) 
					continue;
				
                if (hasText(cfg.getProcessTypeRegex()) && !pi.getDefinitionName().toLowerCase().matches(cfg.getProcessTypeRegex().toLowerCase())) 
                    continue;
               
                if (!(!hasText(cfg.getStateRegex()) || task != null && task.getTaskName().toLowerCase().matches(cfg.getStateRegex().toLowerCase()))) {
                    continue;
                }
                if (hasText(cfg.getLastActionRegex())) {
                	String lastAction = pi.getSimpleAttributeValue("ACTION");
                	if (lastAction == null || !lastAction.toLowerCase().matches(cfg.getLastActionRegex().toLowerCase())) {
                        continue;
                	}
                }
                logger.info("Matched notification #" + cfg.getId() + " for process state change #" + pi.getInternalId());
                List<UserData> recipients = new LinkedList<UserData>();
                if (task != null && cfg.isNotifyTaskAssignee()) {
                    UserData assignee = getRegistry().getUserSource().getUserByLogin(task.getAssignee());
                    if (cfg.isSkipNotificationWhenTriggeredByAssignee() &&
                            assignee != null &&
                            assignee.getLogin() != null &&
                            assignee.getLogin().equals(userLogin)) {
                        logger.info("Not notifying user " + assignee.getLogin() + " - this user has initiated processed action");
                        continue;
                    }
                    if (assignee != null && hasText(assignee.getEmail())) {
                    	recipients.add(assignee);
                        logger.info("Notification will be sent to " + assignee.getEmail());
                    }
                }
                if (hasText(cfg.getNotifyEmailAddresses())) 
                {
                	for(String userEmail: cfg.getNotifyEmailAddresses().split(","))
                	{
                		UserData recipient = getRegistry().getUserSource().getUserByEmail(userEmail);
                		recipients.add(recipient);
                	}
                }
				if (hasText(cfg.getNotifyUserAttributes())) 
				{
					recipients.addAll(extractUsers(cfg.getNotifyUserAttributes(), ctx, pi));
				}
                if (recipients.isEmpty()) {
                    logger.info("Despite matched rules, no emails qualify to notify for cfg #" + cfg.getId());
                    continue;
                }
                String templateName = cfg.getTemplateName();
                Locale locale = cfg.getLocale() != null ? new Locale(cfg.getLocale()) : Locale.getDefault();
                
                for(UserData recipient: recipients)
                {
	                /* Crate new template data */
	                TemplateData templateData = createTemplateData(templateName, locale);
	                templateDataProvider
	                	.addTaskData(templateData, task)
	                	.addProcessData(templateData, pi)
	                	.addUserToNotifyData(templateData, getRegistry().getUserSource().getUserByLogin(userLogin))
	                	.addArgumentProvidersData(templateData, cfg, pi)
	                	.addContextAdditionalData(templateData, cfg, bpmSession);
	                
	                NotificationData notificationData = new NotificationData();
	                notificationData
	                	.setProfileName(DEFAULT_PROFILE_NAME)
	                	.setTemplateData(templateData)
	                	.setRecipient(recipient);
	                                
	                addNotificationToSend(notificationData);
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
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

	private Collection<UserData> extractUsers(String notifyUserAttributes, ProcessToolContext ctx, ProcessInstance pi) {
		pi = ctx.getProcessInstanceDAO().refresh(pi);

		Collection<UserData> users = new HashSet<UserData>();
		for (String attribute : notifyUserAttributes.split(",")) {
			attribute = attribute.trim();
			if(attribute.matches("#\\{.*\\}")){
	        	String loginKey = attribute.replaceAll("#\\{(.*)\\}", "$1");
	        	attribute = pi.getInheritedSimpleAttributeValue(loginKey);
				if(attribute != null && attribute.matches("#\\{.*\\}")) {
					continue;
				}
	        }
			if (hasText(attribute)) {
				UserData user = getRegistry().getUserSource().getUserByLogin(attribute);
				users.add(user);
			}
		}
		return users;
	}


	@Override
	public void registerTemplateArgumentProvider(TemplateArgumentProvider provider) 
	{
		templateDataProvider.registerTemplateArgumentProvider(provider);
	}

	@Override
	public void unregisterTemplateArgumentProvider(TemplateArgumentProvider provider) 
	{
		templateDataProvider.unregisterTemplateArgumentProvider(provider);
	}

	@Override
	public Collection<TemplateArgumentProvider> getTemplateArgumentProviders() {
		return templateDataProvider.getTemplateArgumentProviders();
	}

	@Override
	public List<TemplateArgumentDescription> getDefaultArgumentDescriptions(I18NSource i18NSource) {
		return templateDataProvider.getDefaultArgumentDescriptions(i18NSource);
	}

	@Override
	public List<NotificationHistoryEntry> getNotificationHistoryEntries() {
		return history.getRecentEntries();
	}

	@Override
	public synchronized void invalidateCache() {
		cacheUpdateTime = 0;
	}

    @SuppressWarnings("unchecked")
	public synchronized void refreshConfigIfNecessary()
    {
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
    
    public void addNotificationToSend(NotificationData notificationData) throws Exception
    {
    	ProcessedNotificationData processedNotificationData = processNotificationData(notificationData);
    	
    	addNotificationToSend(processedNotificationData);
    }
   
    
    /** Methods add notification to queue for notifications to be sent in the
     * next scheduler job run
     * 
     */
    public void addNotificationToSend(ProcessedNotificationData processedNotificationData) throws Exception 
    {    	
        if (!processedNotificationData.hasSender()) 
        {
            UserData autoUser = getRegistry().getAutoUser();
            processedNotificationData.setSender(autoUser.getEmail());
        }
        
        if (processedNotificationData.getRecipient() == null)
        {
            throw new IllegalArgumentException("Cannot send email: Recipient is null!");
        }
        
    	/* Transform DTO to DAO's */

    	BpmNotification notification = new BpmNotification();
        notification.setSender(processedNotificationData.getSender());
        notification.setSubject(processedNotificationData.getSubject());
        notification.setBody(processedNotificationData.getBody());
        notification.setRecipient(processedNotificationData.getRecipient().getEmail());
        notification.setSendAsHtml(processedNotificationData.isSendAsHtml());
        notification.setProfileName(processedNotificationData.getProfileName());
        String isGroup = null;
        
        try
        {
        	isGroup = processedNotificationData.getRecipient().getAttribute(messageSource.getMessage("bpmnot.notify.liferay.groupingCheckbox")).toString();
        }
        catch(Exception e)
        {
        	logger.log(Level.SEVERE, "Add custom field true/false for grouping notifications. Property: bpmnot.notify.liferay.groupingCheckbox=key");
        }
        
        if(isGroup == null) {
        	notification.setGroupNotifications(false);
        }
        
        if (notification.isGroupNotifications()){
        	Date d = (Date)processedNotificationData.getRecipient().getAttribute(messageSource.getMessage("bpmnot.notify.liferay.groupingSendHour"));
        	Calendar cal = Calendar.getInstance();
    		cal.set(Calendar.SECOND, d.getSeconds());
    		cal.set(Calendar.HOUR_OF_DAY, d.getHours());
    		cal.set(Calendar.MINUTE, d.getMinutes());
    		
    		//int time = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);
	        
	        notification.setSendAfterHour(cal.getTime());
        }

        notification.encodeAttachments(processedNotificationData.getAttachments());
        
        NotificationsFacade.addNotificationToBeSent(notification);

		history.notificationEnqueued(notification);
		
    	logger.info("EmailSender email sent: sender=" + processedNotificationData.getSender() 
    			+ "\n recipientEmail=" + processedNotificationData.getRecipient().getEmail()  
    			+ "\n subject=" + processedNotificationData.getSubject() 
    			+ "\n body=" + processedNotificationData.getBody());
    }
    
    private void sendNotification(BpmNotification notification) throws Exception 
    {
    	javax.mail.Session mailSession = mailSessionProvider.getSession(notification.getProfileName());
    	
    	/* Create javax mail message from notification bean */
        Message message = createMessageFromNotification(notification, mailSession);
        
        try 
        {
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

			history.notificationSent(notification);

	    	logger.info("Emails sent");
        }
        catch (Exception e) 
        {
			history.errorWhileSendingNotification(notification, e);

            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public static Message createMessageFromNotification(BpmNotification notification, javax.mail.Session mailSession) throws Exception 
    {
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(notification.getSender()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(notification.getRecipient()));

		String recipientSubstiteEmails = getRecipientSubstiteEmails(notification);

		if (recipientSubstiteEmails != null) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(recipientSubstiteEmails));
            logger.info(String.format("Sending email to %s with CC %s", notification.getRecipient(), recipientSubstiteEmails));
		}

		message.setSubject(notification.getSubject());
        message.setSentDate(new Date());
        //body
        MimeBodyPart messagePart = new MimeBodyPart();
        messagePart.setContent(notification.getBody(), (notification.getSendAsHtml() ? "text/html" : "text/plain") + "; charset=\""+MAIL_ENCODING+"\"");
        
        Multipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(messagePart);

        //zalaczniki

        if(notification.getAttachments() != null && !notification.getAttachments().isEmpty())
        {
			List<BpmAttachment> attachments = notification.decodeAttachments();
	        
	        for (BpmAttachment attachment : attachments) {
		        MimeBodyPart attachmentPart = new MimeBodyPart();
				ByteArrayDataSource ds = new ByteArrayDataSource(attachment.getBody(), attachment.getContentType());

				attachmentPart.setDataHandler(new DataHandler(ds));
		        attachmentPart.setFileName(attachment.getName());
		        multipart.addBodyPart(attachmentPart);

				if (logger.isLoggable(Level.INFO)) {
					logger.info("Added attachment " + attachment.getName());
				}
	        }       
        }
        
        message.setContent(multipart);
        message.setSentDate(new Date());
 
        
        return message;
    }

	private static String getRecipientSubstiteEmails(BpmNotification notification) {
		if (!hasText(notification.getRecipient())) {
			 return null;
		}
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

		UserData recipient = getRegistry().getUserSource().getUserByEmail(notification.getRecipient());

		if (recipient == null) {
			return null;
		}

        List <String> substitutesLogins = ctx.getUserSubstitutionDAO().getCurrentSubstitutedUserLogins(recipient.getLogin());

		if (substitutesLogins.isEmpty()) {
			return null;
		}

		Set<String> emails = new TreeSet<String>();

		for (String substituteLogin : substitutesLogins)
        {
            UserData substitute = getRegistry().getUserSource().getUserByLogin(substituteLogin);
			if (hasText(substitute.getEmail())) {
				emails.add(substitute.getEmail());
			}
		}
		return from(emails).ordered().toString(",");
	}

	/** Check if tranport protocol is set to smtps */
    private boolean isSmtpsRequired(javax.mail.Session mailSession)
    {
		Properties emailPrtoperties = mailSession.getProperties();
		String transportProtocol = emailPrtoperties.getProperty("mail.transport.protocol");
		
		return "smtps".equals(transportProtocol);
    }

	@Override
	public String findTemplate(String templateName)
	{
		refreshConfigIfNecessary();
		return templateProvider.findTemplate(templateName);
	}

	@Override
	public String processTemplate(String templateName, TemplateData templateData)
	{
		refreshConfigIfNecessary();
		try
		{
			String messageBody = templateProvider.processTemplate(templateName,templateData);
			return messageBody;
		}
		/* There was a unknown variable used */
		catch(ProcessToolTemplateErrorException ex)
		{
			return "[ERROR] There was a problem with message template! <br>Plase contact administrator and send him following error message: <br>"+ex.getMessage();
		}
	}

	@Override
	public TemplateData createTemplateData(String templateName, Locale locale) 
	{
		return templateDataProvider.createTemplateData(templateName, locale);
	}

	@Override
	public ProcessedNotificationData processNotificationData(NotificationData notificationData) throws Exception 
	{
    	String body = processTemplate(notificationData.getTemplateData().getTemplateName(), notificationData.getTemplateData());
    	String topic = processTemplate(notificationData.getTemplateData().getTemplateName() + SUBJECT_TEMPLATE_SUFFIX, notificationData.getTemplateData());
    	String sender = findTemplate(notificationData.getTemplateData().getTemplateName() + SENDER_TEMPLATE_SUFFIX);
    	
        if (body == null || topic == null || sender == null) {
        	throw new Exception("Error sending email. Cannot find valid template configuration");
        }
        
        ProcessedNotificationData processedNotificationData = new ProcessedNotificationData(notificationData);
        processedNotificationData
        	.setBody(body)
        	.setSubject(topic)
        	.setSender(sender);

		return processedNotificationData;
	}

	@Override
	public ITemplateDataProvider getTemplateDataProvider() {
		return templateDataProvider;
	}
}

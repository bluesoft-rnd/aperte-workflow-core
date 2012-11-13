package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import static pl.net.bluesoft.util.lang.Strings.hasText;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade.NotificationsFacade;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentDescription;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.DatabaseMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.IMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.JndiMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.templates.MailTemplateProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.util.NotificationHistory;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationHistoryEntry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Strings;

/**
 * E-mail notification engine. 
 * 
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class BpmNotificationEngine implements BpmNotificationService 
{
    private static final long CONFIG_DEFAULT_CACHE_REFRESH_INTERVAL = 5* 1000;
    
    private static final String SUBJECT_TEMPLATE_SUFFIX = "_subject";
    private static final String PROVIDER_TYPE = "mail.settings.provider.type";
    private static final String REFRESH_INTERVAL = "mail.settings.refresh.interval";
    
    /** Mail body encoding */
    private static final String MAIL_ENCODING = "UTF-8";
    
    private static final Logger logger = Logger.getLogger(BpmNotificationEngine.class.getName());

    private Collection<BpmNotificationConfig> configCache = new HashSet<BpmNotificationConfig>();

    private long cacheUpdateTime;
    private long refrshInterval;

    private ProcessToolBpmSession bpmSession;
    
    /** Data provider for standard e-mail template */
    private TemplateDataProvider templateDataProvider;
    
    private ProcessToolRegistry registry;
    
    /** Provider for mail main session and mail connection properties */
    private IMailSessionProvider mailSessionProvider;
    
    /** Provider for email templates */
    private MailTemplateProvider templateProvider;

	private NotificationHistory history = new NotificationHistory(1000);
    
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
					ProcessToolContext.Util.setThreadProcessToolContext(ctx);
					
					initComponents();
				}
	        });
    	}
    }
    
    private void initComponents()
    {
    	/* Register simple providers */
    	templateProvider = new  MailTemplateProvider();
    	templateDataProvider = new TemplateDataProvider();
    	
    	readRefreshIntervalFromSettings();
    	
    	registerMailSettingProvider();
    	
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
				ProcessToolContext.Util.setThreadProcessToolContext(ctx);
				
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
	    	
	    	logger.info("[NOTIFICATIONS JOB] "+notificationsToSend.size()+" notifications waiting to be sent...");
	    	
	    	for(BpmNotification notification: notificationsToSend)
	    	{
	    		try
	    		{
	    			sendNotification(notification);
	    			
	    			/* Notification was sent, so remove it from te queue */
	    			NotificationsFacade.removeNotification(notification);
	    		}
	    		catch(Exception ex)
	    		{
	    			logger.log(Level.SEVERE, "[NOTIFICATIONS JOB] Problem during notification sending", ex);
	    		}
	    	}
		}
		/* Table is locked, end transation */
		catch(Exception ex)
		{

		}
    }
    
    
    public void onProcessStateChange(BpmTask task, ProcessInstance pi, UserData userData, boolean processStarted,
									 boolean processEnded, boolean enteringStep) {
        refreshConfigIfNecessary();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

//		logger.log(Level.INFO, "BpmNotificationEngine processes " + configCache.size() + " rules");
		
        for (BpmNotificationConfig cfg : configCache) {
            try {
            	if(enteringStep != cfg.isOnEnteringStep()) {
//            		logger.info("Not matched notification #" + cfg.getId() + ": enteringStep=" + enteringStep );
            		continue;
            	}
            	if(processStarted != cfg.isNotifyOnProcessStart()) {
//            		logger.info("Not matched notification #" + cfg.getId() + ": processStarted=" + processStarted );
            		continue;
            	}
				if (processEnded != cfg.isNotifyOnProcessEnd()) {
					continue;
				}
				if (cfg.isNotifyOnProcessEnd() && task.getProcessInstance().getParent() != null) {
					continue;
				}
                if (hasText(cfg.getProcessTypeRegex()) && !pi.getDefinitionName().toLowerCase().matches(cfg.getProcessTypeRegex().toLowerCase())) {
//            		logger.info("Not matched notification #" + cfg.getId() + ":pra pi.getDefinitionName()=" + pi.getDefinitionName() );
                    continue;
                }
                if (!(
					(!hasText(cfg.getStateRegex()) || (task != null && task.getTaskName().toLowerCase().matches(cfg.getStateRegex().toLowerCase())))
				)) {
//            		logger.info("Not matched notification #" + cfg.getId() + ": task.getTaskName()=" + task.getTaskName() );
                    continue;
                }
                if (hasText(cfg.getLastActionRegex())) {
                	String lastAction = pi.getSimpleAttributeValue("ACTION");
                	if (lastAction == null || !lastAction.toLowerCase().matches(cfg.getLastActionRegex().toLowerCase())) {
//                		logger.info("Not matched notification #" + cfg.getId() + ": lastAction=" + lastAction );
                        continue;
                	}
                }
                logger.info("Matched notification #" + cfg.getId() + " for process state change #" + pi.getInternalId());
                List<String> emailsToNotify = new LinkedList<String>();
                if (task != null && cfg.isNotifyTaskAssignee()) {
                    UserData owner = task.getOwner();
                    if (cfg.isSkipNotificationWhenTriggeredByAssignee() &&
                            owner != null &&
                            owner.getLogin() != null &&
                            owner.getLogin().equals(userData.getLogin())) {
                        logger.info("Not notifying user " + owner.getLogin() + " - this user has initiated processed action");
                        continue;
                    }
                    if (owner != null && hasText(owner.getEmail())) {
                        emailsToNotify.add(owner.getEmail());
                        logger.info("Notification will be sent to " + owner.getEmail());
                    }
                }
                if (hasText(cfg.getNotifyEmailAddresses())) {
                    emailsToNotify.addAll(Arrays.asList(cfg.getNotifyEmailAddresses().split(",")));
                }
				if (hasText(cfg.getNotifyUserAttributes())) {
					emailsToNotify.addAll(extractUserEmails(cfg.getNotifyUserAttributes(), ctx, pi));
				}
                if (emailsToNotify.isEmpty()) {
                    logger.info("Despite matched rules, no emails qualify to notify for cfg #" + cfg.getId());
                    continue;
                }
                String templateName = cfg.getTemplateName();
                
                BpmNotificationTemplate template = templateProvider.getBpmNotificationTemplate(templateName);

                Map<String, Object> data = templateDataProvider.prepareData(bpmSession, task, pi, userData, cfg, ctx);
                String body = processTemplate(templateName, data);
                String subject = processTemplate(templateName + SUBJECT_TEMPLATE_SUFFIX, data);

                
                /* Add all notification to queue */
                for (String rcpt : new HashSet<String>(emailsToNotify)) {
                	addNotificationToSend("Default", template.getSender(), rcpt, subject, body, cfg.isSendHtml());
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
    
    /** Register mail session provider. There is support for:
     * <li> Database configuration (mail.settings.provider.type = database)
     * <li> JNDI resource configuration (mail.settings.provider.type = jndi)
     * 
     * If configuration in pt_settings is not set, default is database
     */
    private void registerMailSettingProvider()
    {	
    	/* Look for configuration for mail provider. If none exists, default is database */
    	String providerName = ProcessToolContext.Util.getThreadProcessToolContext().getSetting(PROVIDER_TYPE);
    	
    	if(providerName == null)
    	{
    		logger.warning("Mail session provider type is not set, using default database provider");
    		mailSessionProvider = new DatabaseMailSessionProvider();
    	}
    	else if(providerName.equals("database"))
    	{
    		logger.info("Mail session provider set to database");
    		mailSessionProvider = new DatabaseMailSessionProvider();
    	}
    	else if(providerName.equals("jndi"))
    	{
    		logger.info("Mail session provider set to jndi resources");
    		mailSessionProvider = new JndiMailSessionProvider();
    	}
    	else
    	{
    		logger.severe("Unknown provider ["+providerName+"]! Service will be stopped");
    		//throw new IllegalArgumentException("Unknown provider ["+providerName+"]! Service will be stopped");
    	}
    	
    	
    }
    
    /** Read config refresh rate */
    private void readRefreshIntervalFromSettings()
    {
    	String refreshIntervalString = ProcessToolContext.Util.getThreadProcessToolContext().getSetting(REFRESH_INTERVAL);
    	
    	if(refreshIntervalString == null)
    	{
    		refrshInterval = CONFIG_DEFAULT_CACHE_REFRESH_INTERVAL;
    	}
    	else
    	{
    		refrshInterval = Long.parseLong(refreshIntervalString);
    	}
    		
    }

	private Collection<String> extractUserEmails(String notifyUserAttributes, ProcessToolContext ctx, ProcessInstance pi) {
		pi = ctx.getProcessInstanceDAO().refresh(pi);

		Set<String> emails = new HashSet<String>();
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
				UserData user = ctx.getUserDataDAO().loadUserByLogin(attribute);
				emails.add(user.getEmail());
			}
		}
		return emails;
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
	public synchronized void refreshConfigIfNecessary() {
        if (cacheUpdateTime + refrshInterval < System.currentTimeMillis()) {
            Session session = ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
            configCache = session
                    .createCriteria(BpmNotificationConfig.class)
                    .add(Restrictions.eq("active", true))
                    .addOrder(Order.asc("id"))
                    .list();

            cacheUpdateTime = System.currentTimeMillis();
            
            /* Update cache refresh rate 8 */
            readRefreshIntervalFromSettings();
            
            registerMailSettingProvider();
            
            /* Refresh config for providers */
            templateProvider.refreshConfig();
            mailSessionProvider.refreshConfig();

            bpmSession = ProcessToolContext.Util.getThreadProcessToolContext().getProcessToolSessionFactory().createAutoSession();
            
            logger.info("Mail configuration updated. Interval is set to "+refrshInterval);
        }

    }
    
    /** Methods add notification to queue for notifications to be sent in the
     * next scheduler job run
     * 
     */
    public void addNotificationToSend(String profileName, String sender, String recipient, String subject, String body, boolean sendAsHtml,  String ... attachments) throws Exception
    {
    	Collection<String> attachmentsCollection = new ArrayList<String>();
    	
    	for(String attachment: attachments)
    		attachmentsCollection.add(attachment);
    	
    	addNotificationToSend(profileName, sender, recipient, subject, body, sendAsHtml, attachmentsCollection);
    }
    
    /** Methods add notification to queue for notifications to be sent in the
     * next scheduler job run
     * 
     */
    public void addNotificationToSend(String profileName, String sender, String recipient, String subject, String body, boolean sendAsHtml, Collection<String> attachments) throws Exception 
    {
        if (!Strings.hasText(sender)) {
            UserData autoUser = ProcessToolContext.Util.getThreadProcessToolContext().getAutoUser();
            sender = autoUser.getEmail();
        }
        
        if (!Strings.hasText(recipient)) {
            throw new IllegalArgumentException("Cannot send email: Recipient is null!");
        }
        
        BpmNotification notification = new BpmNotification();
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setSendAsHtml(sendAsHtml);
        notification.setProfileName(profileName);
        
        StringBuilder attachmentsString = new StringBuilder();
        int attachmentsSize = attachments.size();
        for(String attachment: attachments)
        {
        	attachmentsString.append(attachment);
        	attachmentsSize--;
        	
        	if(attachmentsSize > 0)
        		attachmentsString.append(",");
        }
        
        notification.setAttachments(attachmentsString.toString());
        
        NotificationsFacade.addNotificationToBeSent(notification);

		history.notificationEnqueued(notification);
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
        message.setSubject(notification.getSubject());
        message.setSentDate(new Date());
        //body
        MimeBodyPart messagePart = new MimeBodyPart();
        messagePart.setContent(notification.getBody(), (notification.getSendAsHtml() ? "text/html" : "text/plain") + "; charset=\""+MAIL_ENCODING+"\"");
        
        Multipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(messagePart);

        //zalaczniki
        int counter = 0;
        URL url;
        
        if(notification.getAttachments() != null && !notification.getAttachments().isEmpty())
        {
	        String[] attachments = notification.getAttachments().split(",");
	        
	        for (String u : attachments) {
	        	if (!Strings.hasText(u))
	        		continue;
	        	url = new URL(u);
		        MimeBodyPart attachmentPart = new MimeBodyPart();
		        URLDataSource urlDs = new URLDataSource(url);
		        attachmentPart.setDataHandler(new DataHandler(urlDs));
		        attachmentPart.setFileName("file" + counter++);
		        multipart.addBodyPart(attachmentPart);
		        logger.info("Added attachment " + u);
	        }       
        }
        
        message.setContent(multipart);
        message.setSentDate(new Date());
 
        
        return message;
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
	public String processTemplate(String templateName, Map<String, Object> data)
	{
		refreshConfigIfNecessary();
		return templateProvider.processTemplate(templateName,data);
	}
}

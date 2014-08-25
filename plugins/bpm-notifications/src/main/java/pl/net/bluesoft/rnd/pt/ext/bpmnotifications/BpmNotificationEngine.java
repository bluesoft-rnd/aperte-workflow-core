package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.dao.impl.UserSubstitutionDAOImpl;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.di.annotations.AutoInject;
import pl.net.bluesoft.rnd.processtool.hibernate.lock.OperationWithLock;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade.NotificationsFacade;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade.NotificationsJdbcFacade;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmAttachment;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.*;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.IMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.settings.NotificationsSettingsProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.templates.IMailTemplateLoader;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.Strings.hasText;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

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
    private static final String NOTIFICATION_LOCK_NAME = "notificationLock";
    /** Mail body encoding */
    private static final String MAIL_ENCODING = "UTF-8";

    private static final String GROUP_MAIL_ROLE = "GROUP_MAIL";

    private static final Logger logger = Logger.getLogger(BpmNotificationEngine.class.getName());

    private Collection<BpmNotificationConfig> configCache = new HashSet<BpmNotificationConfig>();
    
    private long cacheUpdateTime;
    private long refrshInterval;

    private ProcessToolBpmSession bpmSession;

    ReentrantLock lock = new ReentrantLock();
    

    /** Data provider for standard e-mail template */
    @AutoInject
    private ITemplateDataProvider templateDataProvider;

    @Autowired
    private ProcessToolRegistry registry;
    

    /** Provider for mail main session and mail connection properties */
    @AutoInject
    private IMailSessionProvider mailSessionProvider;

    /** Provider for email templates */
    @AutoInject
    private IMailTemplateLoader templateProvider;

	private NotificationHistory history = new NotificationHistory(1000);
	
	final I18NSource messageSource = I18NSourceFactory.createI18NSource(Locale.getDefault());

	private final Set<NotificationSentListener> notificationSentListeners = new LinkedHashSet<NotificationSentListener>();
    
    public BpmNotificationEngine()
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    	
    	
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
        try {
            if (lock.tryLock()) {
                registry.withOperationLock(new OperationWithLock<Object>() {
                    @Override
                    public Object action()
                    {
                        handleNotificationsWithContext();
                        return null;
                    }
                }, NOTIFICATION_LOCK_NAME, OperationLockMode.IGNORE, 1);
            } else {
                logger.info("Previous execution did not finish. Skipping.");
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE,"Error while performing job: " + ex.getMessage(),ex);
        } finally {
            lock.unlock();
        }

    }
    
    /** The method check if there are any new notifications in database to be sent */
    public void handleNotificationsWithContext()
    {
        Connection connection = null;
        try
        {
            connection = registry.getDataRegistry().getDataSourceProxy().getConnection();
            connection.setAutoCommit(false);
            logger.info("[NOTIFICATIONS JOB] Checking awaiting notifications... ");

        /* Get all notifications waiting to be sent */
            Collection<BpmNotification> notificationsToSend = NotificationsJdbcFacade.getNotificationsToSend(connection, 30000, 10);

            logger.info("[NOTIFICATIONS JOB] " + notificationsToSend.size() + " notifications waiting to be sent...");

            Map<String, BpmNotification> notificationsToSendMap = new HashMap<String, BpmNotification>();

            for (BpmNotification notification : notificationsToSend) {
                if (notification.isGroupNotifications()) {
                    BpmNotification groupedNotif = notificationsToSendMap.get(notification.getRecipient());

                    if (groupedNotif != null) {

                        notificationsToSendMap.remove(groupedNotif.getRecipient());
                        String body = groupedNotif.getBody();
                        groupedNotif.setSubject(messageSource.getMessage("bpmnot.notify.subject.for.grouped.email"));

                        body += "</br></br>" + notification.getSubject();
                        groupedNotif.setBody(body);

                        notificationsToSendMap.put(groupedNotif.getRecipient(), groupedNotif);
                    } else {
                        notification.setBody(notification.getSubject());
                        notificationsToSendMap.put(notification.getRecipient(), notification);
                    }
                } else {
                    notificationsToSendMap.put(Long.toString(notification.getNotificationCreated().getTime()), notification);
                }
            }

            handleNotificationsToSend(connection, notificationsToSendMap);
        }
        catch (Throwable e)
        {
            try {
                connection.rollback();
                logger.log(Level.SEVERE, "[NOTIFICATIONS JOB] Could not connect to server", e);
                throw new RuntimeException("Problem with notifications", e);
            }
            catch (SQLException ex)
            {
                throw new RuntimeException("Problem with connection", e);
            }
        }
        finally
        {
            try {
                if(connection != null) {
                    connection.close();
                }
            }
            catch (SQLException ex)
            {
                logger.log(Level.SEVERE, "Problem with connection closing!");
                throw new RuntimeException("Problem with connection", ex);
            }
        }

    }

    public void handleNotificationsToSend(Connection connection, Map<String,BpmNotification> notificationsToSendMap) throws SQLException {
        for(BpmNotification notification: notificationsToSendMap.values())
        {
            try
            {
                sendNotification(connection, notification);

                /* Notification was sent, so remove it from te queue */
                NotificationsJdbcFacade.removeNotification(connection, notification);

                connection.commit();
            }
            catch(ConnectException ex)
            {
                logger.log(Level.SEVERE, "[NOTIFICATIONS JOB] Could not connect to server", ex);

                history.errorWhileSendingNotification(notification, ex);

                /* End loop, host is invalid or down */
                throw new RuntimeException("Problem during notification sending...", ex);
            }
            catch(Exception ex)
            {

                logger.log(Level.SEVERE, "[NOTIFICATIONS JOB] Problem during notification sending", ex);

                history.errorWhileSendingNotification(notification, ex);

                throw new RuntimeException("Problem during notification sending...", ex);
            }
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
	                	.addArgumentProvidersData(templateData, cfg.getTemplateArgumentProvider(), pi)
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
        Boolean isGroup = null;


        Boolean aperteDelayEmails = (Boolean)processedNotificationData.getRecipient().getAttribute("aperte-delay-emails");
        if(aperteDelayEmails != null && aperteDelayEmails)
        {
            isGroup = true;
            Date d = (Date)processedNotificationData.getRecipient().getAttribute("aperte-delay-wait-until");
            int time = 1000*((d.getHours()) * 3600 + d.getMinutes() * 60 + d.getSeconds());
            notification.setSendAfterHour(time);
        }
        else if(processedNotificationData.getRecipient() != null && processedNotificationData.getRecipient().hasRole(GROUP_MAIL_ROLE))
        {
            isGroup = true;
            int time = 1000*(15 * 3600);//GMT+2 17:00
            notification.setSendAfterHour(time);
        }
        else
        {
            isGroup = false;
        }

		if(isGroup == null)
            logger.log(Level.SEVERE, "Add custom field true/false for grouping notifications. Property: bpmnot.notify.liferay.groupingCheckbox=key");

        else
            notification.setGroupNotifications(isGroup);

        notification.encodeAttachments(processedNotificationData.getAttachments());
		notification.setSource(processedNotificationData.getSource());
        
        NotificationsFacade.addNotificationToBeSent(notification);

		history.notificationEnqueued(notification);
		
    	logger.info("EmailSender email sent: sender=" + processedNotificationData.getSender() 
    			+ "\n recipientEmail=" + processedNotificationData.getRecipient().getEmail()  
    			+ "\n subject=" + processedNotificationData.getSubject() 
    			+ "\n body=" + processedNotificationData.getBody());
    }
    
    private void sendNotification(Connection connection, BpmNotification notification) throws Exception
    {
    	javax.mail.Session mailSession = mailSessionProvider.getSession(notification.getProfileName());
    	
    	/* Create javax mail message from notification bean */
        Message message = createMessageFromNotification(connection, notification, mailSession);
        
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

			fireNotificationSent(notification, message);

			history.notificationSent(notification);

	    	logger.info("Emails sent");
        }
        catch (Exception e) 
        {
			history.errorWhileSendingNotification(notification, e);

            logger.log(Level.SEVERE, e.getMessage(), e);

            throw new RuntimeException("Problem during notification sending...", e);
        }
    }
    
    public static Message createMessageFromNotification(Connection connection, BpmNotification notification, javax.mail.Session mailSession) throws Exception
    {
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(notification.getSender()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(notification.getRecipient()));

		String recipientSubstiteEmails = getRecipientSubstiteEmails(connection, notification);

		if (recipientSubstiteEmails != null) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(recipientSubstiteEmails));
            logger.info(String.format("Sending email to %s with CC %s", notification.getRecipient(), recipientSubstiteEmails));
		}

		message.setSubject(notification.getSubject());
        message.setSentDate(new Date());

		List<ExtractedImage> extractedImages = extractImages(notification);

        //body

		MimeBodyPart bodyPart = createBodyPart(notification);
		MimeMultipart bodyContent = wrapInMimeMultipart(bodyPart, "related");
		MimeMultipart content = wrapInMimeMultipart(wrapContentInBodyPart(bodyContent), "alternative");

        //zalaczniki

		if(notification.hasAttachments()) {
			content = wrapInMimeMultipart(wrapContentInBodyPart(content), "mixed");
		}

		if(notification.hasAttachments()) {
			List<BpmAttachment> attachments = notification.decodeAttachments();

	        for (BpmAttachment attachment : attachments) {
				MimeBodyPart attachmentPart = createAttachmentPart(attachment);

		        content.addBodyPart(attachmentPart);

				if (logger.isLoggable(Level.INFO)) {
					logger.info("Added attachment " + attachment.getName());
				}
	        }       
        }

		for (int i = 0; i < extractedImages.size(); ++i) {
			ExtractedImage extractedImage = extractedImages.get(i);

			MimeBodyPart imagePart = createInlineImagePart(extractedImage, i);

			bodyContent.addBodyPart(imagePart);
		}

		message.setContent(content);
        message.setSentDate(new Date());

        return message;
    }

	private static MimeBodyPart wrapContentInBodyPart(MimeMultipart content) throws MessagingException {
		MimeBodyPart contentPart = new MimeBodyPart();
		contentPart.setContent(content);
		return contentPart;
	}

	private static MimeBodyPart createBodyPart(BpmNotification notification) throws MessagingException {
		MimeBodyPart bodyPart = new MimeBodyPart();
		String type = (notification.getSendAsHtml() ? "text/html" : "text/plain") + "; charset=\"" + MAIL_ENCODING + '"';

		bodyPart.setContent(notification.getBody(), type);
		return bodyPart;
	}

	private static MimeBodyPart createAttachmentPart(BpmAttachment attachment) throws MessagingException {
		MimeBodyPart attachmentPart = new MimeBodyPart();
		ByteArrayDataSource ds = new ByteArrayDataSource(attachment.getBody(), attachment.getContentType());

		attachmentPart.setDataHandler(new DataHandler(ds));
		attachmentPart.setFileName(attachment.getName());
		return attachmentPart;
	}

	private static MimeBodyPart createInlineImagePart(ExtractedImage extractedImage, int imgIdx) throws MessagingException {
		InternetHeaders headers = new InternetHeaders();

		headers.addHeader("Content-Type", "image/" + extractedImage.type);
		headers.addHeader("Content-Transfer-Encoding", "base64");

		MimeBodyPart imagePart = new MimeBodyPart(headers, extractedImage.base64);

		imagePart.setContentID('<' + extractedImage.cid + '>');
		imagePart.setDisposition(MimeBodyPart.INLINE);
		imagePart.setFileName(extractedImage.name);
		return imagePart;
	}

	private static class ExtractedImage {
		public final String type;
		public final String name;
		public final String cid;
		public final byte[] base64;

		public ExtractedImage(String type, String name, String cid, byte[] base64) {
			this.type = type;
			this.name = name;
			this.cid = cid;
			this.base64 = base64;
		}
	}

	private static final Pattern IMG_REGEX = Pattern.compile("<img([^>]+)>", Pattern.CASE_INSENSITIVE);

	private static List<ExtractedImage> extractImages(BpmNotification notification) {
		if (!notification.getBody().contains("<img")) {
			return Collections.emptyList();
		}

		List<ExtractedImage> result = new ArrayList<ExtractedImage>();

		StringBuffer sb = new StringBuffer(512);
		Matcher matcher = IMG_REGEX.matcher(notification.getBody());

		while (matcher.find()) {
			String img = matcher.group(1);

			ExtractedImage extractedImage = extractImage(img, 1 + result.size());

			if (extractedImage != null) {
				result.add(extractedImage);

				String replacement = "<img src=\"cid:" + extractedImage.cid + "\" />";
				matcher.appendReplacement(sb, replacement);
			}
			else {
				matcher.appendReplacement(sb, matcher.group());
			}
		}

		matcher.appendTail(sb);

		notification.setBody(sb.toString());

		return result;
	}

	private static final Pattern IMG_DATA_REGEX = Pattern.compile(
			"src=\"data:image/([^;]+);base64,([^\"]+)\"", Pattern.CASE_INSENSITIVE);

	private static ExtractedImage extractImage(String img, int idx) {
		Matcher matcher = IMG_DATA_REGEX.matcher(img);

		if (matcher.find()) {
			String type = matcher.group(1);
			String content = matcher.group(2);
			String name = "image" + idx;

			return new ExtractedImage(type, name + '.' + type, name, content.getBytes());
		}
		return null;
	}

	private static MimeMultipart wrapInMimeMultipart(MimeBodyPart bodyPart, String type) throws MessagingException {
		MimeMultipart bodyContent = new MimeMultipart(type);
		bodyContent.addBodyPart(bodyPart);
		return bodyContent;
	}

	private static String getRecipientSubstiteEmails(Connection connection, BpmNotification notification) {
		if (!hasText(notification.getRecipient())) {
			 return null;
		}

		UserData recipient = getRegistry().getUserSource().getUserByEmail(notification.getRecipient());

		if (recipient == null) {
			return null;
		}

        Session session = getRegistry().getDataRegistry().getSessionFactory().openSession(connection);

        UserSubstitutionDAO userSubstitutionDAO = new UserSubstitutionDAOImpl(session);

        List <String> substitutesLogins = userSubstitutionDAO.getCurrentSubstitutedUserLogins(recipient.getLogin());

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

		if (sender == null) {
			sender = notificationData.getDefaultSender();
		}

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

	public void addNotificationSentListener(NotificationSentListener listener) {
		notificationSentListeners.add(listener);
	}

	public void removeNotificationSentListener(NotificationSentListener listener) {
		notificationSentListeners.remove(listener);
	}

	private void fireNotificationSent(BpmNotification notification, Message message) {
		for (NotificationSentListener listener : notificationSentListeners) {
			try {
				listener.notificationSent(notification, message);
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}
}

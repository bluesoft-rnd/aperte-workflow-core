package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.impl.DefaultI18NSource;
import pl.net.bluesoft.util.lang.Strings;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.util.TaskUtil.getTaskLink;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class BpmNotificationEngine implements TemplateLoader, BpmNotificationService {
    private static final String SUBJECT_TEMPLATE_SUFFIX = "_subject";
    private static final String SENDER_TEMPLATE_SUFFIX = "_sender";

    private Logger logger = Logger.getLogger(BpmNotificationEngine.class.getName());

    private I18NSource messageSource = new DefaultI18NSource();

    private Collection<BpmNotificationConfig> configCache = new HashSet<BpmNotificationConfig>();
    private Map<String, BpmNotificationTemplate> templateMap = new HashMap<String, BpmNotificationTemplate>();
    private Map<String, String> templateCache = new HashMap<String, String>();
    private long cacheUpdateTime;
    private Configuration freemarkerConfiguration;
    private static final long CONFIG_CACHE_REFRESH_INTERVAL = 60 * 1000;
    private ProcessToolBpmSession bpmSession;
    private Properties mailProperties;

    private Map<String, Properties> persistentMailProperties = new HashMap<String, Properties>();

    public void onProcessStateChange(BpmTask task, ProcessInstance pi, UserData userData, boolean processStarted) {
        refreshConfigIfNecessary();
        for (BpmNotificationConfig cfg : configCache) {
            try {
                if (hasText(cfg.getProcessTypeRegex()) && !pi.getDefinitionName().matches(cfg.getProcessTypeRegex())) {
                    continue;
                }
                if (hasText(cfg.getStateRegex()) && (task == null || !task.getTaskName().matches(cfg.getStateRegex()))
                        && !cfg.isNotifyOnProcessStart() && !processStarted) {
                    continue;
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
                if (emailsToNotify.isEmpty()) {
                    logger.info("Despite matched rules, no emails qualify to notify for cfg #" + cfg.getId());
                    continue;
                }
                String templateName = cfg.getTemplateName();
                BpmNotificationTemplate template = templateMap.get(templateName);

                ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                Map data = prepareData(task, pi, userData, cfg, ctx);
                String body = processTemplate(templateName, data);
                String subject = processTemplate(templateName + SUBJECT_TEMPLATE_SUFFIX, data);

                javax.mail.Session mailSession = getMailSession(cfg.getProfileName());

                for (String rcpt : emailsToNotify) {
                    try {
                        sendEmail(rcpt, template.getSender(), subject, body, cfg.isSendHtml(), mailSession);
                    }
                    catch (Exception e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private javax.mail.Session getMailSession(String profileName) {
        final Properties properties = hasText(profileName) && persistentMailProperties.containsKey(profileName) ?
                persistentMailProperties.get(profileName) : mailProperties;
        return javax.mail.Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(properties.getProperty("mail.smtp.user"),
                                properties.getProperty("mail.smtp.password"));
                    }
                });
    }

    public void sendNotification(String recipient, String subject, String body) throws Exception {
        sendNotification(null, recipient, subject, body);
    }

    public void sendNotification(String mailSessionProfileName, String recipient, String subject, String body) throws Exception {
        sendNotification(mailSessionProfileName, null, recipient, subject, body);
    }

    public void sendNotification(String mailSessionProfileName, String sender, String recipient, String subject, String body) throws Exception {
        refreshConfigIfNecessary();
        javax.mail.Session mailSession = getMailSession(mailSessionProfileName);
        if (!Strings.hasText(sender)) {
            UserData autoUser = ProcessToolContext.Util.getThreadProcessToolContext().getAutoUser();
            sender = autoUser.getEmail();
        }
        sendEmail(recipient, sender, subject, body, true, mailSession);
    }

    private Map prepareData(BpmTask task, ProcessInstance pi, UserData userData, BpmNotificationConfig cfg, ProcessToolContext ctx) {
        Map m = new HashMap();
        if (task != null) {
            m.put("task", task);

            Locale locale = Strings.hasText(cfg.getLocale()) ? new Locale(cfg.getLocale()) : Locale.getDefault();
            messageSource.setLocale(locale);
            for (ProcessStateConfiguration st : pi.getDefinition().getStates()) {
                if (task.getTaskName().equals(st.getName())) {
                    m.put("taskName", messageSource.getMessage(st.getDescription()));
                    break;
                }
            }

            m.put("taskUrl", getTaskLink(task, ctx));
        }
        m.put("processVisibleId", Strings.hasText(pi.getExternalKey()) ? pi.getExternalKey() : pi.getInternalId());
        m.put("process", pi);
        m.put("user", userData);
        m.put("session", bpmSession);
        m.put("context", ctx);
        m.put("config", cfg);

        return m;
    }

	public String processTemplate(String templateName, Map data) {
    	refreshConfigIfNecessary();
        logger.info("Using template " + templateName);
        StringWriter sw = new StringWriter();
        try {
            Template template = freemarkerConfiguration.getTemplate(templateName);
            template.process(data != null ? data : new HashMap(), sw);
        }
        catch (Exception e) {
            throw new ProcessToolTemplateErrorException(e);
        }
        sw.flush();
        return sw.toString();
    }

    private void sendEmail(String rcpt, String from, String subject, String body, boolean sendHtml, javax.mail.Session mailSession) throws Exception {
        if (!Strings.hasText(rcpt)) {
            throw new IllegalArgumentException("Cannot send email: Recipient is null!");
        }
        logger.info("Sending mail to " + rcpt + " from " + from);
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rcpt));
        message.setSubject(subject);
        message.setContent(body, (sendHtml ? "text/html" : "text/plain") + "; charset=utf-8");
        message.setSentDate(new Date());
        Transport.send(message);
    }

    public synchronized void refreshConfigIfNecessary() {
        if (cacheUpdateTime + CONFIG_CACHE_REFRESH_INTERVAL < System.currentTimeMillis()) {
            Session session = ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
            configCache = session
                    .createCriteria(BpmNotificationConfig.class)
                    .add(Restrictions.eq("active", true))
                    .list();

            Collection<BpmNotificationTemplate> templates = session.createCriteria(BpmNotificationTemplate.class).list();
            for (BpmNotificationTemplate t : templates) {
                templateMap.put(t.getTemplateName(), t);
                templateCache.put(t.getTemplateName(), t.getTemplateBody() != null ? t.getTemplateBody().replaceAll("\\\\u", "\\u") : "");
                templateCache.put(t.getTemplateName() + SUBJECT_TEMPLATE_SUFFIX, t.getSubjectTemplate() != null
                        ? t.getSubjectTemplate().replaceAll("\\\\u", "\\u") : "");
                templateCache.put(t.getTemplateName() + SENDER_TEMPLATE_SUFFIX, t.getSender());
            }
            freemarkerConfiguration = new Configuration();
            freemarkerConfiguration.setTemplateLoader(this);
            cacheUpdateTime = System.currentTimeMillis();
            Properties p = new Properties();
            try {
                p.load(getClass().getResourceAsStream("/pl/net/bluesoft/rnd/pt/ext/bpmnotifications/mail.properties"));
                mailProperties = p;
            }
            catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }

            persistentMailProperties = new HashMap<String, Properties>();
            List<BpmNotificationMailProperties> properties = session.createCriteria(BpmNotificationMailProperties.class).list();
            for (BpmNotificationMailProperties bnmp : properties) 
            {
                if (hasText(bnmp.getProfileName())) 
                {
                    Properties prop = new Properties();
                    
                    if(bnmp.getSmtpHost() != null)
                    	prop.put("mail.smtp.host",  bnmp.getSmtpHost());
                    
                    if(bnmp.getSmtpSocketFactoryPort() != null)
                    	prop.put("mail.smtp.socketFactory.port", bnmp.getSmtpSocketFactoryPort());
                    
                    if(bnmp.getSmtpSocketFactoryClass() != null)
                    	prop.put("mail.smtp.socketFactory.class", bnmp.getSmtpSocketFactoryClass());
                    

                    prop.put("mail.smtp.auth", bnmp.isSmtpAuth());
                    
                    if(bnmp.getSmtpPort() != null)
                    	prop.put("mail.smtp.port", bnmp.getSmtpPort());
                    
                    if(bnmp.getSmtpUser() != null)
                    	prop.put("mail.smtp.user", bnmp.getSmtpUser());
                    
                    if(bnmp.getSmtpPassword() != null)
                    	prop.put("mail.smtp.password", bnmp.getSmtpPassword());
                    
                    prop.put("mail.debug", bnmp.isDebug());
                    prop.put("mail.smtp.starttls.enable", bnmp.isStarttls());
                    
                    persistentMailProperties.put(bnmp.getProfileName(), prop);
                }
                else {
                    logger.log(Level.WARNING, "Unable to determine profile name for mail config with id: " + bnmp.getId());
                }
            }

            bpmSession = ProcessToolContext.Util.getThreadProcessToolContext().getProcessToolSessionFactory().createAutoSession();
        }

    }

    @Override
    public String findTemplate(String templateName) {
        try {
            return (String) findTemplateSource(templateName);
        }
        catch (IOException e) {
            throw new ProcessToolTemplateErrorException(e);
        }
    }

    @Override
    public Object findTemplateSource(String name) throws IOException {
        return templateCache.containsKey(name) ? templateCache.get(name) : null;
    }

    @Override
    public long getLastModified(Object templateSource) {
        return 0;
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        if (templateSource == null) {
            return null;
        }
        return new StringReader(((String) templateSource));
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
    }
    
    public void sendNotification(String mailSessionProfileName, String sender, String recipient, String subject, String body, List<String> attachments) throws Exception {

        refreshConfigIfNecessary();
        javax.mail.Session mailSession = getMailSession(mailSessionProfileName);
        if (!Strings.hasText(sender)) {
            UserData autoUser = ProcessToolContext.Util.getThreadProcessToolContext().getAutoUser();
            sender = autoUser.getEmail();
        }
        
        if (!Strings.hasText(recipient)) {
            throw new IllegalArgumentException("Cannot send email: Recipient is null!");
        }
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(sender));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        
        //body
        MimeBodyPart messagePart = new MimeBodyPart();
        messagePart.setText(body);
        
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messagePart);

        //zalaczniki
        int counter = 0;
        URL url;
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
        
        message.setContent(multipart);
        message.setSentDate(new Date());
        logger.info("Sending mail with attaments to " + recipient + " from " + sender);
        
        sendMessage(message);
    }
    
    private void sendMessage(Message message) throws Exception {
        Transport.send(message);
    }
}

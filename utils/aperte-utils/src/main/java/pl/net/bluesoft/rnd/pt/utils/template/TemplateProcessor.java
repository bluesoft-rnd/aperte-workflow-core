package pl.net.bluesoft.rnd.pt.utils.template;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolNotificationTemplate;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateLoader;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class TemplateProcessor {
    public static final String DEFAULT_SUBJECT_SUFFIX = "___subject";
    public static final long DEFAULT_CONFIG_CACHE_REFRESH_INTERVAL = 60 * 1000;
    private static final Logger logger = Logger.getLogger(TemplateProcessor.class.getName());

    private String subjectSuffix = DEFAULT_SUBJECT_SUFFIX;
    private long cacheRefreshInterval = DEFAULT_CONFIG_CACHE_REFRESH_INTERVAL;

    private Configuration freemarkerConfiguration;
    private Map<String, ProcessToolNotificationTemplate> templateMap = new HashMap<String, ProcessToolNotificationTemplate>();
    private Map<String, String> templateCache = new HashMap<String, String>();
    private long cacheUpdateTime = 0;

    private ProcessToolTemplateLoader templateLoader;

    public TemplateProcessor(ProcessToolTemplateLoader templateLoader) {
        if (templateLoader == null) {
            throw new IllegalArgumentException("Cannot create the template processor without a template loader!");
        }
        this.templateLoader = templateLoader;
    }

    public void processEmail(String templateName, Map dataModel, String recipient, final Properties mailProperties, boolean sendHtml)
            throws IOException, TemplateException, MessagingException {
        refreshTemplates();
        String body = processTemplate(templateName, dataModel);
        String subject = processTemplate(templateName + getSubjectSuffix(), dataModel);
        ProcessToolNotificationTemplate template = templateMap.get(templateName);
        Session mailSession = Session.getDefaultInstance(mailProperties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProperties.getProperty("mail.smtp.user"), mailProperties.getProperty("mail.smtp.password"));
            }
        });
        sendEmail(recipient, template.getSender(), subject, body, sendHtml, mailSession);
    }

    public void sendEmail(String rcpt, String from, String subject, String body, boolean sendHtml, Session mailSession) throws MessagingException {
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rcpt));
        message.setSubject(subject);
        message.setContent(body, sendHtml ? "text/html" : "text/plain");
        Transport.send(message);
    }

    public String processTemplate(String templateName, Map dataModel) throws IOException, TemplateException {
        logger.info("Using template " + templateName);
        Template template = getFreemarkerConfiguration().getTemplate(templateName);
        StringWriter sw = new StringWriter();
        template.process(dataModel, sw);
        sw.flush();
        return sw.toString();
    }

	private synchronized Configuration getFreemarkerConfiguration() {
		return freemarkerConfiguration;
	}

    public synchronized void refreshTemplates() {
        if (cacheUpdateTime + getCacheRefreshInterval() < System.currentTimeMillis()) {
            List<ProcessToolNotificationTemplate> templates = templateLoader.loadTemplates();
            for (ProcessToolNotificationTemplate template : templates) {
                templateMap.put(template.getTemplateName(), template);
                templateCache.put(template.getTemplateName(), template.getBodyTemplate());
                templateCache.put(template.getTemplateName() + DEFAULT_SUBJECT_SUFFIX, template.getSubjectTemplate());
            }
            freemarkerConfiguration = new Configuration();
            freemarkerConfiguration.setTemplateLoader(new TemplateLoader() {
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
            });
            cacheUpdateTime = System.currentTimeMillis();
        }
    }

    public String getSubjectSuffix() {
        return subjectSuffix;
    }

    public void setSubjectSuffix(String subjectSuffix) {
        this.subjectSuffix = subjectSuffix;
    }

    public long getCacheRefreshInterval() {
        return cacheRefreshInterval;
    }

    public void setCacheRefreshInterval(long cacheRefreshInterval) {
        this.cacheRefreshInterval = cacheRefreshInterval;
    }
}

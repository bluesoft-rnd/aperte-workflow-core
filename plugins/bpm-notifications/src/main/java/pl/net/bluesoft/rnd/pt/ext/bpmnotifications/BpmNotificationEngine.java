package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmTask;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class BpmNotificationEngine implements TemplateLoader {
	private Logger logger = Logger.getLogger(BpmNotificationEngine.class.getName());

	private Collection<BpmNotificationConfig> configCache = new HashSet();
	private Map<String, BpmNotificationTemplate> templateMap = new HashMap();
	private Map<String, String> templateCache = new HashMap();
	private long cacheUpdateTime;
	private Configuration freemarkerConfiguration;
	private static final long CONFIG_CACHE_REFRESH_INTERVAL = 60 * 1000;
	private ProcessToolBpmSession bpmSession;
	private Properties mailProperties;

	public void onProcessStateChange(ProcessInstance pi, UserData userData) {
		refreshConfigIfNecessary();
		Collection<BpmTask> taskList = bpmSession.getTaskList(pi, ProcessToolContext.Util.getThreadProcessToolContext());
		for (BpmNotificationConfig cfg : configCache) {
			try {
				if (hasText(cfg.getProcessTypeRegex()) && !pi.getDefinitionName().matches(cfg.getProcessTypeRegex())) {
					continue;
				}
				if (pi.getState() == null) {
					continue;
				}
				if (hasText(cfg.getStateRegex()) && !pi.getState().matches(cfg.getStateRegex())) {
					continue;
				}

				logger.info("Matched notification #" + cfg.getId() + " for process state change #" + pi.getInternalId());
				List<String> emailsToNotify = new LinkedList<String>();
				if (cfg.isNotifyTaskAssignee()) {
					for (BpmTask t : taskList) {
						UserData owner = t.getOwner();
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

				String body = processTemplate(pi, userData, cfg, templateName);
				String subject = processTemplate(pi, userData, cfg, templateName + "___subject");

				javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(mailProperties,
				                                                                       new javax.mail.Authenticator() {
					                                                                       protected PasswordAuthentication getPasswordAuthentication() {
						                                                                       return new PasswordAuthentication(mailProperties.getProperty("mail.smtp.user"),
						                                                                                                         mailProperties.getProperty("mail.smtp.password"));
					                                                                       }
				                                                                       });

				for (String rcpt : emailsToNotify) {
					try {
						sendEmail(rcpt, template.getSender(), subject, body, cfg.isSendHtml(), mailSession);
					} catch (Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	private String processTemplate(ProcessInstance pi, UserData userData, BpmNotificationConfig cfg,
	                               String templateName)
			throws IOException, TemplateException {

		logger.info("Using template " + templateName);
		Template template = freemarkerConfiguration.getTemplate(templateName);
		StringWriter sw = new StringWriter();
		Map m = new HashMap();
		m.put("process", pi);
		m.put("user", userData);
		m.put("session", bpmSession);
		m.put("context", ProcessToolContext.Util.getThreadProcessToolContext());
		m.put("config", cfg);

		template.process(m, sw);
		sw.flush();
		return sw.toString();
	}

	private void sendEmail(String rcpt, String from, String subject, String body, boolean sendHtml,
	                       javax.mail.Session mailSession) throws Exception {

		Message message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(from));
		message.setRecipients(Message.RecipientType.TO,
		                      InternetAddress.parse(rcpt));
		message.setSubject(subject);
		message.setContent(body, sendHtml ? "text/html" : "text/plain");
		Transport.send(message);
	}

	public synchronized void refreshConfigIfNecessary() {
		if (cacheUpdateTime + CONFIG_CACHE_REFRESH_INTERVAL < System.currentTimeMillis()) {
			Session sess = ProcessToolContext.Util.getThreadProcessToolContext()
					.getHibernateSession();
			configCache = sess
					.createCriteria(BpmNotificationConfig.class)
					.add(Restrictions.eq("active", true))
					.list();

			Collection<BpmNotificationTemplate> templates = sess.createCriteria(BpmNotificationTemplate.class).list();
			for (BpmNotificationTemplate t : templates) {
				templateMap.put(t.getTemplateName(), t);
				templateCache.put(t.getTemplateName(), t.getTemplateBody());
				templateCache.put(t.getTemplateName() + "___subject", t.getSubjectTemplate());
			}
			freemarkerConfiguration = new Configuration();
			freemarkerConfiguration.setTemplateLoader(this);
			cacheUpdateTime = System.currentTimeMillis();
			UserData autoUser = new UserData();
			autoUser.setLogin("autonotify");
			autoUser.setEmail("none@none.none");
			autoUser.setRealName("Auto Notify user");
			Properties p = new Properties();
			try {
				p.load(getClass().getResourceAsStream("/pl/net/bluesoft/rnd/pt/ext/bpmnotifications/mail.properties"));
				mailProperties = p;
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
				throw new RuntimeException(e);
			}

			bpmSession = ProcessToolContext.Util.getThreadProcessToolContext().getProcessToolSessionFactory().createSession(autoUser, new HashSet<String>());

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
		if (templateSource == null) return null;
		return new StringReader(((String) templateSource));
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
	}
}

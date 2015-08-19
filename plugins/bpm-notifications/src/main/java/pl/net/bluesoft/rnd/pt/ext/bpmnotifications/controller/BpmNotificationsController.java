package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.controller;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.model.UserDataBean;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationConfigDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationMailPropertiesDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationTemplateDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationHistoryEntry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.ProcessedNotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProvider;
import pl.net.bluesoft.util.lang.cquery.func.F;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * @author: mkrol@bluesoft.net.pl
 */
@OsgiController(name = "bpmnotificationscontroller")
public class BpmNotificationsController implements IOsgiWebController {

	@Autowired
	private ProcessToolRegistry processToolRegistry;

	@ControllerMethod(action = "getProfiles")
	public GenericResultBean getProfileConfigurations(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();
		BpmNotificationMailPropertiesDAO bpmNotificationMailPropertiesDAO = new BpmNotificationMailPropertiesDAO();
		List<BpmNotificationMailProperties> bpmNotificationMailPropertiesList = bpmNotificationMailPropertiesDAO
				.findAll();

		result.setData(bpmNotificationMailPropertiesList);
		return result;
	}

	@ControllerMethod(action = "addProfile")
	public GenericResultBean addProfileConfiguration(final OsgiWebRequest invocation) throws IOException {
		GenericResultBean result = new GenericResultBean();

		HttpServletRequest request = invocation.getRequest();

		BpmNotificationMailProperties profile = new BpmNotificationMailProperties();
		if (!request.getParameter("ProfileId").isEmpty())
			profile.setId(Long.parseLong(request.getParameter("ProfileId")));
		profile.setProfileName(request.getParameter("ProfileName"));
		profile.setSmtpHost(request.getParameter("ProfileSMTPHost"));
		profile.setSmtpPort(request.getParameter("ProfileSMTPPort"));
		profile.setSmtpUser(request.getParameter("ProfileSMTPUser"));
        profile.setDefaultSender(request.getParameter("DefaultSender"));
		profile.setSmtpPassword(request.getParameter("ProfileSMTPPassword"));
		profile.setSmtpAuth("1".equals(request.getParameter("ProfileSMTPAuth")));
		profile.setSmtpSocketFactoryPort(request.getParameter("ProfileSmtpFactoryPort"));
		profile.setSmtpSocketFactoryClass(request.getParameter("ProfileSmtpFactoryClass"));
		profile.setSslSocketFactoryClass(request.getParameter("ProfileSslFactoryClass"));
		profile.setDisablePlainAuth("1".equals(request.getParameter("ProfileDisablePlainAuth")));
		profile.setTransportProtocol(request.getParameter("ProfileTransportProtocol"));
		profile.setStarttls("1".equals(request.getParameter("ProfileStartTls")));
		profile.setDebug("1".equals(request.getParameter("ProfileDebug")));


		BpmNotificationMailPropertiesDAO bpmNotificationMailPropertiesDAO = new BpmNotificationMailPropertiesDAO();
		bpmNotificationMailPropertiesDAO.saveOrUpdate(profile);

        invalidateCache(invocation);

		result.setData(profile);
		return result;
	}

	@ControllerMethod(action = "getTemplates")
	public GenericResultBean getTemplates(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();
		BpmNotificationTemplateDAO bpmNotificationTemplateDAO = new BpmNotificationTemplateDAO();
		List<BpmNotificationTemplate> bpmNotificationTemplatesList = bpmNotificationTemplateDAO.findAll();

		result.setData(bpmNotificationTemplatesList);
		return result;
	}

	@ControllerMethod(action = "addTemplate")
	public GenericResultBean addTemplate(final OsgiWebRequest invocation) throws IOException {
		GenericResultBean result = new GenericResultBean();

		HttpServletRequest request = invocation.getRequest();

		BpmNotificationTemplate template = new BpmNotificationTemplate();
		if (!request.getParameter("TemplateId").isEmpty())
			template.setId(Long.parseLong(request.getParameter("TemplateId")));
		template.setTemplateName(request.getParameter("TemplateName"));
		template.setSender(request.getParameter("TemplateSender"));
		template.setSubjectTemplate(request.getParameter("TemplateSubject"));
		template.setTemplateBody(request.getParameter("TemplateBody"));
		template.setFooterTemplate(request.getParameter("TemplateFooter"));
		template.setSentFolderName(request.getParameter("TemplateSentFolder"));

		BpmNotificationTemplateDAO bpmNotificationTemplateDAO = new BpmNotificationTemplateDAO();
		bpmNotificationTemplateDAO.saveOrUpdate(template);

        invalidateCache(invocation);

		result.setData(template);
		return result;
	}

	@ControllerMethod(action = "getNotifications")
	public GenericResultBean getNotifications(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();

		BpmNotificationConfigDAO bpmNotificationConfigDAO = new BpmNotificationConfigDAO();
		List<BpmNotificationConfig> bpmNotificationConfigsList = bpmNotificationConfigDAO.findAll();

		result.setData(bpmNotificationConfigsList);
		return result;
	}

	@ControllerMethod(action = "addNotification")
	public GenericResultBean addNotification(final OsgiWebRequest invocation) throws IOException {
		GenericResultBean result = new GenericResultBean();

		HttpServletRequest request = invocation.getRequest();

		BpmNotificationConfig notification = new BpmNotificationConfig();
		if (!request.getParameter("NotificationId").isEmpty())
			notification.setId(Long.parseLong(request.getParameter("NotificationId")));
		notification.setProfileName(request.getParameter("NotificationProfile"));
		notification.setTemplateName(request.getParameter("NotificationTemplate"));
		notification.setTemplateArgumentProvider(request.getParameter("NotificationParamProvider"));
		notification.setActive("1".equals(request.getParameter("NotificationActive")));
		notification.setSendHtml("1".equals(request.getParameter("NotificationSendAsHtml")));
		notification.setLocale(request.getParameter("NotificationLocale"));
		notification.setProcessTypeRegex(request.getParameter("NotificationProcess"));
		notification.setStateRegex(request.getParameter("NotificationState"));
		notification.setLastActionRegex(request.getParameter("NotificationLastAction"));
		notification.setNotifyOnProcessStart("1".equals(request.getParameter("NotificationSendOnProcessStart")));
		notification.setNotifyOnProcessEnd("1".equals(request.getParameter("NotificationSendOnProcessEnd")));
		notification.setOnEnteringStep("1".equals(request.getParameter("NotificationSendOnEnteringStep")));
		notification.setSkipNotificationWhenTriggeredByAssignee("1".equals(request
				.getParameter("NotificationSkipWhenTriggeredByAssignee")));
		notification.setNotifyTaskAssignee("1".equals(request.getParameter("NotificationNotifyAssignee")));
		notification.setNotifyEmailAddresses(request.getParameter("NotificationNotifyEmails"));
		notification.setNotifyUserAttributes(request.getParameter("NotificationNotifyUsers"));

		BpmNotificationConfigDAO bpmNotificationConfigDAO = new BpmNotificationConfigDAO();
		bpmNotificationConfigDAO.saveOrUpdate(notification);

		result.setData(notification);
		return result;
	}

	@ControllerMethod(action = "getArgumentProviders")
	public GenericResultBean getArgumentProviders(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();

		Collection<TemplateArgumentProvider> argumentProviders = getService().getTemplateArgumentProviders();

		result.setData(argumentProviders);
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ControllerMethod(action = "getNotificationHistory")
	public GenericResultBean getNotificationHistory(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();

		List<NotificationHistoryEntry> notificationHistoryEntries = getService().getNotificationHistoryEntries();
		notificationHistoryEntries = from(notificationHistoryEntries).orderBy(
				new F<NotificationHistoryEntry, Comparable>() {
					@Override
					public Comparable invoke(NotificationHistoryEntry x) {
						return x.getEnqueueDate();
					}
				}).toList();
		result.setData(notificationHistoryEntries);
		return result;
	}

	@ControllerMethod(action = "testSendEmail")
	public GenericResultBean testSendEmail(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();

		HttpServletRequest request = invocation.getRequest();

		try {
			String sender = request.getParameter("testEmailSender");
			String recipient = request.getParameter("testEmailRecipient");
			String body = request.getParameter("testEmailBody");

			if (sender == null || sender.isEmpty()) {
				// informationNotification(i18NSource.getMessage("bpmnot.send.test.mail.sender.empty"));
				// return;
				return result;
			}

			if (recipient == null || recipient.isEmpty()) {
				// informationNotification(i18NSource.getMessage("bpmnot.send.test.mail.recipient.empty"));
				// return;
				return result;
			}

			UserDataBean recipientUser = new UserDataBean();
			recipientUser.setEmail(recipient);

			ProcessedNotificationData notificationData = new ProcessedNotificationData();
			notificationData.setBody(body).setSubject("Test E-mail").setSender(sender).setProfileName("Default")
					.setRecipient(recipientUser);

			getService().addNotificationToSend(notificationData);
			// informationNotification(i18NSource.getMessage("bpmnot.send.test.mail.sent"));
		} catch (Exception e) {
			result.addError("", e.getMessage());
		}

		return result;
	}

	@ControllerMethod(action = "invalidateCache")
	public GenericResultBean invalidateCache(final OsgiWebRequest invocation) {
		GenericResultBean result = new GenericResultBean();

		getService().invalidateCache();

		return result;
	}

	protected IBpmNotificationService getService() {
		return processToolRegistry.getRegisteredService(IBpmNotificationService.class);
	}

}

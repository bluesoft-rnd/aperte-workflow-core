package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.step;

import org.apache.commons.lang3.StringUtils;
import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ISettingsProvider;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.EmailSender;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.utils.EmailUtils;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.Strings.hasText;

@AliasName(name = "SendMailStep")
public class SendMailStep implements ProcessToolProcessStep {
	private static final Logger logger = Logger.getLogger(SendMailStep.class.getName());

    @AutoWiredProperty(substitute = true)
    private String recipient;
    
    @AutoWiredProperty(substitute = true)
    private String profileName = "Default";
    
    @AutoWiredProperty(substitute = true)
    private String template;

	@AutoWiredProperty(substitute = true)
	private String attachmentIds = "";

	@AutoWiredProperty
	private String templateArgumentProvider;

	@AutoWiredProperty(substitute = true)
	private String source;

	@AutoWiredProperty(substitute = true)
	private String subjectOverride;

	@Autowired
	private IFilesRepositoryFacade filesRepository;

	@Autowired
	private ISettingsProvider settingsProvider;

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception {
        IBpmNotificationService service = getRegistry().getRegisteredService(IBpmNotificationService.class);

		if (!hasText(recipient)) {
			return STATUS_OK;
		}

		if (!hasText(profileName)) {
			profileName = "Default";
		}


		String disabledMailTemplates = settingsProvider.getSetting("disabled.mail.template");
		if(StringUtils.isNotEmpty(disabledMailTemplates) && disabledMailTemplates.contains(template))
		{
			logger.info("[MAIL] Template "+template+" disabled, skipping mail");
			return STATUS_OK;
		}

		Collection<UserData> ussers = EmailUtils.extractUsers(recipient, step.getProcessInstance());

		for(UserData user: ussers) {

			TemplateData templateData = service.createTemplateData(template, Locale.getDefault());

			service.getTemplateDataProvider()
					.addProcessData(templateData, step.getProcessInstance())
					.addUserToNotifyData(templateData, user)
					.addArgumentProvidersData(templateData, templateArgumentProvider, step.getProcessInstance());

			NotificationData notificationData = new NotificationData()
					.setProfileName(profileName)
					.setRecipient(user)
					.setTemplateData(templateData);

			EmailUtils.EmailScope scope = EmailUtils.EmailScope.STANDARD;
			if ("ALL".equals(attachmentIds.toUpperCase()))
				scope = EmailUtils.EmailScope.ALL;
			if ("MAIL".equals(attachmentIds.toUpperCase()))
				scope = EmailUtils.EmailScope.MAIL;

			notificationData.setAttachments(EmailUtils.getAttachments(step.getProcessInstance(), EmailUtils.getAttachmentIds(attachmentIds), filesRepository, scope));

			if (hasText(source)) {
				notificationData.setSource(source);
			} else {
				notificationData.setSource(String.valueOf(step.getProcessInstance().getId()));
			}

			notificationData.setDefaultSender(EmailUtils.getDefaultSender(profileName));
			notificationData.setSubjectOverride(subjectOverride);

			try {
				EmailSender.sendEmail(service, notificationData);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error sending email", e);
				return STATUS_ERROR;
			}
		}

        return STATUS_OK;
    }
}


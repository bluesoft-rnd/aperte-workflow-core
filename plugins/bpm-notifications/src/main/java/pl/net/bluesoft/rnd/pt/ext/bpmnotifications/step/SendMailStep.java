package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.step;

import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserDataBean;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmAttachment;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.EmailSender;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.Strings.hasText;

@AliasName(name = "SendMailStep")
public class SendMailStep implements ProcessToolProcessStep {
	private static final Logger logger = Logger.getLogger(SendMailStep.class.getName());

    @AutoWiredProperty(substitute = true)
    private String recipient;
    
    @AutoWiredProperty
    private String profileName = "Default";
    
    @AutoWiredProperty
    private String template;

	@AutoWiredProperty(substitute = true)
	private String attachmentIds;

	@Autowired
	private IFilesRepositoryFacade filesRepository;

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception {
        IBpmNotificationService service = getRegistry().getRegisteredService(IBpmNotificationService.class);

		if (!hasText(recipient)) {
			return STATUS_OK;
		}

		UserData user = getRecipient();
		
		TemplateData templateData =	service.createTemplateData(template, Locale.getDefault());
		
		service.getTemplateDataProvider()
			.addProcessData(templateData, step.getProcessInstance())
			.addUserToNotifyData(templateData, user);
		
		NotificationData notificationData = new NotificationData()
			.setProfileName("Default")
			.setRecipient(user)
			.setTemplateData(templateData);

		notificationData.setAttachments(getAttachments(step.getProcessInstance()));

        try {
        	EmailSender.sendEmail(service, notificationData);
        }
		catch (Exception e) {
        	logger.log(Level.SEVERE, "Error sending email", e);
        	return STATUS_ERROR;
        }

        return STATUS_OK;
    }

	private UserData getRecipient() {
		if (recipient.contains("@")) {
			UserDataBean result = new UserDataBean();
			result.setEmail(recipient);
			return result;
		}
		return getRegistry().getUserSource().getUserByLogin(recipient);
	}

	private List<BpmAttachment> getAttachments(ProcessInstance pi) {
		if (!hasText(attachmentIds)) {
			return Collections.emptyList();
		}

		List<BpmAttachment> result = new ArrayList<BpmAttachment>();

		if ("all".equals(attachmentIds)) {
			for (IFilesRepositoryItem repositoryItem : filesRepository.getFilesList(pi)) {
				result.add(getBpmAttachment(repositoryItem.getId()));
			}
		}
		else {
			for (String attachmentId : attachmentIds.split(",")) {
				result.add(getBpmAttachment(Long.valueOf(attachmentId)));
			}
		}
		return result;
	}

	private BpmAttachment getBpmAttachment(Long attachmentId) {
	    try {
			FileItemContent fileItemContent = filesRepository.downloadFile(attachmentId);
			BpmAttachment attachment = new BpmAttachment();

			attachment.setName(fileItemContent.getName());
			attachment.setContentType(fileItemContent.getContentType());
			attachment.setBody(fileItemContent.getBytes());
			return attachment;
		}
		catch (DownloadFileException e) {
			throw new RuntimeException(e);
		}
	}
}


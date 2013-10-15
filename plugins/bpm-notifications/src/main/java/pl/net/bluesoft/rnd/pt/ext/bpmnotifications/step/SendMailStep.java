package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.step;

import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.EmailSender;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

@AliasName(name = "SendMailStep")
public class SendMailStep implements ProcessToolProcessStep {
    @AutoWiredProperty
    private String recipient;
    
    @AutoWiredProperty
    private String profileName = "Default";
    
    @AutoWiredProperty
    private String template;
    
    private final static Logger logger = Logger.getLogger(SendMailStep.class.getName());

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception {
        IBpmNotificationService service = getRegistry().getRegisteredService(IBpmNotificationService.class);

		UserData user = findUser(recipient, step.getProcessInstance());
		
		TemplateData templateData =	service.createTemplateData(template, Locale.getDefault());
		
		service.getTemplateDataProvider()
			.addProcessData(templateData, step.getProcessInstance())
			.addUserToNotifyData(templateData, user);
		
		NotificationData notificationData = new NotificationData()
			.setProfileName("Default")
			.setRecipient(user)
			.setTemplateData(templateData);

		EmailSender.sendEmail(service, notificationData);

        try {
        	EmailSender.sendEmail(service, notificationData);
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "Error sending email", e);
        	return STATUS_ERROR;
        }

        return STATUS_OK;
    }

	private UserData findUser(String recipient, ProcessInstance pi) {
		if (recipient == null) {
			return null;
		}
		recipient = recipient.trim();
		if(recipient.matches("#\\{.*\\}")){
        	String loginKey = recipient.replaceAll("#\\{(.*)\\}", "$1");
        	recipient = pi.getSimpleAttributeValue(loginKey);
    		if (recipient == null)
            {
                recipient = pi.getSimpleAttributeValue(loginKey);
                if(recipient == null)
                    return null;
    		}
        }
		return getRegistry().getUserSource().getUserByLogin(recipient);
	}
}


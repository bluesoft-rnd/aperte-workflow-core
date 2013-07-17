package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.step;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComments;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.EmailSender;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

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
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        IBpmNotificationService service = ctx.getRegistry().getRegisteredService(IBpmNotificationService.class);
        
        Map<String, Object> data = new HashMap<String, Object>();
        String processId = step.getProcessInstance().getExternalKey();
        if (!Strings.hasText(processId))
        	processId = step.getProcessInstance().getInternalId();
		
		UserData user = findUser(recipient, ctx, step.getProcessInstance());
		
		TemplateData templateData =	service.createTemplateData(template, Locale.getDefault());
		
		service.getTemplateDataProvider()
			.addProcessData(templateData, step.getProcessInstance())
			.addUserToNotifyData(templateData, user);
	
		templateData.addEntry("latestComment", getLatestComment(step.getProcessInstance()));
		
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

	private Object getLatestComment(ProcessInstance processInstance) {
		ProcessComments comments = processInstance.findAttributeByClass(ProcessComments.class);
		if (comments != null && !comments.getComments().isEmpty()) {
			ProcessComment comment = from(comments.getComments())
					.orderByDescending(new F<ProcessComment, Date>() {
						@Override
						public Date invoke(ProcessComment x) {
							return x.getCreateTime();
						}
					})
					.first();
			return comment.getBody();
		}
		return null;
	}

	private UserData findUser(String recipient, ProcessToolContext ctx, ProcessInstance pi) {
		if (recipient == null) {
			return null;
		}
		recipient = recipient.trim();
		if(recipient.matches("#\\{.*\\}")){
        	String loginKey = recipient.replaceAll("#\\{(.*)\\}", "$1");
        	recipient = pi.getSimpleAttributeValue(loginKey);
    		if (recipient == null)
            {
                recipient = (String)pi.getSimpleAttributeValue(loginKey);
                if(recipient == null)
                    return null;
    		}
        }
		return ctx.getUserDataDAO().loadUserByLogin(recipient);
	}
}


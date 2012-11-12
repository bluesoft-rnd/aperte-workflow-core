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
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.util.EmailSender;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

@AliasName(name = "SendMailStep")
public class SendMailStep implements ProcessToolProcessStep {
    @AutoWiredProperty
    private String recipient;
    
    @AutoWiredProperty
    private String template;
    
    private final static Logger logger = Logger.getLogger(SendMailStep.class.getName());

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        BpmNotificationService service = ctx.getRegistry().getRegisteredService(BpmNotificationService.class);
        
        Map<String, Object> data = new HashMap<String, Object>();
        String processId = step.getProcessInstance().getExternalKey();
        if (!Strings.hasText(processId))
        	processId = step.getProcessInstance().getInternalId();
		
		UserData user = findUser(recipient, ctx, step.getProcessInstance());
		
        data.put("processId", processId);
		data.put("processVisibleId", processId);
		data.put("user", user);  
		data.put("latestComment", getLatestComment(step.getProcessInstance()));
		data.put("creator", step.getProcessInstance().getCreator());

        try {
        	EmailSender.sendEmail(service, user.getEmail(), template, data);
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
        	recipient = (String) ctx.getBpmVariable(pi, loginKey);
    		if (recipient == null) {
    			return null;
    		}
        }
		return ctx.getUserDataDAO().loadUserByLogin(recipient);
	}
}


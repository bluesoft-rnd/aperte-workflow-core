package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.util;

import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;

import java.util.Map;
import java.util.logging.Logger;

public class EmailSender {
	private static final String SUBJECT_TEMPLATE_SUFFIX = "_subject";
	private static final String SENDER_TEMPLATE_SUFFIX = "_sender";
	private static final Logger logger = Logger.getLogger(EmailSender.class.getName());
	
	public static void sendEmail(BpmNotificationService service, String recipient, String template, Map<String, Object> data) throws Exception {
        logger.info("EmailSender with params " + recipient + " " + template);
        
        if (recipient == null){
        	throw new Exception("Error sending email. Recipient is null");
        }
        
        String body, topic, sender;
        
        try {
        	body = service.processTemplate(template, data);
        	topic = service.processTemplate(template + SUBJECT_TEMPLATE_SUFFIX, data);
        	sender = service.findTemplate(template + SENDER_TEMPLATE_SUFFIX);
        } catch (ProcessToolTemplateErrorException e) {
        	throw new Exception("Error preparing email template", e);
        }
        
        if (body == null || topic == null || sender == null) {
        	throw new Exception("Error sending email. Cannot find valid template configuration");
        }
        
        service.addNotificationToSend("Default", sender, recipient, topic, body, true);
    	
    	logger.info("EmailSender email sent: " + sender + " " + recipient + " " + topic + " " + body);
	}
}

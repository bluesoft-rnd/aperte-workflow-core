package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.util;

import java.util.logging.Logger;

import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.data.NotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.data.ProcessedNotificationData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;

public class EmailSender {
	private static final Logger logger = Logger.getLogger(EmailSender.class.getName());
	
	public static void sendEmail(BpmNotificationService service, NotificationData notificationData) throws Exception 
	{

        logger.info("EmailSender with params " + notificationData.getRecipient() + " " + notificationData.getTemplateData().getTemplateName());
        
        if (notificationData.getRecipient() == null)
        	throw new Exception("Error sending email. Recipient is null");
        
        try 
        {
            ProcessedNotificationData processedNotificationData = service.processNotificationData(notificationData);
        	
            service.addNotificationToSend(processedNotificationData);
        } 
        catch (ProcessToolTemplateErrorException e) {
        	throw new Exception("Error preparing email template", e);
        }
        

        

	}
}

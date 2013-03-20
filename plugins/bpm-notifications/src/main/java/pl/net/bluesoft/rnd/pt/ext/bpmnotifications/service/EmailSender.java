package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import java.util.logging.Logger;

import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;

public class EmailSender {
	private static final Logger logger = Logger.getLogger(EmailSender.class.getName());
	
	public static void sendEmail(IBpmNotificationService service, NotificationData notificationData) throws Exception 
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

package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import java.util.List;
import java.util.Map;

public interface BpmNotificationService {
    void sendNotification(String recipient, String subject, String body) throws Exception;

    void sendNotification(String mailSessionProfileName, String recipient, String subject, String body) throws Exception;

    void sendNotification(String mailSessionProfileName, String sender, String recipient, String subject, String body) throws Exception;
    
    void sendNotification(String mailSessionProfileName, String sender, String recipient, String subject, String body, List<String> attachments) throws Exception;

    String findTemplate(String templateName);

    String processTemplate(String templateName, Map data);
}

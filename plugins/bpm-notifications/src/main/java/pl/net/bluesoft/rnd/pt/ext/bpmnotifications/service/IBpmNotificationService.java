package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Bpm notification service 
 * 
 * @author Maciej Pawlak
 *
 */
public interface IBpmNotificationService 
{
	/** Create and add notification with given parameter to queue. It will be send in the next scheduler run */
	void addNotificationToSend(ProcessedNotificationData notificationData) throws Exception;
	
	/** Process raw notification template data and send notification */
	void addNotificationToSend(NotificationData notificationData) throws Exception;
	
	
	TemplateData createTemplateData(String templateName, Locale locale);
	
	ITemplateDataProvider getTemplateDataProvider();
	
	ProcessedNotificationData processNotificationData(NotificationData notificationData) throws Exception;

    String findTemplate(String templateName);

    String processTemplate(String templateName, TemplateData templateData);

	void registerTemplateArgumentProvider(TemplateArgumentProvider provider);
	void unregisterTemplateArgumentProvider(TemplateArgumentProvider provider);
	Collection<TemplateArgumentProvider> getTemplateArgumentProviders();

	List<TemplateArgumentDescription> getDefaultArgumentDescriptions(I18NSource i18NSource);

	List<NotificationHistoryEntry> getNotificationHistoryEntries();

	void invalidateCache();

	void addNotificationSentListener(NotificationSentListener listener);
	void removeNotificationSentListener(NotificationSentListener listener);

	String processSubjectTemplate(String template, IAttributesProvider attributesProvider, String recipient,
						   String templateArgumentProvider, String profileName);
}

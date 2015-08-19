package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.templates;

import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;
import freemarker.cache.TemplateLoader;

/**
 * Interface for template loader
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IMailTemplateLoader extends TemplateLoader
{
    /** Refresh config: look for modifications of templates in database */
	void refreshConfig();
	
	/** Get the bpm notification template */
	BpmNotificationTemplate getBpmNotificationTemplate(String templateName);
	
	/** Inject variables values to template */
	String processTemplate(String templateName, TemplateData templateData) throws ProcessToolTemplateErrorException;
	
	/** Find tempalte by name */
	String findTemplate(String templateName);

	String getTemplateSentFolderName(String templateName);

}

package pl.net.bluesoft.rnd.processtool.web.domain;

import freemarker.cache.TemplateLoader;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;

import java.util.Map;

/**
 * Configuration and template provider for html widget pages 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IHtmlTemplateProvider extends TemplateLoader 
{
	String PROCESS_PARAMTER = "process";
	String TASK_PARAMTER = "task";
    String CASE_PARAMETER = "case";
	String USER_PARAMTER = "user";
    String USER_SOURCE_PARAMTER = "userSource";
	String MESSAGE_SOURCE_PARAMETER = "messageSource";
	String WIDGET_NAME_PARAMETER = "widgetName";
	String WIDGET_ID_PARAMETER = "widgetId";
	String PRIVILEGES_PARAMETER = "privileges";
    String DICTIONARIES_DAO_PARAMETER = "dictionariesDao";
    String DICTIONARIES_FACADE = "dictionariesFacade";
	String BPM_SESSION_PARAMETER = "bpmSession";
    String ATTRIBUTES_PROVIDER = "attributesProvider";
    String SETTINGS_PROVIDER = "settingsProvider";
    String OWNER_LOGIN_PARAMTER = "ownerLogin";
    String QUEUE_ID_PARAMTER = "queueId";
    /** Add template */
	void addTemplate(String templateName, String template);
	
	/** Get template body */
	String getTemplate(String templateName);
	
	/** Process template */
	String processTemplate(String templateName, Map<String, Object> viewData) throws ProcessToolTemplateErrorException;

	/** Remove template by given name */
	void removeTemplate(String templateName);
}

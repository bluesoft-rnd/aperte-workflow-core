package pl.net.bluesoft.rnd.processtool.web.domain;

import java.util.Map;

import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import freemarker.cache.TemplateLoader;

/**
 * Configuration and template provider for html widget pages 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IHtmlTemplateProvider extends TemplateLoader 
{
	static final String PROCESS_PARAMTER = "process";
	static final String TASK_PARAMTER = "task";
	static final String USER_PARAMTER = "user";
	static final String MESSAGE_SOURCE_PARAMETER = "messageSource";
	static final String WIDGET_NAME_PARAMETER = "widgetName";
	static final String WIDGET_ID_PARAMETER = "widgetId";
	static final String PRIVILEGES_PARAMETER = "privileges";
    static final String DICTIONARIES_DAO_PARAMETER = "dictionariesDao";

    /** Add template */
	void addTemplate(String templateName, String template);
	
	/** Get template body */
	String getTemplate(String templateName);
	
	/** Process template */
	String processTemplate(String templateName, Map<String, Object> viewData) throws ProcessToolTemplateErrorException;

	/** Remove template by given name */
	void removeTemplate(String templateName);
}

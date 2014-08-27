package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 
 * Data provider for e-mail notifications
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface ITemplateDataProvider extends IArgumentProviderHandler
{
	/** Process visible id, external process key or internal id if external doesn't exist. Use ${processVisibleId} in template */
	public static final String _PROCESS_VISIBLE_ID = "processVisibleId";
	
	/** Process id in database. Use ${processId} in template */
	public static final String _PROCESS_ID = "processId";
	
	/** Process instance, you can access all process properties if seperatre by dot. Use ${process} in template */
	public static final String _PROCESS = "process";
	
	/** Current assignee instance (UserData), you can access all process properties if seperatre by dot. Use ${user} in template */
	public static final String _USER = "user";
	
	
	/** Process creator instance (UserData), you can access all process properties if seperatre by dot. Use ${user} in template */
	public static final String _CREATOR = "creator";
	
	/** Current session instance, do not use in template */
	public static final String _SESSION = "session";
	
	/** Current context instance, do not use in template */
	public static final String _CONTEXT = "context";
	
	/** Current template config instance, do not use in template */
	public static final String _CONFIG = "config";
	
	
	/** If task exists, this is instance of current BpmTask. Use ${task} in template */
	public static final String _TASK = "task";
	
	/** If task exists, this is the name of current BpmTask. Use ${taskName} in template */
	public static final String _TASK_NAME = "taskName";
	
	/** If task exists, this is url link for webbrowser to this task. Use ${taskUrl} in template */
	public static final String _TASK_URL = "taskUrl";
	
	/** If task exists, this is url link for webbrowser to this task. Use ${taskLink} in template */
	public static final String _TASK_LINK = "taskLink";
	
	/** Current assignee instance (UserData), you can access all process properties if seperatre by dot. Use ${user} in template */
	public static final String _ASSIGNEE = "assignee";
	
	/**
	 * Create new template data using given locale
	 * @param templateName 
	 * 
	 * @param locale
	 * @return empty data object
	 */
	TemplateData createTemplateData(String templateName, Locale locale);
	
	/** 
	 * Add entries:
	 * - task
	 * - taskName
	 * - taskUrl
	 * - taskLink
	 * - assignee
	 */
	ITemplateDataProvider addTaskData(TemplateData templateData, BpmTask task);
	
	/** 
	 * Add entries:
	 * - processVisibleId
	 * - processId
	 * - process
	 * - assignee
	 */
	ITemplateDataProvider addProcessData(TemplateData templateData, IAttributesProvider provider);
	
	/**
	 * Add entries:
	 * - user
	 */
	ITemplateDataProvider addUserToNotifyData(TemplateData templateData, UserData userToNotify);
	
	/** Add entries from additional argument providers */
	ITemplateDataProvider addArgumentProvidersData(TemplateData templateData, String templateArgumentProvider, IAttributesProvider provider);

	ITemplateDataProvider addAttributes(TemplateData templateData, Map<String, Object> attributes);
	
	/** Add technical additional data. This method should be deleted in future refactoring */
	ITemplateDataProvider addContextAdditionalData(TemplateData templateData, BpmNotificationConfig cfg, ProcessToolBpmSession bpmSession);
	
	/** Get default argument descriptions */
	List<TemplateArgumentDescription> getDefaultArgumentDescriptions(I18NSource i18NSource);
	
	
}

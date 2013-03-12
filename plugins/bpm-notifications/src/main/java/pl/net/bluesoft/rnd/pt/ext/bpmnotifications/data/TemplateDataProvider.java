package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.data;

import static pl.net.bluesoft.rnd.util.TaskUtil.getTaskLink;
import static pl.net.bluesoft.util.lang.Strings.hasText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentDescription;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProviderParams;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.util.lang.Strings;


/** 
 * Data provider for standard e-mail notifications
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class TemplateDataProvider implements ITemplateDataProvider
{
	
	private final Set<TemplateArgumentProvider> argumentProviders = new HashSet<TemplateArgumentProvider>();
	
	/** Creates new empty template data object */
	public TemplateData createTemplateData(String templateName, Locale locale)
	{
		I18NSource messageSource = I18NSourceFactory.createI18NSource(locale);
		return new TemplateData(templateName, locale, messageSource);
	}
	
	public ITemplateDataProvider addTaskData(TemplateData templateData, BpmTask task)
	{
		if(task == null)
			return this;
		
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		ProcessInstance pi = task.getProcessInstance();
		
		templateData.addEntry(_TASK, task);
		
		
		pi = ctx.getProcessInstanceDAO().refresh(pi);

		for (ProcessStateConfiguration st : pi.getDefinition().getStates()) {
            if (task.getTaskName().equals(st.getName())) 
            {
            	templateData.addEntry(_TASK_NAME, templateData.getMessageSource().getMessage(st.getDescription()));
                break;
            }
        }

		templateData.addEntry(_TASK_URL, getTaskLink(task, ctx));
		templateData.addEntry(_TASK_LINK, getTaskLink(task, ctx));
		templateData.addEntry(_ASSIGNEE, task.getAssignee());
		
		return this;
	}
	
	public TemplateDataProvider addProcessData(TemplateData templateData, ProcessInstance pi)
	{		
		templateData.addEntry(_PROCESS_VISIBLE_ID, Strings.hasText(pi.getExternalKey()) ? pi.getExternalKey() : pi.getInternalId());
		templateData.addEntry(_PROCESS_ID, Strings.hasText(pi.getExternalKey()) ? pi.getExternalKey() : pi.getInternalId());
		templateData.addEntry(_PROCESS, pi);
		templateData.addEntry(_CREATOR, pi.getCreator());
		
		return this;
	}
	
	
	public TemplateDataProvider addUserToNotifyData(TemplateData templateData, UserData userToNotify)
	{
		templateData.addEntry(_USER, userToNotify);
		
		return this;
	}
	
	public TemplateDataProvider addContextAdditionalData(TemplateData templateData, BpmNotificationConfig cfg, ProcessToolBpmSession bpmSession)
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		templateData.addEntry(_SESSION, bpmSession);
		templateData.addEntry(_CONTEXT, ctx);
		templateData.addEntry(_CONFIG, cfg); 
		
		return this;
	}
	
	public TemplateDataProvider addArgumentProvidersData(TemplateData templateData, BpmNotificationConfig cfg, ProcessInstance pi)
	{
		if (hasText(cfg.getTemplateArgumentProvider())) 
		{
			for (TemplateArgumentProvider argumentProvider : argumentProviders) {
				if (cfg.getTemplateArgumentProvider().equalsIgnoreCase(argumentProvider.getName())) {
					TemplateArgumentProviderParams params = new TemplateArgumentProviderParams();
					params.setProcessInstance(pi);
					argumentProvider.addData(templateData, params);
					break;
				}
			}
		}
		
		return this;
	}
    
	public void registerTemplateArgumentProvider(TemplateArgumentProvider provider) {
		argumentProviders.add(provider);
	}

	public void unregisterTemplateArgumentProvider(TemplateArgumentProvider provider) {
		argumentProviders.add(provider);
	}

	public Collection<TemplateArgumentProvider> getTemplateArgumentProviders() {
		return new ArrayList<TemplateArgumentProvider>(argumentProviders);
	}

	public List<TemplateArgumentDescription> getDefaultArgumentDescriptions(I18NSource i18NSource) {
		return Arrays.asList(
				descr(_PROCESS_VISIBLE_ID, "Process visible id, external process key or internal id if external doesn't exist", i18NSource),
				descr(_PROCESS_ID, "Process id in database", i18NSource),
				descr(_PROCESS, "Process instance, you can access all process properties if seperatre by dot", i18NSource),
				descr(_USER, "Current assignee instance (UserData), you can access all process properties if seperatre by dot.", i18NSource),
				descr(_ASSIGNEE, "Current assignee instance (UserData), you can access all process properties if seperatre by dot.", i18NSource),
				descr(_CREATOR, "Process creator instance (UserData), you can access all process properties if seperatre by dot.", i18NSource),
				descr(_SESSION, "Current session instance, do not use in template", i18NSource),
				descr(_CONTEXT, "Current context instance, do not use in template", i18NSource),
				descr(_CONFIG, "Current template config instance, do not use in template ", i18NSource),
				descr(_TASK, "If task exists, this is instance of current BpmTask", i18NSource),
				descr(_TASK_NAME, "If task exists, this is the name of current BpmTask", i18NSource),
				descr(_TASK_URL, "If task exists, this is url link for webbrowser to this task", i18NSource),
				descr(_TASK_LINK, "If task exists, this is url link for webbrowser to this task", i18NSource)
		);
	}

	private TemplateArgumentDescription descr(String name, String i18NKey, I18NSource i18NSource) {
		return new TemplateArgumentDescription(name, i18NSource.getMessage(i18NKey));
	}
}

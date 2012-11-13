package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import static pl.net.bluesoft.rnd.util.TaskUtil.getTaskLink;
import static pl.net.bluesoft.util.lang.Strings.hasText;

import java.util.*;

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
public class TemplateDataProvider 
{
	/** Process visible id, external process key or internal id if external doesn't exist. Use ${processVisibleId} in template */
	public static final String _PROCESS_VISIBLE_ID = "processVisibleId";
	
	/** Process id in database. Use ${processId} in template */
	public static final String _PROCESS_ID = "processId";
	
	/** Process instance, you can access all process properties if seperatre by dot. Use ${process} in template */
	public static final String _PROCESS = "process";
	
	/** Current assignee instance (UserData), you can access all process properties if seperatre by dot. Use ${user} in template */
	public static final String _USER = "user";
	
	/** Current assignee instance (UserData), you can access all process properties if seperatre by dot. Use ${user} in template */
	public static final String _ASSIGNEE = "assignee";
	
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
	
	private final Set<TemplateArgumentProvider> argumentProviders = new HashSet<TemplateArgumentProvider>();
	
    public Map<String, Object> prepareData(ProcessToolBpmSession bpmSession, BpmTask task, ProcessInstance pi, UserData userData, BpmNotificationConfig cfg, ProcessToolContext ctx) 
    {
    	
    	/* If task exists, add template data for it */
        Map<String, Object> templateData = new HashMap<String, Object>();
        if (task != null) {
            templateData.put(_TASK, task);

            Locale locale = Strings.hasText(cfg.getLocale()) ? new Locale(cfg.getLocale()) : Locale.getDefault();
            I18NSource messageSource = I18NSourceFactory.createI18NSource(locale);

			pi = ctx.getProcessInstanceDAO().refresh(pi);

			for (ProcessStateConfiguration st : pi.getDefinition().getStates()) {
                if (task.getTaskName().equals(st.getName())) {
                    templateData.put(_TASK_NAME, messageSource.getMessage(st.getDescription()));
                    break;
                }
            }

            templateData.put(_TASK_URL, getTaskLink(task, ctx));
            templateData.put(_TASK_LINK, getTaskLink(task, ctx));
        }
        
        
        /* Add assigne */
        UserData assignee = new UserData();
        if(task != null && task.getAssignee() != null) {
        	assignee = ctx.getUserDataDAO().loadUserByLogin(task.getAssignee());
		}
        
        templateData.put(_PROCESS_VISIBLE_ID, Strings.hasText(pi.getExternalKey()) ? pi.getExternalKey() : pi.getInternalId());
        templateData.put(_PROCESS_ID, Strings.hasText(pi.getExternalKey()) ? pi.getExternalKey() : pi.getInternalId());
        templateData.put(_PROCESS, pi);
        templateData.put(_USER, userData);
        templateData.put(_ASSIGNEE, assignee);
        templateData.put(_SESSION, bpmSession);
        templateData.put(_CONTEXT, ctx);
        templateData.put(_CONFIG, cfg);
        templateData.put(_CREATOR, pi.getCreator());

		if (hasText(cfg.getTemplateArgumentProvider())) {
			for (TemplateArgumentProvider argumentProvider : argumentProviders) {
				if (cfg.getTemplateArgumentProvider().equalsIgnoreCase(argumentProvider.getName())) {
					TemplateArgumentProviderParams params = new TemplateArgumentProviderParams();
					params.setProcessInstance(pi);
					argumentProvider.getArguments(templateData, params);
					break;
				}
			}
		}

        return templateData;
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

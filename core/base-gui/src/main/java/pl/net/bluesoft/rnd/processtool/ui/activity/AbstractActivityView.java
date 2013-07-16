package pl.net.bluesoft.rnd.processtool.ui.activity;

import org.aperteworkflow.ui.view.ViewCallback;
import org.aperteworkflow.util.vaadin.ResourceCache;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.Application;
import com.vaadin.ui.VerticalLayout;

/**
 * Main window for the Activity Application
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public abstract class AbstractActivityView extends VerticalLayout implements ViewCallback 
{
	private Application application;
	private I18NSource i18NSource;
	private ProcessToolBpmSession bpmSession;
	private ResourceCache resourceCache;
	
	protected abstract void displayProcessDataInPane(final BpmTask task, final ProcessToolBpmSession bpmSession, boolean forward);
	protected abstract void initLayout();

	public AbstractActivityView(Application application, I18NSource i18NSource, ProcessToolBpmSession bpmSession) 
	{
		this.resourceCache = new ResourceCache(application);
		this.application = application;
		this.i18NSource = i18NSource;
		this.bpmSession = bpmSession;
	}

	public Application getActivityApplication() {
		return application;
	}

	public ResourceCache getResourceCache() {
		return resourceCache;
	}

	public I18NSource getI18NSource() {
		return i18NSource;
	}

	public ProcessToolBpmSession getBpmSession() {
		return bpmSession;
	}
	
	public void displayProcessData(BpmTask task)
	{
		displayProcessData(task,getBpmSession());
	}

	public void displayProcessData(final BpmTask task, final ProcessToolBpmSession bpmSession)
	{
		displayProcessData(task,bpmSession,false);
	}

	public void displayProcessData(BpmTask task, boolean forward)
	{
		displayProcessData(task,null,forward);
	}

	public void displayProcessData(final BpmTask task, final ProcessToolBpmSession bpmSession, boolean forward)
	{
		displayProcessDataInPane(task,bpmSession,forward);
	}
	
	/**
	 * Display view of task by taskId
	 * @param taskId
	 */
	public void displayTaskById(String taskId)
	{
		BpmTask task = getBpmSession().getTaskData(taskId);
		if(task != null)
		{
			displayProcessData(task);
		}
		else
		{
			getActivityApplication().getMainWindow().showNotification(getI18NSource().getMessage("process.data.task-notfound").replaceFirst("%s",taskId));
		}
	}



}

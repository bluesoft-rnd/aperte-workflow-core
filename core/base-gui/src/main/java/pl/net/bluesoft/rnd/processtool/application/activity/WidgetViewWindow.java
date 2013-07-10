package pl.net.bluesoft.rnd.processtool.application.activity;

import static pl.net.bluesoft.util.lang.Formats.nvl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSessionHelper;
import pl.net.bluesoft.rnd.processtool.event.SaveTaskEvent;
import pl.net.bluesoft.rnd.processtool.event.ValidateTaskEvent;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.common.FailedProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetFactory;
import pl.net.bluesoft.rnd.processtool.ui.widgets.event.WidgetEventBus;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.util.lang.Lang;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * Widget view window. This window is created per parent widget which is
 * not handled by the jsp view. 
 * 
 * Window is destroyed when user closes the iframe view. 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class WidgetViewWindow extends Window
{
	private static Logger logger = Logger.getLogger(WidgetViewWindow.class.getName());
	
	private ProcessToolRegistry processToolRegistry;
	private I18NSource i18NSource;
	private WidgetApplication application;
	private ProcessToolBpmSession bpmSession;
	private WidgetFactory widgetFactory;
	
	private String bpmTaskId;
	private Long processStateWidgetId;
	
	private Boolean isInitlized = false;
	
	/** Is widget able to edit data? */
	private boolean isEditMode = false;
	
	private Collection<ProcessToolDataWidget> widgets = new ArrayList<ProcessToolDataWidget>();
	private Collection<String> errors = new ArrayList<String>();
	private WidgetEventBus widgetEventBus;
	
	public WidgetViewWindow(ProcessToolRegistry processToolRegistry, ProcessToolBpmSession bpmSession, WidgetApplication application, I18NSource i18NSource) 
	{
		this.processToolRegistry = processToolRegistry;
		this.bpmSession = bpmSession;
		this.application = application;
		this.i18NSource = i18NSource;
		this.widgetEventBus = new WidgetEventBus();
		
		this.widgetFactory = new WidgetFactory(bpmSession, application, i18NSource);
		
		widgets.clear();
	}
	
	public void validateWidgets(ValidateTaskEvent event)
	{
		/* if there is no data to edit, nothing changes */
		if(!isEditMode)
			return;
		
    	/* Check for task id, we don't want to validate widget from another process view */
    	final String eventTaskId = event.getBpmTask().getInternalTaskId();
    	if(!eventTaskId.equals(this.bpmTaskId))
    		return; 
    	
		errors.clear();
		
		for(ProcessToolDataWidget widget: widgets)
		{
			Collection<String>  errors = widget.validateData(event.getBpmTask(), true);
			
			if(errors != null)
				for(String error: errors)
					event.addError(processStateWidgetId.toString(), error);
		}
	}
	
	public void saveWidgets(SaveTaskEvent event)
	{
		/* if there is no data to edit, nothing changes */
		if(!isEditMode)
			return;
		
    	/* Check for task id, we don't want to save widget from another process view */
    	final String eventTaskId = event.getBpmTask().getInternalTaskId();
    	if(!eventTaskId.equals(this.bpmTaskId))
    		return; 
    	
    	/* Do not save widget if there others have errors */
		if(event.hasErrors())
			return;
		
		/* Save all widgets */
		for(ProcessToolDataWidget widget: widgets)
		{
			try
			{
				widget.saveData(event.getBpmTask());
			}
			catch(Throwable e)
			{
				logger.log(Level.SEVERE, e.getMessage(), e);
				event.addError(processStateWidgetId.toString(), e.getMessage());
			}
		}
	}
	
	@Override
	public void setLocale(Locale locale) 
	{
		this.i18NSource = I18NSourceFactory.createI18NSource(locale);
		super.setLocale(locale);
	}

	
	public void initlizeWidget(String taskId, String widgetId)
	{
		if(isInitlized)
			return;
		
		processStateWidgetId = Long.parseLong(widgetId);
		bpmTaskId = taskId;

		processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
			
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				ProcessStateWidget processStateWidget = ctx.getProcessDefinitionDAO().getProcessStateWidget(processStateWidgetId);
				
				BpmTask task = bpmSession.getTaskData(bpmTaskId);
				
				if(task == null)
					task = bpmSession.getHistoryTask(bpmTaskId);

				ProcessToolWidget widget = getWidget(processStateWidget, ctx, "1", task);
				if (widget instanceof ProcessToolVaadinRenderable && (!nvl(processStateWidget.getOptional(), false) || widget.hasVisibleData())) 
				{
					processWidgetChildren(processStateWidget, widget, ctx, "1", task);
					ProcessToolVaadinRenderable vaadinW = (ProcessToolVaadinRenderable) widget;
					
					Component renderedWidget = vaadinW.render();
					if(renderedWidget != null)
					{

						
						logger.warning("add widget: "+vaadinW.getClass());
						renderedWidget.setSizeFull();
						WidgetViewWindow.this.addComponent(vaadinW.render());
						
	
					}
				}
				
				isInitlized = true;
			}
		});
		
	}

	
	private ProcessToolWidget getWidget(ProcessStateWidget processStateWidget, ProcessToolContext ctx, String generatorKey, BpmTask task) 
	{
		ProcessToolWidget processToolWidget;
		try 
		{
			String widgetClassName = processStateWidget.getClassName() == null ? processStateWidget.getName() : processStateWidget.getClassName();
			
			processToolWidget = widgetFactory.makeWidget(widgetClassName, processStateWidget, bpmSession.getPermissionsForWidget(processStateWidget), true);
			
			processToolWidget.setGeneratorKey(generatorKey);
			processToolWidget.setTaskId(bpmTaskId);
			processToolWidget.setWidgetEventBus(widgetEventBus);

			if (processToolWidget instanceof ProcessToolDataWidget) 
			{
				ProcessToolDataWidget dataWidget = (ProcessToolDataWidget)processToolWidget;
				dataWidget.loadData(task);
				widgets.add(dataWidget);
				
				if(processToolWidget.hasPermission("EDIT"))
					isEditMode = true;
			}
		}
		catch (final Exception e) 
		{
			FailedProcessToolWidget failedProcessToolVaadinWidget = new FailedProcessToolWidget(e);
			failedProcessToolVaadinWidget.setContext(processStateWidget.getConfig(), processStateWidget, i18NSource, bpmSession, application,
					ProcessToolBpmSessionHelper.getPermissionsForWidget(bpmSession, ctx, processStateWidget),
			                         true);
			processToolWidget = failedProcessToolVaadinWidget;
		}
		return processToolWidget;
	}
	
	private void processWidgetChildren(ProcessStateWidget parentWidgetConfiguration, ProcessToolWidget parentWidgetInstance,
			ProcessToolContext ctx, String generatorKey, BpmTask task) 
	{
		Set<ProcessStateWidget> children = parentWidgetConfiguration.getChildren();
		
		/* Sory widgets by priority */
		List<ProcessStateWidget> sortedList = new ArrayList<ProcessStateWidget>(children);
		Collections.sort(sortedList, new Comparator<ProcessStateWidget>() 
		{
			@Override
			public int compare(ProcessStateWidget o1, ProcessStateWidget o2) {
				if (o1.getPriority().equals(o2.getPriority())) {
					return Lang.compare(o1.getId(), o2.getId());
				}
				return o1.getPriority().compareTo(o2.getPriority());
			}
		});


		for (ProcessStateWidget subW : sortedList) 
		{
			if(StringUtils.isNotEmpty(subW.getGenerateFromCollection()))
			{
				generateChildren(parentWidgetInstance, ctx, subW, task);
			} 
			else 
			{
				subW.setParent(parentWidgetConfiguration);
				addWidgetChild(parentWidgetInstance, ctx, subW, generatorKey, task);
			}
		}
	}
	
	private void generateChildren(ProcessToolWidget parentWidgetInstance, ProcessToolContext ctx,
			ProcessStateWidget subW, BpmTask task) 
	{
		String collection = task.getProcessInstance().getSimpleAttributeValue(subW.getGenerateFromCollection(), null);
		if(StringUtils.isEmpty(collection))
			return;
		String[] items = collection.split("[,; ]");

		for(String item : items){
			addWidgetChild(parentWidgetInstance, ctx, subW, item, task);
		}
	}

	private void addWidgetChild(ProcessToolWidget parentWidgetInstance, ProcessToolContext ctx,
			ProcessStateWidget subW, String generatorKey, BpmTask task) 
	{
		ProcessToolWidget widgetInstance = getWidget(subW, ctx, generatorKey, task);
		
		if (!nvl(subW.getOptional(), false) || widgetInstance.hasVisibleData()) 
		{
			processWidgetChildren(subW, widgetInstance, ctx, generatorKey, task);
			parentWidgetInstance.addChild(widgetInstance);
		}
	}
	
	@Override
	protected void close() 
	{
		destroy();
		super.close();
	}

	public void destroy()
	{
		removeAllComponents();
		widgets.clear();
		isInitlized = false;
		bpmTaskId = null;
		
		logger.warning("destroy...: "+getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bpmTaskId == null) ? 0 : bpmTaskId.hashCode());
		result = prime
				* result
				+ ((processStateWidgetId == null) ? 0 : processStateWidgetId
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WidgetViewWindow other = (WidgetViewWindow) obj;
		if (bpmTaskId == null) {
			if (other.bpmTaskId != null)
				return false;
		} else if (!bpmTaskId.equals(other.bpmTaskId))
			return false;
		if (processStateWidgetId == null) {
			if (other.processStateWidgetId != null)
				return false;
		} else if (!processStateWidgetId.equals(other.processStateWidgetId))
			return false;
		return true;
	}
	
	
	
}



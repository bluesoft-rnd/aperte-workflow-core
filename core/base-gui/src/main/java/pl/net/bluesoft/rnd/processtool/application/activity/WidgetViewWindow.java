package pl.net.bluesoft.rnd.processtool.application.activity;

import static pl.net.bluesoft.util.lang.Formats.nvl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.event.SaveTaskEvent;
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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
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
public class WidgetViewWindow extends Window implements ParameterHandler, HttpServletRequestListener
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
	
	private EventBus eventBus;
	
	private Collection<ProcessToolDataWidget> widgets = new ArrayList<ProcessToolDataWidget>();
	private Collection<String> errors = new ArrayList<String>();
	
	public WidgetViewWindow(ProcessToolRegistry processToolRegistry, ProcessToolBpmSession bpmSession, WidgetApplication application, I18NSource i18NSource, EventBus eventBus) 
	{
		this.processToolRegistry = processToolRegistry;
		this.bpmSession = bpmSession;
		this.application = application;
		this.i18NSource = i18NSource;
		this.eventBus = eventBus;
		
		
		this.widgetFactory = new WidgetFactory(bpmSession, application, i18NSource);
		
		this.addParameterHandler(this);
		
		widgets.clear();
	}
	
    @Subscribe
    public void listen(final SaveTaskEvent event)
    {
    	boolean test = isVisible();
    	/* Check for task id, we don't want to save widget from another process view */
    	final String eventTaskId = event.getTaskId();
    	if(!eventTaskId.equals(this.bpmTaskId))
    		return;
    	
    	logger.warning("Perform widget save for taskId: "+bpmTaskId+", windowName: "+this.getName());
    	processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
			
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				BpmTask task = bpmSession.getTaskData(bpmTaskId, ctx);
				
				errors.clear();
				
				for(ProcessToolDataWidget widget: widgets)
				{
					Collection<String>  errors = widget.validateData(task, true);
					
					if(errors != null)
						for(String error: errors)
							event.addError(processStateWidgetId.toString(), error);
				}
				
				if(event.hasErrors())
					return;
				
				/* Save all widgets */
				for(ProcessToolDataWidget widget: widgets)
				{
					try
					{
						widget.saveData(task);
					}
					catch(Throwable e)
					{
						logger.log(Level.SEVERE, e.getMessage(), e);
						event.addError(processStateWidgetId.toString(), e.getMessage());
					}
				}

			}
		});
    }
	
	@Override
	public void setLocale(Locale locale) 
	{
		this.i18NSource = I18NSourceFactory.createI18NSource(locale);
		super.setLocale(locale);
	}


	@Override
	public void handleParameters(Map<String, String[]> parameters) 
	{
		synchronized (isInitlized)
		{
			removeAllComponents();
			
			String[] widgetId = parameters.get("widgetId");
			String[] taskId = parameters.get("taskId");
			String[] close = parameters.get("close");
			
			if(widgetId == null || taskId == null)
				return;
			
			if(close != null)
			{
				this.close();
				return;
			}
			
			if(!isInitlized)
				initlizeWidget(taskId[0], widgetId[0]);
		}

		

	}
	
	public void initlizeWidget(String taskId, String widgetId)
	{
		processStateWidgetId = Long.parseLong(widgetId);
		bpmTaskId = taskId;
		
		addComponent(new Label("widgetId: "+widgetId));
		addComponent(new Label("taskId: "+taskId));
		
		logger.warning("windowName=: "+getName());
		logger.warning("initlize ,widgetId "+processStateWidgetId+", bpmTaskId: "+bpmTaskId);

		processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
			
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				ProcessStateWidget processStateWidget = ctx.getProcessDefinitionDAO().getProcessStateWidget(processStateWidgetId);
				
				BpmTask task = bpmSession.getTaskData(bpmTaskId, ctx);
				
				WidgetEventBus eventBus = new WidgetEventBus();

				ProcessToolWidget widget = getWidget(processStateWidget, ctx, "1", eventBus, task);
				if (widget instanceof ProcessToolVaadinRenderable && (!nvl(processStateWidget.getOptional(), false) || widget.hasVisibleData())) 
				{
					processWidgetChildren(processStateWidget, widget, ctx, "1", eventBus, task);
					ProcessToolVaadinRenderable vaadinW = (ProcessToolVaadinRenderable) widget;
					
					Component renderedWidget = vaadinW.render();
					if(renderedWidget != null)
					{
						renderedWidget.setSizeFull();
						WidgetViewWindow.this.addComponent(vaadinW.render());
					}
				}
				
				isInitlized = true;
				
				logger.fine("Widget window initlized");
			}
		});
		
		this.eventBus.register(this);
	}
	
	
	private ProcessToolWidget getWidget(ProcessStateWidget processStateWidget, ProcessToolContext ctx, String generatorKey, WidgetEventBus widgetEventBus, BpmTask task) 
	{
		ProcessToolWidget processToolWidget;
		try 
		{
			String widgetClassName = processStateWidget.getClassName() == null ? processStateWidget.getName() : processStateWidget.getClassName();
			
			processToolWidget = widgetFactory.makeWidget(widgetClassName, processStateWidget, bpmSession.getPermissionsForWidget(processStateWidget, ctx), true);
			
			processToolWidget.setGeneratorKey(generatorKey);
			processToolWidget.setTaskId(bpmTaskId);
			

			if (processToolWidget instanceof ProcessToolDataWidget) 
			{
				ProcessToolDataWidget dataWidget = (ProcessToolDataWidget)processToolWidget;
				dataWidget.loadData(task);
				widgets.add(dataWidget);
			}
		}
		catch (final Exception e) 
		{
			FailedProcessToolWidget failedProcessToolVaadinWidget = new FailedProcessToolWidget(e);
			failedProcessToolVaadinWidget.setContext(processStateWidget.getConfig(), processStateWidget, i18NSource, bpmSession, application,
			                         bpmSession.getPermissionsForWidget(processStateWidget, ctx),
			                         true);
			processToolWidget = failedProcessToolVaadinWidget;
		}
		return processToolWidget;
	}
	
	private void processWidgetChildren(ProcessStateWidget parentWidgetConfiguration, ProcessToolWidget parentWidgetInstance,
			ProcessToolContext ctx, String generatorKey, WidgetEventBus widgetEventBus, BpmTask task) 
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
		
//		if(parentWidgetInstance instanceof ProcessToolChildrenFilteringWidget){
//			sortedList = ((ProcessToolChildrenFilteringWidget)parentWidgetInstance).filterChildren(task, sortedList);
//		}

		for (ProcessStateWidget subW : sortedList) 
		{
			if(StringUtils.isNotEmpty(subW.getGenerateFromCollection()))
			{
				generateChildren(parentWidgetInstance, ctx, subW, widgetEventBus, task);
			} 
			else 
			{
				subW.setParent(parentWidgetConfiguration);
				addWidgetChild(parentWidgetInstance, ctx, subW, generatorKey, widgetEventBus, task);
			}
		}
	}
	
	private void generateChildren(ProcessToolWidget parentWidgetInstance, ProcessToolContext ctx,
			ProcessStateWidget subW, WidgetEventBus widgetEventBus, BpmTask task) 
	{
		String collection = task.getProcessInstance().getSimpleAttributeValue(subW.getGenerateFromCollection(), null);
		if(StringUtils.isEmpty(collection))
			return;
		String[] items = collection.split("[,; ]");

		for(String item : items){
			addWidgetChild(parentWidgetInstance, ctx, subW, item, widgetEventBus, task);
		}
	}

	private void addWidgetChild(ProcessToolWidget parentWidgetInstance, ProcessToolContext ctx,
			ProcessStateWidget subW, String generatorKey, WidgetEventBus widgetEventBus, BpmTask task) 
	{
		ProcessToolWidget widgetInstance = getWidget(subW, ctx, generatorKey, widgetEventBus, task);
		
		if (!nvl(subW.getOptional(), false) || widgetInstance.hasVisibleData()) 
		{
			processWidgetChildren(subW, widgetInstance, ctx, generatorKey, widgetEventBus, task);
			parentWidgetInstance.addChild(widgetInstance);
		}
	}
	
	@Override
	protected void close() 
	{
		try
		{
			/* We have to unregister view form bus */
			eventBus.unregister(this);
			logger.warning("destroy...: "+getName());
			this.getApplication().removeWindow(this);
		}
		catch(IllegalArgumentException ex)
		{
			
		}
		super.close();
	}
	
	@Override
	public void detach() 
	{
		try
		{
			/* We have to unregister view form bus */
			eventBus.unregister(this);
			logger.warning("destroy...: "+getName());
		}
		catch(IllegalArgumentException ex)
		{
			
		}
		super.detach();
	}
	
	

	@Override
	public void onRequestStart(HttpServletRequest request,HttpServletResponse response) 
	{

		
	}

	@Override
	public void onRequestEnd(HttpServletRequest request,HttpServletResponse response) 
	{
		
	}
	
	
}



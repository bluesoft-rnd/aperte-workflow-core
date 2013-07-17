package org.aperteworkflow.webapi.main.processes.processor;

import java.util.Collection;

import org.aperteworkflow.webapi.main.processes.action.domain.SaveResultBean;
import org.aperteworkflow.webapi.main.processes.action.domain.ValidateResultBean;
import org.aperteworkflow.webapi.main.processes.domain.HtmlWidget;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.event.SaveTaskEvent;
import pl.net.bluesoft.rnd.processtool.event.ValidateTaskEvent;
import pl.net.bluesoft.rnd.processtool.event.beans.ErrorBean;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetValidator;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.google.common.eventbus.EventBus;

/**
 * Task save processor class 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class TaskProcessor 
{
	private EventBus eventBus;
	private BpmTask task;
	private ProcessToolContext ctx;
	private Collection<HtmlWidget> widgets;
	private I18NSource messageSource;
	
	public TaskProcessor(BpmTask task, ProcessToolContext ctx, EventBus eventBus, I18NSource messageSource, Collection<HtmlWidget> widgets)
	{
		this.eventBus = eventBus;
		this.task = task;
		this.ctx = ctx;
		this.widgets = widgets;
	}
	
	/** Validate vaadin and html widgets. Validation is performed to all widgets and
	 *  one widgets error does not stop validation processes */
	public ValidateResultBean validateWidgets()
	{
		ValidateResultBean validateResult = new ValidateResultBean();
		validateHtmlWidgets(validateResult);
		validateVaadinWidgets(validateResult);
		
		return validateResult;
	}
	
	/** Save vaadin and html widgets */
	public SaveResultBean saveWidgets()
	{
		SaveResultBean saveResult = new SaveResultBean();
		saveHtmlWidgets(saveResult);
		saveVaadinWidgets(saveResult);
		
		return saveResult;
	}
	
	private void validateHtmlWidgets(ValidateResultBean validateResult)
	{
		for(HtmlWidget widgetToValidate: widgets)
		{
			/** Get widget definition to retrive validator class */
			ProcessHtmlWidget processWidget = ctx.getRegistry().getHtmlWidget(widgetToValidate.getWidgetName());
			if(processWidget == null)
				throw new RuntimeException(messageSource.getMessage("process.widget.name.unknown", (String)widgetToValidate.getWidgetName()));
			
			IWidgetValidator widgetValidator = processWidget.getValidator();
			
			Collection<String> errors = widgetValidator.validate(task, widgetToValidate.getData());
			for(String error: errors)
				validateResult.addError(widgetToValidate.getWidgetId().toString(), error);
		}
	}
	
	private void saveHtmlWidgets(SaveResultBean saveResult)
	{
		for(HtmlWidget widgetToSave: widgets)
		{
			/** Get widget definition to retrive data handler class */
			ProcessHtmlWidget processWidget = ctx.getRegistry().getHtmlWidget(widgetToSave.getWidgetName());
			if(processWidget == null)
				throw new RuntimeException(messageSource.getMessage("process.widget.name.unknown", (String)widgetToSave.getWidgetName()));
			
			IWidgetDataHandler widgetDataHandler = processWidget.getDataHandler();
			
			widgetDataHandler.handleWidgetData(task, widgetToSave.getData());
		}
	}
	
	/** Send event to all vaadin widgets to perform validation task. Widgets are 
	 * registered for this event and filtration is done by taskId
	 * 
	 * @param taskId of task to being saved
	 * @return
	 */
	private void validateVaadinWidgets(ValidateResultBean validateResult)
	{
		ValidateTaskEvent validateEvent = new ValidateTaskEvent(task);
		
		eventBus.post(validateEvent);
		
		/* Copy all errors from event */
		for(ErrorBean errorBean: validateEvent.getErrors())
			validateResult.addError(errorBean);
	}
	
	/** Send event to all vaadin widgets to perform save task. Widgets are 
	 * registered for this event and filtration is done by taskId
	 * 
	 * @param taskId of task to being saved
	 * @return
	 */
	private void saveVaadinWidgets(SaveResultBean saveResult)
	{
		SaveTaskEvent saveEvent = new SaveTaskEvent(task);
		
		eventBus.post(saveEvent);
		
		/* Copy all errors from event */
		for(ErrorBean errorBean: saveEvent.getErrors())
			saveResult.addError(errorBean);
	}
}

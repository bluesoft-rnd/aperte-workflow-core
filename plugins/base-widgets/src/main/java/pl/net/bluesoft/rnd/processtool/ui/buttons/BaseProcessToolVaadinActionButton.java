package pl.net.bluesoft.rnd.processtool.ui.buttons;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;

import org.apache.commons.lang3.StringUtils;
import org.aperteworkflow.util.vaadin.TaskAlreadyCompletedException;
import pl.net.bluesoft.rnd.processtool.ui.WidgetContextSupport;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolActionButton;
import org.aperteworkflow.util.vaadin.VaadinExceptionHandler;
import org.aperteworkflow.util.vaadin.VaadinUtility;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public abstract class BaseProcessToolVaadinActionButton extends BaseProcessToolActionButton implements ProcessToolVaadinRenderable {
	protected Component renderedComponent = null;
	protected BpmTask task;

	@Override
	public Component render() {
		Button button = VaadinUtility.button(getVisibleLabel(), getVisibleDescription(), getComponentStyleName());
		button.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				withErrorHandling(
				                  new VaadinExceptionHandler() {
				                	  public void onThrowable(Throwable e) {
				                		  VaadinExceptionHandler.Util.onException(application, e);
				                		  callback.actionFailed(definition);
				                	  }
				                  }, new Runnable() {
				                	  @Override
				                	  public void run() {
				                		  WidgetContextSupport support = callback.getWidgetContextSupport();
				                		  task = support.refreshTask(bpmSession, task);
				                		  performAction(callback.getWidgetContextSupport());
				                	  }
				                  }
						);
			}
		});
		button.addStyleName(actionType);
//		if(ProcessStateAction.SECONDARY_ACTION.equals(actionType))
//			button.addStyleName(BaseTheme.BUTTON_LINK);
		button.setEnabled(enabled);
		renderedComponent = button;
		return button;
	}

	protected abstract void performAction(WidgetContextSupport support);

	protected void invokeBpmTransition() {
		ProcessToolContext ctx = getCurrentContext();
		task = bpmSession.performAction(definition, task, ctx);
		if (task == null || task.isFinished()) {
			showTransitionNotification();
		}
		if (task == null) {
			throw new TaskAlreadyCompletedException();
		}
		callback.getWidgetContextSupport().updateTask(task);
	}

	private void showTransitionNotification() {
		if(!StringUtils.isEmpty(notification) && !"null".equals(notification))
			VaadinUtility.informationNotification(getApplication(), getMessage(notification));
	}

	protected void invokeSaveTask() {
		WidgetContextSupport support = callback.getWidgetContextSupport();
		task = support.refreshTask(bpmSession, task);
		support.saveTaskData(task, this);
	}

    protected void invokeSaveTaskWithoutData() {
		WidgetContextSupport support = callback.getWidgetContextSupport();
		task = support.refreshTask(bpmSession, task);
		support.saveTaskWithoutData(task, this);
	}

	@Override
	public void loadData(BpmTask task) {
		this.task = task;
	}

	@Override
	public void saveData(BpmTask task) {
		task.getProcessInstance().getRootProcessInstance().setSimpleAttribute("markedImportant", markProcessImportant);
		// override
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (renderedComponent != null) {
			renderedComponent.setEnabled(enabled);
		}
	}


}

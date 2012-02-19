package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TasksMainPane;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class QueueListPane extends ProcessListPane {

	private ProcessQueue q;

	public QueueListPane(ActivityMainPane activityMainPane,
	                     String title,
	                     ProcessQueue q) {
		super(activityMainPane, title);
		this.q = q;
		refreshData();
	}

    protected QueueListPane(ActivityMainPane activityMainPane,
	                     String title,
	                     ProcessQueue q,
                        boolean callInit) {
		super(activityMainPane, title, callInit);
		this.q = q;
        if (callInit) {
            init(title);
        }
	}

    @Override
    protected void init(String title) {
        super.init(title);
        refreshData();
    }

	@Override
	protected Component getTaskItem(final TasksMainPane.TaskTableItem tti) {
        final ProcessInstance pi = tti.getProcessInstance();
		Panel p = new Panel(getMessage(pi.getDefinition().getDescription()) + " " +
				              nvl(pi.getExternalKey(), pi.getInternalId()) + " " +
				              new SimpleDateFormat("yyyy-MM-dd").format(pi.getCreateDate()));
		VerticalLayout vl = new VerticalLayout();
		Label titleLabel = new Label(getMessage(tti.getState()));
		titleLabel.addStyleName("h2 color processtool-title");
		titleLabel.setWidth("100%");
		HorizontalLayout hl = horizontalLayout("100%",
		                                       titleLabel);
//	                                          new Label(nvl(tti.processInstance.getKeyword(), "")),
//	                                          new Label(nvl(tti.getProcessInstance().getDescription(), "")));
		hl.setExpandRatio(titleLabel, 1.0f);
		vl.addComponent(hl);
		Button button = new Button(getMessage("activity.tasks.task-claim"));
		button.addStyleName(BaseTheme.BUTTON_LINK);
		button.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				withErrorHandling(getApplication(), new Runnable() {
					public void run() {
						ProcessInstance processInstance = getBpmSession().assignTaskFromQueue(q, tti.getProcessInstance(),
						                                                                                       ProcessToolContext.Util.getThreadProcessToolContext());
						if (processInstance != null) {
							getApplication().getMainWindow().showNotification(getMessage("process-tool.task.assigned"),
							                                                  Window.Notification.TYPE_HUMANIZED_MESSAGE);
							displayProcessData(pi);


						}
					}
				});
			}
		});
        if (tti.getStateConfiguration() != null) {
            vl.addComponent(horizontalLayout(new Label(nvl(getMessage(tti.getStateConfiguration().getCommentary()), ""),
                    Label.CONTENT_XHTML), button));
        }
		vl.setWidth("100%");
		if (pi.getKeyword() != null) {
			vl.addComponent(new Label(pi.getKeyword()));
		}
		if (pi.getDescription() != null) {
			vl.addComponent(new Label(pi.getDescription()));
		}
		p.setWidth("100%");
		p.addComponent(vl);
		return p;
	}

    protected void displayProcessData(ProcessInstance processInstance) {
        activityMainPane.displayProcessData(processInstance);        
    }

	protected List<ProcessInstance> getProcessInstances(String filterExpression, int offset, int limit) {
		if (q == null) return new ArrayList();
        if (filterExpression == null || filterExpression.trim().isEmpty()) {
            return new ArrayList(getBpmSession().getQueueContents(q, offset, limit,
                    ProcessToolContext.Util.getThreadProcessToolContext()));
        } else {
            return new ArrayList<ProcessInstance>(ProcessToolContext.Util.getThreadProcessToolContext().getProcessInstanceDAO()
                                .searchProcesses(filterExpression, offset, limit, true, null, null, q.getName()));
        }
	}
}

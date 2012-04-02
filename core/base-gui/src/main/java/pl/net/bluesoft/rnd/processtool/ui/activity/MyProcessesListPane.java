package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TasksMainPane;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TasksMainPane.TaskTableItem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.aperteworkflow.util.vaadin.VaadinUtility;
import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class MyProcessesListPane extends ProcessListPane {

    public MyProcessesListPane(ActivityMainPane activityMainPane,
	                           String title) {
        super(activityMainPane, title);
	}

    protected MyProcessesListPane(ActivityMainPane activityMainPane,
	                           String title, boolean callInit) {
        super(activityMainPane, title, callInit);
	}

    @Override
    protected Component getTaskItem(final TasksMainPane.TaskTableItem tti) {
    	ProcessInstance pi = tti.getProcessInstance();

		Panel p = new Panel(buildTaskItemHeader(pi));

        p.setWidth("100%");
        p.addStyleName("tti-panel");

        GridLayout layout = new GridLayout(6, 2);
        layout.setWidth("100%");
        layout.setSpacing(true);
        layout.addListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                openProcessInstance(tti);
            }
        });

        // the icon
        Embedded taskIcon = new Embedded(null, createTaskIcon(tti));
        taskIcon.setDescription(getMessage(pi.getDefinition().getDescription()));

        VerticalLayout til = new VerticalLayout();
        til.setWidth(60, UNITS_PIXELS);
        til.addComponent(taskIcon);
        til.setComponentAlignment(taskIcon, Alignment.MIDDLE_CENTER);

        layout.addComponent(til, 0, 0, 0, 1);
        layout.setComponentAlignment(til, Alignment.MIDDLE_CENTER);

        // upper bar

        Button processNameButton = createOpenProcessInstanceButton(nvl(pi.getExternalKey(), pi.getInternalId()), tti);
        Button processDescriptionButton = createOpenProcessInstanceButton(getMessage(pi.getDefinition().getDescription()), tti);

        layout.addComponent(processDescriptionButton, 1, 0, 1, 0);
        layout.addComponent(processNameButton, 2, 0, 5, 0);
        layout.setComponentAlignment(processDescriptionButton, Alignment.MIDDLE_LEFT);
        layout.setComponentAlignment(processNameButton, Alignment.MIDDLE_RIGHT);

        // lower bar

		Label longProcessStepName = new Label(getMessage(tti.getState()));
        longProcessStepName.setWidth(200, UNITS_PIXELS);
        longProcessStepName.addStyleName("tti-details");
        longProcessStepName.setDescription(getMessage("activity.state"));
		layout.addComponent(longProcessStepName, 1, 1, 1, 1);
        layout.setComponentAlignment(longProcessStepName, Alignment.MIDDLE_CENTER);

        Date createDate = pi.getCreateDate();
        Date deadlineDate = getDeadlineDate(pi);
        String creatorName = pi.getCreator().getRealName();
        String assignedName = getBpmSession().getUser(ProcessToolContext.Util.getThreadProcessToolContext()).getRealName();

        Component cell = createGridCell("/img/user_creator.png", creatorName, "tti-person", getMessage("activity.creator"));
        layout.addComponent(cell, 2, 1, 2, 1);
        layout.setComponentAlignment(cell, Alignment.MIDDLE_CENTER);

        layout.addComponent(cell = createGridCell("/img/date_standard.png", formatDate(createDate), "tti-date",
                getMessage("activity.creationDate")), 3, 1, 3, 1);
        layout.setComponentAlignment(cell, Alignment.MIDDLE_CENTER);

        layout.addComponent(cell = createGridCell("/img/user_assigned.png", assignedName, "tti-person",
                getMessage("activity.assigned")), 4, 1, 4, 1);
        layout.setComponentAlignment(cell, Alignment.MIDDLE_CENTER);

        layout.addComponent(cell = createGridCell("/img/date_deadline.png", deadlineDate != null ? formatDate(deadlineDate)
                : getMessage("activity.nodeadline"), "tti-date", getMessage("activity.deadlineDate")), 5, 1, 5, 1);
        layout.setComponentAlignment(cell, Alignment.MIDDLE_LEFT);

        layout.setColumnExpandRatio(layout.getColumns() - 1, 1);
        layout.setRowExpandRatio(layout.getRows() - 1, 1);

		p.addComponent(layout);
		return p;
	}

    private Button createOpenProcessInstanceButton(String caption, final TaskTableItem tti) {
        Button b = new Button(caption);
        b.setHeight("26px");
        b.setStyleName("link tti-head");
        b.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                openProcessInstance(tti);
            }
        });
        return b;
    }

    protected void openProcessInstance(final TaskTableItem tti) {
        withErrorHandling(getApplication(), new Runnable() {
            public void run() {
                displayProcessData(tti.getProcessInstance());
            }
        });
    }

    protected void displayProcessData(ProcessInstance processInstance) {
        activityMainPane.displayProcessData(processInstance);
    }

    private Date getDeadlineDate(ProcessInstance pi) {
        Set<ProcessDeadline> deadlines = pi.findAttributesByClass(ProcessDeadline.class);
        for (ProcessDeadline pd : deadlines) {
            if (pd.getTaskName().equals(pi.getState())) {
                return pd.getDueDate();
            }
        }
        return null;
    }

    @Override
    protected boolean getDataPaneUsesSpacing() {
        return false;
    }

    private Resource createTaskIcon(TasksMainPane.TaskTableItem tti) {
        final ProcessDefinitionConfig cfg = tti.getProcessInstance().getDefinition();
        String path = cfg.getProcessLogo() != null ? cfg.getBpmDefinitionKey() + "_" + cfg.getId() +
                "_logo.png" : "/img/aperte-logo.png";
        Resource res = getResource(path);
        if (res == null) {
            if (cfg.getProcessLogo() != null) {
                res = new StreamResource(new StreamSource() {
                    @Override
                    public InputStream getStream() {
                        return new ByteArrayInputStream(cfg.getProcessLogo());
                    }
                }, path, activityMainPane.getApplication());
                cacheResource(path, res);
            }
            else {
                res = getImage(path);
            }
        }
        return res;
	}

    private HorizontalLayout createGridCell(String image, String caption, String style, String description) {
        Embedded img = new Embedded(null, getImage(image));
        img.setDescription(description);
        Label label = new Label(caption, Label.CONTENT_XHTML);
        label.setDescription(description);
        if (style != null) {
            label.setStyleName(style);
        }
        HorizontalLayout hl = VaadinUtility.horizontalLayout(Alignment.MIDDLE_LEFT, img, label);
        hl.setWidth("-1px");
        return hl;
    }

    protected List<ProcessInstance> getProcessInstances(String filterExpression, int offset, int limit) {
        if (filterExpression != null && !filterExpression.trim().isEmpty()) {
            return new ArrayList<ProcessInstance>(ProcessToolContext.Util.getThreadProcessToolContext().getProcessInstanceDAO()
                    .searchProcesses(filterExpression, offset, limit, true, null, getBpmSession().getUserLogin()));
        } else {
            return new ArrayList(getBpmSession()
                    .getUserProcesses(offset, limit, ProcessToolContext.Util.getThreadProcessToolContext()));
        }
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy").format(date);
    }
}

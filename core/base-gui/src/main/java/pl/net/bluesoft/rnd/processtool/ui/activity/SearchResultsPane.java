package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.tasks.TasksMainPane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.aperteworkflow.util.vaadin.VaadinUtility.getLocalizedMessage;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class SearchResultsPane extends ProcessListPane {

    public SearchResultsPane(String query, ActivityMainPane activityMainPane) {
        super(activityMainPane, getLocalizedMessage("activity.search.results.title") + " " + query);
        this.filterExpression = query;
        searchField.setEnabled(false); //hide search field
    }

    @Override
    protected List<ProcessInstance> getProcessInstances(String filterExpression, int offset, int limit) {

        Collection<String> roleNames = activityMainPane.getBpmSession().getRoleNames();
        return new ArrayList<ProcessInstance>(ProcessToolContext.Util.getThreadProcessToolContext()
                .getProcessInstanceDAO().searchProcesses(filterExpression, offset, limit,
                        true,
                        roleNames.toArray(new String[roleNames.size()]),
                        null));
    }

    @Override
    protected Component getTaskItem(TasksMainPane.TaskTableItem tti) {
        final ProcessInstance pi = tti.getProcessInstance();
        Panel p = new Panel(buildTaskItemHeader(pi));
        VerticalLayout vl = new VerticalLayout();
        Label titleLabel = new Label(getMessage(tti.getState()));
        titleLabel.addStyleName("h2 color processtool-title");
        titleLabel.setWidth("100%");
        HorizontalLayout hl = horizontalLayout("100%", titleLabel);
        hl.setExpandRatio(titleLabel, 1.0f);
        vl.addComponent(hl);

        vl.addComponent(new Label(nvl(getMessage(tti.getStateConfiguration().getCommentary()), ""),
                Label.CONTENT_XHTML));
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
}

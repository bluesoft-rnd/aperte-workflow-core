package org.aperteworkflow.ui.processadmin;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmTask;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.*;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.getLocalizedMessage;
import static pl.net.bluesoft.util.lang.FormatUtil.formatFullDate;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessInstanceAdminManagerPane extends VerticalLayout implements VaadinUtility.HasRefreshButton {

    private Logger logger = Logger.getLogger(ProcessInstanceAdminManagerPane.class.getName());
    
    TextField searchField = new TextField(getLocalizedMessage("processinstances.search.prompt"));
    CheckBox onlyActive = new CheckBox(getLocalizedMessage("processinstances.search.onlyActive"));
    VerticalLayout searchResults = new VerticalLayout();

    int offset = 0;
    int limit = 10;
    int cnt = 0;
    String filter = null;
    Label errorLbl = new Label();
    private ProcessToolBpmSession bpmSession;

    public ProcessInstanceAdminManagerPane(Application application, ProcessToolBpmSession session) {
        this.bpmSession = session;
        setWidth("100%");
        setSpacing(true);

        addComponent(width(horizontalLayout(
                refreshIcon(application, this),
                styled(new Label(getLocalizedMessage("processinstances.console.title")), "h1")),
                null));

        addComponent(new Label(getLocalizedMessage("processinstances.console.info"), Label.CONTENT_XHTML));

        onlyActive.setValue(true);
        onlyActive.setImmediate(true);
        onlyActive.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                offset = 0;
                refreshData();
            }
        });
        searchField.setWidth("100%");
        searchField.setTextChangeTimeout(500);
        searchField.addListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
                offset = 0;
                filter = textChangeEvent.getText();
                refreshData();
            }
        });
        searchResults.setWidth("100%");

        addComponent(searchField);
        addComponent(onlyActive);
        addComponent(errorLbl);
        
        addComponent(searchResults);


    }

    private Component getNavigation() {
        HorizontalLayout hl = new HorizontalLayout();

        hl.setSpacing(true);
        Button prevButton = new Button(getLocalizedMessage("processinstances.console.tasks.previous"));
        prevButton.setStyleName(BaseTheme.BUTTON_LINK);
        prevButton.setEnabled(offset > 0);
        prevButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                offset -= limit;
                if (offset < 0) offset = 0;
                refreshData();
            }
        });
        hl.addComponent(prevButton);

        hl.addComponent(new Label((offset + 1) + "-" + Math.min(offset + limit, offset+cnt)));
        Button nextButton = new Button(getLocalizedMessage("processinstances.console.tasks.next"));
        nextButton.setStyleName(BaseTheme.BUTTON_LINK);
        nextButton.setEnabled(limit < cnt);
        nextButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                offset += limit;
//                if (offset > cnt - 1) offset = cnt - 1;
                refreshData();
            }
        });
        hl.addComponent(nextButton);

        return hl;
    }

    @Override
    public void refreshData() {
        searchResults.removeAllComponents();
        cnt = 0;
        try {
            if (filter == null || filter.trim().isEmpty()) {
                errorLbl.setVisible(true);
                errorLbl.setValue(getLocalizedMessage("processinstances.console.noresults"));
                return;
            }
            List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>(ProcessToolContext.Util.getProcessToolContextFromThread().getProcessInstanceDAO()
                    .searchProcesses(filter, offset, limit + 1, (Boolean) onlyActive.getValue(), null, null));
            cnt = processInstances.size();
            if (processInstances.size() > limit) {
                processInstances = processInstances.subList(0, limit);
            }
            if (cnt == 0) {
                errorLbl.setVisible(true);
                errorLbl.setValue(getLocalizedMessage("processinstances.console.noresults"));
            } else {
                errorLbl.setVisible(false);
                Component navi = getNavigation();
                searchResults.addComponent(navi);
                searchResults.setComponentAlignment(navi, Alignment.TOP_RIGHT);

                for (ProcessInstance pi : processInstances) {
                    searchResults.addComponent(getProcessInstancePane(pi));
                }
            }
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            errorLbl.setVisible(true);
            errorLbl.setValue(getLocalizedMessage("processinstances.console.failed") + " " + e.getClass().getName() 
                    + ": " + e.getMessage());
        }


    }

    private Component getProcessInstancePane(final ProcessInstance pi) {
        VerticalLayout vl = new VerticalLayout();
        ProcessDefinitionConfig definition = pi.getDefinition();
        vl.addComponent(styled(new Label(
                pi.getInternalId() + " " + definition.getDescription()
                        + " (def id: " + definition.getId() + ") "
                        + definition.getBpmDefinitionKey()), "h2"));

        final HorizontalLayout history = new HorizontalLayout();
        history.setWidth("100%");
        final Panel historyPanel = new Panel(getLocalizedMessage("processinstances.console.history.title"));
        historyPanel.setHeight("100px");
        historyPanel.setWidth("100%");
        historyPanel.setStyleName(Reindeer.PANEL_LIGHT);
        historyPanel.setVisible(false);

        final Label lbl = new Label(
                getLocalizedMessage("processinstances.console.history.createdby") + " " +
                        (pi.getCreator() != null ? pi.getCreator().getLogin() : "unknown") + " " +
                        getLocalizedMessage("processinstances.console.history.on") + " " +
                        formatFullDate(pi.getCreateDate()));

        Button historyButton = linkButton(getLocalizedMessage("processinstances.console.history.showhide"),
                new Runnable() {
                    @Override
                    public void run() {
                        if (historyPanel.isVisible()) {
                            historyPanel.setVisible(false);
                            history.removeComponent(historyPanel);
                            history.addComponent(lbl, 0);
                            history.setExpandRatio(lbl, 1.0f);
                        } else {
                            //fill in history Panel
                            historyPanel.setContent(getHistoryPane(pi));
                            historyPanel.setVisible(true);
                            history.removeComponent(lbl);
                            history.addComponent(historyPanel, 0);
                            history.setExpandRatio(historyPanel, 1.0f);

                        }
                    }
                });
        history.addComponent(lbl);
        history.setExpandRatio(lbl, 1.0f);
        vl.addComponent(historyButton);
        vl.setComponentAlignment(historyButton, Alignment.TOP_RIGHT);

        vl.addComponent(history);

        List<BpmTask> taskList = 
                new ArrayList<BpmTask>(bpmSession.getTaskList(pi, ProcessToolContext.Util.getProcessToolContextFromThread()));
        for (final BpmTask task : taskList) {
            vl.addComponent(
                  hl(
                          width(new Label(getLocalizedMessage("processinstances.console.entry.state") + " " +
                                  task.getTaskName() + ", " + task.getInternalTaskId()),
                                  "50%"),
                          width(new Label(getLocalizedMessage("processinstances.console.entry.owner") + " " + 
                                  (task.getOwner() != null ? task.getOwner().getLogin() : "NIL")),
                                  "50%")
                  ));
            ProcessStateConfiguration cfg = bpmSession.getProcessStateConfiguration(pi, ProcessToolContext.Util.getProcessToolContextFromThread());
            if (cfg != null && !cfg.getActions().isEmpty()) {
                vl.addComponent(new Label(getLocalizedMessage("processinstances.console.entry.available-actions")));
                HorizontalLayout hl = new HorizontalLayout();
                hl.setSpacing(true);
                for (final ProcessStateAction psa : cfg.getActions()) {
                    hl.addComponent(linkButton(getLocalizedMessage(nvl(psa.getLabel(), psa.getBpmName())),
                            confirmable(getApplication(), getLocalizedMessage("processinstances.console.force-action.confirm.title"),
                                    getLocalizedMessage(nvl(psa.getDescription(), psa.getLabel(), psa.getBpmName())),
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            bpmSession.adminCompleteTask(pi, task, psa);
                                            refreshData();
                                            Window.Notification n = new Window.Notification(getLocalizedMessage("processinstances.console.force-action.success"));
                                            n.setDelayMsec(-1);
                                            getApplication().getMainWindow().showNotification(n);
                                        }
                                    })));
                }
                vl.addComponent(hl);
            }
        }
        vl.addComponent(linkButton(getLocalizedMessage("processinstances.console.cancel-process"),
                confirmable(getApplication(),
                        getLocalizedMessage("processinstances.console.cancel-process.confirm.title"),
                        getLocalizedMessage("processinstances.console.cancel-process.confirm.message"),
                        new Runnable() {
                            @Override
                            public void run() {
                                bpmSession.adminCancelProcessInstance(pi);
                                refreshData();
                                Window.Notification n =
                                        new Window.Notification(getLocalizedMessage("processinstances.console.cancel-process.success"));
                                n.setDelayMsec(-1);
                                getApplication().getMainWindow().showNotification(n);
                            }
                        })));
      
        return vl;
    }

    public class ProcessLogInfo {
        public String userDescription;
        public String actionDescription;
        public String entryDescription;
        public String stateDescription;
        public String performDate;

        public String getUserDescription() {
            return userDescription;
        }

        public void setUserDescription(String userDescription) {
            this.userDescription = userDescription;
        }

        public String getActionDescription() {
            return actionDescription;
        }

        public void setActionDescription(String actionDescription) {
            this.actionDescription = actionDescription;
        }

        public String getEntryDescription() {
            return entryDescription;
        }

        public void setEntryDescription(String entryDescription) {
            this.entryDescription = entryDescription;
        }

        public String getStateDescription() {
            return stateDescription;
        }

        public void setStateDescription(String stateDescription) {
            this.stateDescription = stateDescription;
        }

        public String getPerformDate() {
            return performDate;
        }

        public void setPerformDate(String performDate) {
            this.performDate = performDate;
        }
    }

    private ProcessLogInfo getProcessLogInfo(ProcessInstanceLog pl) {
        ProcessLogInfo plInfo = new ProcessLogInfo();
        String userDescription = pl.getUser() != null ? nvl(pl.getUser().getRealName(), pl.getUser().getLogin()) : "";
        if (pl.getUserSubstitute() != null) {
            String substituteDescription = nvl(pl.getUserSubstitute().getRealName(), pl.getUserSubstitute().getLogin());
            plInfo.userDescription = substituteDescription + "(" +
                    getLocalizedMessage("processinstances.console.history.substituting") + " " + userDescription + ")";
        } else {
            plInfo.userDescription = userDescription;
        }
        plInfo.entryDescription = nvl(pl.getAdditionalInfo(), pl.getLogValue());
        plInfo.actionDescription = getLocalizedMessage(pl.getEventI18NKey());
        if (hasText(plInfo.getEntryDescription())) {
            plInfo.actionDescription = plInfo.actionDescription + " - " + getLocalizedMessage(plInfo.entryDescription);
        }
        plInfo.performDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pl.getEntryDate().getTime());
        plInfo.stateDescription = pl.getState() != null ? nvl(pl.getState().getDescription(), pl.getState().getName()) : "";
        return plInfo;
    }

    private ComponentContainer getHistoryPane(ProcessInstance pi) {
        //refresh
        pi = ProcessToolContext.Util.getProcessToolContextFromThread().getProcessInstanceDAO().getProcessInstance(pi.getId());

        List<ProcessInstanceLog> processLogs = new ArrayList<ProcessInstanceLog>(pi.getProcessLogs());
        Collections.sort(processLogs);
        VerticalLayout vl = new VerticalLayout();
        HorizontalLayout hl;
        for (ProcessInstanceLog pil : processLogs) {
            ProcessLogInfo pli = getProcessLogInfo(pil);

            hl = new HorizontalLayout();
            hl.setSpacing(true);
            if (hasText(pli.getUserDescription()))
                hl.addComponent(htmlLabel("<b>" + pli.getUserDescription() + "</b>", 150));
            else
                hl.addComponent(htmlLabel("<b>System</b>", 150));

            hl.addComponent(htmlLabel("<b>" + pli.getPerformDate() + "</b>", 130));
            hl.addComponent(new Label("<b>" + getLocalizedMessage("processinstances.console.history.stateDescription") +
                    "</b>", Label.CONTENT_XHTML));
            hl.addComponent(label(getLocalizedMessage(pli.getStateDescription()), 350));
            vl.addComponent(hl);
            hl = new HorizontalLayout();
            hl.setSpacing(true);
            hl.setMargin(new Layout.MarginInfo(false, false, true, true));
            Label l = new Label(pli.getActionDescription(), Label.CONTENT_XHTML);
            l.setWidth("100%");
            hl.addComponent(l);
            vl.addComponent(hl);
        }
        return vl;
    }
}

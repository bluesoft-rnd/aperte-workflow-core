package org.aperteworkflow.ui.processadmin;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang.StringEscapeUtils;
import org.aperteworkflow.bpm.graph.GraphElement;
import org.aperteworkflow.bpm.graph.StateNode;
import org.aperteworkflow.bpm.graph.TransitionArc;
import org.aperteworkflow.bpm.graph.TransitionArcPoint;
import org.aperteworkflow.portlets.ProcessInstanceManagerPortlet;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.aperteworkflow.util.vaadin.VaadinUtility.*;
import static pl.net.bluesoft.util.lang.FormatUtil.formatFullDate;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessInstanceAdminManagerPane extends VerticalLayout implements Refreshable {

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
        searchField.focus();
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
            List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>(ProcessToolContext.Util.getThreadProcessToolContext().getProcessInstanceDAO()
                    .searchProcesses(filter, offset, limit + 1,
                            (Boolean) onlyActive.getValue(), null, null));
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
                new ArrayList<BpmTask>(bpmSession.findProcessTasks(pi,
                        ProcessToolContext.Util.getThreadProcessToolContext()));
        for (final BpmTask task : taskList) {
            vl.addComponent(getTaskStateComponent(pi, task));
            ProcessStateConfiguration cfg = ProcessToolContext.Util.getThreadProcessToolContext()
                    .getProcessDefinitionDAO().getProcessStateConfiguration(task);
            if (cfg != null && !cfg.getActions().isEmpty()) {
                vl.addComponent(new Label(getLocalizedMessage("processinstances.console.entry.available-actions")));
                HorizontalLayout hl = new HorizontalLayout();
                hl.setSpacing(true);
                List<ProcessStateAction> actions = new ArrayList<ProcessStateAction>(cfg.getActions());
                Collections.sort(actions, new Comparator<ProcessStateAction>() {
                    @Override
                    public int compare(ProcessStateAction o1, ProcessStateAction o2) {
                        int res = nvl(o1.getPriority(), 0).compareTo(nvl(o2.getPriority(), 0));
                        if (res == 0) 
                            res = o1.getId().compareTo(o2.getId());
                        return res;
                    }
                });
                
                for (final ProcessStateAction psa : actions) {
                    hl.addComponent(getActionForceButton(pi, task, psa));
                }
                vl.addComponent(hl);
            }
        }
        vl.addComponent(hl(getCancelProcessButton(pi), getDisplayProcessMapButton(pi)));
      
        return vl;
    }

    private Button getDisplayProcessMapButton(final ProcessInstance pi) {
        Button button = linkButton(getLocalizedMessage("processinstances.console.show-process-map"),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        //doesn't show in window!!!
                                        //showWindowWithProcessMapEmbedded(pi);
                                        BufferedImage read;
                                        final byte[] png = bpmSession.getProcessMapImage(pi);

                                        try {
                                            read = ImageIO.read(new ByteArrayInputStream(png));
                                            String url = ProcessInstanceManagerPortlet.getProcessInstanceMapRequestUrl(getApplication(),
                                                    pi.getInternalId());
                                            Window w = new Window(getLocalizedMessage("processinstances.console.process.image-map") + ":" + pi.getInternalId());
//                                            w.setWidth(read.getWidth() + "px");
//                                            w.setHeight(read.getHeight() + "px");
                                            VerticalLayout newContent = new VerticalLayout();
                                            newContent.setMargin(false);
                                            newContent.setSpacing(false);
                                            w.setContent(newContent);
                                            w.getContent().setWidth(read.getWidth() + "px");
                                            w.getContent().setHeight(read.getHeight() + "px");
                                            w.center();
                                            Embedded e = new Embedded(null, new ExternalResource(url));
                                            e.setWidth(read.getWidth() + "px");
                                            e.setHeight(read.getHeight() + "px");
                                            e.setType(Embedded.TYPE_BROWSER);
                                            w.getContent().addComponent(e);
                                            w.setResizable(true);
                                            getApplication().getMainWindow().addWindow(w);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });
                return button;            
    }

//    private void showWindowWithProcessMapEmbedded(ProcessInstance pi) {
//        List<GraphElement> processHistory = bpmSession.getProcessHistory(pi);
//        final StringBuffer svg = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
//        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\"\n" +
//                "     xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
//        final byte[] png = bpmSession.getProcessMapImage(pi);
//        if (png != null) {
//            BufferedImage read;
//            try {
//                read = ImageIO.read(new ByteArrayInputStream(png));
//                String url = ProcessInstanceManagerPortlet.getProcessInstanceMapRequestUrl(getApplication(),
//                        pi.getInternalId());
//                url = StringEscapeUtils.escapeXml(url);
////                                                svg.append(String.format("<image x=\"0\" y=\"0\" width=\"%d\" height=\"%d\"\n" +
////                                                        "xlink:href=\"%s\" />",
////                                                        read.getWidth(),
////                                                        read.getHeight(),
////                                                        url));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            String strokeStyle = "stroke:#C14F45;stroke-width:3;stroke-opacity:0.25;";
//
//            for (GraphElement el : processHistory) {
//                if (el instanceof StateNode) {
//                    StateNode sn = (StateNode) el;
//                    svg.append(String.format("<rect x=\"%d\" y=\"%d\" height=\"%d\" width=\"%d\"\n" +
//                                        " rx=\"5\" ry=\"5\"\n" +
//                                        " style=\"" + strokeStyle + "fill-opacity:0.0\"/>\n",
//                                        sn.getX(),
//                                        sn.getY(),
//                                        sn.getHeight(),
//                                        sn.getWidth()));
//                } else if (el instanceof TransitionArc) {
//                    TransitionArc ta = (TransitionArc) el;
//                    TransitionArcPoint prevPoint = null;
//                    for (TransitionArcPoint p : ta.getPath()) {
//                        if (prevPoint != null) {
//                            svg.append(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\"\n" +
//                                    "  style=\"" + strokeStyle + "\"/>\n",
//                                    prevPoint.getX(),
//                                    prevPoint.getY(),
//                                    p.getX(),
//                                    p.getY()
//                                    ));
//                        }
//                        prevPoint = p;
//                    }
//                }
//            }
//            svg.append("</svg>");
//
//            Window w = new Window(getLocalizedMessage("process.image-map"));
//            w.setWidth(read.getWidth() + "px");
//            w.setHeight(read.getHeight() + "px");
//            w.center();
//            Embedded e = new Embedded();
//            e.setSizeFull();
//            e.setType(Embedded.TYPE_OBJECT);
//            e.setMimeType("image/svg+xml");
//            e.setSource(new StreamResource(new StreamResource.StreamSource() {
//                @Override
//                public InputStream getStream() {
//                    return new ByteArrayInputStream(svg.toString().getBytes());
//                }
//            },  "map.svg", getApplication()));
//            w.getContent().addComponent(e);
//            w.setResizable(false);
//            getApplication().getMainWindow().addWindow(w);
//        } else {
//            getApplication().getMainWindow().showNotification(getLocalizedMessage("process.no-image.error"));
//        }
//    }

    private Button getCancelProcessButton(final ProcessInstance pi) {
        Button button = linkButton(getLocalizedMessage("processinstances.console.cancel-process"),
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
                                n.setDelayMsec(5000);
                                getApplication().getMainWindow().showNotification(n);
                            }
                        }));
        button.setEnabled(pi.getRunning());
        return button;
    }

    private Button getActionForceButton(final ProcessInstance pi, final BpmTask task, final ProcessStateAction psa) {
        return linkButton(getLocalizedMessage(nvl(psa.getLabel(), psa.getBpmName())),
                confirmable(getApplication(), getLocalizedMessage("processinstances.console.force-action.confirm.title"),
                        getLocalizedMessage(nvl(psa.getDescription(), psa.getLabel(), psa.getBpmName())),
                        new Runnable() {
                            @Override
                            public void run() {
                                bpmSession.adminCompleteTask(pi, task, psa);
                                refreshData();
                                Window.Notification n = new Window.Notification(getLocalizedMessage("processinstances.console.force-action.success"));
                                n.setDelayMsec(5000);
                                getApplication().getMainWindow().showNotification(n);
                            }
                        }));
    }

    private HorizontalLayout getTaskStateComponent(final ProcessInstance pi, final BpmTask task) {
        HorizontalLayout hl = hl(new Label(getLocalizedMessage("processinstances.console.entry.state") + " " +
                task.getTaskName() + ", " + task.getInternalTaskId()));

        if (task.getOwner() != null)
            hl.addComponent(new Label(getLocalizedMessage("processinstances.console.entry.owner") + " " +
                    (task.getOwner() != null ? task.getOwner().getLogin() : "NIL")));
        else
            hl.addComponent(new Label(getLocalizedMessage("processinstances.console.entry.no-owner")));

        hl.setSizeUndefined();
        hl.addComponent(linkButton(getLocalizedMessage("processinstances.console.entry.change-owner"), new Runnable() {
            @Override
            public void run() {
                Window w = new Window(getLocalizedMessage("processinstances.console.entry.change-owner.title"));
                w.setModal(true);
                w.setWidth("400px");
                w.center();
                w.setContent(new UserSearchComponent(task, pi, w));
                getApplication().getMainWindow().addWindow(w);

            }
        }));
        if (task.getOwner() != null) {
            hl.addComponent(linkButton(getLocalizedMessage("processinstances.console.entry.remove-owner"),
                    confirmable(getApplication(),
                            getLocalizedMessage("processinstances.console.remove-owner.confirm.title"),
                            getLocalizedMessage("processinstances.console.remove-owner.confirm.message"),
                    new Runnable() {
                @Override
                public void run() {
                    bpmSession.adminReassignProcessTask(pi, task, null);
                    refreshData();
                    Window.Notification n =
                            new Window.Notification(getLocalizedMessage("processinstances.console.remove-owner.success"));
                    n.setDelayMsec(5000);
                    getApplication().getMainWindow().showNotification(n);
                }
            })));
        }
        return hl;
    }

    private class UserSearchComponent extends VerticalLayout {


        private TextField smallSearchField = new TextField();
        private CssLayout results = new CssLayout();
        private String filter;

        public UserSearchComponent(final BpmTask task, final ProcessInstance pi, final Window w) {

            setWidth("100%");
            setSpacing(true);
            addComponent(styled(new Label(getLocalizedMessage("processinstances.console.entry.change-owner.title")), "h2"));
            addComponent(styled(new Label(getLocalizedMessage("processinstances.console.entry.change-owner.info")), "h2"));

            addComponent(smallSearchField);
            addComponent(results);
            smallSearchField.setImmediate(true);
            smallSearchField.setWidth("100%");
            smallSearchField.setTextChangeTimeout(500);
            smallSearchField.focus();
            smallSearchField.addListener(new FieldEvents.TextChangeListener() {
                @Override
                public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
                    filter = textChangeEvent.getText();
                    results.removeAllComponents();
                    List<String> availableLogins = bpmSession.getAvailableLogins(filter.trim());
                    for (final String login : availableLogins) {
                        results.addComponent(linkButton(login,
                                confirmable(getApplication(),
                                        getLocalizedMessage("processinstances.console.change-owner.confirm.title"),
                                        getLocalizedMessage("processinstances.console.change-owner.confirm.message"),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                bpmSession.adminReassignProcessTask(pi, task, login);
                                                Window mainWindow = getApplication().getMainWindow();
                                                mainWindow.removeWindow(w);
                                                refreshData();
                                                Window.Notification n =
                                                        new Window.Notification(getLocalizedMessage("processinstances.console.change-owner.success"));
                                                n.setDelayMsec(5000);
                                                mainWindow.showNotification(n);
                                            }
                                        })));
                    }
                }
            });

        }
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
        pi = ProcessToolContext.Util.getThreadProcessToolContext().getProcessInstanceDAO().getProcessInstance(pi.getId());

        List<ProcessInstanceLog> processLogs = new ArrayList<ProcessInstanceLog>(pi.getProcessLogs());
        Collections.sort(processLogs, ProcessInstanceLog.DEFAULT_COMPARATOR);
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

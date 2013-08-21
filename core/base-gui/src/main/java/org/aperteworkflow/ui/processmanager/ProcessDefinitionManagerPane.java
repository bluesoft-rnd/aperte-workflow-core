package org.aperteworkflow.ui.processmanager;

import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.aperteworkflow.util.vaadin.VaadinUtility.*;
import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.FormatUtil.formatFullDate;
import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDefinitionManagerPane extends VerticalLayout {
    private VerticalLayout definitionList = new VerticalLayout();

    public ProcessDefinitionManagerPane(Application application) {
        setWidth("100%");
        setSpacing(true);

        definitionList.setSpacing(true);

        addComponent(width(horizontalLayout(
                refreshIcon(application, new Refreshable() {
                    @Override
                    public void refreshData() {
                        displayDefinitionList();
                    }
                }),
                styled(new Label(getLocalizedMessage("processdefinitions.console.title")), "h2")),
                null));

        addComponent(new Label(getLocalizedMessage("processdefinitions.console.info"), Label.CONTENT_XHTML));

        addComponent(definitionList);
        displayDefinitionList();
    }

    private void displayDefinitionList() {
        definitionList.removeAllComponents();
		ProcessDefinitionDAO dao = getThreadProcessToolContext().getProcessDefinitionDAO();
        List<ProcessDefinitionConfig> latestConfigurations = new ArrayList<ProcessDefinitionConfig>(dao.getActiveConfigurations());
        Collections.sort(latestConfigurations, ProcessDefinitionConfig.DEFAULT_COMPARATOR);

        for (final ProcessDefinitionConfig cfg : latestConfigurations) {
            HorizontalLayout buttonLayout = new HorizontalLayout();
            buttonLayout.setSpacing(true);

            buttonLayout.addComponent(linkButton(getLocalizedMessage(!cfg.isEnabled() ? "processdefinitions.console.enable" : "processdefinitions.console.disable"),
                    new Runnable() {
                        @Override
                        public void run() {
							ProcessDefinitionDAO dao = getThreadProcessToolContext().getProcessDefinitionDAO();
                            dao.setConfigurationEnabled(cfg, !cfg.isEnabled());
                            String msg = getLocalizedMessage(!cfg.isEnabled() ? "processdefinitions.console.enable.success" : "processdefinitions.console.disable.success");
                            Window.Notification n = new Window.Notification(msg);
                            n.setDelayMsec(-1);
                            getApplication().getMainWindow().showNotification(n);
                            displayDefinitionList();
                        }
                    }));

            Panel historyPanel = new Panel(getLocalizedMessage("processdefinitions.console.history.title"));
            historyPanel.setHeight("100px");
            historyPanel.setWidth("100%");
            historyPanel.setStyleName(Reindeer.PANEL_LIGHT);
            historyPanel.setVisible(false);

            VerticalLayout entry = verticalLayout(styled(new Label(
					String.format("%s: %s (%s)",
							cfg.getId(),
							getLocalizedMessage(cfg.getDescription()),
							cfg.getBpmDefinitionKey())),
					"h2"));
            entry.addComponent(new Label(getLocalizedMessage(cfg.getComment()), Label.CONTENT_XHTML));

            HorizontalLayout history = new HorizontalLayout();
            history.setWidth("100%");
            history.setSpacing(true);

            Label lbl = getVersionLabel(cfg);
            history.addComponent(lbl);
            Button b = getToggleHistoryButton(cfg, history, lbl, historyPanel);
            history.addComponent(b);
            history.setComponentAlignment(b, Alignment.TOP_RIGHT);
            history.setExpandRatio(lbl, 1.0f);

            entry.addComponent(history);
            entry.addComponent(buttonLayout);

            definitionList.addComponent(entry);
        }
    }

    private Button getToggleHistoryButton(final ProcessDefinitionConfig cfg, final HorizontalLayout history,
                                          final Label lbl,
                                          final Panel historyPanel) {
        return linkButton(getLocalizedMessage("processdefinitions.console.history.showhide"),
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
                            historyPanel.setContent(getHistoryPanel(cfg));
                            historyPanel.setVisible(true);
                            history.removeComponent(lbl);
                            history.addComponent(historyPanel, 0);
                            history.setExpandRatio(historyPanel, 1.0f);
                        }
                    }
                });
    }

    private Label getVersionLabel(ProcessDefinitionConfig cfg) {
        return new Label(
                getLocalizedMessage("processdefinitions.console.version") + ' '
                        + cfg.getBpmDefinitionVersion() + ' ' +
                        getLocalizedMessage("processdefinitions.console.uploadedby") + ' ' +
						nvl(cfg.getCreatorLogin(), "unknown") + ' '
                        + getLocalizedMessage("processdefinitions.console.uploadedon") + ' ' +
						formatFullDate(cfg.getCreateDate()));
    }

    private VerticalLayout getHistoryPanel(ProcessDefinitionConfig cfg) {
		ProcessDefinitionDAO dao = getThreadProcessToolContext().getProcessDefinitionDAO();
        List<ProcessDefinitionConfig> configurationVersions = new ArrayList<ProcessDefinitionConfig>(dao.getConfigurationVersions(cfg));
        Collections.sort(configurationVersions, ProcessDefinitionConfig.DEFAULT_COMPARATOR);

        VerticalLayout vl = new VerticalLayout();
        vl.setWidth("100%");
        vl.setMargin(true);
        vl.setSpacing(true);
        for (ProcessDefinitionConfig version : configurationVersions) {
            vl.addComponent(getVersionLabel(version));
        }
        return vl;
    }
}

package pl.net.bluesoft.rnd.processtool.ui.newprocess;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.ui.activity.ActivityMainPane;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataPane;
import pl.net.bluesoft.rnd.processtool.ui.process.WindowProcessDataDisplayContext;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.List;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class NewProcessPane extends VerticalLayout {
    private ProcessToolBpmSession session;
    private I18NSource i18NSource;
    private ActivityMainPane activityMainPane;

    public NewProcessPane(final ProcessToolBpmSession session, final I18NSource i18NSource, final ActivityMainPane activityMainPane) {
        this.activityMainPane = activityMainPane;
        this.session = session;
        this.i18NSource = i18NSource;

        BeanItemContainer<ProcessDefinitionConfig> bic = new BeanItemContainer<ProcessDefinitionConfig>(ProcessDefinitionConfig.class);
        final ComboBox l = new ComboBox(getMessage("tasks.processType"), bic);
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        for (ProcessDefinitionConfig cfg : session.getAvailableConfigurations(ctx)) {
            bic.addItem(cfg);
            l.setItemCaption(cfg, getMessage(cfg.getDescription()));
        }
        if (bic.size() != 0) {
            l.setValue(bic.firstItemId());
        }
        l.setNullSelectionAllowed(false);
        l.setNewItemsAllowed(false);
        l.setFilteringMode(ComboBox.FILTERINGMODE_OFF);

        l.setCaption(null);//getMessage("newProcess.caption"));
        Button b = new Button(getMessage("newProcess.start"));
        b.addStyleName("small default");
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (l.getValue() == null) {
                    return;
                }
                withErrorHandling(getApplication(), new Runnable() {
                    public void run() {
                        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                        ProcessDefinitionConfig cfg = (ProcessDefinitionConfig) l.getValue();
                        cfg = ctx.getProcessDefinitionDAO().getActiveConfigurationByKey(cfg.getBpmDefinitionKey());
                        ProcessInstance instance = session.createProcessInstance(cfg, null, ctx, null, null, "portlet", null);
                        getWindow().showNotification(getMessage("newProcess.started"), 2000);
                        getWindow().executeJavaScript("Liferay.trigger('processtool.bpm.newProcess', '" + instance.getInternalId() + "');");
                        getWindow().executeJavaScript("vaadin.forceSync();");

                        List<BpmTask> tasks = session.findUserTasks(instance, ctx);
                        if (!tasks.isEmpty()) {
                            BpmTask task = tasks.get(0);
                            if (activityMainPane != null) {
                                activityMainPane.displayProcessData(task, session);
                            }
                            else {
                                Window w = new Window(instance.getInternalId());
                                w.setContent(new ProcessDataPane(getApplication(), session, i18NSource, task, new WindowProcessDataDisplayContext(w)));
                                w.center();
                                getWindow().addWindow(w);
                                w.focus();
                            }
                        }
                        else if (activityMainPane != null) {
                            activityMainPane.reloadCurrentViewData();
                        }
                    }
                });

            }
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.addComponent(l);
        hl.addComponent(b);
        hl.setSpacing(true);
        setSpacing(true);

        addComponent(new Label(getMessage("newProcess.caption")));
        addComponent(hl);
        setMargin(new MarginInfo(true, false, false, false));
    }

    private String getMessage(String s) {
        return i18NSource.getMessage(s);
    }
}

package pl.net.bluesoft.rnd.processtool.ui.newprocess;

import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.ui.activity.ActivityMainPane;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataPane;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.VaadinUtility.HasRefreshButton;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.embedded;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class NewProcessExtendedPane extends VerticalLayout implements HasRefreshButton {
    private ProcessToolBpmSession session;
    private ActivityMainPane activityMainPane;
    private I18NSource i18NSource;

    private VerticalLayout processesLayout;
    private Label title;

    public NewProcessExtendedPane(final ProcessToolBpmSession session,
                                  final I18NSource i18NSource,
                                  final ActivityMainPane activityMainPane) {
        this.activityMainPane = activityMainPane;
        this.session = session;
        this.i18NSource = i18NSource;

        title = new Label(getMessage("newProcess.caption-simple"), Label.CONTENT_XHTML);
        title.addStyleName("small");

        addComponent(horizontalLayout(title, refreshIcon(activityMainPane.getApplication(), this)));

        processesLayout = new VerticalLayout();
        processesLayout.setWidth("100%");
        addComponent(processesLayout);

        setSpacing(true);
        setMargin(new MarginInfo(true, false, false, false));

        refreshData();
    }

    @Override
    public void refreshData() {
        processesLayout.removeAllComponents();
        title.setValue(getMessage("newProcess.caption-simple"));
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        for (final ProcessDefinitionConfig cfg2 : session.getAvailableConfigurations(ctx)) {
            final Button b = new Button(getMessage("newProcess.start-simple"));
            b.addStyleName("small default");
            b.setStyleName(BaseTheme.BUTTON_LINK);
            b.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    withErrorHandling(getApplication(), new Runnable() {
                        public void run() {
                            ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                            //find latest definition
                            ProcessDefinitionConfig cfg = ctx.getProcessDefinitionDAO()
                                    .getActiveConfigurationByKey(cfg2.getBpmDefinitionKey());
                            ProcessInstance instance = session.createProcessInstance(cfg, null, ctx, null, null, "gui");
                            getWindow().showNotification(getMessage("newProcess.started"), 2000);
                            getWindow().executeJavaScript(
                                    "Liferay.trigger('processtool.bpm.newProcess', '" + instance.getInternalId() + "');");
                            getWindow().executeJavaScript("vaadin.forceSync();");//other portlets may need this
                            if (session.isProcessOwnedByUser(instance, ctx)) {
                                if (activityMainPane != null) {
                                    activityMainPane.displayProcessData(instance);
                                }
                                else {
                                    Window w = new Window(instance.getInternalId());
                                    w.setContent(new ProcessDataPane(getApplication(), session, i18NSource, instance,
                                            new ProcessDataPane.WindowDisplayProcessContextImpl(w)));
                                    w.center();
                                    getWindow().addWindow(w);
                                    w.focus();
                                }
                            }
                        }
                    });
                }
            });

            Label titleLabel = new Label(getMessage(cfg2.getDescription()));
            titleLabel.addStyleName("h3 color");

            Embedded logo = cfg2.getProcessLogo() == null ? embedded(activityMainPane.getApplication(), "/img/aperte-logo.png")
                    : new Embedded(null, new StreamResource(new StreamSource() {
                @Override
                public InputStream getStream() {
                    return new ByteArrayInputStream(cfg2.getProcessLogo());
                }
            }, cfg2.getBpmDefinitionKey() + "_logo.png", activityMainPane.getApplication()));
            processesLayout.addComponent(horizontalLayout(Alignment.MIDDLE_LEFT, logo, titleLabel));

            HorizontalLayout hl = new HorizontalLayout();
            hl.addComponent(new Label(nvl(getMessage(cfg2.getComment()), ""), Label.CONTENT_XHTML) {{
                setWidth("100%");
            }});
            hl.addComponent(b);
            hl.setExpandRatio(hl.getComponent(0), 1.0f);
            hl.setSpacing(true);
            hl.setWidth("100%");
            hl.setComponentAlignment(b, Alignment.BOTTOM_RIGHT);
            processesLayout.addComponent(hl);
        }
    }

    private String getMessage(String s) {
        return i18NSource.getMessage(s);
    }
}

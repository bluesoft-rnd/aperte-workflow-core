package pl.net.bluesoft.rnd.processtool.ui.process;

import com.vaadin.Application;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.*;
import org.apache.commons.beanutils.BeanUtils;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.vaadin.ui.Label.CONTENT_XHTML;
import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDataPane extends VerticalLayout {
    private static final Logger logger = Logger.getLogger(ProcessDataPane.class.getName());

    private ProcessToolBpmSession bpmSession;
    private I18NSource i18NSource;
    private ProcessInstance process;

    private Set<ProcessToolDataWidget> dataWidgets = new HashSet<ProcessToolDataWidget>();
    private boolean failed = false;
    private boolean isOwner;

    private Application application;
    private DisplayProcessContext displayProcessContext;

    public ProcessDataPane(final Application application,
                           final ProcessToolBpmSession bpmSession,
                           I18NSource i18NSource,
                           final ProcessInstance process,
                           DisplayProcessContext hideProcessHandler) {
        this.application = application;
        this.bpmSession = bpmSession;
        this.i18NSource = i18NSource;
        this.displayProcessContext = hideProcessHandler;
        setSpacing(true);
        setMargin(new MarginInfo(false, false, true, true));
        this.process = bpmSession.getProcessData(process.getInternalId(), ProcessToolContext.Util.getThreadProcessToolContext());
        initLayout(ProcessToolContext.Util.getThreadProcessToolContext(), false);
    }

    private boolean initLayout(ProcessToolContext ctx, boolean autohide) {
        failed = false;

        removeAllComponents();
        if (autohide && !bpmSession.isProcessRunning(process.getInternalId(), ctx)) {
            application.getMainWindow().showNotification(getMessage("process.data.process-ended"));
            displayProcessContext.hide();
            return true;
        }

        dataWidgets.clear();
        isOwner = bpmSession.isProcessOwnedByUser(process, ctx);

        if (autohide && !isOwner) {
            displayProcessContext.hide();
            return false;
        }
        setWidth("100%");

        ProcessStateConfiguration stateConfiguration = bpmSession.getProcessStateConfiguration(process, ctx);
        if (stateConfiguration == null) {
            application.getMainWindow().showNotification(getMessage("process.data.no-config"));
            displayProcessContext.hide();
            return true;

        }
        Label l = new Label(getMessage(stateConfiguration.getDescription()));
        l.addStyleName("h1 color processtool-title");

        addComponent(l);
        if (StringUtil.hasText(stateConfiguration.getCommentary())) {
            addComponent(new Label(getMessage(stateConfiguration.getCommentary()), Label.CONTENT_XHTML));
        }

        setComponentAlignment(l, Alignment.TOP_RIGHT);

        displayProcessContext.setCaption(process.getInternalId());
        HorizontalLayout buttonLayout = getButtonsPanel(stateConfiguration);
        addComponent(buttonLayout);
        setComponentAlignment(buttonLayout, Alignment.BOTTOM_LEFT);

        final VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);

        // sort the widgets to preserve the displaying order
        List<ProcessStateWidget> widgets = new ArrayList<ProcessStateWidget>(stateConfiguration.getWidgets());
        Collections.sort(widgets, new WidgetPriorityComparator());

        for (ProcessStateWidget w : widgets) {
            try {
                ProcessToolWidget realWidget = getWidget(w, stateConfiguration, ctx);
                if (realWidget instanceof ProcessToolVaadinWidget && (!nvl(w.getOptional(), false) || realWidget.hasVisibleData())) {
                    processWidgetChildren(w, realWidget, stateConfiguration, ctx);
                    ProcessToolVaadinWidget vaadinW = (ProcessToolVaadinWidget) realWidget;
                    vl.addComponent(vaadinW.render());
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                failed = true;
                vl.addComponent(new Label(getMessage("process.data.widget.exception-occurred")));
                vl.addComponent(new Label(e.getMessage()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                e.printStackTrace(new PrintWriter(baos));
                vl.addComponent(new Label("<pre>" + baos.toString() + "</pre>", CONTENT_XHTML));
            }

        }
        addComponent(vl);
        setExpandRatio(vl, 1.0f);

        buttonLayout = getButtonsPanel(stateConfiguration);
        addComponent(buttonLayout);
        setComponentAlignment(buttonLayout, Alignment.TOP_LEFT);

        return isOwner;
    }

    private HorizontalLayout getButtonsPanel(ProcessStateConfiguration stateConfiguration) {
        // sort the actions to preserve the displaying order
        List<ProcessStateAction> actionList = new ArrayList<ProcessStateAction>(stateConfiguration.getActions());
        Collections.sort(actionList, new ActionPriorityComparator());

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        for (final ProcessStateAction a : actionList) {

            final ProcessToolActionButton actionButton = makeButton(a);
            Button button = new Button(getMessage(actionButton.getLabel(process)));
            button.addStyleName("default");
            button.setDescription(getMessage(actionButton.getDescription(process)));

            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    final ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                    ProcessInstance pd = bpmSession.getProcessData(process.getInternalId(), ctx);
                    Map<ProcessToolDataWidget, Collection<String>> validationErrors = getValidationErrors(pd);
                    actionButton.onButtonPress(pd, ctx, dataWidgets, validationErrors, new ProcessToolActionCallback() {
                        @Override
                        public boolean saveProcessData() {
                            withErrorHandling(application, new Runnable() {
                                public void run() {
                                    final ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                                    ProcessInstance pi = bpmSession.getProcessData(process.getInternalId(), ctx);
                                    for (ProcessToolDataWidget w : dataWidgets) {
                                        w.saveData(pi);
                                    }
                                    actionButton.saveData(pi);
                                    bpmSession.saveProcessInstance(pi, ctx);
                                }
                            });
                            return true;
                        }

                        @Override
                        public void performAction(final ProcessStateAction a) {
                            withErrorHandling(application, new Runnable() {
                                public void run() {
                                    final ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                                    ProcessDataPane.this.performAction(ctx, a);
                                    initLayout(ctx, actionButton.isAutoHide());
                                }
                            });
                        }

                    });
                }
            });
            button.setEnabled(isOwner);
            buttonLayout.addComponent(button);
        }

        HorizontalLayout masterLayout = new HorizontalLayout();
        masterLayout.setWidth("100%");
        masterLayout.setMargin(new MarginInfo(false, true, false, true));

        masterLayout.addComponent(buttonLayout);
        masterLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
        return masterLayout;
    }

    private boolean validateAndSaveData(ProcessToolContext ctx) {

        ProcessInstance pd = bpmSession.getProcessData(process.getInternalId(), ctx);
        return validateAndSaveData(ctx, pd);

    }

    private void displayValidationErrors(Map<ProcessToolDataWidget, Collection<String>> errorMap) {
        String errorMessage = VaadinUtility.widgetsErrorMessage(i18NSource, errorMap);
        application.getMainWindow().showNotification(VaadinUtility.validationNotification(i18NSource.getMessage("process.data.data-error"),
                errorMessage));
    }

    private boolean validateAndSaveData(ProcessToolContext ctx, ProcessInstance pd) {
        Map<ProcessToolDataWidget, Collection<String>> errorMap = getValidationErrors(pd);
        if (errorMap.isEmpty()) {
            saveProcessData(ctx, pd);
            return true;
        } else {
            displayValidationErrors(errorMap);
            return false;
        }
    }

    private ProcessToolActionButton makeButton(ProcessStateAction a) {
        try {
            ProcessToolRegistry registry = ProcessToolContext.Util.getThreadProcessToolContext().getRegistry();
            ProcessToolActionButton actionButton = registry.makeButton(a.getButtonName());
            actionButton.setLoggedUser(bpmSession.getUser(ProcessToolContext.Util.getThreadProcessToolContext()));
            processAutowiredProperties(actionButton, a);
            if (actionButton instanceof ProcessToolVaadinActionButton) {
                ProcessToolVaadinActionButton vButton = (ProcessToolVaadinActionButton) actionButton;
                vButton.setApplication(application);
                vButton.setI18NSource(i18NSource);
            }
            actionButton.setDefinition(a);
            return actionButton;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void performAction(ProcessToolContext ctx, ProcessStateAction a) {
        bpmSession.performAction(a, process, ctx);
        process = bpmSession.getProcessData(process.getInternalId(), ctx);
        if (!initLayout(ctx, a.getAutohide())) {
            application.getMainWindow().showNotification(getMessage("process.action.performed"));
        }
    }

    public void saveProcessData(ProcessToolContext ctx, ProcessInstance pd) {
        for (ProcessToolDataWidget w : dataWidgets) {
            w.saveData(pd);
        }
        bpmSession.saveProcessInstance(pd, ctx);
    }

    private Map<ProcessToolDataWidget, Collection<String>> getValidationErrors(ProcessInstance pd) {
        Map<ProcessToolDataWidget, Collection<String>> errorMap
                = new HashMap<ProcessToolDataWidget, Collection<String>>();
        for (ProcessToolDataWidget w : dataWidgets) {
            Collection<String> errors = w.validateData(pd);
            if (errors != null && !errors.isEmpty()) {
                errorMap.put(w, errors);
            }
        }
        return errorMap;
    }

    private String getMessage(String key) {
        return i18NSource.getMessage(key);
    }

    private void processWidgetChildren(ProcessStateWidget parentWidgetConfiguration,
                                       ProcessToolWidget parentWidgetInstance,
                                       ProcessStateConfiguration stateConfiguration,
                                       ProcessToolContext ctx) {
        // sort the widgets to preserve the displaying order
        List<ProcessStateWidget> widgets = new ArrayList<ProcessStateWidget>(parentWidgetConfiguration.getChildren());
        Collections.sort(widgets, new WidgetPriorityComparator());

        for (ProcessStateWidget widget : widgets) {
            widget.setParent(parentWidgetConfiguration);
            ProcessToolWidget widgetInstance = getWidget(widget, stateConfiguration, ctx);
            if (!nvl(widget.getOptional(), false) || widgetInstance.hasVisibleData()) {
                processWidgetChildren(widget, widgetInstance, stateConfiguration, ctx);
                parentWidgetInstance.addChild(widgetInstance);
            }
        }
    }

    private ProcessToolWidget getWidget(ProcessStateWidget w,
                                        ProcessStateConfiguration stateConfiguration,
                                        ProcessToolContext ctx) {
        try {
            ProcessToolWidget processToolWidget;
            ProcessToolRegistry toolRegistry = VaadinUtility.getProcessToolContext(application.getContext()).getRegistry();
            if (w.getClassName() == null) {
                processToolWidget = toolRegistry.makeWidget(w.getName());
            } else {
                processToolWidget = toolRegistry.makeWidget(w.getClassName());
            }
            processToolWidget.setContext(stateConfiguration, w, i18NSource, bpmSession, application,
                    bpmSession.getPermissionsForWidget(w, ctx),
                    isOwner);
            processAutowiredProperties(processToolWidget, w);
            if (processToolWidget instanceof ProcessToolDataWidget) {
                ((ProcessToolDataWidget) processToolWidget).loadData(process);
                dataWidgets.add((ProcessToolDataWidget) processToolWidget);
            }
            return processToolWidget;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            failed = true;
            FailedProcessToolVaadinWidget failedProcessToolVaadinWidget = new FailedProcessToolVaadinWidget(e);
            failedProcessToolVaadinWidget.setContext(stateConfiguration, w, i18NSource, bpmSession, application,
                    bpmSession.getPermissionsForWidget(w, ctx),
                    isOwner);
            dataWidgets.add(failedProcessToolVaadinWidget);
            return failedProcessToolVaadinWidget;
        }
    }

    private void processAutowiredProperties(ProcessToolWidget processToolWidget, ProcessStateWidget w) {
        Map<String, String> m = new HashMap<String, String>();
        for (ProcessStateWidgetAttribute attr : w.getAttributes()) {
            m.put(attr.getName(), attr.getValue());
        }
        processAutowiredProperties(processToolWidget, m);
    }

    private void processAutowiredProperties(ProcessToolActionButton processToolActionButton, ProcessStateAction a) {
        Map<String, String> m = new HashMap<String, String>();
        m.put("buttonName", a.getButtonName());
        m.put("autoHide", String.valueOf(a.getAutohide()));
        m.put("description", a.getDescription());
        m.put("label", a.getLabel());
        m.put("bpmAction", a.getBpmName());
        m.put("skipSaving", String.valueOf(a.getSkipSaving()));
        m.put("priority", String.valueOf(a.getPriority()));

        for (ProcessStateActionAttribute attr : a.getAttributes()) {
            m.put(attr.getName(), attr.getValue());
        }

        processAutowiredProperties(processToolActionButton, m);
    }

    private void processAutowiredProperties(Object object, Map<String, String> m) {
        Class cls = object.getClass();
        processAutowiredProperties(object, m, cls);
    }

    private void processAutowiredProperties(Object object, Map<String, String> m, Class cls) {
        for (Field f : cls.getDeclaredFields()) {
            String autoName = null;
            for (Annotation a : f.getAnnotations()) {
                if (a instanceof AutoWiredProperty) {
                    AutoWiredProperty awp = (AutoWiredProperty) a;
                    if (AutoWiredProperty.DEFAULT.equals(awp.name())) {
                        autoName = f.getName();
                    } else {
                        autoName = awp.name();
                    }
                }
            }
            String value = nvl(
                    m.get(autoName),
                    ProcessToolContext.Util.getThreadProcessToolContext().getSetting("autowire." + autoName)
            );
            if (autoName != null && value != null) {
                try {
                    logger.fine("Setting attribute " + autoName + " to " + value);
                    BeanUtils.setProperty(object, autoName, value);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        if (cls.equals(Object.class)) {
            return;
        }
        processAutowiredProperties(object, m, cls.getSuperclass());
    }

    public static interface DisplayProcessContext {
        void hide();
        void setCaption(String newCaption);
    }

    public static class WindowDisplayProcessContextImpl implements DisplayProcessContext {

        private Window window;

        public WindowDisplayProcessContextImpl(Window window) {
            this.window = window;

            window.addAction(new ShortcutListener("Close window", ShortcutAction.KeyCode.ESCAPE, null) {
                @Override
                public void handleAction(Object sender, Object target) {
                    Window w = WindowDisplayProcessContextImpl.this.window;
                    w.getParent().removeWindow(w);
                }
            });
        }

        @Override
        public void hide() {
            window.getParent().removeWindow(window);
        }

        @Override
        public void setCaption(String newCaption) {
            window.setCaption(newCaption);
        }
    }

    /**
     * Comparator for {@link ProcessStateWidget} objects that takes intro account widget priority
     */
    private class WidgetPriorityComparator implements Comparator<ProcessStateWidget> {
        @Override
        public int compare(ProcessStateWidget w1, ProcessStateWidget w2) {
            if (w1 == null || w2 == null) {
                throw new NullPointerException("Can not compare null ProcessStateWidgets");
            }

            if (w1 == w2) {
                return 0;
            }

            if (w1.getPriority() != null && w2.getPriority() != null) {
                return w1.getPriority().compareTo(w2.getPriority());
            } else if (w1.getPriority() != null && w2.getPriority() == null) {
                return 1;
            } else if (w1.getPriority() == null && w2.getPriority() != null) {
                return -1;
            } else {
                return w1.getId().compareTo(w2.getId());
            }
        }
    }

    /**
     * Comparator for {@link ProcessStateAction} object that takes into account action priority
     */
    private class ActionPriorityComparator implements Comparator<ProcessStateAction> {
        @Override
        public int compare(ProcessStateAction a1, ProcessStateAction a2) {
            if (a1 == null || a2 == null) {
                throw new NullPointerException("Can not compare null ProcessStateActions");
            }

            if (a1 == a2) {
                return 0;
            }

            if (a1.getPriority() != null && a1.getPriority() != null) {
                return a1.getPriority().compareTo(a2.getPriority());
            } else if (a1.getPriority() != null && a2.getPriority() == null) {
                return 1;
            } else if (a1.getPriority() == null && a2.getPriority() != null) {
                return -1;
            } else {
                return a1.getId().compareTo(a2.getId());
            }
        }
    }

    /**
     * Widget used to display widgets that failed to be created
     */
    private class FailedProcessToolVaadinWidget extends BaseProcessToolWidget
            implements ProcessToolVaadinWidget, ProcessToolDataWidget {

        private final Exception e;

        public FailedProcessToolVaadinWidget(Exception e) {
            this.e = e;
        }

        @Override
        public String getAttributeValue(String key) {
            return super.getAttributeValue(key);
        }

        @Override
        public Component render() {
            Panel p = new Panel();
            VerticalLayout vl = new VerticalLayout();
            vl.addComponent(new Label(getMessage("process.data.widget.exception-occurred")));
            vl.addComponent(new Label(e.getMessage()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintWriter(baos));
            vl.addComponent(new Label("<pre>" + baos.toString() + "</pre>", CONTENT_XHTML));
            vl.addStyleName("error");
            p.addComponent(vl);
            p.setHeight("150px");
            return p;
        }

        @Override
        public Collection<String> validateData(ProcessInstance processInstance) {
            return Arrays.asList("process.data.widget.exception-occurred");
        }

        @Override
        public void saveData(ProcessInstance processInstance) {
            // do nothing
        }

        @Override
        public void loadData(ProcessInstance processInstance) {
            // do nothing
        }

        @Override
        public void addChild(ProcessToolWidget child) {
            // do nothing
        }

    }
}

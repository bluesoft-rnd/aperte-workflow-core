package pl.net.bluesoft.rnd.util.vaadin;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.*;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.ui.LocalizedPagedTable;

import java.util.Collection;
import java.util.Map;

import static com.vaadin.ui.Window.Notification.POSITION_CENTERED;
import static com.vaadin.ui.Window.Notification.TYPE_HUMANIZED_MESSAGE;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class VaadinUtility {


    public static final ThreadLocal<I18NSource> i18nSource = new ThreadLocal<I18NSource>();

    public static void setThreadI18nSource(I18NSource source) {
        i18nSource.set(source);
    }

    public static I18NSource getThreadI18nSource() {
        return i18nSource.get();
    }

    public static String getLocalizedMessage(String key) {
        return getThreadI18nSource().getMessage(key);
    }

    public static ProcessToolContextFactory getProcessToolContext(ApplicationContext applicationContext) {
        ProcessToolRegistry factory = null;
        if (applicationContext instanceof PortletApplicationContext2) {
            PortletApplicationContext2 portletCtx = (PortletApplicationContext2) applicationContext;
            factory = (ProcessToolRegistry) portletCtx.getPortletConfig()
                    .getPortletContext()
                    .getAttribute(ProcessToolRegistry.class.getName());

        }
        return factory.getProcessToolContextFactory();
    }

    public static HorizontalLayout horizontalLayout(String width, com.vaadin.ui.Component... components) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(width);
        for (com.vaadin.ui.Component c : components) {
            hl.addComponent(c);
        }
        return hl;
    }

    public static VerticalLayout verticalLayout(com.vaadin.ui.Component... components) {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        vl.setWidth("100%");
        for (com.vaadin.ui.Component c : components) {
            vl.addComponent(c);
        }
        return vl;
    }

    public static Select select(String caption, Container container, String itemCaptionPropertyId) {
        Select select = new Select(caption);
        select.setNullSelectionAllowed(true);
        select.setNullSelectionItemId(null);
        select.setImmediate(true);
        select.setContainerDataSource(container);
        select.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
        select.setItemCaptionPropertyId(itemCaptionPropertyId);
        select.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        return select;
    }

    public static Panel panel(String title, com.vaadin.ui.Component... components) {
        Panel p = new Panel();
        p.setWidth("100%");
        p.setCaption(title);
        for (com.vaadin.ui.Component c : components) {
            p.addComponent(c);
        }
        return p;
    }

    public static Label label(String message, int width) {
        Label l = new Label(message);
        l.setWidth(width + "px");
        return l;
    }
    public static Label htmlLabel(String message, int width) {
        Label l = new Label(message, Label.CONTENT_XHTML);
        l.setWidth(width + "px");
        return l;
    }

    public static Label htmlLabel(String message) {
        return new Label(message, Label.CONTENT_XHTML);
    }

    public static HorizontalLayout horizontalLayout(com.vaadin.ui.Component c1, com.vaadin.ui.Component c2) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.addComponent(c1);
        hl.addComponent(c2);
        hl.setComponentAlignment(c2, Alignment.TOP_RIGHT);
        hl.setExpandRatio(c1, 1.0f);
        return hl;
    }

    public static HorizontalLayout horizontalLayout(Alignment alignment, com.vaadin.ui.Component... components) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        if (components != null && components.length > 0) {
            for (com.vaadin.ui.Component c : components) {
                hl.addComponent(c);
                hl.setComponentAlignment(c, alignment);
            }
            if (alignment.isRight()) {
                hl.setExpandRatio(hl.getComponent(0), 1.0f);
            } else if (alignment.isLeft()) {
                hl.setExpandRatio(hl.getComponent(hl.getComponentCount() - 1), 1.0f);
            }
        }
        return hl;
    }

    public static Notification validationNotification(String caption, String description) {
        Notification notification = new Notification(caption, description, Notification.TYPE_ERROR_MESSAGE);
        notification.setStyleName("invalid");
        return notification;
    }

    public static LocalizedPagedTable pagedTable(final Container container, String[] visibleViewColumns, String[] columnViewHeaders,
                                                 Map<String, ColumnGenerator> customViewColumns, ItemClickListener itemClickListener) {
        LocalizedPagedTable table = new LocalizedPagedTable();
        table.setSizeFull();
        table.setPageLength(10);
        table.setImmediate(true);
        table.setSelectable(true);
        table.setContainerDataSource(container);
        if (itemClickListener != null) {
            table.addListener(itemClickListener);
        }
        if (customViewColumns != null && !customViewColumns.isEmpty()) {
            for (String columnId : customViewColumns.keySet()) {
                table.addGeneratedColumn(columnId, customViewColumns.get(columnId));
            }
        }
        table.setVisibleColumns(visibleViewColumns);
        table.setColumnHeaders(columnViewHeaders);
        table.setSortAscending(false);
        table.setSortContainerPropertyId(visibleViewColumns[0]);
        return table;
    }

    public static VerticalLayout wrapPagedTable(I18NSource messageSource, LocalizedPagedTable table) {
        VerticalLayout tableCarrier = new VerticalLayout();
        tableCarrier.setWidth("100%");
        tableCarrier.addComponent(table);
        tableCarrier.addComponent(table.createControls(messageSource.getMessage("pagedtable.itemsperpage"), messageSource.getMessage("pagedtable.page")));
        return tableCarrier;
    }

    public static Button smallButton(String caption) {
        Button button = new Button(caption);
        button.setImmediate(true);
        button.setStyleName("default small");
        return button;
    }

    public static Window modalWindow(String title, ComponentContainer content) {
        Window window = new Window(title, content);
        window.setClosable(false);
        window.setModal(true);
        window.setSizeUndefined();
        return window;
    }

    public static void informationNotification(Application application, I18NSource i18NSource, String message) {
        Notification notification = new Notification(i18NSource.getMessage("notification.info"),
                "<br/><b>" + message + "</b>", TYPE_HUMANIZED_MESSAGE);
        notification.setPosition(POSITION_CENTERED);
        notification.setDelayMsec(3000);
        application.getMainWindow().showNotification(notification);
    }

    public static void validationNotification(Application application, I18NSource messageSource, String errorMessage) {
        Notification notification = new Notification(messageSource.getMessage("process.data.data-error"),
                "<br/>" + errorMessage,
                Notification.TYPE_ERROR_MESSAGE);
        notification.setStyleName("invalid");
        application.getMainWindow().showNotification(notification);
    }

    public static Button addIcon(Application application) {
        return icon(application, "add.png");
    }

    public static Button deleteIcon(Application application) {
        return icon(application, "delete.png");
    }

    public static Button refreshIcon(Application application) {
        return icon(application, "view_refresh.png");
    }

    public static Button refreshIcon(final Application application, final HasRefreshButton hasRefreshButton) {
        Button b = refreshIcon(application);
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                withErrorHandling(application, new Runnable() {
                    public void run() {
                        hasRefreshButton.refreshData();
                    }
                });
            }
        });
        return b;
    }

    public static interface HasRefreshButton {
        void refreshData();
    }

    public static Button icon(Application application, String fileName) {
        Button b = new Button();
        b.setStyleName(BaseTheme.BUTTON_LINK);
        b.setIcon(new ClassResource(VaadinUtility.class, "/img/" + fileName, application));
        b.setImmediate(true);
        return b;
    }

    public static Embedded embedded(Application application, String fileName) {
        return new Embedded(null, new ClassResource(VaadinUtility.class, fileName, application));
    }

    public static String widgetsErrorMessage(I18NSource i18NSource, Map<ProcessToolDataWidget, Collection<String>> errorMap) {
        String errorMessage = "<ul>";
        for (ProcessToolDataWidget w : errorMap.keySet()) {
            Collection<String> col = errorMap.get(w);
            String caption = null;
            if (w instanceof BaseProcessToolWidget) {
                caption = ((BaseProcessToolWidget) w).getAttributeValue("caption");
            }
            if (caption != null) {
                errorMessage += "<li>" + i18NSource.getMessage(caption) + "<ul>";
            }
            for (String m : col) {
                errorMessage += "<li>" + i18NSource.getMessage(m) + "</li>\n";
            }
            if (caption != null) {
                errorMessage += "</ul></li>";
            }
        }
        errorMessage += "</ul>";
        return errorMessage;
    }

    public static <T extends Component> T styled(T c, String style) {
        c.addStyleName(style);
        return c;
    }

    public static Button linkButton(String caption, final Runnable onClick) {
        Button b = button(caption, onClick);
        b.setStyleName(Reindeer.BUTTON_LINK);
        return b;
    }
    
    public static Button button(String caption, final Runnable onClick) {
        Button b = new Button(caption);
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                onClick.run();
            }
        });
        return b;
    }
    
    public static <T extends Component> T width(T c, String width) {
        c.setWidth(width);
        return c;
    }
}

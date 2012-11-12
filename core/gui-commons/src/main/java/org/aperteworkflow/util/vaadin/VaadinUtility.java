package org.aperteworkflow.util.vaadin;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.*;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;
import org.aperteworkflow.util.vaadin.ui.table.LocalizedPagedTable;
import org.vaadin.dialogs.ConfirmDialog;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.StringUtil;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.vaadin.ui.Window.Notification.*;
import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class VaadinUtility {
    public static final String SIMPLE_DATE_FORMAT_STRING = "yyyy-MM-dd";
    public static final String FULL_DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    private static final String REGISTER_CLOSE_WARNING = " registerCloseHandler(function() { return \"%s\"; }); ";
    private static final String UNREGISTER_CLOSE_WARNING = " clearCloseHandler(); ";

    public static DateFormat simpleDateFormat() {
        return new SimpleDateFormat(SIMPLE_DATE_FORMAT_STRING);
    }

    public static DateFormat fullDateFormat() {
        return new SimpleDateFormat(FULL_DATE_FORMAT_STRING);
    }

    public static ProcessToolContextFactory getProcessToolContext(ApplicationContext applicationContext) {
        ProcessToolRegistry factory = null;
        if (applicationContext instanceof PortletApplicationContext2) {
            PortletApplicationContext2 portletCtx = (PortletApplicationContext2) applicationContext;
            factory = (ProcessToolRegistry) portletCtx.getPortletConfig()
                    .getPortletContext()
                    .getAttribute(ProcessToolRegistry.class.getName());
        }
        return factory != null ? factory.getProcessToolContextFactory() : null;
    }

    public static HorizontalLayout horizontalLayout(String width, com.vaadin.ui.Component... components) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setWidth(width);
        if (components != null) {
            for (com.vaadin.ui.Component c : components) {
                hl.addComponent(c);
            }
        }
        return hl;
    }

    public static HorizontalLayout fullHorizontalLayout(com.vaadin.ui.Component... components) {
        return horizontalLayout("100%", components);
    }

    public static VerticalLayout verticalLayout(com.vaadin.ui.Component... components) {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        vl.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        if (components != null) {
            for (com.vaadin.ui.Component c : components) {
                if (c != null) {
                    vl.addComponent(c);
                }
            }
        }
        return vl;
    }

    public static CheckBox checkBox(String caption) {
        CheckBox cb = new CheckBox();
        if (caption != null) {
            cb.setCaption(caption);
        }
        cb.setValue(false);
        cb.setImmediate(true);
        cb.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        return cb;
    }

    public static Select select(String caption, Container container, String itemCaptionPropertyId) {
        Select select = new Select(caption);
        select.setNullSelectionAllowed(true);
        select.setNullSelectionItemId(null);
        select.setImmediate(true);
        select.setContainerDataSource(container);
        select.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
        select.setItemCaptionPropertyId(itemCaptionPropertyId);
        select.setSizeUndefined();
        select.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        return select;
    }

    public static Panel panel(String title, com.vaadin.ui.Component... components) {
        Panel p = new Panel();
        p.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        p.setCaption(title);
        for (com.vaadin.ui.Component c : components) {
            p.addComponent(c);
        }
        return p;
    }

    public static Label label(String message, int width) {
        Label l = new Label(message);
        l.setWidth(width, Sizeable.UNITS_PIXELS);
        return l;
    }

    public static HorizontalLayout horizontalLayout(com.vaadin.ui.Component c1, com.vaadin.ui.Component c2) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        hl.setSpacing(true);
        hl.addComponent(c1);
        hl.addComponent(c2);
        hl.setComponentAlignment(c2, Alignment.TOP_RIGHT);
        hl.setExpandRatio(c1, 1.0f);
        return hl;
    }

    public static HorizontalLayout horizontalLayout(Alignment alignment, com.vaadin.ui.Component... components) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        hl.setSpacing(true);
        if (components != null && components.length > 0) {
            for (com.vaadin.ui.Component c : components) {
                hl.addComponent(c);
                hl.setComponentAlignment(c, alignment);
            }
            if (alignment.isRight()) {
                hl.setExpandRatio(hl.getComponent(0), 1.0f);
            }
            else if (alignment.isLeft()) {
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

    public static Table simpleTable(Container dataSource, Object[] visiblePropertyIds, Map<String, ColumnGenerator> customColumns) {
        Table table = new Table();
        table.addStyleName("big striped borderless");
        table.setSizeFull();
        table.setPageLength(0);
        table.setImmediate(false);
        table.setSelectable(false);
        table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
        table.setColumnCollapsingAllowed(false);
        table.setSortDisabled(true);
        if (customColumns != null) {
			for (Map.Entry<String, ColumnGenerator> entry : customColumns.entrySet()) {
                table.addGeneratedColumn(entry.getKey(), entry.getValue());
            }
        }
        table.setContainerDataSource(dataSource);
        table.setVisibleColumns(visiblePropertyIds);
        table.setColumnExpandRatio(visiblePropertyIds[visiblePropertyIds.length - 1], 1.0f);
        return table;
    }

    public static LocalizedPagedTable pagedTable(final Container container, String[] visibleViewColumns, String[] columnViewHeaders,
                                                 Map<String, ColumnGenerator> customViewColumns, ItemClickListener itemClickListener) {
        LocalizedPagedTable table = new LocalizedPagedTable();
        table.addStyleName("striped strong");
        table.setSizeFull();
        table.setPageLength(10);
        table.setImmediate(true);
        table.setSelectable(true);
        table.setContainerDataSource(container);
        if (itemClickListener != null) {
            table.addListener(itemClickListener);
        }
        if (customViewColumns != null && !customViewColumns.isEmpty()) {
			for (Map.Entry<String, ColumnGenerator> entry : customViewColumns.entrySet()) {
                table.addGeneratedColumn(entry.getKey(), entry.getValue());
            }
        }
        table.setVisibleColumns(visibleViewColumns);
        table.setColumnHeaders(columnViewHeaders);
        table.setSortAscending(true);
        table.setSortContainerPropertyId(visibleViewColumns[0]);
        return table;
    }

    public static VerticalLayout wrapPagedTable(I18NSource messageSource, LocalizedPagedTable table) {
        VerticalLayout tableCarrier = new VerticalLayout();
        tableCarrier.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        tableCarrier.addComponent(table);
        tableCarrier.addComponent(tableControls(messageSource, table));
        return tableCarrier;
    }

    public static HorizontalLayout tableControls(I18NSource messageSource, LocalizedPagedTable table) {
        return table.createControls(messageSource);
    }

    public static Label boldLabel(String text) {
        return new Label("<b>" + text + "</b>", Label.CONTENT_XHTML);
    }

    public static Label hr() {
        return new Label("<hr/>", Label.CONTENT_XHTML);
    }

    public static Button smallButton(String caption) {
        Button button = new Button(caption);
        button.setImmediate(true);
        button.setStyleName("default small");
        return button;
    }

    public static Button link(String caption, Resource icon, Button.ClickListener listener) {
        Button b = button(caption, null, "link", listener);
        b.setIcon(icon);
        b.setWidth(b.getWidth() + 10, Sizeable.UNITS_PIXELS);
        return b;
    }

    public static Button link(String caption, Button.ClickListener listener) {
        return button(caption, null, "link", listener);
    }

    public static Button link(String caption) {
        return link(caption, null);
    }

    public static Button button(String caption, String description, String style) {
        return button(caption, description, style, null);
    }

    public static Button button(String caption, String description, String style, Button.ClickListener listener) {
        Button button = new Button(caption);
        if (description != null) {
            button.setDescription(description);
        }
        if(style != null)
        	button.setStyleName(style);
        if (listener != null) {
            button.addListener(listener);
        }
        button.setImmediate(true);
        button.setWidth(haxWidth(caption, (style != null ? style.contains("link") : false)));
        return button;
    }

    /*
     * IE7 be doomed!!!
     */
    private static String haxWidth(String base, boolean link) {
        //linear regression: button 7.5x + 35, link: 6.75x + 10
        return (StringUtil.hasText(base) ? base.length() * (link ? 6.75 : 7.5) + (link ? 10 : 40) : -1) + "px";
    }

    public static Window modalWindow(String title, ComponentContainer content) {
        Window window = new Window(title, content);
        window.setClosable(false);
        window.setModal(true);
        window.setSizeUndefined();
        return window;
    }

    public static void informationNotification(Application application, String message) {
        informationNotification(application, message, 3000);
    }

    public static void informationNotification(Application application, String message, int delay) {
        Notification notification = new Notification("<b>" + message + "</b>", TYPE_HUMANIZED_MESSAGE);
        notification.setPosition(POSITION_CENTERED);
        notification.setDelayMsec(delay);
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

    public static Button copyIcon(Application application) {
        return icon(application, "copy.png");
    }

    public static Button refreshIcon(Application application) {
        return icon(application, "view_refresh.png");
    }

    public static Button refreshIcon(final Application application, final Refreshable refreshable) {
        Button b = refreshIcon(application);
        b.setWidth(18, Sizeable.UNITS_PIXELS);
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                withErrorHandling(application, new Runnable() {
                    @Override
					public void run() {
                        refreshable.refreshData();
                    }
                });
            }
        });
        return b;
    }

    public static interface Refreshable {
        void refreshData();
    }

    public static Button icon(Application application, String fileName) {
        return icon(application, fileName, null);
    }

    public static Button icon(Application application, String fileName, Button.ClickListener listener) {
        return icon(imageResource(application, fileName), listener);
    }

    public static Button icon(Resource icon, Button.ClickListener listener) {
        return icon(null, icon, listener);
    }

    public static Button icon(String description, Resource icon, Button.ClickListener listener) {
        Button b = new Button();
        b.setStyleName("link");
        b.setIcon(icon);
        b.setImmediate(true);
        b.setDescription(description);
        if (listener != null) {
            b.addListener(listener);
        }
        return b;
    }

    public static ClassResource imageResource(Application application, String fileName) {
        return new ClassResource(VaadinUtility.class, "/img/" + fileName, application);
    }

    public static Embedded embedded(Application application, String fileName) {
        return new Embedded(null, new ClassResource(VaadinUtility.class, fileName, application));
    }

    public static String widgetsErrorMessage(I18NSource i18NSource, Map<ProcessToolDataWidget, Collection<String>> errorMap) {
        StringBuilder errorMessage = new StringBuilder("<ul>");
		for (Map.Entry<ProcessToolDataWidget, Collection<String>> entry : errorMap.entrySet()) {
			ProcessToolDataWidget w = entry.getKey();
            Collection<String> col = entry.getValue();
            String caption = null;
            if (w instanceof BaseProcessToolWidget) {
                caption = ((BaseProcessToolWidget) w).getAttributeValue("caption");
            }
            if (caption != null) {
                errorMessage.append("<li>").append(i18NSource.getMessage(caption)).append("<ul>");
            }
            for (String m : col) {
                errorMessage.append("<li>").append(i18NSource.getMessage(m)).append("</li>\n");
            }
            if (caption != null) {
                errorMessage.append("</ul></li>");
            }
        }
        errorMessage.append("</ul>");
        return errorMessage.toString();
    }

    public static String formErrorMessage(Collection<String> errorMap) {
        StringBuilder errorMessage = new StringBuilder("<ul>");
        for (String msg : errorMap) {
            errorMessage.append("<li>").append(msg).append("</li>\n");
		}
        errorMessage.append("</ul>");
        return errorMessage.toString();
    }

    public static void displayConfirmationWindow(Application application, I18NSource i18NSource, String title, String question, final EventHandler okEvent, final EventHandler cancelEvent) {
    	displayConfirmationWindow(application, i18NSource, title, question, okEvent, cancelEvent, i18NSource.getMessage("button.ok"), i18NSource.getMessage("button.cancel"));
    }

	public static void displayConfirmationWindow(Application application, I18NSource i18NSource,
                                                 String title, String question,
                                                 final EventHandler okEvent,
                                                 final EventHandler cancelEvent, String okButtonLabel,
                                                 String cancelButtonLabel) {
        final Window newConfirmationWindow = new Window(title);
        newConfirmationWindow.setModal(true);
        newConfirmationWindow.setBorder(0);
        newConfirmationWindow.setClosable(false);
        newConfirmationWindow.setWidth(500, Sizeable.UNITS_PIXELS);

        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        Button okButton = button(i18NSource.getMessage(okButtonLabel), null, "default", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                newConfirmationWindow.getParent().removeWindow(newConfirmationWindow);
                if (okEvent != null) {
                    okEvent.onEvent();
                }
            }
        });
        hl.addComponent(okButton);
        Button cancelButton = button(i18NSource.getMessage(cancelButtonLabel), null, "default", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                newConfirmationWindow.getParent().removeWindow(newConfirmationWindow);
                if (cancelEvent != null) {
                    cancelEvent.onEvent();
                }
            }
        });

        if (cancelEvent != null) {
            newConfirmationWindow.addListener(new Window.CloseListener() {
                @Override
                public void windowClose(Window.CloseEvent e) {
                    cancelEvent.onEvent();
                }
            });
        }
        hl.addComponent(cancelButton);

        vl.addComponent(new Label(question));
        vl.addComponent(hl);
        vl.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);

        newConfirmationWindow.addComponent(vl);

        application.getMainWindow().addWindow(newConfirmationWindow);
    }

	public static void displayConfirmationWindow(Application application, I18NSource i18NSource,
												 String title, String question,
												 final String[] labels,
												 final EventHandler[] events, final EventHandler cancelEvent) {
		final Window newConfirmationWindow = new Window(title);
		newConfirmationWindow.setModal(true);
		newConfirmationWindow.setBorder(0);
		newConfirmationWindow.setClosable(false);
		newConfirmationWindow.setWidth(500, Sizeable.UNITS_PIXELS);

		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);

		for (int i = 0; i < events.length; ++i) {
			final EventHandler buttonEvent = events[i];
			final String buttonLabel = labels[i];

			if (buttonLabel != null) {
				Button button = button(i18NSource.getMessage(buttonLabel), null, "default", new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						newConfirmationWindow.getParent().removeWindow(newConfirmationWindow);

						if (buttonEvent != null) {
							buttonEvent.onEvent();
						}
					}
				});
				hl.addComponent(button);
			}
		}

		if (cancelEvent != null) {
			newConfirmationWindow.addListener(new Window.CloseListener() {
				@Override
				public void windowClose(Window.CloseEvent e) {
					cancelEvent.onEvent();
				}
			});
		}

		vl.addComponent(new Label(question));
		vl.addComponent(hl);
		vl.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);

		newConfirmationWindow.addComponent(vl);

		application.getMainWindow().addWindow(newConfirmationWindow);
	}

    public static HorizontalLayout labelWithIcon(Resource image, String caption, String style, String description) {
        Embedded img = new Embedded(null, image);
        img.setDescription(description);
        Label label = new Label(caption, Label.CONTENT_XHTML);
        label.setDescription(description);
        if (style != null) {
            label.setStyleName(style);
        }
        HorizontalLayout hl = VaadinUtility.horizontalLayout(Alignment.MIDDLE_LEFT, img, label);
        hl.setWidth(-1, Sizeable.UNITS_PIXELS);
        return hl;
    }

    public static void registerClosingWarning(Window window, String warningMessage) {
    	String msg = String.format(REGISTER_CLOSE_WARNING, warningMessage);
    	window.executeJavaScript(msg);
    }

    public static void unregisterClosingWarning(Window window) {
    	window.executeJavaScript(UNREGISTER_CLOSE_WARNING);
    }


    /////////////////////////////////////////////////////
    /////////////////////////////////////////////////////

    public static void errorNotification(Application application, I18NSource messageSource, String message) {
        Notification notification = new Notification(messageSource.getMessage("notification.error"),
                "<br/><b>" + message + "</b>", TYPE_ERROR_MESSAGE);
        notification.setPosition(POSITION_CENTERED);
        notification.setStyleName("error");
        application.getMainWindow().showNotification(notification);
    }


    public static Label htmlLabel(String message, int width) {
         Label l = new Label(message, Label.CONTENT_XHTML);
         l.setWidth(width, Sizeable.UNITS_PIXELS);
         return l;
     }

     public static Label htmlLabel(String message) {
         return new Label(message, Label.CONTENT_XHTML);
     }

     public static HorizontalLayout hl(com.vaadin.ui.Component... components) {
         HorizontalLayout hl = new HorizontalLayout();
         hl.setWidth(100, Sizeable.UNITS_PERCENTAGE);
         hl.setSpacing(true);
         for (Component c : components) {
             hl.addComponent(c);
         }
         return hl;
     }


    public static Embedded embedded(Application application, File file) {
        return new Embedded(null, new FileResource(file, application));
    }

    public static String getLocalizedMessage(String key) {
		return I18NSource.ThreadUtil.getLocalizedMessage(key);
  	}


	public static <T extends Component> T styled(T c, String style) {
		c.addStyleName(style);
		return c;
	}

	public static Runnable confirmable(final Application app, final String windowCaption, final String message,
									   final Runnable runnable) {
		return new Runnable() {
			@Override
			public void run() {
				ConfirmDialog.show(app.getMainWindow(),
						windowCaption, message,
						getLocalizedMessage("confirm.yes"),
						getLocalizedMessage("confirm.no"),
						new ConfirmDialog.Listener() {
							@Override
							public void onClose(ConfirmDialog confirmDialog) {
								if (confirmDialog.isConfirmed()) {
									runnable.run();
								}
							}
						});
			}
		};
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

	public static  <C extends Component> Component joinHorizontally(List<C> components) {
		switch (components.size()) {
			case 0:
				return null;
			case 1:
				return components.get(0);
			default:
				HorizontalLayout hl = new HorizontalLayout();
				for (Component c : components) {
					hl.addComponent(c);
				}
				return hl;
		}
	}
}

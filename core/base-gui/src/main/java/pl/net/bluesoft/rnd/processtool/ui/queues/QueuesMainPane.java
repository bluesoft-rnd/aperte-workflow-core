package pl.net.bluesoft.rnd.processtool.ui.queues;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueRight;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataPane;
import pl.net.bluesoft.rnd.processtool.ui.process.WindowProcessDataDisplayContext;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.VaadinUtility.Refreshable;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;

public class QueuesMainPane extends VerticalLayout implements Refreshable {
	private I18NSource i18NSource;
	private ProcessToolBpmSession session;
	private EventListener<BpmEvent> bpmEventSubScriber;
	private GenericVaadinPortlet2BpmApplication application;
	private Table table;
	private BeanItemContainer<ProcessQueue> bic;
	private Window addQueueWindow;

	public QueuesMainPane(I18NSource i18nSource, ProcessToolBpmSession bpmSession, GenericVaadinPortlet2BpmApplication application) {
		this.i18NSource = i18nSource;
		this.session = bpmSession;
		this.application = application;
		initUI();
	}

	private void initUI() {
		final ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

		final Table table = getUserTasksTable(ctx);		
		Button addButton = new Button(getMessage("queues.add"));
		Button removeButton = new Button(getMessage("queues.remove"));
		
		Component refreshButton = refreshIcon(application, this);
        addComponent(horizontalLayout(new Label(getMessage("queues.help.short")), refreshButton));
		addComponent(table);
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.addComponent(addButton);
		hl.addComponent(removeButton);
		addComponent(hl);
		setSpacing(true);
		
		addButton.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				VerticalLayout pane = new VerticalLayout();
				pane.setMargin(true);
				pane.setWidth("500px");
				Form form = getAddQueueForm();
				pane.addComponent(form);
				addQueueWindow = new Window(getMessage("queues.add.title"), pane);
				addQueueWindow.setClosable(true);
				addQueueWindow.setModal(true);
				addQueueWindow.setSizeUndefined();
				addQueueWindow.setResizable(false);
				getApplication().getMainWindow().addWindow(addQueueWindow);
			}
		});
		
		removeButton.addListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				if (table.getValue() == null)
					return;
				
				ProcessQueue pq = (ProcessQueue)table.getValue();
				if (pq.getUserAdded() == null || pq.getUserAdded() == false) {
					getWindow().showNotification(getMessage("queues.remove.forbidden"));
					return;
				}
				
				final Collection<ProcessQueueConfig> queues = new ArrayList<ProcessQueueConfig>();
				ProcessQueueConfig config = new ProcessQueueConfig();
				config.setName(pq.getName());
				queues.add(config);
				
				withErrorHandling(getApplication(), new Runnable() {

					@Override
					public void run() {
						ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().withProcessToolContext(new ProcessToolContextCallback() {
							@Override
							public void withContext(ProcessToolContext ctx) {
								ctx.getProcessDefinitionDAO().removeQueueConfigs(queues);
							}
						});
					}
				});
				
				refreshData();
				
				getWindow().showNotification(getMessage("queues.remove.succeed"));
			}
		});
	}

	private Table getUserTasksTable(ProcessToolContext ctx) {
		table = new Table();
		table.setWidth("100%");
		table.setHeight("200px");

		table.setImmediate(true); // react at once when something is selected
		table.setSelectable(true);
		table.setEditable(!isReadOnly());

		bic = new BeanItemContainer<ProcessQueue>(ProcessQueue.class);
		for (ProcessQueue pq : session.getUserAvailableQueues(ctx)) {
			bic.addBean(pq);
		}
		table.setContainerDataSource(bic);
		table.setTableFieldFactory(new DefaultFieldFactory() {
			@Override
			public Field createField(Container container, Object itemId,
					Object propertyId, Component uiContext) {
                Field field;
                if ("browsable".equals(propertyId) || "userAdded".equals(propertyId)) {
                    field = new CheckBox();
                    field.setWidth("16px");
                }
                else {
                    field = super.createField(container, itemId, propertyId, uiContext);
                    field.setWidth("100%");
                }
                field.setReadOnly(true);
                return field;
	            
			}
		});

        table.addGeneratedColumn("translation", new ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                ProcessQueue queue = (ProcessQueue) itemId;
                String translation = getMessage(queue.getDescription());
                return translation != null ? translation : "";
            }
        });
		
		table.setVisibleColumns(new Object[] { "description", "translation", "processCount", "name", "browsable", "userAdded" });

		for (Object o : table.getVisibleColumns()) {
			table.setColumnHeader(o, getMessage("queues." + o));
		}

		table.addListener(
			new ItemClickEvent.ItemClickListener() {
				public void itemClick(final ItemClickEvent event) {
                    withErrorHandling(getApplication(), new Runnable() {
                        public void run() {
                             if (event.isDoubleClick()) {
                                 ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                                 BeanItem<ProcessQueue> beanItem = bic.getItem(event.getItemId());
                                 BpmTask task = session.assignTaskFromQueue(beanItem.getBean(), ctx);
                                 if (task != null) {
                                     getWindow().executeJavaScript("Liferay.trigger('processtool.bpm.assignProcess', '"
                                             + task.getProcessInstance().getInternalId() + "');");
                                     getWindow().executeJavaScript("vaadin.forceSync();");
                                     Window w = new Window(task.getProcessInstance().getInternalId());
                                     w.setContent(new ProcessDataPane(getApplication(), session, i18NSource, task, new WindowProcessDataDisplayContext(w)));
                                     w.center();
                                     getWindow().addWindow(w);
	                                 w.focus();

                                 }

                             }
                        }});
				}
			 });
		if (bpmEventSubScriber == null) {
			session.getEventBusManager().subscribe(BpmEvent.class, bpmEventSubScriber = new MyBpmEventEventListener());
		}


		return table;
	}

	public void refreshData() {
		bic.removeAllItems();
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		for (ProcessQueue pq : session.getUserAvailableQueues(ctx)) {
			bic.addBean(pq);
		}
	}

	private String getMessage(String key) {
		return i18NSource.getMessage(key);
	}
	
	private Form getAddQueueForm() {
		
		final QueueForm form = new QueueForm(ProcessToolContext.Util.getThreadProcessToolContext(), i18NSource, application);
		final QueueBean queue = new QueueBean();
		BeanItem<QueueBean> item = new BeanItem<QueueBean>(queue);
		form.setItemDataSource(item, Arrays.asList("process", "name", "description", "rights"));
		form.setWriteThrough(false);
		form.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		
		Button saveButton = new Button(getMessage("queues.add.save"));
		saveButton.addListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				if (form.isValid()) {
					form.commit();

					final ProcessQueueConfig config = new ProcessQueueConfig();
					config.setDescription(queue.getDescription());
					config.setName(queue.getName());
					config.setUserAdded(true);
					config.getRights().addAll(queue.getRights());
					final Collection<ProcessQueueConfig> queues = new ArrayList<ProcessQueueConfig>();
					queues.add(config);
					
					withErrorHandling(getApplication(), new Runnable() {
						@Override
						public void run() {
							ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().withProcessToolContext(new ProcessToolContextCallback() {

								@Override
								public void withContext(ProcessToolContext ctx) {

                                    //no way!
//									for (ProcessQueueRight r : config.getRights()) {
//										if (ctx.getRegistry().createRoleIfNotExists(r.getRoleName(), "Role generated by queue portlet"))
//											logger.info("Created new role " + r.getRoleName());
//									}
									
									ctx.getProcessDefinitionDAO().updateOrCreateQueueConfigs(queues);
									getApplication().getMainWindow().removeWindow(addQueueWindow);

								}
							});
						}
					});
					
					refreshData();
				}
				else {
                    StringBuilder sb = new StringBuilder("<ul>");
                    for (Object propertyId : form.getItemPropertyIds()) {
                        Field field = form.getField(propertyId);
                        if (!field.isValid() && field.isRequired()) {
                            sb.append("<li>").append(field.getRequiredError()).append("</li>");
                        }
                    }
                    sb.append("</ul>");
                    VaadinUtility.validationNotification(application, i18NSource, sb.toString());
				}
			}
		});
		
		form.getFooter().addComponent(saveButton);
		return form;
	}

	private class MyBpmEventEventListener implements EventListener<BpmEvent>, Serializable {
		@Override
		public void onEvent(BpmEvent e) {
			if (QueuesMainPane.this.isVisible() && QueuesMainPane.this.getApplication() != null) {
				refreshData();
			}
		}
	}
}

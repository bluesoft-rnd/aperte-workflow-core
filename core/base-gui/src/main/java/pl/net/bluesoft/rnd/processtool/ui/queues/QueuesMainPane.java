package pl.net.bluesoft.rnd.processtool.ui.queues;

import com.vaadin.Application;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataPane;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.HasRefreshButton;
import pl.net.bluesoft.util.eventbus.EventListener;

import static pl.net.bluesoft.rnd.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.horizontalLayout;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.refreshIcon;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class QueuesMainPane extends VerticalLayout implements HasRefreshButton {

	private I18NSource i18NSource;
	private ProcessToolBpmSession session;
	private EventListener<BpmEvent> bpmEventSubScriber;
	private Application application;
	private Table table;
	private BeanItemContainer<ProcessQueue> bic;

	public QueuesMainPane(I18NSource i18nSource,
	                      ProcessToolBpmSession bpmSession,
	                      Application application) {
		this.i18NSource = i18nSource;
		this.session = bpmSession;
		this.application = application;
		initUI();
	}

	
	private void initUI() {
		ProcessToolContext ctx = ProcessToolContext.Util.getProcessToolContextFromThread();

		Table table = getUserTasksTable(ctx);

		Component refreshButton = refreshIcon(application, this);
        addComponent(horizontalLayout(new Label(getMessage("queues.help.short")), refreshButton));
		addComponent(table);
	}

	private Table getUserTasksTable(ProcessToolContext ctx) {
		table = new Table();
		table.setWidth("100%");
		table.setHeight("200px");

		table.setImmediate(true); // react at once when something is selected
		table.setSelectable(true);

		bic = new BeanItemContainer<ProcessQueue>(ProcessQueue.class);
		for (ProcessQueue pq : session.getUserAvailableQueues(ctx)) {
			bic.addBean(pq);
		}
		table.setContainerDataSource(bic);
		table.setVisibleColumns(new Object[] { "description", "processCount" });

		for (Object o : table.getVisibleColumns()) {
			table.setColumnHeader(o, getMessage("queues." + o));
		}

		table.addListener(
			new ItemClickEvent.ItemClickListener() {
				public void itemClick(final ItemClickEvent event) {
                    withErrorHandling(getApplication(), new Runnable() {
                        public void run() {
                             if (event.isDoubleClick()) {
                                 BeanItem<ProcessQueue> instanceBeanItem = bic.getItem(event.getItemId());
                                 ProcessToolContext ctx = ProcessToolContext.Util.getProcessToolContextFromThread();
                                 ProcessInstance instance = session.assignTaskFromQueue(instanceBeanItem.getBean(), ctx);
        //						 getWindow().showNotification(instance != null ? instance.getInternalId() : "NIL");
                                 if (instance != null) {
                                     getWindow().executeJavaScript("Liferay.trigger('processtool.bpm.assignProcess', '" + instance.getInternalId() + "');");
                                     getWindow().executeJavaScript("vaadin.forceSync();");//other portlets may need this
                                     Window w = new Window(instance.getInternalId());
                                     w.setContent(new ProcessDataPane(getApplication(), session, i18NSource,instance, new ProcessDataPane.WindowDisplayProcessContextImpl(w)));
                                     w.center();
                                     getWindow().addWindow(w);
	                                 w.focus();

                                 }

                             }
                        }});
				}
			 });
		if (bpmEventSubScriber == null) {
			session.getEventBusManager().subscribe(BpmEvent.class, bpmEventSubScriber = new EventListener<BpmEvent>() {
				@Override
				public void onEvent(BpmEvent e) {
					refreshData();
				}
			});
		}


		return table;
	}

	public void refreshData() {
		bic.removeAllItems();
		ProcessToolContext ctx = ProcessToolContext.Util.getProcessToolContextFromThread();
		for (ProcessQueue pq : session.getUserAvailableQueues(ctx)) {
			bic.addBean(pq);
		}
	}

	private String getMessage(String key) {
		return i18NSource.getMessage(key);
	}
}

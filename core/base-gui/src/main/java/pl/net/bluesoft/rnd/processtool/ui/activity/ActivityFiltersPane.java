package pl.net.bluesoft.rnd.processtool.ui.activity;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.BaseTheme;

import org.aperteworkflow.util.vaadin.EventHandler;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceFilterDAO;
import pl.net.bluesoft.rnd.processtool.filters.FilterChangedEvent;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.util.List;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;

public class ActivityFiltersPane extends Panel implements VaadinUtility.Refreshable {

	private ActivityMainPane activityMainPane;
	private GridLayout filterList;
	private EventListener<FilterChangedEvent> filterEventSubscriber = null;

	public ActivityFiltersPane(ActivityMainPane activityMainPane) {
		this.activityMainPane = activityMainPane;
		setWidth("100%");
		setCaption(getMessage("activity.filters.title"));
		addComponent(horizontalLayout(new Label(getMessage("activity.filters.help.short"), Label.CONTENT_XHTML),
				                             refreshIcon(activityMainPane.getApplication(), this)));
		filterList = new GridLayout();
		filterList.setColumns(2);
		filterList.setMargin(true);
		filterList.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		addComponent(filterList);
		refreshData();

		if (filterEventSubscriber == null) {
			activityMainPane.getBpmSession().getEventBusManager().subscribe(FilterChangedEvent.class,
                    filterEventSubscriber = new EventListener<FilterChangedEvent>() {
				@Override
				public void onEvent(FilterChangedEvent e) {
                    if (ActivityFiltersPane.this.isVisible() && ActivityFiltersPane.this.getApplication() != null) {
                        refreshData();
                    }
                }
			});
		}

	}



    @Override
	public void refreshData() {
		filterList.removeAllComponents();
		ProcessToolContext processToolContextFromThread = ProcessToolContext.Util.getThreadProcessToolContext();

		final ProcessToolBpmSession bpmSession = activityMainPane.getBpmSession();
		final UserData user = bpmSession.getUser(processToolContextFromThread);

		final ProcessInstanceFilterDAO processInstanceFilterDAO = processToolContextFromThread.getProcessInstanceFilterDAO();
		List<ProcessInstanceFilter> filters = processInstanceFilterDAO.findAllByUserData(user);

		for (final ProcessInstanceFilter filter : filters) {
//			HorizontalLayout task = new HorizontalLayout();
			Button taskName = new Button(filter.getName());			
			ResultsPageWrapper<BpmTask> tasks = bpmSession.findProcessTasks(filter, 0, 0, processToolContextFromThread);
			taskName.setCaption(taskName.getCaption() + " (" + tasks.getTotal() + ")");
			if(tasks.getTotal() == 0)
				taskName.addStyleName("v-disabled");
			
			taskName.addStyleName(BaseTheme.BUTTON_LINK);
			taskName.addListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					withErrorHandling(getApplication(), new Runnable() {
						@Override
						public void run() {
							ProcessInstanceFilterDAO processInstanceFilterDAO =
                                    ProcessToolContext.Util.getThreadProcessToolContext().getProcessInstanceFilterDAO();
							final ProcessInstanceFilter fullFilter = processInstanceFilterDAO.fullLoadById(filter.getId());
							activityMainPane.displayFilterPane(fullFilter);
						}
					});
				}
			});

			filterList.addComponent(taskName);
			filterList.setComponentAlignment(taskName, Alignment.MIDDLE_LEFT);

			Button taskDelete = new Button(getMessage("activity.filters.delete"));
			taskDelete.setStyleName(BaseTheme.BUTTON_LINK);
			taskDelete.addListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					withErrorHandling(getApplication(), new Runnable() {
						@Override
						public void run() {

							EventHandler okHandler = new EventHandler() {
								@Override
								public void onEvent() {
									ProcessInstanceFilterDAO processInstanceFilterDAO = ProcessToolContext.Util
                                            .getThreadProcessToolContext().getProcessInstanceFilterDAO();
									processInstanceFilterDAO.deleteFilter(filter);
									activityMainPane.getBpmSession().getEventBusManager().publish(new FilterChangedEvent());
								}
							};
							VaadinUtility.displayConfirmationWindow(activityMainPane.getApplication(),
									                                       activityMainPane.getI18NSource(),
									                                       getMessage("activity.filters.delete.popup.title"),
									                                       getMessage("activity.filters.delete.popup.question")
                                                                                   .replaceAll("%s", filter.getName()),
									                                       okHandler,
									                                       null
							);
						}
					});
				}
			});

			filterList.addComponent(taskDelete);
			filterList.setComponentAlignment(taskDelete, Alignment.MIDDLE_RIGHT);
		}
	}

	private String getMessage(String title) {
		return activityMainPane.getI18NSource().getMessage(title);
	}
}

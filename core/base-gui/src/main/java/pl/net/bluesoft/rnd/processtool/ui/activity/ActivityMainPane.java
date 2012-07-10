package pl.net.bluesoft.rnd.processtool.ui.activity;

import static org.aperteworkflow.util.vaadin.VaadinUtility.verticalLayout;
import static pl.net.bluesoft.util.lang.Formats.nvl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aperteworkflow.ui.view.ViewCallback;
import org.aperteworkflow.ui.view.ViewRegistry;
import org.aperteworkflow.ui.view.ViewRenderer;
import org.aperteworkflow.util.vaadin.EventHandler;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import org.aperteworkflow.util.vaadin.ResourceCache;
import org.aperteworkflow.util.vaadin.UriChangedCallback;
import org.aperteworkflow.util.vaadin.VaadinUtility;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.ui.newprocess.NewProcessExtendedPane;
import pl.net.bluesoft.rnd.processtool.ui.process.ProcessDataViewComponent;
import pl.net.bluesoft.rnd.processtool.view.impl.BasicViewController;
import pl.net.bluesoft.rnd.processtool.view.impl.ComponentPaneRenderer;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Strings;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

public class ActivityMainPane extends VerticalLayout implements ViewCallback
{
	private Application application;
	private I18NSource i18NSource;
	private ProcessToolBpmSession bpmSession;

	private UriFragmentUtility uriFragmentUtility = new UriFragmentUtility();

	private List<UriChangedCallback> uriCallbacks = new ArrayList<UriChangedCallback>();

	private BasicViewController viewController;
	private Button showHideButton1;
	private LeftPanelVisibilityTrigger leftPanelTrigger;
	private Button showHideButton2;
	private HorizontalLayout horizontalLayout;
	private Button showHideButton0;

	private ResourceCache resourceCache;
	private ActivityQueuesPane activityQueuesPane;

	public ActivityMainPane(Application application, I18NSource i18NSource, ProcessToolBpmSession bpmSession)
	{
		this.application = application;
		this.i18NSource = i18NSource;
		this.bpmSession = bpmSession;
		this.resourceCache = new ResourceCache(application);
		setWidth("100%");
		initLayout();
	}

	private void initLayout()
	{
		uriFragmentUtility.addListener(new FragmentChangedListener()
		{
			@Override
			public void fragmentChanged(FragmentChangedEvent source)
			{
				String fragment = uriFragmentUtility.getFragment();
				if(!uriCallbacks.isEmpty() && Strings.hasText(fragment))
				{
					for(UriChangedCallback callback: uriCallbacks)
					{
						callback.handle(fragment);
					}
				}
			}
		});

		activityQueuesPane = new ActivityQueuesPane(this);
		initViewController();
		activityQueuesPane.refreshData();
		viewController.displayCurrentView();

		removeAllComponents();

		showHideButton0 = new Button();
		showHideButton0.setStyleName(BaseTheme.BUTTON_LINK);
		showHideButton0.setIcon(resourceCache.getImage("/img/guzik_1.png"));

		showHideButton1 = new Button(); 
		showHideButton1.setStyleName(BaseTheme.BUTTON_LINK);
		showHideButton1.setIcon(resourceCache.getImage("/img/guzik_2.png"));

		
		showHideButton2 = new Button(); 				
		showHideButton2.setStyleName(BaseTheme.BUTTON_LINK);
		showHideButton2.setIcon(resourceCache.getImage("/img/guzik_2.png"));
		final VerticalLayout leftPanel =
				verticalLayout(showHideButton1,new NewProcessExtendedPane(bpmSession, i18NSource, this),activityQueuesPane,new ActivityFiltersPane(this),
						showHideButton2);

		leftPanelTrigger = new LeftPanelVisibilityTrigger(leftPanel, true);
		leftPanel.setWidth("300px");
		showHideButton0.addListener(leftPanelTrigger);
		showHideButton1.addListener(leftPanelTrigger);
		showHideButton2.addListener(leftPanelTrigger);

		ComponentContainer viewContainer = viewController.getViewContainer();

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth(100,Sizeable.UNITS_PERCENTAGE);
		horizontalLayout.addComponent(leftPanel);
		horizontalLayout.addComponent(viewContainer);
		horizontalLayout.setExpandRatio(viewContainer,1.0f);
		addComponent(showHideButton0);
		addComponent(horizontalLayout);
		addComponent(uriFragmentUtility);
	}

	private void initViewController()
	{
		viewController =
				new BasicViewController(new ComponentPaneRenderer<MyProcessesListPane>(new MyProcessesListPane(this,
						i18NSource.getMessage("activity.assigned.tasks")))
				{
					@Override
					public Component render(Map<String,?> viewData)
					{
						ProcessInstanceFilter filter = (ProcessInstanceFilter)viewData.get("filter");
						if(filter != null)
						{
							pane.setTitle(filter.getName());
							pane.setFilter(filter);
						}
						if(leftPanelTrigger != null)
							leftPanelTrigger.show();
						return pane.init();
					}
				});

		viewController.addView(new ComponentPaneRenderer<ProcessDataViewComponent>(new ProcessDataViewComponent(application, i18NSource, viewController))
		{
			@Override
			public String getViewId()
			{
				return ProcessDataViewComponent.class.getName();
			}

			@Override
			public Component render(Map<String,?> viewData)
			{
				ProcessToolBpmSession bpmSession = (ProcessToolBpmSession)viewData.get("bpmSession");
				BpmTask task = (BpmTask)viewData.get("task");
				pane.attachProcessDataPane(task,bpmSession);
				return pane;
			}
		});

		viewController.addView(new ComponentPaneRenderer<OtherUserProcessesListPane>(new OtherUserProcessesListPane(this, i18NSource
				.getMessage("activity.user.tasks")))
		{
			@Override
			public Component render(Map<String,?> viewData)
			{
				ProcessInstanceFilter filter = (ProcessInstanceFilter)viewData.get("filter");
				if(filter != null)
				{
					pane.setTitle(filter.getName());
				}
				pane.setFilter(filter);
				pane.setUserData(filter.getFilterOwner());
				leftPanelTrigger.show();
				return pane.init();
			}
		});

		viewController.addView(new ComponentPaneRenderer<QueueListPane>(new QueueListPane(this))
		{
			@Override
			public Component render(Map<String,?> viewData)
			{
				ProcessQueue q = (ProcessQueue)viewData.get("queue");
				pane.setQueue(q);
				leftPanelTrigger.show();
				return pane.init();
			}
		});

		viewController.addView(new ComponentPaneRenderer<OtherUserQueueListPane>(new OtherUserQueueListPane(this))
		{
			@Override
			public Component render(Map<String,?> viewData)
			{
				ProcessQueue queue = (ProcessQueue)viewData.get("queue");
				UserData user = (UserData)viewData.get("user");
				pane.setUserData(user);
				pane.setQueue(queue);
				leftPanelTrigger.show();
				return pane.init();
			}
		});

		viewController.addView(new ComponentPaneRenderer<RecentProcessesListPane>(new RecentProcessesListPane(this, i18NSource
				.getMessage("activity.recent.tasks")))
		{
			@Override
			public Component render(Map<String,?> viewData)
			{
				Calendar minDate = (Calendar)viewData.get("minDate");
				pane.setMinDate(minDate);
				leftPanelTrigger.show();
				return pane.init();
			}
		});

		// to remove "strange" views, depending on external addons. Such
		// approach also gives us much greater flexibility
		ViewRegistry registeredService = ProcessToolContext.Util.getThreadProcessToolContext().getRegistry().getRegisteredService(ViewRegistry.class);
		if(registeredService != null)
		{
			for(final ViewRenderer viewRenderer: registeredService.getViews())
			{
				viewRenderer.setUp(application);
				viewRenderer.setViewCallback(this);
				viewController.addView(viewRenderer);
				activityQueuesPane.addButton(viewRenderer.getTitle(),new Runnable()
				{
					@Override
					public void run()
					{
						viewRenderer.setBpmSession(getBpmSession());
						viewRenderer.handleDisplayAction();
						viewController.displayView(viewRenderer.getViewId(),null);
					}
				});
			}
		}

	}

	public void addUriCallback(UriChangedCallback callback)
	{
		uriCallbacks.add(callback);
	}

	@Override
	public Application getApplication()
	{
		return application;
	}

	public I18NSource getI18NSource()
	{
		return i18NSource;
	}

	public ProcessToolBpmSession getBpmSession()
	{
		return bpmSession;
	}

	public void displayMyTasksPane()
	{
		confirmTaskClosing(new EventHandler()
		{
			@Override
			public void onEvent()
			{
				setShowExitWarning(application,false);
				VaadinUtility.unregisterClosingWarning(application.getMainWindow());
				viewController.displayView(MyProcessesListPane.class);
			}
		});
	}

	public void displayFilterPane(final ProcessInstanceFilter filter)
	{
		confirmTaskClosing(new EventHandler()
		{
			@Override
			public void onEvent()
			{
				setShowExitWarning(application,false);
				VaadinUtility.unregisterClosingWarning(application.getMainWindow());
				viewController.displayView(MyProcessesListPane.class,Collections.singletonMap("filter",filter));
			}
		});
	}

	public void displayOtherUserTasksPane(final ProcessInstanceFilter filter)
	{
		confirmTaskClosing(new EventHandler()
		{
			@Override
			public void onEvent()
			{
				setShowExitWarning(application,false);
				VaadinUtility.unregisterClosingWarning(application.getMainWindow());
				viewController.displayView(OtherUserProcessesListPane.class,Collections.singletonMap("filter",filter));
			}
		});
	}

	public void displayQueue(final ProcessQueue q)
	{
		confirmTaskClosing(new EventHandler()
		{
			@Override
			public void onEvent()
			{
				setShowExitWarning(application,false);
				VaadinUtility.unregisterClosingWarning(application.getMainWindow());
				viewController.displayView(QueueListPane.class,Collections.singletonMap("queue",q));
			}
		});
	}

	public void displayOtherUserQueue(final ProcessQueue q, final UserData user)
	{
		confirmTaskClosing(new EventHandler()
		{
			@Override
			public void onEvent()
			{
				setShowExitWarning(application,false);
				VaadinUtility.unregisterClosingWarning(application.getMainWindow());
				viewController.displayView(OtherUserQueueListPane.class,new HashMap<String,Object>()
				{
					{
						put("queue",q);
						put("user",user);
					}
				});
			}
		});
	}

	public void displayRecentTasksPane(final Calendar minDate)
	{
		confirmTaskClosing(new EventHandler()
		{
			@Override
			public void onEvent()
			{
				setShowExitWarning(application,false);
				VaadinUtility.unregisterClosingWarning(application.getMainWindow());
				viewController.displayView(RecentProcessesListPane.class,Collections.singletonMap("minDate",minDate));
			}
		});
	}


	public void displayProcessData(BpmTask task)
	{
		displayProcessData(task,null);
	}

	public void displayProcessData(final BpmTask task, final ProcessToolBpmSession bpmSession)
	{
		displayProcessData(task,bpmSession,false);
	}

	public void displayProcessData(BpmTask task, boolean forward)
	{
		displayProcessData(task,null,forward);
	}

	public void displayProcessData(final BpmTask task, final ProcessToolBpmSession bpmSession, boolean forward)
	{
		displayProcessDataInPane(task,bpmSession,forward);
	}

	private void confirmTaskClosing(EventHandler eventHandler)
	{
		BpmTask task;
		final ProcessToolContext processToolContextFromThread = ProcessToolContext.Util.getThreadProcessToolContext();
		if(viewController.getCurrentViewId() != null && viewController.getCurrentViewId().equals(ProcessDataViewComponent.class.getName())
				&& (task = (BpmTask)viewController.getCurrentViewData().get("task")) != null
				&& getBpmSession().isProcessRunning(task.getProcessInstance().getInternalId(),processToolContextFromThread))
		{
			// show confirmation if there already is an open process
			VaadinUtility.displayConfirmationWindow(application,getI18NSource(),i18NSource.getMessage("activity.close.process.confirmation.title"),
					i18NSource.getMessage("activity.close.process.confirmation.question"),eventHandler,null,"activity.close.process.confirmation.ok",
					"activity.close.process.confirmation.cancel");
		}
		else
		{
			eventHandler.onEvent();
		}
	}

	private void displayProcessDataInPane(final BpmTask task, final ProcessToolBpmSession bpmSession, boolean forward)
	{
		viewController.displayView(ProcessDataViewComponent.class,new HashMap<String,Object>()
		{
			{
				put("bpmSession",nvl(bpmSession,ActivityMainPane.this.bpmSession));
				put("task",task);
			}
		},forward);
	}

	public void reloadCurrentViewData()
	{
		viewController.refreshCurrentView();
	}

	public final class LeftPanelVisibilityTrigger implements ClickListener
	{
		private final VerticalLayout leftPanel;
		private boolean leftPanelVisible = true;

		public LeftPanelVisibilityTrigger(VerticalLayout leftPanel, boolean leftPanelVisible)
		{
			this.leftPanel = leftPanel;
			this.leftPanelVisible = leftPanelVisible;
			trigger(leftPanelVisible);
		}

		@Override
		public void buttonClick(ClickEvent event)
		{
			trigger(!leftPanelVisible);
		}

		public void trigger(boolean showPanel)
		{
			if(showPanel)
			{
				show();
			}
			else
			{
				hide();
			}
		}

		public void hide()
		{
			leftPanel.setVisible(false);
			leftPanelVisible = false;
			showHideButton0.setVisible(true);
		}

		public void show()
		{
			leftPanel.setVisible(true);
			leftPanelVisible = true;
			showHideButton0.setVisible(false);
		}
	}

	private void setShowExitWarning(Application application, boolean show)
	{
		if(application instanceof GenericVaadinPortlet2BpmApplication)
			((GenericVaadinPortlet2BpmApplication)application).setShowExitWarning(show);
	}

	public void displayTaskById(String taskId)
	{
		BpmTask task = bpmSession.getTaskData(taskId,ProcessToolContext.Util.getThreadProcessToolContext());
		if(task != null)
		{
			displayProcessData(task);
		}
		else
		{
			application.getMainWindow().showNotification(i18NSource.getMessage("process.data.task-notfound").replaceFirst("%s",taskId));
		}
	}
}

package pl.net.bluesoft.rnd.processtool.ui.activity;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aperteworkflow.util.liferay.LiferayBridge;
import org.aperteworkflow.util.vaadin.VaadinUtility;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.TaskState;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.util.eventbus.EventListener;
import pl.net.bluesoft.util.lang.DateUtil;
import pl.net.bluesoft.util.lang.cquery.func.F;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ChameleonTheme;

public class ActivityQueuesPane extends Panel implements VaadinUtility.Refreshable
{

	private ActivityMainPane activityMainPane;
	private VerticalLayout taskList;
	private EventListener<BpmEvent> bpmEventListener = null;
	private Tree substitutionsTree;
	private Panel substitutionsPanel;

	private Collection<Button> taskButtons = new ArrayList<Button>();

	public ActivityQueuesPane(ActivityMainPane activityMainPane)
	{
		this.activityMainPane = activityMainPane;
		setWidth("100%");
		setCaption(getMessage("activity.queues.title"));
		addComponent(horizontalLayout(new Label(getMessage("activity.queues.help.short"), Label.CONTENT_XHTML),
				refreshIcon(activityMainPane.getApplication(),this)));
		taskList = new VerticalLayout();
		addComponent(taskList);

		if(bpmEventListener == null)
		{
			activityMainPane.getBpmSession().getEventBusManager().subscribe(BpmEvent.class,bpmEventListener = new EventListener<BpmEvent>()
			{
				@Override
				public void onEvent(BpmEvent e)
				{
					if(ActivityQueuesPane.this.isVisible() && ActivityQueuesPane.this.getApplication() != null)
					{
						refreshData();
					}
				}
			});
		}
	}

	@Override
	public void refreshData()
	{
		taskList.removeAllComponents();

		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		final ProcessToolBpmSession bpmSession = activityMainPane.getBpmSession();
		UserData user = bpmSession.getUser(ctx);

		buildMainTasksViews(ctx,bpmSession,user);

		final List<ProcessQueue> userAvailableQueues = buildUserQueues(ctx,bpmSession);

		List<UserData> substitutedUsers = getSubstitutedUsers(user,new Date());

		if(!substitutedUsers.isEmpty())
		{
			Map<UserData,ProcessToolBpmSession> substitutedUserToSession = from(substitutedUsers).mapTo(new F<UserData,ProcessToolBpmSession>()
			{
				@Override
				public ProcessToolBpmSession invoke(UserData substitutedUser)
				{
					return bpmSession.createSession(substitutedUser,substitutedUser.getRoleNames(),ProcessToolContext.Util.getThreadProcessToolContext());
				}
			});

			substitutionsPanel = new Panel(getMessage("activity.substitutions"));
			substitutionsPanel.setStyleName(ChameleonTheme.PANEL_LIGHT);
			VerticalLayout vl = VaadinUtility.verticalLayout(substitutionsPanel);
			vl.setMargin(true,false,false,false);
			taskList.addComponent(vl);

			buildSubstitutedTasksViews(ctx,substitutedUsers,substitutedUserToSession,userAvailableQueues);
		}
	}

	private void buildMainTasksViews(ProcessToolContext ctx, final ProcessToolBpmSession bpmSession, UserData user)
	{
		ProcessInstanceFilter assignedTasksFromOthers = getProcessInstanceFilter(user,null,user,getMessage("activity.assigned.tasks"),TaskState.OPEN);
		taskList.addComponent(createUserTasksButton(bpmSession,ctx,assignedTasksFromOthers,true));
		ProcessInstanceFilter assignedTasksByMyself =
				getProcessInstanceFilter(user,user,user,getMessage("activity.created.assigned.tasks"),TaskState.OPEN);
		taskList.addComponent(createUserTasksButton(bpmSession,ctx,assignedTasksByMyself,true));
		ProcessInstanceFilter myTasksBeingDoneByOthers = getProcessInstanceFilter(user,user,null,getMessage("activity.created.tasks"),TaskState.OPEN);
		taskList.addComponent(createUserTasksButton(bpmSession,ctx,myTasksBeingDoneByOthers,true));
		ProcessInstanceFilter myTasksClosed = getProcessInstanceFilter(user,user,null,getMessage("activity.created.closed.tasks"),TaskState.CLOSED);
		taskList.addComponent(createUserTasksButton(bpmSession,ctx,myTasksClosed,false));

		for(Button taskButton: taskButtons)
		{
			taskList.addComponent(taskButton);
		}
	}

	public void addButton(String title, final Runnable r)
	{
		Button b = new Button(title);
		b.setStyleName(BaseTheme.BUTTON_LINK);
		b.addListener(new Button.ClickListener()
		{
			@Override
			public void buttonClick(Button.ClickEvent event)
			{
				withErrorHandling(getApplication(),r);
			}
		});
		taskButtons.add(b);
		taskList.addComponent(b);
	}

	private List<ProcessQueue> buildUserQueues(ProcessToolContext ctx, final ProcessToolBpmSession bpmSession)
	{
		final List<ProcessQueue> userAvailableQueues = new ArrayList<ProcessQueue>(bpmSession.getUserAvailableQueues(ctx));

		Collections.sort(userAvailableQueues,new Comparator<ProcessQueue>()
		{
			@Override
			public int compare(ProcessQueue o1, ProcessQueue o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});

		for(ProcessQueue q: userAvailableQueues)
		{
			taskList.addComponent(createQueueButton(q,bpmSession,null));
		}
		return userAvailableQueues;
	}

	private int buildSubstitutedTasks(ProcessToolContext ctx, final ProcessToolBpmSession bpmSessionForSubstituted, UserData user,
			HierarchicalContainer container, final ProcessInstanceFilter parent)
	{
		Collection<ProcessInstanceFilter> taskFilters = new ArrayList<ProcessInstanceFilter>();
		
		taskFilters.add(getProcessInstanceFilter(user,null,user,getMessage("activity.subst.assigned.tasks"),TaskState.OPEN));
		taskFilters.add(getProcessInstanceFilter(user,user,user,getMessage("activity.subst.created.assigned.tasks"),TaskState.OPEN));
		taskFilters.add(getProcessInstanceFilter(user,user,null,getMessage("activity.subst.created.tasks"),TaskState.OPEN));
		taskFilters.add(getProcessInstanceFilter(user,user,null,getMessage("activity.subst.created.closed.tasks"),TaskState.CLOSED));
		

		int total = 0;

		for(ProcessInstanceFilter filter: taskFilters)
		{
			container.addItem(filter);
			if(filter.getOwners().contains(user) && !filter.getStates().contains(TaskState.CLOSED))
			{
				ResultsPageWrapper<BpmTask> tasks = bpmSessionForSubstituted.findProcessTasks(filter,0,0,ctx);
				total += tasks.getTotal();

				container.getItem(filter).getItemProperty("name").setValue(filter.getName() + " (" + tasks.getTotal() + ")");
				container.getItem(filter).getItemProperty("enabled").setValue(tasks.getTotal() > 0);
			}
			else
			{
				container.getItem(filter).getItemProperty("name").setValue(filter.getName());
			}
			container.setParent(filter,parent);
			container.setChildrenAllowed(filter,false);
		}
		
		return total;
	}

	private void buildSubstitutedTasksViews(ProcessToolContext ctx, List<UserData> substitutedUsers,
			Map<UserData,ProcessToolBpmSession> substitutedUserToSession, List<ProcessQueue> userAvailableQueues)
	{
		final HierarchicalContainer container = new HierarchicalContainer();
		
		container.addContainerProperty("name",String.class,"");
		container.addContainerProperty("description",String.class,null);
		container.addContainerProperty("queueUserSession",QueueUserSession.class,null);
		container.addContainerProperty("enabled",Boolean.class,Boolean.TRUE);

		for(UserData substitutedUser: substitutedUsers)
		{
			ProcessToolBpmSession bpmSessionForSubstituted = substitutedUserToSession.get(substitutedUser);
			UserData liferayUser = ctx.getUserDataDAO().loadUserByLogin(substitutedUser.getLogin());
			liferayUser.getRoleNames().addAll(substitutedUser.getRoleNames());

			ProcessInstanceFilter substAssignedTasks =
					getProcessInstanceFilter(substitutedUser,null,liferayUser,getMessage("activity.other.users.tasks",liferayUser.getRealName()),TaskState.OPEN);
			
			substAssignedTasks.getNotCreators().clear();

			container.addItem(substAssignedTasks);

			int total = buildSubstitutedTasks(ctx,bpmSessionForSubstituted,liferayUser,container,substAssignedTasks);
			int totalQueues = buildSubstitutedQueues(ctx,userAvailableQueues,bpmSessionForSubstituted,substitutedUser,container,substAssignedTasks);

			container.getItem(substAssignedTasks).getItemProperty("name").setValue(substAssignedTasks.getName() + " (" + total + ";" + totalQueues + ")");
			container.getItem(substAssignedTasks).getItemProperty("description")
					.setValue(getMessage("activity.substitutions.description",liferayUser.getRealName(),total,totalQueues));

		}

		final Tree substitutionsTree = getSubstitutionsTree();
		substitutionsTree.setContainerDataSource(container);
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(false);
		vl.setSpacing(false);
		vl.addComponent(substitutionsTree);
		substitutionsPanel.setContent(vl);
	}

	private int buildSubstitutedQueues(ProcessToolContext ctx, final List<ProcessQueue> userAvailableQueues, ProcessToolBpmSession bpmSessionForSubstituted,
			UserData user, HierarchicalContainer container, ProcessInstanceFilter parent)
	{
		Map<String,List<QueueUserSession>> map = new HashMap<String,List<QueueUserSession>>();
		for(ProcessQueue q: bpmSessionForSubstituted.getUserAvailableQueues(ctx))
		{
			if(!map.containsKey(q.getName()))
			{
				map.put(q.getName(),new ArrayList<QueueUserSession>());
			}
			QueueUserSession qus = new QueueUserSession();
			qus.queue = q;
			qus.user = user;
			qus.bpmSession = bpmSessionForSubstituted;
			map.get(q.getName()).add(qus);
		}

		Collection<String> substitutedUserQueueNames = from(map.keySet()).except(from(userAvailableQueues).select(new F<ProcessQueue,String>()
		{
			@Override
			public String invoke(ProcessQueue q)
			{
				return q.getName();
			}
		})).ordered();

		int total = 0;

		for(String queueName: substitutedUserQueueNames)
		{
			QueueUserSession qus = map.get(queueName).get(0);
			container.addItem(qus);

			long count = qus.queue.getProcessCount();

			container.getItem(qus).getItemProperty("name").setValue(qus.queue.getDescription() + " (" + count + ")");
			container.getItem(qus).getItemProperty("enabled").setValue(count > 0);
			container.getItem(qus).getItemProperty("description").setValue(qus.queue.getDescription());
			container.getItem(qus).getItemProperty("queueUserSession").setValue(qus);

			container.setParent(qus,parent);
			container.setChildrenAllowed(qus,false);
			total += count;
		}

		return total;
	}

	private Tree getSubstitutionsTree()
	{
		if(substitutionsTree != null)
		{
			return substitutionsTree;
		}
		substitutionsTree = new Tree();
		substitutionsTree.setItemCaptionPropertyId("name");
		substitutionsTree.setSelectable(false);
		substitutionsTree.setItemStyleGenerator(new ItemStyleGenerator()
		{
			@Override
			public String getStyle(Object itemId)
			{
				if(substitutionsTree.hasChildren(itemId))
					return "";
				return ((Boolean)substitutionsTree.getItem(itemId).getItemProperty("enabled").getValue()) ? "link-enabled" : "link-disabled";
			}
		});
		substitutionsTree.addListener(new ItemClickListener()
		{
			@Override
			public void itemClick(ItemClickEvent event)
			{
				if(substitutionsTree.hasChildren(event.getItemId()))
				{
					if(substitutionsTree.isExpanded(event.getItemId()))
					{
						substitutionsTree.collapseItem(event.getItemId());
					}
					else
					{
						substitutionsTree.expandItem(event.getItemId());
					}
					substitutionsTree.unselect(event.getItemId());
				}
				else if(event.getItemId() instanceof ProcessInstanceFilter)
				{
					ProcessInstanceFilter filter = (ProcessInstanceFilter)event.getItemId();
					activityMainPane.displayOtherUserTasksPane(filter);
				}
				else if(event.getItemId() instanceof QueueUserSession)
				{
					QueueUserSession qus = (QueueUserSession)event.getItemId();
					queueClicked(qus.queue,qus.bpmSession,qus.user);
				}
			}
		});
		substitutionsTree.setItemDescriptionGenerator(new ItemDescriptionGenerator()
		{
			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId)
			{
				if(substitutionsTree.getItem(itemId).getItemProperty("description").getValue() == null)
					return null;
				return substitutionsTree.getItem(itemId).getItemProperty("description").getValue().toString();
			}
		});
		return substitutionsTree;
	}

	private String getMessage(String key, Object... objects)
	{
		return MessageFormat.format(getMessage(key),objects);
	}

	private static class QueueUserSession
	{
		ProcessQueue queue;
		ProcessToolBpmSession bpmSession;
		UserData user;
	}

	private Button createUserTasksButton(final ProcessToolBpmSession bpmSession, final ProcessToolContext ctx,
			final ProcessInstanceFilter processInstanceFilter, final boolean showCounter)
	{

		final Button b = new Button(processInstanceFilter.getName());
		b.setStyleName(BaseTheme.BUTTON_LINK);
		if(showCounter)
		{
			ResultsPageWrapper<BpmTask> tasks = bpmSession.findProcessTasks(processInstanceFilter,0,0,ctx);
			b.setCaption(b.getCaption() + " (" + tasks.getTotal() + ")");
			b.setEnabled(tasks.getTotal() > 0);
		}

		b.addListener(new Button.ClickListener()
		{
			@Override
			public void buttonClick(Button.ClickEvent event)
			{
				withErrorHandling(getApplication(),new Runnable()
				{
					@Override
					public void run()
					{
						activityMainPane.displayFilterPane(processInstanceFilter);
					}
				});
			}
		});
		return b;
	}

	private ProcessInstanceFilter getProcessInstanceFilter(UserData user, UserData creator, UserData owner, String name, TaskState... states)
	{
		ProcessInstanceFilter pif = new ProcessInstanceFilter();
		pif.setFilterOwner(user);
		pif.setName(name);
		pif.getStates().addAll(Arrays.asList(states));

		if(creator != null)
			pif.getCreators().add(creator);
		else
			pif.getNotCreators().add(owner);

		if(owner != null)
			pif.getOwners().add(owner);
		else if(!pif.getStates().contains(TaskState.CLOSED))
			pif.getNotOwners().add(creator);

		return pif;
	}

	private Button createRecentTasksButton(ProcessToolContext ctx)
	{
		final Calendar minDate = Calendar.getInstance();
		minDate.add(Calendar.DAY_OF_YEAR,-5);
		Integer recentProcessesSize = activityMainPane.getBpmSession().getRecentTasksCount(minDate,ctx);
		Button b = new Button(getMessage("activity.recent.tasks"));// + " (" +
																	// recentProcessesSize
																	// + ")");
		b.setStyleName(BaseTheme.BUTTON_LINK);
		b.setEnabled(recentProcessesSize > 0);
		b.addListener(new Button.ClickListener()
		{
			@Override
			public void buttonClick(Button.ClickEvent event)
			{
				withErrorHandling(getApplication(),new Runnable()
				{
					@Override
					public void run()
					{
						activityMainPane.displayRecentTasksPane(minDate);
					}
				});
			}
		});
		return b;
	}

	private Button createQueueButton(final ProcessQueue q, final ProcessToolBpmSession bpmSession, final UserData user)
	{
		long processCount = q.getProcessCount();
		String desc = getMessage(q.getDescription());
		Button qb = new Button(desc + " (" + processCount + ")");
		qb.setDescription(desc);
		qb.setStyleName(BaseTheme.BUTTON_LINK);
		qb.setEnabled(processCount > 0);
		qb.addListener(new Button.ClickListener()
		{
			@Override
			public void buttonClick(final Button.ClickEvent event)
			{
				queueClicked(q,bpmSession,user);
			}
		});
		return qb;
	}

	private static List<UserData> getSubstitutedUsers(UserData user, Date date)
	{
		List<UserData> substitutedUsers =
				ProcessToolContext.Util.getThreadProcessToolContext().getUserSubstitutionDAO().getSubstitutedUsers(user,DateUtil.truncHours(date));
		return from(substitutedUsers).select(new F<UserData,UserData>()
		{
			@Override
			public UserData invoke(UserData user)
			{
				return LiferayBridge.getLiferayUser(user.getLogin(),user.getCompanyId());
			}
		}).orderBy(new F<UserData,String>()
		{
			@Override
			public String invoke(UserData user)
			{
				return user.getRealName() != null ? user.getRealName().toLowerCase() : null;
			}
		}).toList();
	}

	private String getMessage(String title)
	{
		return activityMainPane.getI18NSource().getMessage(title);
	}

	private void queueClicked(final ProcessQueue q, final ProcessToolBpmSession bpmSession, final UserData user)
	{
		withErrorHandling(getApplication(),new Runnable()
		{
			@Override
			public void run()
			{
				if(q.isBrowsable())
				{
					if(user == null)
					{
						activityMainPane.displayQueue(q);
					}
					else
					{
						activityMainPane.displayOtherUserQueue(q,user);
					}
				}
				else
				{
					ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
					BpmTask task = bpmSession.assignTaskFromQueue(q,ctx);
					if(task != null)
					{
						getWindow().executeJavaScript("Liferay.trigger('processtool.bpm.assignProcess', '" + task.getProcessInstance().getInternalId() + "');");
						getWindow().executeJavaScript("vaadin.forceSync();");
						activityMainPane.displayProcessData(task,user == null ? null : bpmSession);
					}
					else
					{
						activityMainPane.reloadCurrentViewData();
					}
				}
			}
		});
	}

}

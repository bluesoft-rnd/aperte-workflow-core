package pl.net.bluesoft.rnd.processtool.ui.activity;

import static org.aperteworkflow.util.vaadin.VaadinExceptionHandler.Util.withErrorHandling;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;
import static pl.net.bluesoft.util.lang.Strings.hasText;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aperteworkflow.ui.view.ViewEvent;
import org.aperteworkflow.util.vaadin.VaadinUtility;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.filters.factory.ProcessInstanceFilterFactory;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.ui.utils.QueuesPanelRefresherUtil;
import pl.net.bluesoft.util.eventbus.EventListener;
import pl.net.bluesoft.util.lang.DateUtil;
import pl.net.bluesoft.util.lang.TaskWatch;
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

/**
 * Panel providing link buttons to view specific tasks
 * 
 * @author tlipski, mpawlak@bluesoft.net.pl
 *
 */
public class ActivityQueuesPane extends Panel implements VaadinUtility.Refreshable
{
    private static final Logger logger = Logger.getLogger(ActivityQueuesPane.class.getName());
    private static final String USER_QUEUE_PREFIX = "user.queue.name.";

	private ActivityMainPane activityMainPane;
	private VerticalLayout taskList;
	private Tree substitutionsTree;
	private Panel substitutionsPanel;
	private TaskWatch watch;
	private Collection<Button> taskButtons = new ArrayList<Button>();
	
	/** Filter factory */
	private ProcessInstanceFilterFactory filterFactory;

	private boolean refreshRequested; 

	protected boolean onEvent = false;

	public ActivityQueuesPane(ActivityMainPane activityMainPane)
	{
		this.activityMainPane = activityMainPane;
		
		filterFactory = new ProcessInstanceFilterFactory();
		
		setWidth("100%");
		setCaption(getMessage("activity.queues.title"));
		addComponent(horizontalLayout(new Label(getMessage("activity.queues.help.short"), Label.CONTENT_XHTML),
				refreshIcon(activityMainPane.getApplication(),this)));
		taskList = new VerticalLayout();
		addComponent(taskList);

		//listen for BPM events - they usually mean there can be something changed in processes list
		activityMainPane.getBpmSession().getEventBusManager().subscribe(BpmEvent.class, new EventListener<BpmEvent>(){
			@Override
			public void onEvent(BpmEvent e){
				if(ActivityQueuesPane.this.isVisible() && ActivityQueuesPane.this.getApplication() != null){
					synchronized (this) {
						refreshRequested = true;
					}
				}
			}
		});
		//listen for ViewEvent ACTION_COMPLETE - it means BPM processing is done, so if there were some BPM events before, now is the time to refresh data.

		activityMainPane.getBpmSession().getEventBusManager().subscribe(ViewEvent.class, new EventListener<ViewEvent>(){
			@Override
			public void onEvent(ViewEvent e) {
				if(ActivityQueuesPane.this.isVisible() && ActivityQueuesPane.this.getApplication() != null && e.getEventType().equals(ViewEvent.Type.ACTION_COMPLETE)){
					synchronized(this) {
						if(refreshRequested) {
							refreshRequested = false;
							onEvent = true;
							refreshData();
							onEvent = false;
						}
					}
				}
			}
		});
	}

	@Override
	public void refreshData()
	{	
		try 
		{
			watch = new TaskWatch(ActivityQueuesPane.class.getSimpleName() + " - lista kolejek " + (onEvent ? " refresh ON_EVENT" : ""));
			watch.watchTask("Total refreshing data", new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					internalRefreshData();
					return null;
				}
			});
			watch.stopAll();
			logger.log(Level.INFO, watch.printSummary());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Refreshing data", e);
			throw new RuntimeException(e);
		}
	}

	public void internalRefreshData() 
	{
		taskList.removeAllComponents();

		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		final ProcessToolBpmSession bpmSession = activityMainPane.getBpmSession();
		UserData user = bpmSession.getUser(ctx);

		buildMainTasksViews(ctx,bpmSession,user);

		final List<ProcessQueue> userAvailableQueues = buildUserQueues(ctx,bpmSession);

		List<UserData> substitutedUsers =
				ProcessToolContext.Util.getThreadProcessToolContext().getUserSubstitutionDAO().getSubstitutedUsers(user,DateUtil.truncHours(new Date()));
		
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

	/** Build main task view, containing buttons to select assigned to current employee task, closed 
	 * task and task created by current employee but assigned to others
	 */
	private void buildMainTasksViews(ProcessToolContext ctx, final ProcessToolBpmSession bpmSession, UserData user)
	{
		/* Create filters for specific task list */
//		ProcessInstanceFilter assignedTasksFromOthers = filterFactory.createOthersTaskAssignedToMeFilter(user);
//		ProcessInstanceFilter assignedTasksByMyself = filterFactory.createMyTasksAssignedToMeFilter(user);
		ProcessInstanceFilter tasksAssignedToMe = filterFactory.createTaskAssignedToMeFilter(user);
		ProcessInstanceFilter myTasksBeingDoneByOthers = filterFactory.createMyTaskDoneByOthersFilter(user);
		ProcessInstanceFilter myTasksClosed = filterFactory.createMyClosedTasksFilter(user);
				
//		taskList.addComponent(createUserTasksButton(bpmSession,ctx,assignedTasksFromOthers,true));
//		taskList.addComponent(createUserTasksButton(bpmSession,ctx,assignedTasksByMyself,true));
		taskList.addComponent(createUserTasksButton(bpmSession,ctx,tasksAssignedToMe,true));
		taskList.addComponent(createUserTasksButton(bpmSession,ctx,myTasksBeingDoneByOthers,true));
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

	private int buildSubstitutedTasks(final ProcessToolContext ctx, final ProcessToolBpmSession bpmSessionForSubstituted, UserData substitutedUser,
			HierarchicalContainer container, final ProcessInstanceFilter parent)
	{
		Collection<ProcessInstanceFilter> taskFilters = new ArrayList<ProcessInstanceFilter>();
		
//		taskFilters.add(filterFactory.createSubstitutedOthersTaskAssignedToMeFilter(substitutedUser));
//		taskFilters.add(filterFactory.createSubstitutedTasksAssignedToMeFilter(substitutedUser));
		taskFilters.add(filterFactory.createTasksAssignedToSubstitutedUserFilter(substitutedUser));
		taskFilters.add(filterFactory.createSubstitutedTaskDoneByOthersFilter(substitutedUser));
		taskFilters.add(filterFactory.createSubstitutedClosedTasksFilter(substitutedUser));
		
		int total = 0;

		for(final ProcessInstanceFilter filter: taskFilters)
		{
			container.addItem(filter);
			if(filter.getOwners().contains(substitutedUser) && !filter.getQueueTypes().contains(QueueType.OWN_FINISHED))
			{
				int totalTasks = activityMainPane.getBpmSession().getTasksCount(ctx, filter.getFilterOwner().getLogin(), filter.getQueueTypes());
				
				total += totalTasks;
				
				/* button id for the refresher */
				String buttonId = QueuesPanelRefresherUtil.getSubstitutedQueueTaskId(filter.getName(), substitutedUser.getLogin());
				
				//String taskName =  MessageFormat.format(getMessage("activity.other.users.tasks"), user.getRealName());

				container.getItem(filter).getItemProperty("name").setValue(getMessage(filter.getName()) + " (" + totalTasks + ")");
				container.getItem(filter).getItemProperty("enabled").setValue(totalTasks > 0);
				container.getItem(filter).getItemProperty("debugId").setValue(buttonId);
			}
			else
			{
				container.getItem(filter).getItemProperty("name").setValue(getMessage(filter.getName()));
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
		container.addContainerProperty("debugId",String.class,null);
		
		for(UserData substitutedUser: substitutedUsers)
		{
			ProcessToolBpmSession bpmSessionForSubstituted = substitutedUserToSession.get(substitutedUser);
			UserData liferaySubstitutedUser = ctx.getUserDataDAO().loadUserByLogin(substitutedUser.getLogin());
			liferaySubstitutedUser.getRoleNames().addAll(substitutedUser.getRoleNames());

			ProcessInstanceFilter substAssignedTasks = filterFactory.createOtherUserTaskForSubstitutedUser(liferaySubstitutedUser);

			container.addItem(substAssignedTasks);

			int total = buildSubstitutedTasks(ctx,bpmSessionForSubstituted,liferaySubstitutedUser,container,substAssignedTasks);
			int totalQueues = buildSubstitutedQueues(ctx,userAvailableQueues,bpmSessionForSubstituted,substitutedUser,container,substAssignedTasks);

			container.getItem(substAssignedTasks).getItemProperty("name").setValue(getMessage(substAssignedTasks.getName(), liferaySubstitutedUser.getRealName()) + " (" + total + ";" + totalQueues + ")");
			container.getItem(substAssignedTasks).getItemProperty("description")
					.setValue(getMessage("activity.substitutions.description",liferaySubstitutedUser.getRealName(),total,totalQueues));
			
			/* button id for the refresher */
			String buttonId = QueuesPanelRefresherUtil.getSubstitutedRootNodeId(substitutedUser.getLogin());
			
			container.getItem(substAssignedTasks).getItemProperty("debugId").setValue(buttonId);
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
			
			/* button id for the refresher */
			String buttonId = QueuesPanelRefresherUtil.getSubstitutedQueueProcessQueueId(qus.queue.getName(), user.getLogin());
			
			String desc = getQueueDescr(qus.queue);

			container.getItem(qus).getItemProperty("name").setValue(desc + " (" + count + ")");
			container.getItem(qus).getItemProperty("enabled").setValue(count > 0);
			container.getItem(qus).getItemProperty("description").setValue(desc);
			container.getItem(qus).getItemProperty("queueUserSession").setValue(qus);
			container.getItem(qus).getItemProperty("debugId").setValue(buttonId);

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
					return substitutionsTree.getItem(itemId).getItemProperty("debugId").toString();
				
				String itemClass = ((Boolean)substitutionsTree.getItem(itemId).getItemProperty("enabled").getValue()) ? "link-enabled" : "link-disabled";
				itemClass += " " + substitutionsTree.getItem(itemId).getItemProperty("debugId");
				
				return itemClass;
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
		return internalCreateUserTasksButton(bpmSession, ctx, processInstanceFilter, showCounter);

	}

	public Button internalCreateUserTasksButton(
			final ProcessToolBpmSession bpmSession,
			final ProcessToolContext ctx,
			final ProcessInstanceFilter processInstanceFilter,
			final boolean showCounter) {
		final Button b = new Button(getMessage(processInstanceFilter.getName()));
		
		/* button id for the refresher */
		String buttonId = QueuesPanelRefresherUtil.getQueueTaskId(processInstanceFilter.getName());
		
		b.setStyleName(BaseTheme.BUTTON_LINK);
		b.setDebugId(buttonId);
		b.addStyleName(" "+buttonId);
		
		if(showCounter)
		{
			int taskCount = bpmSession.getTasksCount(ctx, processInstanceFilter.getFilterOwner().getUser().getLogin(), processInstanceFilter.getQueueTypes());
			b.setCaption(b.getCaption() + " (" + taskCount + ")");
			
			String styleName = taskCount > 0 ? "v-enabled" : "v-disabled";
			b.addStyleName(styleName);
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
	


	private Button createQueueButton(final ProcessQueue q, final ProcessToolBpmSession bpmSession, final UserData user)
	{
		long processCount = q.getProcessCount();
		String desc = getQueueDescr(q);

		/* button id for the refresher */
		String buttonId = QueuesPanelRefresherUtil.getQueueProcessQueueId(q.getName());
		
		Button qb = new Button(desc + " (" + processCount + ")");
		qb.setDescription(desc);
		qb.setStyleName(BaseTheme.BUTTON_LINK);
		
		String styleName = processCount > 0 ? "v-enabled" : "v-disabled";
		qb.addStyleName(styleName);
		
		qb.setDebugId(buttonId);
		qb.addStyleName(" "+buttonId);
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

	private String getQueueDescr(ProcessQueue q) {
		String desc = getMessage(q.getDescription());
		/* The name of the queue */
		String queueName = getMessage(USER_QUEUE_PREFIX+q.getName());

		boolean nonblankDesc = hasText(desc);
		boolean nonblankQueueName = hasText(queueName);

		if (nonblankDesc && nonblankQueueName) {
			return desc + " " + queueName;
		}
		if (nonblankDesc) {
			return desc;
		}
		if (nonblankQueueName) {
			return queueName;
		}
		desc = getMessageNoBlank(q.getDescription());
		if (hasText(desc)) {
			return desc;
		}
		return q.getName();
	}

	private String getMessage(String title)
	{
		return activityMainPane.getI18NSource().getMessage(title, "");
	}

	private String getMessageNoBlank(String title)
	{
		return activityMainPane.getI18NSource().getMessage(title, title);
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

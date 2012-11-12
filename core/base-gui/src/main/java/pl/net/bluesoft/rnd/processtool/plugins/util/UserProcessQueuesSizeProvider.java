package pl.net.bluesoft.rnd.processtool.plugins.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.filters.factory.ProcessInstanceFilterFactory;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.utils.QueuesPanelRefresherUtil;
import pl.net.bluesoft.util.lang.DateUtil;

/**
 * User Process Queues size provider. For given user login it
 * returns map witch queue_id as key and size of those queues
 * as values
 * 
 * @author Maciej Pawlak
 *
 */
public class UserProcessQueuesSizeProvider 
{
	private Collection<UsersQueuesSize> usersQueuesSize;
	private String userLogin;
	private ProcessToolRegistry reg;
	private ProcessToolContext ctx;
	
	public UserProcessQueuesSizeProvider(ProcessToolRegistry reg, String userLogin) 
	{
		this.usersQueuesSize = new ArrayList<UsersQueuesSize>();
		this.reg = reg;
		this.userLogin = userLogin;
	}
	
	/** Map with queue id as key and its size as value */
	public Collection<UsersQueuesSize> getUserProcessQueueSize()
	{
		
		fillUserQueuesMap();
		
		return usersQueuesSize;
	}
	
	/** Initialize new session and context for database access */
	private void fillUserQueuesMap()
	{
		reg.getProcessToolContextFactory().withProcessToolContext(new ProcessToolContextCallback() 
		{
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				ProcessToolContext.Util.setThreadProcessToolContext(ctx);
				
				UserData userData = reg.getUserDataDAO(ctx.getHibernateSession()).loadUserByLogin(userLogin);
				UserProcessQueuesSizeProvider.this.ctx = ctx;
				
				/* Fill queues for main user */
				ProcessToolBpmSession mainUserSession = ctx.getProcessToolSessionFactory().createSession(userData, userData.getRoleNames());
				
				fillUserQueues(mainUserSession);
				
				/* Fill queues for substitutedUsers */
				List<UserData> substitutedUsers =
						ProcessToolContext.Util.getThreadProcessToolContext().getUserSubstitutionDAO().getSubstitutedUsers(userData,DateUtil.truncHours(new Date()));
				
				for(UserData substitutedUser: substitutedUsers)
				{
					ProcessToolBpmSession substitutedUserSession = ctx.getProcessToolSessionFactory().createSession(substitutedUser, substitutedUser.getRoleNames());
					fillSubstitutionUserQueues(substitutedUserSession);
				}
			}
			
		});
	}
	
	
	private void fillUserQueues(ProcessToolBpmSession bpmSession)
	{
		String currentUserLogin = bpmSession.getUserLogin();
		
		UserData user = reg.getUserDataDAO(ctx.getHibernateSession()).loadUserByLogin(currentUserLogin);
		
		ProcessInstanceFilterFactory filterFactory = new ProcessInstanceFilterFactory();
		Collection<ProcessInstanceFilter> queuesFilters = new ArrayList<ProcessInstanceFilter>();
		
		UsersQueuesSize userQueueSize = new UsersQueuesSize(currentUserLogin);
		
		/* Create organized tasks filters */
		queuesFilters.add(filterFactory.createTaskAssignedToMeFilter(user));
		queuesFilters.add(filterFactory.createMyTaskDoneByOthersFilter(user));
		
		for(ProcessInstanceFilter queueFilter: queuesFilters)
		{
			int filteredQueueSize = bpmSession.getTasksCount(ctx, queueFilter.getFilterOwner().getLogin(), queueFilter.getQueueTypes());
			//int filteredQueueSize = session.getFilteredTasksCount(queueFilter, ctx);
			
			String queueId = QueuesPanelRefresherUtil.getQueueTaskId(queueFilter.getName());
			userQueueSize.addProcessQueueSize(queueId, filteredQueueSize);
		}
		
		/* Add queues */
		List<ProcessQueue> userAvailableQueues = new ArrayList<ProcessQueue>(bpmSession.getUserAvailableQueues(ctx));
		for(ProcessQueue processQueue: userAvailableQueues)
		{
			Long processCount = processQueue.getProcessCount();
			
			String queueId = QueuesPanelRefresherUtil.getQueueProcessQueueId(processQueue.getName());
			userQueueSize.addProcessQueueSize(queueId, processCount.intValue());
		}
		
		usersQueuesSize.add(userQueueSize);
	}
	
	private void fillSubstitutionUserQueues(ProcessToolBpmSession bpmSession)
	{
		String currentUserLogin = bpmSession.getUserLogin();
		
		UserData user = reg.getUserDataDAO(ctx.getHibernateSession()).loadUserByLogin(currentUserLogin);
		
		ProcessInstanceFilterFactory filterFactory = new ProcessInstanceFilterFactory();
		Collection<ProcessInstanceFilter> queuesFilters = new ArrayList<ProcessInstanceFilter>();
		
		UsersQueuesSize userQueueSize = new UsersQueuesSize(currentUserLogin);
		
		queuesFilters.add(filterFactory.createTasksAssignedToSubstitutedUserFilter(user));
//		queuesFilters.add(filterFactory.createSubstitutedOthersTaskAssignedToMeFilter(user));
		
		
		for(ProcessInstanceFilter queueFilter: queuesFilters)
		{
			int filteredQueueSize = bpmSession.getTasksCount(ctx, queueFilter.getFilterOwner().getLogin(), queueFilter.getQueueTypes());
			//int filteredQueueSize = session.getFilteredTasksCount(queueFilter, ctx);
			
			String queueId = QueuesPanelRefresherUtil.getSubstitutedQueueTaskId(queueFilter.getName(), currentUserLogin);
			userQueueSize.addProcessQueueSize(queueId, filteredQueueSize);
		}
		
		List<ProcessQueue> userAvailableQueues = new ArrayList<ProcessQueue>(bpmSession.getUserAvailableQueues(ctx));
		for(ProcessQueue processQueue: userAvailableQueues)
		{
			Long processCount = processQueue.getProcessCount();
			
			String queueId = QueuesPanelRefresherUtil.getSubstitutedQueueProcessQueueId(processQueue.getName(), currentUserLogin);
			userQueueSize.addProcessQueueSize(queueId, processCount.intValue());
		}
		
		usersQueuesSize.add(userQueueSize);
	}

	public static class UsersQueuesSize 
	{
		private final String userLogin;
		private final Map<String, Integer> userProcessQueueSize;

		public UsersQueuesSize(String userLogin) 
		{
			this.userLogin = userLogin;
			this.userProcessQueueSize = new HashMap<String, Integer>();
		}
		
		public void addProcessQueueSize(String queueId, Integer size)
		{
			this.userProcessQueueSize.put(queueId, size);
		}

		public String getUserLogin() {
			return userLogin;
		}
		
		public Map<String, Integer> getUserProcessQueueSize() {
			return userProcessQueueSize;
		}

	}

	

}

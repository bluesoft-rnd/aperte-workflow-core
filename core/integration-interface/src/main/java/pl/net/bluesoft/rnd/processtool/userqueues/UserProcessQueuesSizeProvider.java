package pl.net.bluesoft.rnd.processtool.userqueues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.filters.factory.ProcessInstanceFilterFactory;
import pl.net.bluesoft.rnd.processtool.filters.factory.QueuesNameUtil;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.lang.DateUtil;

/**
 * User Process Queues size provider. For given user login it
 * returns map witch queue_id as key and size of those queues
 * as values
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class UserProcessQueuesSizeProvider 
{
	private Collection<UsersQueuesDTO> usersQueuesSize;
	private String userLogin;
	private ProcessToolRegistry reg;
	private ProcessToolContext ctx;
	
	public UserProcessQueuesSizeProvider(ProcessToolRegistry reg, String userLogin) 
	{
		this.usersQueuesSize = new ArrayList<UsersQueuesDTO>();
		this.reg = reg;
		this.userLogin = userLogin;
	}
	
	/** Map with queue id as key and its size as value */
	public Collection<UsersQueuesDTO> getUserProcessQueueSize()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		if(ctx == null)
		{
			reg.getProcessToolContextFactory().withProcessToolContext(new ProcessToolContextCallback() 
			{
				@Override
				public void withContext(ProcessToolContext ctx) 
				{
					fillUserQueuesMap(ctx);
				}
			});
		}
		else
		{
			fillUserQueuesMap(ctx);
		}
		
		return usersQueuesSize;
	}
	
	/** Initialize new session and context for database access */
	private void fillUserQueuesMap(ProcessToolContext ctx)
	{
		ProcessToolContext.Util.setThreadProcessToolContext(ctx);
		
		UserData userData = reg.getUserDataDAO(ctx.getHibernateSession()).loadUserByLogin(userLogin);

		/* prevent null pointers during restart when old client instance is open in browser */
		if (userData == null) {
			return;
		}

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
	
	
	private void fillUserQueues(ProcessToolBpmSession bpmSession)
	{
		String currentUserLogin = bpmSession.getUserLogin();
		
		UserData user = reg.getUserDataDAO(ctx.getHibernateSession()).loadUserByLogin(currentUserLogin);
		
		ProcessInstanceFilterFactory filterFactory = new ProcessInstanceFilterFactory();
		Collection<ProcessInstanceFilter> queuesFilters = new ArrayList<ProcessInstanceFilter>();
		
		UsersQueuesDTO userQueueSize = new UsersQueuesDTO(currentUserLogin);
		
		/* Create organized tasks filters */
		queuesFilters.add(filterFactory.createMyTasksAssignedToMeFilter(user));
		queuesFilters.add(filterFactory.createMyTaskDoneByOthersFilter(user));
		queuesFilters.add(filterFactory.createOthersTaskAssignedToMeFilter(user));
		queuesFilters.add(filterFactory.createMyClosedTasksFilter(user));
		
		for(ProcessInstanceFilter queueFilter: queuesFilters)
		{
			int filteredQueueSize = bpmSession.getTasksCount(ctx, queueFilter.getFilterOwner().getLogin(), queueFilter.getQueueTypes());
			//int filteredQueueSize = session.getFilteredTasksCount(queueFilter, ctx);
			
			String queueId = QueuesNameUtil.getQueueTaskId(queueFilter.getName());
			userQueueSize.addProcessListSize(queueFilter.getName(), queueId, filteredQueueSize);
		}
		
		/* Add queues */
		List<ProcessQueue> userAvailableQueues = new ArrayList<ProcessQueue>(bpmSession.getUserAvailableQueues(ctx));
		for(ProcessQueue processQueue: userAvailableQueues)
		{
			Long processCount = processQueue.getProcessCount();
			
			String queueId = QueuesNameUtil.getQueueProcessQueueId(processQueue.getName());
			userQueueSize.addQueueSize(processQueue.getName(), queueId, processQueue.getDescription(), processCount.intValue());
		}
		
		usersQueuesSize.add(userQueueSize);
	}
	
	private void fillSubstitutionUserQueues(ProcessToolBpmSession bpmSession)
	{
		String currentUserLogin = bpmSession.getUserLogin();
		
		UserData user = reg.getUserDataDAO(ctx.getHibernateSession()).loadUserByLogin(currentUserLogin);
		
		ProcessInstanceFilterFactory filterFactory = new ProcessInstanceFilterFactory();
		Collection<ProcessInstanceFilter> queuesFilters = new ArrayList<ProcessInstanceFilter>();
		
		UsersQueuesDTO userQueueSize = new UsersQueuesDTO(currentUserLogin);
		
		queuesFilters.add(filterFactory.createSubstitutedTasksAssignedToMeFilter(user));
		queuesFilters.add(filterFactory.createSubstitutedOthersTaskAssignedToMeFilter(user));
		
		
		for(ProcessInstanceFilter queueFilter: queuesFilters)
		{
			int filteredQueueSize = bpmSession.getTasksCount(ctx, queueFilter.getFilterOwner().getLogin(), queueFilter.getQueueTypes());
			//int filteredQueueSize = session.getFilteredTasksCount(queueFilter, ctx);
			
			String queueId = QueuesNameUtil.getSubstitutedQueueTaskId(queueFilter.getName(), currentUserLogin);
			userQueueSize.addProcessListSize(queueFilter.getName(), queueId, filteredQueueSize);
		}
		
		List<ProcessQueue> userAvailableQueues = new ArrayList<ProcessQueue>(bpmSession.getUserAvailableQueues(ctx));
		for(ProcessQueue processQueue: userAvailableQueues)
		{
			Long processCount = processQueue.getProcessCount();
			
			String queueId = QueuesNameUtil.getSubstitutedQueueProcessQueueId(processQueue.getName(), currentUserLogin);
			userQueueSize.addQueueSize(processQueue.getName(), queueId, processQueue.getDescription(), processCount.intValue());
		}
		
		usersQueuesSize.add(userQueueSize);
	}

	/**
	 * DTO Class which binds user login with its queues 
	 * 
	 * @author mpawlak@bluesoft.net.pl
	 *
	 */
	public static class UsersQueuesDTO implements Serializable
	{
		private static final long serialVersionUID = -6144244162774763817L;
		
		private String userLogin;
		private Collection<UserQueueDTO> processesList;
		private Collection<UserQueueDTO> queuesList;
		
		public UsersQueuesDTO(String userLogin) 
		{
			this.userLogin = userLogin;
			this.processesList = new HashSet<UserQueueDTO>();
			this.queuesList = new HashSet<UserQueueDTO>();
		}
		
		public void addProcessListSize(String listName, String listId, Integer queueSize)
		{
			UserQueueDTO userQueue = new UserQueueDTO();
			userQueue.setQueueName(listName);
			userQueue.setQueueId(listId);
			userQueue.setQueueSize(queueSize);
			userQueue.setQueueDesc(listName);
			
			this.processesList.add(userQueue);
		}
		
		public void addQueueSize(String queueName, String queueId, String queueDesc, Integer queueSize)
		{
			UserQueueDTO userQueue = new UserQueueDTO();
			userQueue.setQueueName(queueName);
			userQueue.setQueueId(queueId);
			userQueue.setQueueSize(queueSize);
			userQueue.setQueueDesc(queueDesc);
			
			this.queuesList.add(userQueue);
		}

		public String getUserLogin() {
			return userLogin;
		}

		public Collection<UserQueueDTO> getProcessesList() {
			return processesList;
		}

		public Collection<UserQueueDTO> getQueuesList() {
			return queuesList;
		}
	}
	
	/**
	 * DTO for user queue 
	 * @author mpawlak@bluesoft.net.pl
	 *
	 */
	public static class UserQueueDTO implements Serializable
	{
		private static final long serialVersionUID = -3013374207013807349L;
		
		private String queueName;
		private String queueDesc;
		private String queueId;
		private Integer queueSize;
		
		public String getQueueName() {
			return queueName;
		}
		public void setQueueName(String queueName) {
			this.queueName = queueName;
		}
		public String getQueueId() {
			return queueId;
		}
		public void setQueueId(String queueId) {
			this.queueId = queueId;
		}
		public Integer getQueueSize() {
			return queueSize;
		}
		public void setQueueSize(Integer queueSize) {
			this.queueSize = queueSize;
		}
		
		
		
		
		public String getQueueDesc() {
			return queueDesc;
		}
		public void setQueueDesc(String queueDesc) {
			this.queueDesc = queueDesc;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((queueId == null) ? 0 : queueId.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserQueueDTO other = (UserQueueDTO) obj;
			if (queueId == null) {
				if (other.queueId != null)
					return false;
			} else if (!queueId.equals(other.queueId))
				return false;
			return true;
		}
		
		
		
		
	}

	

}

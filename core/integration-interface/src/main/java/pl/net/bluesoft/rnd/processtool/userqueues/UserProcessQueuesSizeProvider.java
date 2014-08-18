package pl.net.bluesoft.rnd.processtool.userqueues;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory.ExecutionType;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.filters.factory.ProcessInstanceFilterFactory;
import pl.net.bluesoft.rnd.processtool.filters.factory.QueuesNameUtil;
import pl.net.bluesoft.rnd.processtool.web.view.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

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
	private I18NSource messageSource;
	
	public UserProcessQueuesSizeProvider(ProcessToolRegistry reg, String userLogin, I18NSource messageSource) 
	{
		this.usersQueuesSize = new ArrayList<UsersQueuesDTO>();
		this.reg = reg;
		this.userLogin = userLogin;
		this.messageSource = messageSource;
	}
	
	/** Map with queue id as key and its size as value */
	public Collection<UsersQueuesDTO> getUserProcessQueueSize()
	{
		ProcessToolContext ctx = getThreadProcessToolContext();
		
		if(ctx == null)
		{
			reg.withProcessToolContext(new ProcessToolContextCallback()
			{
				@Override
				public void withContext(ProcessToolContext ctx)
				{
					fillUserQueuesMap();
				}
			}, ExecutionType.NO_TRANSACTION);
		}
		else
		{
			fillUserQueuesMap();
		}
		
		return usersQueuesSize;
	}
	
	/** Initialize new session and context for database access */
	private void fillUserQueuesMap()
	{
		/* prevent null pointers during restart when old client instance is open in browser */
		if (userLogin == null) {
			return;
		}

		/* Fill queues for main user */
		ProcessToolBpmSession mainUserSession = getRegistry().getProcessToolSessionFactory().createSession(userLogin);
		
		fillUserQueues(userLogin, mainUserSession);
		
		/* Fill queues for substitutedUsers */
		List<String> substitutedUserLogins = getThreadProcessToolContext().getUserSubstitutionDAO()
				.getCurrentSubstitutedUserLogins(userLogin);
		
		for (String substitutedUserLogin : substitutedUserLogins)
		{
			ProcessToolBpmSession substitutedUserSession = getRegistry().getProcessToolSessionFactory().createSession(substitutedUserLogin);
			fillUserQueues(substitutedUserLogin, substitutedUserSession);
		}
	}

	private void fillUserQueues(String userLogin, ProcessToolBpmSession bpmSession)
	{
		String currentUserLogin = bpmSession.getUserLogin();
		
		ProcessInstanceFilterFactory filterFactory = new ProcessInstanceFilterFactory();
		Collection<ProcessInstanceFilter> queuesFilters = new ArrayList<ProcessInstanceFilter>();
		
		UsersQueuesDTO userQueueSize = new UsersQueuesDTO(currentUserLogin);
		
		/* Create organized tasks filters */
        queuesFilters.add(filterFactory.createAllTasksFilter(userLogin));
	//	queuesFilters.add(filterFactory.createMyTasksFilter(userLogin));
		queuesFilters.add(filterFactory.createMyTasksInProgress(userLogin));
		queuesFilters.add(filterFactory.createMyClosedTasksFilter(userLogin));
		
		for(ProcessInstanceFilter queueFilter: queuesFilters)
		{
			int filteredQueueSize = bpmSession.getTasksCount(queueFilter);
			
			String queueId = QueuesNameUtil.getQueueTaskId(queueFilter.getName());
			String queueDesc = messageSource.getMessage(queueFilter.getName());
			
			userQueueSize.addProcessListSize(queueFilter.getName(), queueId, queueDesc, filteredQueueSize);
			
			if(isAssignedToUserFilter(queueFilter)) {
				userQueueSize.setActiveTasks(userQueueSize.getActiveTasks() + filteredQueueSize);
			}
		}
		
		/* Add queues */
		List<ProcessQueue> userAvailableQueues = new ArrayList<ProcessQueue>(bpmSession.getUserAvailableQueues());
		for(ProcessQueue processQueue: userAvailableQueues)
		{
			int processCount = processQueue.getProcessCount();
			
			String queueId = QueuesNameUtil.getQueueProcessQueueId(processQueue.getName());
			String queueDesc = messageSource.getMessage(processQueue.getDescription());
			
			userQueueSize.addQueueSize(processQueue.getName(), queueId, queueDesc, processCount);
            userQueueSize.setActiveTasks(userQueueSize.getActiveTasks() + processCount);
		}
		
		usersQueuesSize.add(userQueueSize);
	}

	private boolean isAssignedToUserFilter(ProcessInstanceFilter filter)
	{
		if(filter.getQueueTypes().contains(QueueType.ALL_TASKS))
			return true;

		return false;
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
		private Integer activeTasks = 0;
		
		public UsersQueuesDTO(String userLogin) 
		{
			this.userLogin = userLogin;
			this.processesList = new LinkedHashSet<UserQueueDTO>();
			this.queuesList = new LinkedHashSet<UserQueueDTO>();
		}
		
		public void addProcessListSize(String listName, String listId, String listDesc, Integer queueSize)
		{
			UserQueueDTO userQueue = new UserQueueDTO();
			userQueue.setQueueName(listName);
			userQueue.setQueueId(listId);
			userQueue.setQueueSize(queueSize);
			userQueue.setQueueDesc(listDesc);
			
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

		public Integer getActiveTasks() {
			return activeTasks;
		}

		public void setActiveTasks(Integer activeTasks) {
			this.activeTasks = activeTasks;
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
					+ (queueId == null ? 0 : queueId.hashCode());
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

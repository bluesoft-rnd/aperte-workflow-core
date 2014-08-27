package pl.net.bluesoft.rnd.processtool.web.view;

import org.hibernate.annotations.GenericGenerator;
import pl.net.bluesoft.rnd.processtool.model.QueueOrder;
import pl.net.bluesoft.rnd.processtool.model.QueueType;

import javax.persistence.*;
import java.util.*;


public class ProcessInstanceFilter  {

	protected Long id;
	private String name;
    private String processBpmKey;
    private String viewName;

    private Set<ProcessInstanceFilterSortingColumn> sortingColumns = new HashSet<ProcessInstanceFilterSortingColumn>();

    private String expression;

	private Locale locale;

	private String filterOwnerLogin;

	private boolean usePrivileges;

	private Set<QueueType> queueTypes = new HashSet<QueueType>();

	private Set<String> ownerLogins = new HashSet<String>();

	private Set<String> creatorLogins = new HashSet<String>();

	private Set<String> queues = new HashSet<String>();

	private Set<String> taskNames = new HashSet<String>();


	public Set<String> getOwnerLogins() {
		return ownerLogins;
	}

	public void setOwnerLogins(Set<String> ownerLogins) {
		this.ownerLogins = ownerLogins;
	}

	public Set<String> getQueues() {
		return queues;
	}

	public void setQueues(Set<String> queues) {
		this.queues = queues;
	}

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}


	public void addOwner(String userLogin) {
		ownerLogins.add(userLogin);
	}

	public void addQueue(String q) {
		queues.add(q);
	}

	public void addTaskName(String taskName) {
		taskNames.add(taskName);
	}

	public Set<String> getTaskNames() {
		return taskNames;
	}

	public void setTaskNames(Set<String> taskNames) {
		this.taskNames = taskNames;
	}

	public String getFilterOwnerLogin() {
		return filterOwnerLogin;
	}

	public void setFilterOwnerLogin(String filterOwnerLogin) {
		this.filterOwnerLogin = filterOwnerLogin;
	}

	public boolean isUsePrivileges() {
		return usePrivileges;
	}

	public void setUsePrivileges(boolean usePrivileges) {
		this.usePrivileges = usePrivileges;
	}

	public Set<String> getCreatorLogins() {
		return creatorLogins;
	}

	public void setCreatorLogins(Set<String> creatorLogins) {
		this.creatorLogins = creatorLogins;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Set<QueueType> getQueueTypes() {
		return queueTypes;
	}
	
	public QueueType getFirstQueueType()
	{
		for(QueueType queueType: queueTypes)
			return queueType;
		
		return null;
	}

	public void setQueueTypes(Set<QueueType> queueTypes) {
		this.queueTypes = queueTypes;
	}

	public void addQueueType(QueueType queueType) 
	{
		this.queueTypes.add(queueType);
		
	}


    public String getProcessBpmKey() {
        return processBpmKey;
    }

    public void setProcessBpmKey(String processBpmKey) {
        this.processBpmKey = processBpmKey;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void addSortingColumnOrder(ProcessInstanceFilterSortingColumn column)
    {
        this.sortingColumns.add(column);
    }

    public Set<ProcessInstanceFilterSortingColumn> getSortingColumns()
    {
        return sortingColumns;
    }
}

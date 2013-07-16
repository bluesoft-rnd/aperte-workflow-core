package pl.net.bluesoft.rnd.processtool.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.swing.*;

import org.hibernate.annotations.*;

@Entity
@Table(name = "pt_pi_filters")
public class ProcessInstanceFilter extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_INST_FILTER")
			}
	)
	@Column(name = "id")
	protected Long id;

	private Date createdAfter;
	private Date createdBefore;
	private Date updatedAfter;
	private Date notUpdatedAfter;
	private String genericQuery;
	private String name;
    private String processBpmKey;

    @Enumerated(EnumType.STRING)
    private QueueOrder sortOrder;

    @Enumerated(EnumType.STRING)
    private QueueOrderCondition sortOrderCondition;



    private String expression;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "filter_owner_id")
	private UserData filterOwner;
	
	/** Type of the queues */
	@ElementCollection(fetch = FetchType.LAZY)
	@Enumerated(EnumType.STRING)
	@JoinTable(name = "pt_pi_filters_queue_types", joinColumns = @JoinColumn(name = "filter_id"))
	private Set<QueueType> queueTypes = new HashSet<QueueType>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "pt_pi_filters_owners", joinColumns = @JoinColumn(name = "filter_id"), inverseJoinColumns = @JoinColumn(name = "owner_id"))
	private Set<UserData> owners = new HashSet<UserData>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "pt_pi_filters_creats", joinColumns = @JoinColumn(name = "filter_id"), inverseJoinColumns = @JoinColumn(name = "creator_id"))
	private Set<UserData> creators = new HashSet<UserData>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "pt_pi_filters_queues", joinColumns = @JoinColumn(name = "filter_id"))
	private Set<String> queues = new HashSet<String>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "pt_pi_filters_tasks", joinColumns = @JoinColumn(name = "filter_id"))
	private Set<String> taskNames = new HashSet<String>();

	public static final String[] LAZY_RELATIONS = { "owners", "creators", "queues" };

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<UserData> getOwners() {
		return owners;
	}

	public void setOwners(Set<UserData> owners) {
		this.owners = owners;
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

	public Date getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(Date createdAfter) {
		this.createdAfter = createdAfter;
	}

	public Date getCreatedBefore() {
		return createdBefore;
	}

	public void setCreatedBefore(Date createdBefore) {
		this.createdBefore = createdBefore;
	}

	public Date getUpdatedAfter() {
		return updatedAfter;
	}

	public void setUpdatedAfter(Date updatedAfter) {
		this.updatedAfter = updatedAfter;
	}

	public Date getNotUpdatedAfter() {
		return notUpdatedAfter;
	}

	public void setNotUpdatedAfter(Date notUpdatedAfter) {
		this.notUpdatedAfter = notUpdatedAfter;
	}

	public void addOwner(UserData userData) {
		owners.add(userData);
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

	public String getGenericQuery() {
		return genericQuery;
	}

	public void setGenericQuery(String genericQuery) {
		this.genericQuery = genericQuery;
	}

	public Set<UserData> getCreators() {
		return creators;
	}

	public void setCreators(Set<UserData> creators) {
		this.creators = creators;
	}

	public UserData getFilterOwner() {
		return filterOwner;
	}

	public void setFilterOwner(UserData filterOwner) {
		this.filterOwner = filterOwner;
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

    public QueueOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(QueueOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public QueueOrderCondition getSortOrderCondition() {
        return sortOrderCondition;
    }

    public void setSortOrderCondition(QueueOrderCondition sortOrderCondition) {
        this.sortOrderCondition = sortOrderCondition;
    }

    public String getProcessBpmKey() {
        return processBpmKey;
    }

    public void setProcessBpmKey(String processBpmKey) {
        this.processBpmKey = processBpmKey;
    }
}

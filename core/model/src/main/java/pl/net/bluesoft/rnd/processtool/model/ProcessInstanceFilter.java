package pl.net.bluesoft.rnd.processtool.model;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.time.DateUtils;

@Entity
@Table(name = "pt_pi_filters")
public class ProcessInstanceFilter extends PersistentEntity {
	private Date createdAfter;
	private Date createdBefore;
	private Date updatedAfter;
	private Date notUpdatedAfter;
	private String genericQuery;
	private String name;
	private Boolean processEnded;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "filter_owner_id")
	private UserData filterOwner;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "pt_pi_filters_owners", joinColumns = @JoinColumn(name = "filter_id"), inverseJoinColumns = @JoinColumn(name = "owner_id"))
	private Set<UserData> owners = new HashSet<UserData>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "pt_pi_filters_not_owners", joinColumns = @JoinColumn(name = "filter_id"), inverseJoinColumns = @JoinColumn(name = "owner_id"))
	private Set<UserData> notOwners = new HashSet<UserData>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "pt_pi_filters_creats", joinColumns = @JoinColumn(name = "filter_id"), inverseJoinColumns = @JoinColumn(name = "creator_id"))
	private Set<UserData> creators = new HashSet<UserData>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "pt_pi_filters_not_creats", joinColumns = @JoinColumn(name = "filter_id"), inverseJoinColumns = @JoinColumn(name = "creator_id"))
	private Set<UserData> notCreators = new HashSet<UserData>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "pt_pi_filters_queues", joinColumns = @JoinColumn(name = "filter_id"))
	private Set<String> queues = new HashSet<String>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "pt_pi_filters_states", joinColumns = @JoinColumn(name = "filter_id"))
	@Enumerated(value = EnumType.STRING)
	private Set<TaskState> states = new HashSet<TaskState>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "pt_pi_filters_tasks", joinColumns = @JoinColumn(name = "filter_id"))
	private Set<String> taskNames = new HashSet<String>();

	public static final String[] LAZY_RELATIONS = new String[]{"owners", "creators", "queues", "states", "notOwners", "notCreators"};

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

	public Calendar getUpdatedAfterCalendar() {
		return DateUtils.toCalendar(updatedAfter);
	}

	public void setUpdatedAfter(Date updatedAfter) {
		this.updatedAfter = updatedAfter;
	}

	public Date getNotUpdatedAfter() {
		return notUpdatedAfter;
	}

	public Calendar getNotUpdatedAfterCalendar() {
		return DateUtils.toCalendar(notUpdatedAfter);
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

	public Set<TaskState> getStates() {
		return states;
		//	    return null;
	}

	public void setStates(Set<TaskState> states) {
		this.states = states;
	}

	public void addState(TaskState state) {
		states.add(state);
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

	public Set<UserData> getNotOwners() {
		return notOwners;
	}

	public void setNotOwners(Set<UserData> notOwners) {
		this.notOwners = notOwners;
	}

	public Set<UserData> getNotCreators() {
		return notCreators;
	}

	public void setNotCreators(Set<UserData> notCreators) {
		this.notCreators = notCreators;
	}
	
	public boolean isProcessEnded()
	{
		return processEnded != null && processEnded;
	}

	public Boolean getProcessEnded()
	{
		return processEnded;
	}

	public void setProcessEnded(Boolean processEnded)
	{
		this.processEnded = processEnded;
	}
}

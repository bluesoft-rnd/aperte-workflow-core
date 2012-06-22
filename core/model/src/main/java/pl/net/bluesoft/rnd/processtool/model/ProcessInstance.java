package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * Entity representing process instance data. It should be persisted in appropriate database.
 * Custom processes can reference this class.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_instance")
public class ProcessInstance extends PersistentEntity {
	private String externalKey;
	private String internalId;
	private String definitionName;
	private String state;
	private String description;
	private String keyword;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

	@Transient
	private String taskId;
    @Transient
    private String[] assignees;
    @Transient
    private String[] taskQueues;

    @Transient
    private BpmTask[] activeTasks;

    private Boolean running;

	private Date createDate;

	@ManyToOne(cascade = {})
	@JoinColumn(name="creator_id")
	private UserData creator;

	@ManyToOne
	@JoinColumn(name="definition_id")
	private ProcessDefinitionConfig definition;

	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name="process_instance_id")
	private Set<ProcessInstanceAttribute> processAttributes = new HashSet();

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessInstanceLog> processLogs = new HashSet();

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="parent_id")
	private Set<ProcessInstance> children = new HashSet();

	@ManyToOne
	@JoinColumn(name="parent_id")
	private ProcessInstance parent;

	@Transient
	private Set toDelete;


	public ProcessInstance() {

	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public ProcessInstance(String externalKey, UserData creator, String definionName) {
		this.externalKey = externalKey;
		this.creator = creator;
		this.definitionName = definionName;
		this.createDate = new Date();
	}

	public String getExternalKey() {
		if(externalKey == null && parent != null){
			return parent.getExternalKey();
		}
		return externalKey;
	}
	
	public String getOwnExternalKey() {
		return externalKey;
	}

	public void setExternalKey(String externalKey) {	
		this.externalKey = externalKey;
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public UserData getCreator() {
		return creator;
	}

	public void setCreator(UserData creator) {
		this.creator = creator;
	}

	public String getDefinitionName() {
		return definitionName;
	}

	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Set<ProcessInstanceAttribute> getProcessAttributes() {
		if (processAttributes == null) processAttributes = new HashSet<ProcessInstanceAttribute>();
		return processAttributes;
	}

	public void setProcessAttributes(Set<ProcessInstanceAttribute> processAttributes) {
		this.processAttributes = processAttributes;
	}

	public ProcessDefinitionConfig getDefinition() {
		return definition;
	}

	public void setDefinition(ProcessDefinitionConfig definition) {
		this.definition = definition;
	}

	public void removeAttribute(ProcessInstanceAttribute attr) {
		attr.setProcessInstance(null);
		processAttributes.remove(attr);
		if (attr.getId() > 0) {
            if (toDelete == null) {
                toDelete = new HashSet();
            }
            toDelete.add(attr);
        }
	}

	public Set getToDelete() {
		return toDelete;
	}

	public void addAttribute(ProcessInstanceAttribute attr) {
		attr.setProcessInstance(this);
		processAttributes.add(attr);
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public Set<ProcessInstanceLog> getProcessLogs() {
		return processLogs;
	}

	public void setProcessLogs(Set<ProcessInstanceLog> processLogs) {
		this.processLogs = processLogs;
	}

	public void addProcessLog(ProcessInstanceLog log) {
		log.setProcessInstance(this);
		processLogs.add(log);
	}

	public ProcessInstanceAttribute findAttributeByKey(String key) {
		Set<ProcessInstanceAttribute> attrs = getProcessAttributes();
		for (ProcessInstanceAttribute pia : attrs) {
			if (pia.getKey() != null && pia.getKey().equals(key)) {
				return pia;
			}
		}
		return null;
	}

    public <T extends ProcessInstanceAttribute> T findAttributeByClass(Class<T> clazz) {
        Set<ProcessInstanceAttribute> attrs = getProcessAttributes();
        for (ProcessInstanceAttribute pia : attrs) {
            if (clazz.isAssignableFrom(pia.getClass())) {
                return (T) pia;
            }
        }
        return null;
    }

    public <T extends ProcessInstanceAttribute> Set<T> findAttributesByClass(Class<T> clazz) {
        Set<T> result = new HashSet<T>();
        Set<ProcessInstanceAttribute> attrs = getProcessAttributes();
        for (ProcessInstanceAttribute pia : attrs) {
            if (clazz.isAssignableFrom(pia.getClass())) {
                result.add((T) pia);
            }
        }
        return result;
    }

    public String getSimpleAttributeValue(String key) {
        ProcessInstanceAttribute attr = findAttributeByKey(key);
        return attr != null ?  ((ProcessInstanceSimpleAttribute)attr).getValue() : null;
    }

    public String getSimpleAttributeValue(String key, String default_) {
        ProcessInstanceAttribute attr = findAttributeByKey(key);
        return attr != null ? ((ProcessInstanceSimpleAttribute)attr).getValue() : default_;
    }

    public void setSimpleAttribute(String key, String value) {
        ProcessInstanceSimpleAttribute attr = (ProcessInstanceSimpleAttribute)findAttributeByKey(key);
        if (attr != null) {
            attr.setValue(value);
        }
        else {
            addAttribute(new ProcessInstanceSimpleAttribute(key, value));
        }
    }

    public void addDictionaryAttributeItem(String dictionary, String key, String value){
        ProcessInstanceDictionaryAttribute attr = (ProcessInstanceDictionaryAttribute)findAttributeByKey(dictionary);
        if (attr == null) {
            addAttribute(attr = new ProcessInstanceDictionaryAttribute(dictionary));
        }
        attr.put(key, value);

    }


    public String[] getAssignees() {
        return nvl(assignees, new String[] { });
    }

    public void setAssignees(String... assignees) {
        this.assignees = assignees;
    }

    public String[] getTaskQueues() {
        return nvl(taskQueues, new String[] { });
    }

    public void setTaskQueues(String... taskQueues) {
        this.taskQueues = taskQueues;
    }

    public Boolean getRunning() {
        return nvl(running,true);
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public BpmTask[] getActiveTasks() {
        return activeTasks;
    }

    public void setActiveTasks(BpmTask[] activeTasks) {
        this.activeTasks = activeTasks;
    }

    public <T extends ProcessInstanceAttribute> Set<T>  findAttributesByClassAndKey(Class<T> clazz, String key){
//      TODO
        return null;
    }


	public Set<ProcessInstance> getChildren() {
		return children;
	}


	public void setChildren(Set<ProcessInstance> children) {
		this.children = children;
	}


	public ProcessInstance getParent() {
		return parent;
	}


	public void setParent(ProcessInstance parent) {
		this.parent = parent;
	}
}

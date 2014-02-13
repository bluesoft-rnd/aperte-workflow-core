package pl.net.bluesoft.rnd.processtool.model;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.processdata.*;
import pl.net.bluesoft.util.lang.cquery.func.F;

import javax.persistence.*;
import java.util.*;
import java.util.logging.LogRecord;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * Entity representing process instance data. It should be persisted in appropriate database.
 * Custom processes can reference this class.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_instance")
@org.hibernate.annotations.Table(
        appliesTo="pt_process_instance",
        indexes = {
                @Index(name = "idx_pt_instance_pk",
                        columnNames = {"id"}
                )
        })
public class ProcessInstance extends AbstractPersistentEntity
{
	public static final String _EXTERNAL_KEY = "externalKey";
	public static final String _INTERNAL_ID = "internalId";
	public static final String _DEFINITION_NAME = "definitionName";
	public static final String _STATUS = "status";
	public static final String _CREATE_DATE = "createDate";
	public static final String _CREATOR_LOGIN = "creatorLogin";
	public static final String _DEFINITION = "definition";
	public static final String _PROCESS_ATTRIBUTES = "processAttributes";
	public static final String _PROCESS_LOGS = "processLogs";
	public static final String _CHILDREN = "children";
	public static final String _PARENT = "parent";
	public static final String _OWNERS = "owners";

    public static final String EXTERNAL_KEY_PROPERTY = "externalKey";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_INST")
			}
	)
    @Index(name="idx_pt_pk")
	@Column(name = "id")
	protected Long id;
    @Index(name="idx_pt_externalkey")
	private String externalKey;

    @Index(name="idx_pt_internalid")
	private String internalId;
	private String definitionName;

    /** Technical process status */
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    /** Business process status */
    @Index(name="idx_pt_business_status")
    @Column(name = "business_status", nullable = true)
    private String businessStatus;

	private Date createDate;
    @Index(name="idx_pt_process_creator")
	private String creatorLogin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="definition_id")
	private ProcessDefinitionConfig definition;

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessInstanceSimpleLargeAttribute> processSimpleLargeAttributes = new HashSet<ProcessInstanceSimpleLargeAttribute>();

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name="process_instance_id")
    private Set<ProcessInstanceSimpleAttribute> processSimpleAttributes = new HashSet<ProcessInstanceSimpleAttribute>();

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessInstanceAttribute> processAttributes = new HashSet<ProcessInstanceAttribute>();

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessComment> comments;

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessDeadline> deadlines;


	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessInstanceLog> processLogs = new HashSet<ProcessInstanceLog>();

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="parent_id")
	private Set<ProcessInstance> children = new HashSet<ProcessInstance>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="parent_id")
	private ProcessInstance parent;
	
	/** Owners of the process. Owner is diffrent then process creator. Process can have many owners */
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "pt_process_instance_owners", joinColumns = @JoinColumn(name = "process_id"))
	private Set<String> owners = new HashSet<String>();

	@Transient
	private Set<ProcessInstanceAttribute> toDelete;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}



    public ProcessInstance getRootProcessInstance() {
    	ProcessInstance parentProcess = this;
    	while (parentProcess.parent != null){
    		parentProcess = parentProcess.parent;
    	}
    	return parentProcess;
    }

	public ProcessInstance(String externalKey, String creatorLogin, String definionName) {
		this.externalKey = externalKey;
		this.creatorLogin = creatorLogin;
		this.definitionName = definionName;
		this.createDate = new Date();
	}

	public ProcessInstance() {
	}

	public String getExternalKey() {
		ProcessInstance other = this;
		while (true) {
			if (other.externalKey == null && other.parent != null) {
				other = other.parent;
				continue;
			}
			return other.externalKey;
		}
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

	public String getCreatorLogin() {
		return creatorLogin;
	}

	public void setCreatorLogin(String creatorLogin) {
		this.creatorLogin = creatorLogin;
	}

	public String getDefinitionName() {
		return definitionName;
	}

	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}

	public Set<String> getOwners() {
		return owners;
	}

	public void setOwners(Set<String> ownersLogins) {
		this.owners = ownersLogins;
	}

    public String getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    public void addOwner(String ownerLogin) {
		this.owners.add(ownerLogin);
	}

	public void addOwners(Collection<String> ownerLogins) {
		this.owners.addAll(ownerLogins);
	}

	public void removeOwner(String ownerLogin) {
		this.owners.remove(ownerLogin);
	}

    public Set<ProcessInstanceSimpleLargeAttribute> getProcessSimpleLargeAttributes() {
        if (processSimpleLargeAttributes == null) {
            processSimpleLargeAttributes = new HashSet<ProcessInstanceSimpleLargeAttribute>();
        }
        return processSimpleLargeAttributes;
    }

    public void setProcessSimpleLargeAttributes(Set<ProcessInstanceSimpleLargeAttribute> processSimpleLargeAttributes) {
        this.processSimpleLargeAttributes = processSimpleLargeAttributes;
    }

    public Set<ProcessInstanceSimpleAttribute> getProcessSimpleAttributes() {
		if (processSimpleAttributes == null) {
			processSimpleAttributes = new HashSet<ProcessInstanceSimpleAttribute>();
		}
		return processSimpleAttributes;
	}

	public void setProcessSimpleAttributes(Set<ProcessInstanceSimpleAttribute> processSimpleAttributes) {
		this.processSimpleAttributes = processSimpleAttributes;
	}

	public Set<ProcessInstanceAttribute> getProcessAttributes() {
		if (processAttributes == null) {
			processAttributes = new HashSet<ProcessInstanceAttribute>();
		}
		return processAttributes;
	}

    public ProcessInstanceAttribute getProcessAttribute(IAttributeName key)
    {
        return getProcessAttribute(key.value());
    }

    public ProcessInstanceAttribute getProcessAttribute(String key)
    {
        for (ProcessInstanceAttribute pia : getProcessAttributes()) {
            if (pia.getKey() != null && pia.getKey().equals(key)) {
                return pia;
            }
        }
        return null;
    }

	public void setProcessAttributes(Set<ProcessInstanceAttribute> processAttributes) {
		this.processAttributes = processAttributes;
	}

	public Set<ProcessComment> getComments() {
		if (comments == null) {
			comments = new HashSet<ProcessComment>();
		}
		return comments;
	}

	public List<ProcessComment> getCommentsOrderedByDate(boolean ascending) {
		if (ascending) {
			return from(getComments()).orderBy(BY_CREATE_DATE).toList();
		}
		else {
			return from(getComments()).orderByDescending(BY_CREATE_DATE).toList();
		}
	}

	private static final F<ProcessComment, Long> BY_CREATE_DATE = new F<ProcessComment, Long>() {
		@Override
		public Long invoke(ProcessComment x) {
			return x.getCreateTime().getTime();
		}
	};

	public void setComments(Set<ProcessComment> comments) {
		this.comments = comments;
	}

	public Set<ProcessDeadline> getDeadlines() {
		if (deadlines == null) {
			deadlines = new HashSet<ProcessDeadline>();
		}
		return deadlines;
	}

	public void setDeadlines(Set<ProcessDeadline> deadlines) {
		this.deadlines = deadlines;
	}

	public ProcessDeadline getDeadline(String taskName) {
		for (ProcessDeadline deadline : getDeadlines()) {
			if (taskName.equals(deadline.getTaskName())) {
				return deadline;
			}
		}
		return null;
	}

	public ProcessDeadline getDeadline(BpmTask task) {
		return getDeadline(task.getTaskName());
	}

	public ProcessDefinitionConfig getDefinition() {
		return definition;
	}

	public void setDefinition(ProcessDefinitionConfig definition) {
		this.definition = definition;
	}

	public void removeAttribute(ProcessInstanceAttribute attr) {
		attr.setProcessInstance(null);
		getProcessAttributes().remove(attr);
		if (attr.getId() > 0) {
            if (toDelete == null) {
                toDelete = new HashSet<ProcessInstanceAttribute>();
            }
            toDelete.add(attr);
        }
	}

    public void removeAttribute(IAttributeName attributeKey)
    {
        removeAttribute(attributeKey.value());
    }

    public void removeAttribute(String attributeKey)
    {
        ProcessInstanceAttribute attr = getProcessAttribute(attributeKey);
        if(attr == null)
            return;

        removeAttribute(attr);
    }

    public void removeSimpleAttribute(IAttributeName attributeKey)
    {
        removeAttribute(attributeKey.value());
    }

    public void removeSimpleAttribute(String attributeKey)
    {
        String value = getSimpleAttributeValue(attributeKey);
        if(value == null)
            return;

        setSimpleAttribute(attributeKey, null);
    }

	public Set<ProcessInstanceAttribute> getToDelete() {
		return toDelete;
	}

	public void addAttribute(ProcessInstanceAttribute attr) {
		attr.setProcessInstance(this);
		getProcessAttributes().add(attr);
	}

	public Set<ProcessInstanceLog> getProcessLogs() {
		return processLogs;
	}

    public List<ProcessInstanceLog> getProcessLogsSortedByDate() {
        List<ProcessInstanceLog> list = new ArrayList<ProcessInstanceLog>(processLogs);
        Collections.sort(list, new Comparator<ProcessInstanceLog>() {
            @Override
            public int compare(ProcessInstanceLog o1, ProcessInstanceLog o2) {
                return o1.getEntryDate().compareTo(o2.getEntryDate());
            }
        });
        return list;
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

	public AbstractProcessInstanceAttribute findAnyAttributeByKey(String key) {
		ProcessInstanceSimpleAttribute simpleAttribute = findSimpleAttributeByKey(key);

		if (simpleAttribute != null) {
			return simpleAttribute;
		}
		return findAttributeByKey(key);
	}

	private ProcessInstanceSimpleAttribute findSimpleAttributeByKey(String key) {
		Set<ProcessInstanceSimpleAttribute> attrs = getProcessSimpleAttributes();
		for (ProcessInstanceSimpleAttribute pia : attrs) {
			if (pia.getKey() != null && pia.getKey().equals(key)) {
				return pia;
			}
		}
		return null;
	}

    private ProcessInstanceSimpleLargeAttribute findSimpleLargeAttributeByKey(String key) {
        Set<ProcessInstanceSimpleLargeAttribute> attrs = getProcessSimpleLargeAttributes();
        for (ProcessInstanceSimpleLargeAttribute pia : attrs) {
            if (pia.getKey() != null && pia.getKey().equals(key)) {
                return pia;
            }
        }
        return null;
    }

    public String getSimpleLargeAttributeValue(String key) {
        return getSimpleLargeAttributeValue(key, null);
    }

    public String getSimpleLargeAttributeValue(String key, String default_) {
        ProcessInstanceSimpleLargeAttribute attr = findSimpleLargeAttributeByKey(key);
        return attr != null ? attr.getValue() : default_;
    }

    public String getSimpleAttributeValue(String key) {
        return getSimpleAttributeValue(key, null);
    }

    public String getSimpleAttributeValue(IAttributeName attributeName) {
        return getSimpleAttributeValue(attributeName.value(), null);
    }

    public String getSimpleAttributeValue(String key, String default_) {
		ProcessInstanceSimpleAttribute attr = findSimpleAttributeByKey(key);
        return attr != null ? attr.getValue() : default_;
    }


	public Map<String, String> getSimpleAttributeValues() {
		Map<String, String> result = new HashMap<String, String>();

		for (ProcessInstanceSimpleAttribute attribute : getProcessSimpleAttributes()) {
			result.put(attribute.getKey(), attribute.getValue());
		}
		return result;
	}


	public String getInheritedSimpleAttributeValue(String key) {
		return getInheritedSimpleAttributeValue(key, null);
	}

	public String getInheritedSimpleAttributeValue(String key, String default_) {
		for (ProcessInstance pi = this; pi != null; pi = pi.parent) {
			ProcessInstanceSimpleAttribute attr = findSimpleAttributeByKey(key);
			if (attr != null) {
				return attr.getValue();
			}
		}
		return default_;
	}

    public void setSimpleAttribute(IAttributeName key, String value) {
        setSimpleAttribute(key.value(), value);
    }

    public void setSimpleAttribute(String key, String value) {
        ProcessInstanceSimpleAttribute attr = findSimpleAttributeByKey(key);

        if (attr != null) {
            attr.setValue(value);
        }
        else {
			attr = new ProcessInstanceSimpleAttribute(key, value);
			attr.setProcessInstance(this);
			getProcessSimpleAttributes().add(attr);
        }
    }

    public void setSimpleLargeAttribute(IAttributeName key, String value) {
        setSimpleLargeAttribute(key.value(), value);
    }

    public void setSimpleLargeAttribute(String key, String value) {
        ProcessInstanceSimpleLargeAttribute attr = findSimpleLargeAttributeByKey(key);

        if (attr != null) {
            attr.setValue(value);
        }
        else {
            attr = new ProcessInstanceSimpleLargeAttribute(key, value);
            attr.setProcessInstance(this);
            getProcessSimpleLargeAttributes().add(attr);
        }
    }

    public void addDictionaryAttributeItem(String dictionary, String key, String value){
        ProcessInstanceDictionaryAttribute attr = (ProcessInstanceDictionaryAttribute)findAttributeByKey(dictionary);
        if (attr == null) {
            addAttribute(attr = new ProcessInstanceDictionaryAttribute(dictionary));
        }
        attr.put(key, value);
	}

    public ProcessStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessStatus status) {
        this.status = status;
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
	
	/** Method checks if the process is in running or new state */
	public boolean isProcessRunning() {
		return status == ProcessStatus.NEW || status == ProcessStatus.RUNNING;
	}

	@Override
	public String toString() {
		return "ProcessInstance [externalKey=" + getExternalKey() + ", internalId=" + internalId + "]";
	}

	/** Check if process is subprocess (has parent process) */
	public boolean isSubprocess() {
		return parent != null;
	}

	public void addComment(ProcessComment comment) {
		comment.setProcessInstance(this);
		getComments().add(comment);
	}

	public void addComments(Collection<ProcessComment> comments) {
		for (ProcessComment comment : comments) {
			addComment(comment);
		}
	}

	public void addDeadline(ProcessDeadline deadline) {
		deadline.setProcessInstance(this);
		getDeadlines().add(deadline);
	}

	public void setAllProcessAttributes(Collection<AbstractProcessInstanceAttribute> attributes) {
		Set<ProcessInstanceSimpleAttribute> simpleAttributes = new HashSet<ProcessInstanceSimpleAttribute>();
		Set<ProcessInstanceAttribute> genericAttributes = new HashSet<ProcessInstanceAttribute>();

		for (AbstractProcessInstanceAttribute attribute : attributes) {
			if (attribute instanceof ProcessInstanceSimpleAttribute) {
				simpleAttributes.add((ProcessInstanceSimpleAttribute)attribute);
			}
			else {
				genericAttributes.add((ProcessInstanceAttribute)attribute);
			}
			attribute.setProcessInstance(this);
		}

		processSimpleAttributes = simpleAttributes;
		processAttributes = genericAttributes;
	}

	public Collection<AbstractProcessInstanceAttribute> getAllProcessAttributes() {
		List<AbstractProcessInstanceAttribute> result = new ArrayList<AbstractProcessInstanceAttribute>();
		result.addAll(getProcessSimpleAttributes());
		result.addAll(getProcessAttributes());
		return result;
	}



    public void addProcessLogInfo(String infoHeader, String infoBody, Collection<String> parameters)
    {
        ProcessInstanceLog log = new ProcessInstanceLog();
        log.setState(null);
        log.setEntryDate(new Date());
        log.setEventI18NKey(infoHeader);
        log.setUserLogin("");
        log.setLogType(ProcessInstanceLog.LOG_TYPE_INFO);
        log.setOwnProcessInstance(this);
        log.setLogValue(infoBody);
        log.setAdditionalInfo(StringUtils.join(parameters, ","));
        getRootProcessInstance().addProcessLog(log);
    }
}

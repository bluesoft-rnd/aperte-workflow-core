package pl.net.bluesoft.rnd.processtool.model;

import org.hibernate.annotations.GenericGenerator;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.util.lang.cquery.func.F;

import javax.persistence.*;
import java.util.*;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * Entity representing process instance data. It should be persisted in appropriate database.
 * Custom processes can reference this class.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_instance")
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
	@Column(name = "id")
	protected Long id;

	private String externalKey;
	private String internalId;
	private String definitionName;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

	private Date createDate;
	private String creatorLogin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="definition_id")
	private ProcessDefinitionConfig definition;

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessInstanceAttribute> processAttributes = new HashSet<ProcessInstanceAttribute>();

	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private Set<ProcessComment> comments;


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

	public void addOwner(String ownerLogin) {
		this.owners.add(ownerLogin);
	}

	public void addOwners(Collection<String> ownerLogins) {
		this.owners.addAll(ownerLogins);
	}

	public void removeOwner(String ownerLogin) {
		this.owners.remove(ownerLogin);
	}

	public Set<ProcessInstanceAttribute> getProcessAttributes() {
		if (processAttributes == null) {
			processAttributes = new HashSet<ProcessInstanceAttribute>();
		}
		return processAttributes;
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
	
    public <T extends ProcessInstanceAttribute> T findAttributeByClassName(String className) {
        Set<ProcessInstanceAttribute> attrs = getProcessAttributes();
        for (ProcessInstanceAttribute pia : attrs) {
            if (className.equals(pia.getClass().getName())) {
                return (T) pia;
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

	public <T extends ProcessInstanceAttribute> T findOrCreateAttribute(Class<T> attrClass) {
		T attribute = findAttributeByClass(attrClass);
		if(attribute == null) {
			try {
				attribute = attrClass.newInstance();
				addAttribute(attribute);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return attribute;	
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

	public Map<String, String> getSimpleAttributeValues() {
		Map<String, String> result = new HashMap<String, String>();

		for (ProcessInstanceAttribute attribute : getProcessAttributes()) {
			if (attribute instanceof ProcessInstanceSimpleAttribute) {
				ProcessInstanceSimpleAttribute simpleAttribute = (ProcessInstanceSimpleAttribute)attribute;

				result.put(simpleAttribute.getKey(), simpleAttribute.getValue());
			}
		}
		return result;
	}


	public String getInheritedSimpleAttributeValue(String key) {
		return getInheritedSimpleAttributeValue(key, null);
	}

	public String getInheritedSimpleAttributeValue(String key, String default_) {
		for (ProcessInstance pi = this; pi != null; pi = pi.parent) {
			ProcessInstanceAttribute attr = findAttributeByKey(key);
			if (attr instanceof ProcessInstanceSimpleAttribute) {
				return ((ProcessInstanceSimpleAttribute)attr).getValue();
			}
		}
		return default_;
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
}

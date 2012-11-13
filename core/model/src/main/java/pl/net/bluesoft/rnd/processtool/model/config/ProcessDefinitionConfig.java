package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * Configuration of a process definition.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_definition_config")
public class ProcessDefinitionConfig extends PersistentEntity implements Serializable {
	private String processName;
	private String description;
	private String bpmDefinitionKey;

    @Column(name="comment_")
	private String comment;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="creator_id")
	private UserData creator;

	private Date createDate;

	@OneToMany(cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name="definition_id")
	private Set<ProcessStateConfiguration> states = new HashSet<ProcessStateConfiguration>();

	@OneToMany(cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name="definition_id")
	private Set<ProcessDefinitionPermission> permissions = new HashSet<ProcessDefinitionPermission>();

    @Lob
    private byte[] processLogo;
	
    private Boolean enabled;

    private String taskItemClass;
	/**
	 * latest definition of process with processName ensures uniqueness and versioning of definitions
	 */
	private Boolean latest;

    public byte[] getProcessLogo() {
        return processLogo;
    }

    public void setProcessLogo(byte[] processLogo) {
        this.processLogo = Lang2.noCopy(processLogo);
    }

    public String getTaskItemClass() {
        return taskItemClass;
    }

    public void setTaskItemClass(String taskItemClass) {
        this.taskItemClass = taskItemClass;
    }

    public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBpmDefinitionKey() {
		return bpmDefinitionKey;
	}

	public void setBpmDefinitionKey(String bpmDefinitionKey) {
		this.bpmDefinitionKey = bpmDefinitionKey;
	}

	public Boolean getLatest() {
		return latest;
	}

	public void setLatest(Boolean latest) {
		this.latest = latest;
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

	public Set<ProcessStateConfiguration> getStates() {
		return states;
	}

	public void setStates(Set<ProcessStateConfiguration> states) {
		this.states = states;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


    public Boolean getEnabled() {
        return nvl(enabled, true);
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

	public static final Comparator<ProcessDefinitionConfig> DEFAULT_COMPARATOR = new Comparator<ProcessDefinitionConfig>() {
		@Override
		public int compare(ProcessDefinitionConfig o1, ProcessDefinitionConfig o2) {
			int res = nvl(o1.getDescription(), "").compareToIgnoreCase(nvl(o2.getDescription(), ""));
			if (res == 0) {
				res = nvl(o2.getId(), Long.MIN_VALUE).compareTo(nvl(o1.getId(), Long.MIN_VALUE));
			}
			return res;
		}
	};

    public Set<ProcessDefinitionPermission> getPermissions() {
        if (permissions == null) {
            permissions = new HashSet<ProcessDefinitionPermission>();
        }
        return permissions;
    }

    public void setPermissions(Set<ProcessDefinitionPermission> permissions) {
        this.permissions = permissions;
    }
}

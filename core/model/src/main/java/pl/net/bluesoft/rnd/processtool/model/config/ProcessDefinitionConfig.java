package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;

import javax.persistence.*;
import java.util.*;
import java.util.regex.Pattern;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * Configuration of a process definition.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_definition_config")
@org.hibernate.annotations.Table(
        appliesTo="pt_process_definition_config",
        indexes = {
                @Index(name = "idx_pt_def_conf_pk",
                        columnNames = {"id"}
                )
        })
public class ProcessDefinitionConfig extends AbstractPersistentEntity {
	private static final long serialVersionUID = 3568533142091163609L;

	public static final String _DESCRIPTION = "description";
	public static final String _BPM_DEFINITION_KEY = "bpmDefinitionKey";
	public static final String _BPM_DEFINITION_VERSION = "bpmDefinitionVersion";
	public static final String _DEPLOYMENT_ID = "deploymentId";
	public static final String _COMMENT = "comment";
	public static final String _CREATOR_LOGIN = "creatorLogin";
	public static final String _CREATE_DATE = "createDate";
	public static final String _STATES = "states";
	public static final String _PERMISSIONS = "permissions";
	public static final String _PROCESS_LOGO = "processLogo";
	public static final String _ENABLED = "enabled";
	public static final String _LATEST = "latest";

	public static final String VERSION_SEPARATOR = "_";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_DEF_CONF")
			}
	)
    @Index(name="idx_p_def_config_id")
	@Column(name = "id")
	protected Long id;

	private String description;
    @Index(name="idx_p_def_config_key")
	private String bpmDefinitionKey;
	private int bpmDefinitionVersion;
    @Index(name="idx_p_def_config_d_id")
	private String deploymentId;

	@Column(name="comment_")
	private String comment;

	private String creatorLogin;

	private Date createDate;

	private String defaultStepInfoPattern;
	private String supportedLocales;

	private String externalKeyPattern;

	private String processGroup;

	@OneToMany(targetEntity = ProcessStateConfiguration.class,cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name="definition_id")
	private Set<ProcessStateConfiguration> states = new HashSet<ProcessStateConfiguration>();

	@OneToMany(targetEntity = ProcessDefinitionPermission.class,cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
	@JoinColumn(name="definition_id")
	private Set<ProcessDefinitionPermission> permissions = new HashSet<ProcessDefinitionPermission>();

    @Lob
    private byte[] processLogo;
	
    private boolean enabled;

	/**
	 * latest definition of process with processName ensures uniqueness and versioning of definitions
	 */
	private boolean latest;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public byte[] getProcessLogo() {
        return processLogo;
    }

    public void setProcessLogo(byte[] processLogo) {
        this.processLogo = Lang2.noCopy(processLogo);
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

	public int getBpmDefinitionVersion() {
		return bpmDefinitionVersion;
	}

	public void setBpmDefinitionVersion(int bpmDefinitionVersion) {
		this.bpmDefinitionVersion = bpmDefinitionVersion;
	}

	public String getBpmProcessId() {
		return bpmDefinitionKey + VERSION_SEPARATOR + bpmDefinitionVersion;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public boolean isLatest() {
		return latest;
	}

	public void setLatest(boolean latest) {
		this.latest = latest;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getDefaultStepInfoPattern() {
		return defaultStepInfoPattern;
	}

	public void setDefaultStepInfoPattern(String defaultStepInfoPattern) {
		this.defaultStepInfoPattern = defaultStepInfoPattern;
	}

	public String getSupportedLocales() {
		return supportedLocales;
	}

	public void setSupportedLocales(String supportedLocales) {
		this.supportedLocales = supportedLocales;
	}

	public String getExternalKeyPattern() {
		return externalKeyPattern;
	}

	public void setExternalKeyPattern(String externalKeyPattern) {
		this.externalKeyPattern = externalKeyPattern;
	}

	public String getProcessGroup() {
		return processGroup;
	}

	public void setProcessGroup(String processGroup) {
		this.processGroup = processGroup;
	}

	public String getCreatorLogin() {
		return creatorLogin;
	}

	public void setCreatorLogin(String creatorLogin) {
		this.creatorLogin = creatorLogin;
	}

	public Set<ProcessStateConfiguration> getStates()
	{
        if (states == null) 
        	states = new HashSet<ProcessStateConfiguration>();
        
		return states;
	}

	public void setStates(Set<ProcessStateConfiguration> states) 
	{
		this.states = states;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getProcessName() {
		return description;
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

    public Set<ProcessDefinitionPermission> getPermissions() 
    {
        if (permissions == null) {
			permissions = new HashSet<ProcessDefinitionPermission>();
		}
        return permissions;
    }
    
    /** Get the process state by action name */
    public ProcessStateConfiguration getProcessStateConfigurationByName(String stateName)
    {
    	for(ProcessStateConfiguration state: getStates())
    		if(state.getName().equals(stateName))
    			return state;
    	
    	return null;
    }

    public void setPermissions(Set<ProcessDefinitionPermission> permissions) 
    {
		this.permissions = permissions;
    }

	public boolean hasPriviledge(String priviledge, Collection<String> roleNames) {
		if (getPermissions().isEmpty()) {
			return true;
		}
		for (IPermission permission : getPermissions()) {
			String roleName = permission.getRoleName();
			if (priviledge.equals(permission.getPrivilegeName()) && roleName != null && hasMatchingRole(roleName, roleNames)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasMatchingRole(String roleName, Collection<String> roleNames) {
		for (String role : roleNames) {
			if (role != null && role.matches(roleName)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasVersion(String processId) {
		return processId.matches("^.*" + Pattern.quote(VERSION_SEPARATOR) + "\\d+$");
	}

	public static String extractBpmDefinitionKey(String processId) {
		int separatorPos = processId.lastIndexOf(VERSION_SEPARATOR);

		return separatorPos >= 0 ? processId.substring(0, separatorPos) : processId;
	}

	public static Integer extractBpmDefinitionVersion(String processId) {
		int separatorPos = processId.lastIndexOf(VERSION_SEPARATOR);

		return separatorPos >= 0 ? Integer.valueOf(processId.substring(separatorPos + VERSION_SEPARATOR.length())) : null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
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
		ProcessDefinitionConfig other = (ProcessDefinitionConfig) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}

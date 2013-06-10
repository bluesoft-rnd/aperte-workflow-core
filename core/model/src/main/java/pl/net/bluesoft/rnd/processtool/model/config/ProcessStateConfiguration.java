package pl.net.bluesoft.rnd.processtool.model.config;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name = "pt_process_state_config")
public class ProcessStateConfiguration extends AbstractPersistentEntity 
{
	private static final long serialVersionUID = -4196353066985174280L;
	
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_STATE_CONF")
			}
	)
	@Column(name = "id")
	protected Long id;
	
	private String name;
    @Column(length = 2048)
	private String description;
    @Column(length = 2048)
    private String commentary;
    
    /** Enable access to process state by token */
    @Column(name = "enable_external_access")
    private Boolean enableExternalAccess;

    private Boolean enableManualSave;

	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="state_id")
	private Set<ProcessStateWidget> widgets = new HashSet<ProcessStateWidget>();

	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="state_id")
	private Set<ProcessStateAction> actions = new HashSet<ProcessStateAction>();

    
    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinColumn(name="state_id")
    private Set<ProcessStatePermission> permissions = new HashSet<ProcessStatePermission>();


	@ManyToOne
	@JoinColumn(name="definition_id")
	private ProcessDefinitionConfig definition;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getCommentary() {
		return commentary;
	}


	public void setCommentary(String commentary) {
		this.commentary = commentary;
	}


	public Boolean getEnableManualSave() {
		return enableManualSave;
	}


	public void setEnableManualSave(Boolean enableManualSave) {
		this.enableManualSave = enableManualSave;
	}


	public Set<ProcessStateWidget> getWidgets() 
	{
		if(widgets == null)
			this.widgets = new HashSet<ProcessStateWidget>();

		return widgets;
	}


	public void setWidgets(Set<ProcessStateWidget> widgets) 
	{
		this.widgets = widgets;
	}


	public Set<ProcessStateAction> getActions() 
	{
		if(actions == null)
			this.actions = new HashSet<ProcessStateAction>();

		return actions;
	}


	public void setActions(Set<ProcessStateAction> actions) 
	{
		this.actions = actions;
	}


	public Set<ProcessStatePermission> getPermissions() 
	{
		if(permissions == null)
			this.permissions = new HashSet<ProcessStatePermission>();

		return permissions;
	}


	public void setPermissions(Set<ProcessStatePermission> permissions) 
	{
		this.permissions = permissions;
	}


	public ProcessDefinitionConfig getDefinition() {
		return definition;
	}


	public void setDefinition(ProcessDefinitionConfig definition) {
		this.definition = definition;
	}
	
    public Boolean getEnableExternalAccess() {
		return enableExternalAccess;
	}

	public void setEnableExternalAccess(Boolean enableExternalAccess) {
		this.enableExternalAccess = enableExternalAccess;
	}
	
	/** Get the process state action by it's name */
	public ProcessStateAction getProcessStateActionByName(String actionName)
	{
		for(ProcessStateAction action: getActions())
			if(action.getBpmName().equals(actionName))
				return action;
		
		return null;
	}

 
}

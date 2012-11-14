package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.*;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name = "pt_process_state_config")
public class ProcessStateConfiguration extends AbstractPersistentEntity {
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

    private Boolean enableManualSave;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="state_id")
	private Set<ProcessStateWidget> widgets = new HashSet();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="state_id")
	private Set<ProcessStateAction> actions = new HashSet();

    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="state_id")
    private Set<ProcessStatePermission> permissions = new HashSet();

//    @XmlTransient
	@ManyToOne
	@JoinColumn(name="definition_id")
	private ProcessDefinitionConfig definition;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getEnableManualSave() {
        return enableManualSave;
    }

    public void setEnableManualSave(Boolean enableManualSave) {
        this.enableManualSave = enableManualSave;
    }

    @XmlTransient
	public ProcessDefinitionConfig getDefinition() {
		return definition;
	}

//    @XmlTransient
	public void setDefinition(ProcessDefinitionConfig definition) {
		this.definition = definition;
	}

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
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

	public Set<ProcessStateWidget> getWidgets() {
		return widgets != null ? widgets : (widgets = new HashSet<ProcessStateWidget>());
	}

	public void setWidgets(Set<ProcessStateWidget> widgets) {
		this.widgets = widgets;
	}

	public Set<ProcessStateAction> getActions() {
		return actions != null ? actions : (actions = new HashSet<ProcessStateAction>());
	}

	public void setActions(Set<ProcessStateAction> actions) {
		this.actions = actions;
	}
	public Set<ProcessStatePermission> getPermissions() {
		return permissions != null ? permissions : (permissions = new HashSet<ProcessStatePermission>());
	}

	public void setPermissions(Set<ProcessStatePermission> permissions) {
		this.permissions = permissions;
	}
}

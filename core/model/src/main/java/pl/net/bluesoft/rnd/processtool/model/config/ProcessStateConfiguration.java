package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_state_configuration")
public class ProcessStateConfiguration extends PersistentEntity {
	private String name;
    @Column(length = 2048)
	private String description;
    @Column(length = 2048)
    private String commentary;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="state_id")
	private Set<ProcessStateWidget> widgets = new HashSet();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name="state_id")
	private Set<ProcessStateAction> actions = new HashSet();

	@ManyToOne
	@JoinColumn(name="definition_id")
	private ProcessDefinitionConfig definition;

	public ProcessDefinitionConfig getDefinition() {
		return definition;
	}

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
}

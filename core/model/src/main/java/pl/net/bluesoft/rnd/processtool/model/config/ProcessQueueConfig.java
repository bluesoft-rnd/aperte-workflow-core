package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.*;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_queue_config")
public class ProcessQueueConfig extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_QUEUE_CONF")
			}
	)
	@Column(name = "id")
	protected Long id;

	private String name;
	private String description;
	private Boolean userAdded;

	@OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinColumn(name= "queue_id")
	private Set<ProcessQueueRight> rights;

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

	public Set<ProcessQueueRight> getRights() {
		return rights != null ? rights : (rights = new HashSet<ProcessQueueRight>());
	}

	public void setRights(Set<ProcessQueueRight> rights) {
		this.rights = rights;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setUserAdded(Boolean userAdded) {
		this.userAdded = userAdded;
	}

	public Boolean getUserAdded() {
		return userAdded;
	}
}

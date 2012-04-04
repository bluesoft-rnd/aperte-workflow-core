package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_queue_config")
public class ProcessQueueConfig extends PersistentEntity {
	private String name;
	private String description;
	private Boolean userAdded;

	@OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinColumn(name= "queue_id")
	private Set<ProcessQueueRight> rights;

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

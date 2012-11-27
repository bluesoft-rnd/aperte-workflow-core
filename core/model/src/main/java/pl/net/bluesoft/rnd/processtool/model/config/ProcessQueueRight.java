package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.*;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_queue_right")
public class ProcessQueueRight extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_QUEUE_RIGHT")
			}
	)
	@Column(name = "id")
	protected Long id;

	private String roleName;

    @XmlTransient
	@ManyToOne
	@JoinColumn(name="queue_id")
	private ProcessQueueConfig queue;

	private Boolean browseAllowed;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

    @XmlTransient
	public ProcessQueueConfig getQueue() {
		return queue;
	}

//    @XmlTransient
	public void setQueue(ProcessQueueConfig queue) {
		this.queue = queue;
	}

	public boolean isBrowseAllowed() {
		return browseAllowed;
	}

	public void setBrowseAllowed(boolean browseAllowed) {
		this.browseAllowed = browseAllowed;
	}
}

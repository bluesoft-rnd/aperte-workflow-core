package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_queue_right")
public class ProcessQueueRight extends PersistentEntity {
	private String roleName;

    @XmlTransient
	@ManyToOne
	@JoinColumn(name="queue_id")
	private ProcessQueueConfig queue;

	private Boolean browseAllowed;

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

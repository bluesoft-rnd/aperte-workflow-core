package pl.net.bluesoft.rnd.processtool.model.config;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_queue_right")
public class ProcessQueueRight extends PersistentEntity {
	private String roleName;

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

	public ProcessQueueConfig getQueue() {
		return queue;
	}

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

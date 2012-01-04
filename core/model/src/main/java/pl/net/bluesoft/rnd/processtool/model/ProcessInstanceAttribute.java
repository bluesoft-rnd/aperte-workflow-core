package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.*;

/**
 * Process instance attribute. This class is meant to be expanded.
 *
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_instance_attr")
@Inheritance(strategy=InheritanceType.JOINED)
public class ProcessInstanceAttribute extends PersistentEntity {
    @Column(name="_key")
	private String key;

	@ManyToOne
	@JoinColumn(name="process_instance_id")
	private ProcessInstance processInstance;

	public ProcessInstanceAttribute() {
	}

	public ProcessInstanceAttribute(long id, String key) {
		this.id = id;
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}
}

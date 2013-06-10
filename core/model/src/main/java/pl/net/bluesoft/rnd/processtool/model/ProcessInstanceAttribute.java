package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Process instance attribute. This class is meant to be expanded.
 *
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_instance_attr")
@Inheritance(strategy=InheritanceType.JOINED)
public class ProcessInstanceAttribute extends PersistentEntity {
    @Column(name="key_")
	private String key;

//    @XmlTransient
	@ManyToOne(fetch = FetchType.LAZY)
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

    @XmlTransient
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

//    @XmlTransient
	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}
}

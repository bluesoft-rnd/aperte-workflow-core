package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Process instance attribute. This class is meant to be expanded.
 *
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_instance_attr")
@Inheritance(strategy=InheritanceType.JOINED)
@XmlSeeAlso({ ProcessDeadline.class, ProcessInstanceSimpleAttribute.class })
public class ProcessInstanceAttribute extends PersistentEntity {
	public static final String _KEY = "key";
	public static final String _PROCESS_INSTANCE = "processInstance";
	public static final String _PROCESS_INSTANCE_ID = _PROCESS_INSTANCE + '.' + _ID;

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

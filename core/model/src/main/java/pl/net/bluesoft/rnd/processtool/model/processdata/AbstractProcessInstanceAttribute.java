package pl.net.bluesoft.rnd.processtool.model.processdata;

import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * User: POlszewski
 * Date: 2013-10-16
 * Time: 15:41
 */
@MappedSuperclass
public abstract class AbstractProcessInstanceAttribute extends AbstractPersistentEntity {
	public static final String _KEY = "key";
	public static final String _PROCESS_INSTANCE = "processInstance";
	public static final String _PROCESS_INSTANCE_ID = _PROCESS_INSTANCE + '.' + _ID;

	@Column(name="key_")
	private String key;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private ProcessInstance processInstance;

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

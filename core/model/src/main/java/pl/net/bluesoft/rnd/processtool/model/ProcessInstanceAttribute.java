package pl.net.bluesoft.rnd.processtool.model;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Process instance attribute. This class is meant to be expanded.
 *
 * @author tlipski@bluesoft.net.pl
 * @author kkolodziej@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_instance_attr")
@Inheritance(strategy=InheritanceType.JOINED)
public class ProcessInstanceAttribute extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_INST_ATTR")
			}
	)
	@Column(name = "id")
	protected Long id;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

package pl.net.bluesoft.rnd.processtool.model.processdata;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.IAttribute;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Process instance attribute. This class is meant to be expanded.
 *
 * Use table  per class inheritance strategy for better performance
 *
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
@Entity
@Table(name="pt_process_instance_attr")
@Inheritance(strategy=InheritanceType.JOINED)
@XmlSeeAlso({ ProcessInstanceDictionaryAttribute.class, ProcessInstanceAttachmentAttribute.class })
public abstract class ProcessInstanceAttribute extends AbstractProcessInstanceAttribute {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_PROC_INST_ATTR")
			}
	)
    @Index(name="idx_p_attribute_id")
	@Column(name = "id")
	protected Long id;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
}

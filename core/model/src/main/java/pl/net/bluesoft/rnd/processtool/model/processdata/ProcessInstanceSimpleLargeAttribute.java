package pl.net.bluesoft.rnd.processtool.model.processdata;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * Large attribute with clob as value
 */

@Entity
@Table(name="pt_process_instance_s_l_attr")
@org.hibernate.annotations.Table(
        appliesTo="pt_process_instance_s_l_attr")
public class ProcessInstanceSimpleLargeAttribute extends AbstractProcessInstanceAttribute {
	public static final String _VALUE = "value";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_PROC_INST_s_l_ATTR")
			}
	)
    @Index(name="idx_p_s_l_attribute_id")
	@Column(name = "id")
	protected Long id;

    @Column(name="value_")
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
	private String value;

    public ProcessInstanceSimpleLargeAttribute() {
    }

    public ProcessInstanceSimpleLargeAttribute(String key, String value) {
        setKey(key);
		this.value = value;
    }

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return getKey() + '=' + value;
	}
}

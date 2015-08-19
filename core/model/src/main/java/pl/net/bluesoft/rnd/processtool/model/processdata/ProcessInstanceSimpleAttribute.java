package pl.net.bluesoft.rnd.processtool.model.processdata;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

import javax.persistence.*;

/**
 * Simple attribute with String value.
 * 
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_instance_s_attr")
@org.hibernate.annotations.Table(
        appliesTo="pt_process_instance_s_attr",
        indexes = {
                @Index(name = "idx_p_s_attribute_process_id_key",
                        columnNames = {"process_instance_id", "key_"}
                )
        })
public class ProcessInstanceSimpleAttribute extends AbstractProcessInstanceAttribute {
	public static final String _VALUE = "value";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_PROC_INST_S_ATTR")
			}
	)
    @Index(name="idx_p_s_attribute_id")
	@Column(name = "id")
	protected Long id;

    @Column(name="value_", length = 255)
	private String value;

    public ProcessInstanceSimpleAttribute() {
    }

    public ProcessInstanceSimpleAttribute(String key, String value) {
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

package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * User: POlszewski
 * Date: 2011-11-25
 * Time: 14:21:26
 */
@Entity
@Table(name="pt_sequence")
public class ProcessToolSequence extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@Parameter(name = "initial_value", value = "" + 1),
					@Parameter(name = "value_column", value = "_DB_ID"),
					@Parameter(name = "sequence_name", value = "DB_SEQ_ID_PT_SEQ")
			}
	)
	@Column(name = "id")
	protected Long id;

	private String processDefinitionName;
	private String name;
	private long value;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}

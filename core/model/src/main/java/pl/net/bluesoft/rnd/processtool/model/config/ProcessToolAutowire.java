package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.Cacheable;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

@Entity
@Table(name = "pt_autowire")
public class ProcessToolAutowire extends AbstractPersistentEntity implements Cacheable<String, String> {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@Parameter(name = "initial_value", value = "" + 1),
					@Parameter(name = "value_column", value = "_DB_ID"),
					@Parameter(name = "sequence_name", value = "DB_SEQ_ID_PT_AUTOWIRE")
			}
	)
	@Column(name = "id")
	protected Long id;

	private String key;
    private String value;
    private String description;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

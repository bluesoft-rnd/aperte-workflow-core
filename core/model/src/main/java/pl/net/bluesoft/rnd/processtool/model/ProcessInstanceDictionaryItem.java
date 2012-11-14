package pl.net.bluesoft.rnd.processtool.model;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/7/12
 * Time: 3:31 PM
 */

@Entity
@Table(name = "pt_pi_dict_item")
public class ProcessInstanceDictionaryItem extends AbstractPersistentEntity {

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_DICT_ITEM")
			}
	)
	@Column(name = "id")
	protected Long id;

    @Column(name = "key_")
    private String key;

    @Column(name = "value_")
    private String value;

    @ManyToOne
    @JoinColumn
    private ProcessInstanceDictionaryAttribute dictionary;

    public ProcessInstanceDictionaryItem() {
    }

    public ProcessInstanceDictionaryItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProcessInstanceDictionaryAttribute getDictionary() {
        return dictionary;
    }

    public void setDictionary(ProcessInstanceDictionaryAttribute dictionary) {
        this.dictionary = dictionary;
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




}

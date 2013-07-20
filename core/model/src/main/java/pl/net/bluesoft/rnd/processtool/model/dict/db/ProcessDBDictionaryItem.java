package pl.net.bluesoft.rnd.processtool.model.dict.db;

import java.util.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;

import org.hibernate.annotations.*;

import org.hibernate.annotations.CascadeType;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;

@Entity
@Table(name = "pt_dictionary_item")
public class ProcessDBDictionaryItem extends AbstractPersistentEntity implements ProcessDictionaryItem {
	public static final String _DICTIONARY = "dictionary";
	public static final String _KEY = "key";
	public static final String _VALUE_TYPE = "valueType";
	public static final String _DESCRIPTION = "description";
	public static final String _VALUES = "values";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_DB_DICT_ITEM")
			}
	)
	@Column(name = "id")
	protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(value = {CascadeType.REFRESH})
    private ProcessDBDictionary dictionary;

    @Column(name="key_", nullable=false)
    private String key;
    private String valueType;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(value = CascadeType.ALL)
    private Set<ProcessDBDictionaryItemValue> values = new HashSet<ProcessDBDictionaryItemValue>();

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public void setDictionary(ProcessDBDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public ProcessDBDictionary getDictionary() {
        return dictionary;
    }

    @Override
	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
	public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Set<ProcessDBDictionaryItemValue> getValues() {
        return values;
    }

    public void setValues(Set<ProcessDBDictionaryItemValue> values) {
        this.values = values;
    }

	public void addValue(ProcessDBDictionaryItemValue value) {
		value.setItem(this);
		values.add(value);
	}

	public void removeValue(ProcessDBDictionaryItemValue value) {
		value.setItem(null);
		values.remove(value);
	}

    @Override
    public Collection<ProcessDictionaryItemValue> values() {
        return Collections.unmodifiableCollection((Set)values);
    }

    @Override
    public ProcessDBDictionaryItemValue getValueForCurrentDate() {
        return getValueForDate(new Date());
    }

    @Override
	public ProcessDBDictionaryItemValue getValueForDate(Date date) {
        for (ProcessDBDictionaryItemValue value : values) {
            if (value.isValidForDate(date)) {
                return value;
            }
        }
        return null;
    }
}

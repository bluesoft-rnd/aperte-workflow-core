package pl.net.bluesoft.rnd.processtool.model.dict.db;

import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;

@Entity
@Table(name = "pt_dictionary_item_ext")
public class ProcessDBDictionaryItemExtension extends AbstractPersistentEntity implements ProcessDictionaryItemExtension<String> {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_DB_DICT_ITEM_EXT")
			}
	)
	@Column(name = "id")
	protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(value = {CascadeType.ALL})
    private ProcessDBDictionaryItemValue itemValue;

    private String name;
    @Column(name="value_")
    private String value;
    @Column(name="description_")
    private String description;
    private String valueType;

    public ProcessDBDictionaryItemExtension() {
    }

    private ProcessDBDictionaryItemExtension(ProcessDBDictionaryItemExtension ext) {
        id = ext.getId();
        name = ext.getName();
        value = ext.getValue();
        valueType = ext.getValue();
        description = ext.getDescription();
        itemValue = ext.getItemValue();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProcessDBDictionaryItemExtension exactCopy() {
        return new ProcessDBDictionaryItemExtension(this);
    }

    public ProcessDBDictionaryItemExtension shallowCopy() {
        ProcessDBDictionaryItemExtension ext = exactCopy();
        ext.setItemValue(null);
        ext.setId(null);
        return ext;
    }

    public ProcessDBDictionaryItemValue getItemValue() {
        return itemValue;
    }

    public void setItemValue(ProcessDBDictionaryItemValue itemValue) {
        this.itemValue = itemValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	public String getStringValue() {
		return getValue();
	}

	public void setStringValue(String value) {
		setValue(value);
	}

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

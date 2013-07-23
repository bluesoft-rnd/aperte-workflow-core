package pl.net.bluesoft.rnd.processtool.model.dict.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;

@Entity
@Table(name = "pt_dictionary_item_ext")
public class ProcessDBDictionaryItemExtension extends PersistentEntity implements ProcessDictionaryItemExtension {
    public static final String _ITEM_VALUE = "itemValue";
	public static final String _NAME = "name";
	public static final String _VALUE = "value";
	public static final String _DESCRIPTION = "description";
	public static final String _VALUE_TYPE = "valueType";

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
        name = ext.name;
        value = ext.value;
        valueType = ext.value;
        description = ext.description;
        itemValue = ext.itemValue;
    }

    public ProcessDBDictionaryItemExtension exactCopy() {
        return new ProcessDBDictionaryItemExtension(this);
    }

    public ProcessDBDictionaryItemValue getItemValue() {
        return itemValue;
    }

    public void setItemValue(ProcessDBDictionaryItemValue itemValue) {
        this.itemValue = itemValue;
    }

    @Override
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
	public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
	public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    @Override
	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

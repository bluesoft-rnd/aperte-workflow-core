package pl.net.bluesoft.rnd.processtool.model.dict;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "pt_dictitem_ext")
public class ProcessDBDictionaryItemExtension extends PersistentEntity implements ProcessDictionaryItemExtension<String> {
    @ManyToOne
    @JoinColumn(name = "item_id")
    private ProcessDBDictionaryItem item;

    private String name;
    private String value;
    private String valueType;

    public ProcessDBDictionaryItemExtension() {
    }

    public ProcessDBDictionaryItemExtension(ProcessDBDictionaryItemExtension ext) {
        id = ext.getId();
        name = ext.getName();
        value = ext.getValue();
        valueType = ext.getValue();
        item = ext.getItem();
    }

    public ProcessDBDictionaryItem getItem() {
        return item;
    }

    public void setItem(ProcessDBDictionaryItem item) {
        this.item = item;
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

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
}

package pl.net.bluesoft.rnd.processtool.model.dict.db;

//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "pt_dictionary_item")
public class ProcessDBDictionaryItem extends PersistentEntity
        implements ProcessDictionaryItem<String, String> {
    @ManyToOne
    private ProcessDBDictionary dictionary;

    @Column(name="key_")
    private String key;
    private String valueType;
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ProcessDBDictionaryItemValue> values = new HashSet<ProcessDBDictionaryItemValue>();

    public void setDictionary(ProcessDBDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public ProcessDBDictionary getDictionary() {
        return dictionary;
    }

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

    @Override
    public Collection<ProcessDictionaryItemValue<String>> values() {
        return new HashSet<ProcessDictionaryItemValue<String>>(values);
    }

    @Override
    public void addValue(ProcessDictionaryItemValue<String> value) {
        ProcessDBDictionaryItemValue val = (ProcessDBDictionaryItemValue) value;
        val.setItem(this);
        values.add(val);
    }

    @Override
    public void removeValue(ProcessDictionaryItemValue<String> value) {
        values.remove(value);
    }

    @Override
    public ProcessDBDictionaryItemValue getValueForCurrentDate() {
        return getValueForDate(new Date());
    }

    public ProcessDBDictionaryItemValue getValueForDate(Date date) {
        for (ProcessDBDictionaryItemValue value : values) {
            if (value.isValidForDate(date)) {
                return value;
            }
        }
        return null;
    }
}

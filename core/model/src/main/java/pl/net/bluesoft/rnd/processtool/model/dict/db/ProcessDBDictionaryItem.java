package pl.net.bluesoft.rnd.processtool.model.dict.db;

//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;

@Entity
@Table(name = "pt_dictionary_item")
public class ProcessDBDictionaryItem extends PersistentEntity implements ProcessDictionaryItem<String, String> 
{
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(value = {CascadeType.REFRESH})
    private ProcessDBDictionary dictionary;

    @Column(name="key_")
    private String key;
    private String valueType;
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(value = CascadeType.ALL)
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

	public String getStringKey() {
		return getKey();
	}

	public void setStringKey(String key) {
		setKey(key);
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

	public Set<ProcessDBDictionaryItemValue> getDbValues() {
		return getValues();
	}

	public void setDbValues(Set<ProcessDBDictionaryItemValue> values) {
		setValues(values);
	}

    @Override
    public Collection<ProcessDictionaryItemValue<String>> values() {
        return new HashSet<ProcessDictionaryItemValue<String>>(values);
    }

	public Collection<ProcessDictionaryItemValue<String>> dbValues() {
		return values();
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

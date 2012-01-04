package pl.net.bluesoft.rnd.processtool.model.dict;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "pt_dict_item")
public class ProcessDBDictionaryItem extends PersistentEntity implements ProcessDictionaryItem<String, String> {
    @ManyToOne
    @JoinColumn(name = "dict_id")
    private ProcessDBDictionary dictionary;

    @Column(name = "key_")
    private String key;
    @Column(name = "value_")
    private String value;
    private String valueType;
    @Column(length = 2048)
    private String description;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @MapKey(name = "name")
    private Map<String, ProcessDBDictionaryItemExtension> extensions = new HashMap<String, ProcessDBDictionaryItemExtension>();

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

    @Override
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

    public void removeItemExtension(String name) {
        ProcessDBDictionaryItemExtension ext = extensions.get(key);
        if (ext != null) {
            ext.setItem(null);
            extensions.remove(key);
        }
    }

    public void addItemExtension(ProcessDBDictionaryItemExtension itemExtension) {
        itemExtension.setItem(this);
        extensions.put(itemExtension.getName(), itemExtension);
    }

    public Map<String, ProcessDBDictionaryItemExtension> getExtensions() {
        return extensions;
    }

    @Override
    public Collection<ProcessDictionaryItemExtension> extensions() {
        Set<ProcessDictionaryItemExtension> set = new HashSet<ProcessDictionaryItemExtension>();
        for (ProcessDictionaryItemExtension ext : extensions.values()) {
            set.add(ext);
        }
        return set;
    }

    public Collection<String> getExtensionNames() {
        return extensions.keySet();
    }

    public ProcessDBDictionaryItemExtension getExtensionByName(String extensionName) {
        return extensions.get(extensionName);
    }

    public void setExtensions(Map<String, ProcessDBDictionaryItemExtension> extensions) {
        this.extensions = extensions;
    }
}

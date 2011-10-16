package pl.net.bluesoft.rnd.processtool.model.dict;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "pt_dictionary")
public class ProcessDBDictionary extends PersistentEntity implements ProcessDictionary<String, String> {
    private String dictionaryId;
    private String dictionaryName;
    private String languageCode;
    @Column(length = 2048)
    private String description;

    private Boolean defaultDictionary = Boolean.FALSE;

    public Boolean isDefaultDictionary() {
        return defaultDictionary;
    }

    public void setDefaultDictionary(Boolean defaultDictionary) {
        this.defaultDictionary = defaultDictionary;
    }

    @ManyToOne
    @JoinColumn(name = "definition_id")
    private ProcessDefinitionConfig processDefinition;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @MapKey(name = "key")
    private Map<String, ProcessDBDictionaryItem> items = new HashMap<String, ProcessDBDictionaryItem>();

    @Override
    public ProcessDictionaryItem<String, String> lookup(String key) {
        return items.get(key);
    }

    @Override
    public Collection<String> itemKeys() {
        return items.keySet();
    }

    public void removeItem(String key) {
        ProcessDBDictionaryItem item = items.get(key);
        if (item != null) {
            item.setDictionary(null);
            items.remove(key);
        }
    }

    @Override
    public Collection<ProcessDictionaryItem<String, String>> items() {
        Set<ProcessDictionaryItem<String, String>> dictItems = new HashSet<ProcessDictionaryItem<String, String>>();
        for (ProcessDBDictionaryItem item : items.values()) {
            dictItems.add(item);
        }
        return dictItems;
    }

    public ProcessDefinitionConfig getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinitionConfig processDefinition) {
        this.processDefinition = processDefinition;
    }

    @Override
    public String getDictionaryId() {
        return dictionaryId;
    }

    public void setDictionaryId(String dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getDictionaryName() {
        return dictionaryName;
    }

    public void setDictionaryName(String dictionaryName) {
        this.dictionaryName = dictionaryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addItem(ProcessDBDictionaryItem item) {
        item.setDictionary(this);
        items.put(item.getKey(), item);
    }

    public Map<String, ProcessDBDictionaryItem> getItems() {
        return items;
    }

    public void setItems(Map<String, ProcessDBDictionaryItem> items) {
        this.items = items;
    }
}

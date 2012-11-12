package pl.net.bluesoft.rnd.processtool.model.dict.db;

//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;

@Entity
@Table(name = "pt_dictionary")
public class ProcessDBDictionary extends PersistentEntity implements ProcessDictionary<String, String> {
    private String dictionaryId;
    private String dictionaryName;
    private String languageCode;
    @Column(length = 2048, name="description_")
    private String description;

    private Boolean defaultDictionary = Boolean.FALSE;

    @OneToMany(mappedBy = "dictionary", fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(value = CascadeType.ALL)
    private Set<ProcessDBDictionaryPermission> permissions = new HashSet<ProcessDBDictionaryPermission>();

    @OneToMany(mappedBy = "dictionary", fetch = FetchType.EAGER, orphanRemoval = true)
    @MapKey(name = "key")
    @Cascade(value = CascadeType.ALL)
    private Map<String, ProcessDBDictionaryItem> items = new HashMap<String, ProcessDBDictionaryItem>();

    @ManyToOne(optional = true)
    @JoinColumn(name = "definition_id")
    @Cascade(value = CascadeType.REFRESH)
    private ProcessDefinitionConfig processDefinition;

    public Set<ProcessDBDictionaryPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<ProcessDBDictionaryPermission> permissions) {
        this.permissions = permissions;
    }

    public Boolean isDefaultDictionary() {
        return defaultDictionary;
    }

    public void setDefaultDictionary(Boolean defaultDictionary) {
        this.defaultDictionary = defaultDictionary;
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

    @Override
    public ProcessDictionaryItem<String, String> lookup(String key) {
        return items.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return items.containsKey(key);
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

    public void addItem(ProcessDBDictionaryItem item) {
        item.setDictionary(this);
        if (items.containsKey(item.getKey())) {
            throw new IllegalArgumentException("Dictionary already contains an entry for key: " + item.getKey());
        }
        items.put(item.getKey(), item);
    }

    public void addPermission(ProcessDBDictionaryPermission permission) {
        permission.setDictionary(this);
        permissions.add(permission);
    }

    public Map<String, ProcessDBDictionaryItem> getItems() {
        return items;
    }

    public void setItems(Map<String, ProcessDBDictionaryItem> items) {
        this.items = items;
    }
}

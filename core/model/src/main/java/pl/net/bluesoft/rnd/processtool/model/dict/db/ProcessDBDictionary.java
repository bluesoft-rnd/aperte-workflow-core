package pl.net.bluesoft.rnd.processtool.model.dict.db;

//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.Parameter;
import javax.persistence.Table;

import org.hibernate.annotations.*;

import org.hibernate.annotations.CascadeType;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;

@Entity
@Table(name = "pt_dictionary")
public class ProcessDBDictionary extends AbstractPersistentEntity implements ProcessDictionary<String, String> {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_DB_DICT")
			}
	)
	@Column(name = "id")
	protected Long id;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
    
    public boolean isGlobalDictionary()
    {
    	return getProcessDefinition() == null;
    }
    
    @Override
    public String toString() {
    	return dictionaryName;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dictionaryId == null) ? 0 : dictionaryId.hashCode());
		result = prime * result
				+ ((dictionaryName == null) ? 0 : dictionaryName.hashCode());
		result = prime * result
				+ ((languageCode == null) ? 0 : languageCode.hashCode());
		result = prime
				* result
				+ ((processDefinition == null) ? 0 : processDefinition
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessDBDictionary other = (ProcessDBDictionary) obj;
		if (dictionaryId == null) {
			if (other.dictionaryId != null)
				return false;
		} else if (!dictionaryId.equals(other.dictionaryId))
			return false;
		if (dictionaryName == null) {
			if (other.dictionaryName != null)
				return false;
		} else if (!dictionaryName.equals(other.dictionaryName))
			return false;
		if (languageCode == null) {
			if (other.languageCode != null)
				return false;
		} else if (!languageCode.equals(other.languageCode))
			return false;
		if (processDefinition == null) {
			if (other.processDefinition != null)
				return false;
		} else if (!processDefinition.equals(other.processDefinition))
			return false;
		return true;
	}
    
    
}

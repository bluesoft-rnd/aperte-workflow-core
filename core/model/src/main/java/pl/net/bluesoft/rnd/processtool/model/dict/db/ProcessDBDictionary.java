package pl.net.bluesoft.rnd.processtool.model.dict.db;

import java.util.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.Table;

import org.hibernate.annotations.*;

import org.hibernate.annotations.CascadeType;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;

@Entity
@Table(name = "pt_dictionary")
public class ProcessDBDictionary extends AbstractPersistentEntity implements ProcessDictionary {
	public static final String _DICTIONARY_ID = "dictionaryId";
	public static final String _DEFAULT_NAME = "defaultName";
	public static final String _LOCALIZED_NAMES = "localizedNames";
	public static final String _PERMISSIONS = "permissions";
	public static final String _ITEMS = "items";

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
    private String defaultName;

	@OneToMany(orphanRemoval = true)
	@Cascade(value = CascadeType.ALL)
	@JoinColumn(name = "dictionary_id", nullable = true)
	@Fetch(value = FetchMode.SELECT)
	private List<ProcessDBDictionaryI18N> localizedNames = new ArrayList<ProcessDBDictionaryI18N>();

	private String description;

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(value = CascadeType.ALL)
	@JoinColumn(name = "dictionary_id", nullable = true)
    private Set<ProcessDBDictionaryPermission> permissions = new HashSet<ProcessDBDictionaryPermission>();

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(value = CascadeType.ALL)
	@JoinColumn(name = "dictionary_id", nullable = true)
	@MapKey(name = "key")
    private Map<String, ProcessDBDictionaryItem> items = new HashMap<String, ProcessDBDictionaryItem>();

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getDictionaryId() {
		return dictionaryId;
	}

	public void setDictionaryId(String dictionaryId) {
		this.dictionaryId = dictionaryId;
	}

	@Override
	public String getDefaultName() {
		return defaultName;
	}

	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	public List<ProcessDBDictionaryI18N> getLocalizedNames() {
		return localizedNames;
	}

	public void setLocalizedNames(List<ProcessDBDictionaryI18N> localizedNames) {
		this.localizedNames = localizedNames;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<ProcessDBDictionaryPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<ProcessDBDictionaryPermission> permissions) {
		this.permissions = permissions;
	}

	public Map<String, ProcessDBDictionaryItem> getItems() {
		return items;
	}

	public void setItems(Map<String, ProcessDBDictionaryItem> items) {
		this.items = items;
	}

	@Override
	public String getName(String languageCode) {
		return ProcessDBDictionaryI18N.getLocalizedText(localizedNames, languageCode, defaultName);
	}

	@Override
	public String getName(Locale locale) {
		return getName(locale.toString());
	}

	public void setName(String languageCode, String name) {
		if (languageCode == null) {
			this.defaultName = name;
			return;
		}
		ProcessDBDictionaryI18N.setLocalizedText(localizedNames, languageCode, name);
	}

	@Override
    public ProcessDictionaryItem lookup(String key) {
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
    public Collection<ProcessDictionaryItem> items() {
        return Collections.unmodifiableCollection((Set)items.values());
    }

    public void addItem(ProcessDBDictionaryItem item) {
        if (items.containsKey(item.getKey())) {
            throw new IllegalArgumentException("Dictionary already contains an entry for key: " + item.getKey());
        }
		item.setDictionary(this);
        items.put(item.getKey(), item);
    }

    public void addPermission(ProcessDBDictionaryPermission permission) {
        permissions.add(permission);
    }
    
    @Override
    public String toString() {
    	return defaultName;
    }

	public Set<String> getUsedLanguageCodes() {
		Set<String> result = new HashSet<String>();

		for (ProcessDBDictionaryI18N localizedName : localizedNames) {
			result.add(localizedName.getLanguageCode());
		}

		for (ProcessDBDictionaryItem item : items.values()) {
			for (ProcessDBDictionaryItemValue value : item.getValues()) {
				for (ProcessDBDictionaryI18N localizedValue : value.getLocalizedValues()) {
					result.add(localizedValue.getLanguageCode());
				}
			}
		}
		return result;
	}
}

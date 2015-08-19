package pl.net.bluesoft.rnd.processtool.dict.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.Collections;
import java.util.List;

@XStreamAlias("dict")
public class Dictionary {
    @XStreamAsAttribute
    private String id;
    @XStreamAsAttribute
    private String name;
	@XStreamImplicit(itemFieldName = "i18n-name")
	private List<DictionaryI18N> localizedNames;
    @XStreamAsAttribute
    private String description;

    @XStreamImplicit
    private List<DictionaryEntry> entries;

    @XStreamImplicit
    private List<DictionaryPermission> permissions;

	@XStreamImplicit
	private List<DictionaryDefaultEntryExtension> defaultExtensions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public List<DictionaryI18N> getLocalizedNames() {
		return localizedNames != null ? localizedNames : Collections.<DictionaryI18N>emptyList();
	}

	public void setLocalizedNames(List<DictionaryI18N> localizedNames) {
		this.localizedNames = localizedNames;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DictionaryEntry> getEntries() {
        return entries != null ? entries : Collections.<DictionaryEntry>emptyList();
    }

    public void setEntries(List<DictionaryEntry> entries) {
        this.entries = entries;
    }

    public List<DictionaryPermission> getPermissions() {
        return permissions != null ? permissions : Collections.<DictionaryPermission>emptyList();
    }

    public void setPermissions(List<DictionaryPermission> permissions) {
        this.permissions = permissions;
    }

	public List<DictionaryDefaultEntryExtension> getDefaultExtensions() {
		return defaultExtensions;
	}

	public void setDefaultExtensions(List<DictionaryDefaultEntryExtension> defaultExtensions) {
		this.defaultExtensions = defaultExtensions;
	}
}

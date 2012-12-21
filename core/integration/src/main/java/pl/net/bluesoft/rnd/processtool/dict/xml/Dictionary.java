package pl.net.bluesoft.rnd.processtool.dict.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("dict")
public class Dictionary {
    @XStreamAsAttribute
    private String dictionaryId;
    @XStreamAsAttribute
    private String dictionaryName;
    @XStreamAsAttribute
    private String languageCode;
    @XStreamAsAttribute
    private String description;

    @XStreamImplicit
    private List<DictionaryEntry> entries;

    @XStreamImplicit
    private List<DictionaryPermission> permissions;

    public String getDictionaryId() {
        return dictionaryId;
    }

    public void setDictionaryId(String dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    public String getDictionaryName() {
        return dictionaryName;
    }

    public void setDictionaryName(String dictionaryName) {
        this.dictionaryName = dictionaryName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DictionaryEntry> getEntries() {
        return entries == null ? (entries = new ArrayList<DictionaryEntry>()) : entries;
    }

    public void setEntries(List<DictionaryEntry> entries) {
        this.entries = entries;
    }

    public List<DictionaryPermission> getPermissions() {
        return permissions == null ? (permissions = new ArrayList<DictionaryPermission>()) : permissions;
    }

    public void setPermissions(List<DictionaryPermission> permissions) {
        this.permissions = permissions;
    }
}

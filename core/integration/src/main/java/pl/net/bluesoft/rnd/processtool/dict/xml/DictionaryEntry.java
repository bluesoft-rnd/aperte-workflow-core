package pl.net.bluesoft.rnd.processtool.dict.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.Collections;
import java.util.List;

@XStreamAlias("entry")
public class DictionaryEntry {
    @XStreamAsAttribute
    private String key;
    @XStreamAsAttribute
    private String description;
    @XStreamImplicit(itemFieldName = "i18n-description")
    private List<DictionaryI18N> localizedDescriptions;
    @XStreamAsAttribute
    private String valueType;
    @XStreamImplicit
    private List<DictionaryEntryValue> values;

    public List<DictionaryEntryValue> getValues() {
        return values != null ? values : Collections.<DictionaryEntryValue>emptyList();
    }

    public void setValues(List<DictionaryEntryValue> values) {
        this.values = values;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DictionaryI18N> getLocalizedDescriptions() {
        return localizedDescriptions != null ? localizedDescriptions : Collections.<DictionaryI18N>emptyList();
    }

    public void setLocalizedDescriptions(List<DictionaryI18N> localizedDescriptions) {
        this.localizedDescriptions = localizedDescriptions;
    }
}

package pl.net.bluesoft.rnd.processtool.dict.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("entry")
public class DictionaryEntry {
    @XStreamAsAttribute
    private String key;
    @XStreamAsAttribute
    private String value;
    @XStreamAsAttribute
    private String description;
    @XStreamAsAttribute
    private String valueType;
    @XStreamImplicit
    private List<DictionaryEntryExtension> extensions;

    public List<DictionaryEntryExtension> getExtensions() {
        return extensions == null ? (extensions = new ArrayList<DictionaryEntryExtension>()) : extensions;
    }

    public void setExtensions(List<DictionaryEntryExtension> extensions) {
        this.extensions = extensions;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

package pl.net.bluesoft.rnd.pt.dict.global.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("ext")
public class DictionaryEntryExtension {
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private String value;
    @XStreamAsAttribute
    private String valueType;
    @XStreamAsAttribute
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.RequiredAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "item")
@XStreamAlias("item")
public class ItemElement implements Serializable {

    @XmlAttribute
    @XStreamAsAttribute
    @RequiredAttribute
    @AperteDoc(humanNameKey = "item.key.humanName", descriptionKey = "item.key.description")
    private String key;

    @XmlAttribute
    @XStreamAsAttribute
    @RequiredAttribute
    @AperteDoc(humanNameKey = "item.value.humanName", descriptionKey = "item.value.description")
    private String value;

    public ItemElement() {
    }

    public ItemElement(String key, String value) {
        this.key = key;
        this.value = value;
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

    public List<XmlValidationError> validateElement() {
        List<XmlValidationError> errors = new ArrayList<XmlValidationError>();
        if (!(StringUtil.hasText(value) && StringUtil.hasText(key))) {
            errors.add(new XmlValidationError("item", "[key & value]", XmlConstants.XML_TAG_EMPTY));
        }
        return errors;
    }
}

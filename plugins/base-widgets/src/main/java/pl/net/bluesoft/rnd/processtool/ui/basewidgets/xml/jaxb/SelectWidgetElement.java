package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "select")
@XStreamAlias("select")
public class SelectWidgetElement extends WidgetElement {
    @XmlElements({
            @XmlElement(name = "item", type = ItemElement.class)
    })
    @XStreamImplicit
    private List<ItemElement> values;

    @XmlElement
    private ScriptElement script;

    @XmlAttribute
    @XStreamAsAttribute
    private Integer defaultSelect;

    @XmlAttribute
    @XStreamAsAttribute
    private Boolean required;

    public List<ItemElement> getValues() {
        return values == null ? (values = new ArrayList<ItemElement>()) : values;
    }

    public void setValues(List<ItemElement> values) {
        this.values = values;
    }

    public ScriptElement getScript() {
        return script;
    }

    public void setScript(ScriptElement script) {
        this.script = script;
    }

    public Integer getDefaultSelect() {
        return defaultSelect;
    }

    public void setDefaultSelect(Integer defaultSelect) {
        this.defaultSelect = defaultSelect;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    @Override
    public List<XmlValidationError> validate() {
        List<XmlValidationError> errors = new ArrayList<XmlValidationError>();
        if (StringUtil.hasText(provider) && !StringUtil.hasText(dict)) {
            errors.add(new XmlValidationError("select", "dict", XmlConstants.XML_TAG_EMPTY));
        } else if (!StringUtil.hasText(provider) && StringUtil.hasText(dict)) {
            errors.add(new XmlValidationError("select", "provider", XmlConstants.XML_TAG_EMPTY));
        } else if (!(StringUtil.hasText(provider) && StringUtil.hasText(dict)) &&
                script == null && getValues().isEmpty()) {
            errors.add(new XmlValidationError("select", "[dict & provider | values | script]", XmlConstants.XML_TAG_EMPTY));
        } else if (script != null) {
            errors.addAll(script.validate());
        } else if (!getValues().isEmpty()) {
            for (ItemElement ie : getValues()) {
                errors.addAll(ie.validateElement());
            }
        }
        return errors;
    }
    @Override
    public List<XmlValidationError> validateElement() {
        List<XmlValidationError> errors = new ArrayList<XmlValidationError>();
        if (StringUtil.hasText(provider) && !StringUtil.hasText(dict)) {
            errors.add(new XmlValidationError("select", "dict", XmlConstants.XML_TAG_EMPTY));
        } else if (!StringUtil.hasText(provider) && StringUtil.hasText(dict)) {
            errors.add(new XmlValidationError("select", "provider", XmlConstants.XML_TAG_EMPTY));
        } else if (!(StringUtil.hasText(provider) && StringUtil.hasText(dict)) &&
                script == null && getValues().isEmpty()) {
            errors.add(new XmlValidationError("select", "[dict & provider | values | script]", XmlConstants.XML_TAG_EMPTY));
        }
        return errors;
    }
}

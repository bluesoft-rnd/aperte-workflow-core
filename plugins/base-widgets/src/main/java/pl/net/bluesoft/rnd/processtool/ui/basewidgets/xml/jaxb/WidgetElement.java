package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
@XmlTransient
public abstract class WidgetElement implements Serializable {
    @XmlAttribute
    @XStreamAsAttribute
    protected String width;
    @XmlAttribute
    @XStreamAsAttribute
    protected String height;
    @XmlAttribute
    @XStreamAsAttribute
    protected Boolean fullSize;
    @XmlAttribute
    @XStreamAsAttribute
    protected Boolean undefinedSize;
    @XmlAttribute
    @XStreamAsAttribute
    protected String bind;
    @XmlAttribute
    @XStreamAsAttribute
    protected String provider;
    @XmlAttribute
    @XStreamAsAttribute
    protected String dict;
    @XmlAttribute
    @XStreamAsAttribute
    protected Boolean readonly;
    @XmlAttribute
    @XStreamAsAttribute
    protected String caption;
    @XmlAttribute
    @XStreamAsAttribute
    protected String style;
    @XmlAttribute
    @XStreamAsAttribute
    protected String attributeClass;

    @XmlTransient
    @XStreamOmitField
    protected WidgetElement parent;

    public WidgetElement getParent() {
        return parent;
    }

    public void setParent(WidgetElement parent) {
        this.parent = parent;
    }

    public String getAttributeClass() {
        return attributeClass;
    }

    public String getInheritedAttributeClass() {
        return !StringUtil.hasText(attributeClass) && parent != null ? parent.getInheritedAttributeClass() : attributeClass;
    }

    public void setAttributeClass(String attributeClass) {
        this.attributeClass = attributeClass;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public Boolean getFullSize() {
        return fullSize;
    }

    public void setFullSize(Boolean fullSize) {
        this.fullSize = fullSize;
    }

    public Boolean getUndefinedSize() {
        return undefinedSize;
    }

    public void setUndefinedSize(Boolean undefinedSize) {
        this.undefinedSize = undefinedSize;
    }

    public String getBind() {
        return bind;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDict() {
        return dict;
    }

    public void setDict(String dict) {
        this.dict = dict;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public  List<XmlValidationError> validate() {
        return validateElement();
    }
    public List<XmlValidationError> validateElement() {
        return new ArrayList<XmlValidationError>();
    }
}

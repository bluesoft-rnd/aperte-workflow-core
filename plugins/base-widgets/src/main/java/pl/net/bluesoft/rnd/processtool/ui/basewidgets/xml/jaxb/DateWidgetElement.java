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
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "date")
@XStreamAlias("date")
public class DateWidgetElement extends WidgetElement {

    @XmlAttribute
    @XStreamAsAttribute
    @RequiredAttribute
    @AperteDoc(humanNameKey = "date.format", descriptionKey = "date.format.description")
    private String format;

    @XmlAttribute(name = "not-before")
    @XStreamAsAttribute
    @XStreamAlias("not-before")
    @AperteDoc(humanNameKey = "date.notBefore", descriptionKey = "date.notBefore.description")
    private String notBefore;

    @XmlAttribute(name = "not-after")
    @XStreamAsAttribute
    @XStreamAlias("not-after")
    @AperteDoc(humanNameKey = "date.notAfter", descriptionKey = "date.notAfter.description")
    private String notAfter;

    @XmlAttribute
    @XStreamAsAttribute
    @AperteDoc(humanNameKey = "date.showMinutes", descriptionKey = "date.showMinutes.description")
    private Boolean showMinutes;

    @XmlAttribute
    @XStreamAsAttribute
    @AperteDoc(humanNameKey = "date.discludeNotBefore", descriptionKey = "date.discludeNotBefore.description")
    private Boolean discludeNotBefore;
    
    @XmlAttribute
    @XStreamAsAttribute
    @AperteDoc(humanNameKey = "date.discludeNotAfter", descriptionKey = "date.discludeNotAfter.description")
    private Boolean discludeNotAfter;
    
    @XmlAttribute
    @XStreamAsAttribute
    @AperteDoc(humanNameKey = "any.required", descriptionKey = "any.required.description")
    private Boolean required;

    public Boolean getShowMinutes() {
        return showMinutes;
    }

    public void setShowMinutes(Boolean showMinutes) {
        this.showMinutes = showMinutes;
    }
    
    public Boolean getDiscludeNotAfter() {
        return discludeNotAfter;
    }

    public void setDiscludeNotAfter(Boolean discludeNotAfter) {
        this.discludeNotAfter = discludeNotAfter;
    }
    
    public Boolean getDiscludeNotBefore() {
        return discludeNotBefore;
    }

    public void setDiscludeNotBefore(Boolean discludeNotBefore) {
        this.discludeNotBefore = discludeNotBefore;
    }    

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(String notBefore) {
        this.notBefore = notBefore;
    }

    public String getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(String notAfter) {
        this.notAfter = notAfter;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    @Override
    public List<XmlValidationError> validateElement() {
        List<XmlValidationError> errors = new ArrayList<XmlValidationError>();
        if (!StringUtil.hasText(format)) {
            errors.add(new XmlValidationError("date", "format", XmlConstants.XML_TAG_EMPTY));
        }
        return errors;
    }
}

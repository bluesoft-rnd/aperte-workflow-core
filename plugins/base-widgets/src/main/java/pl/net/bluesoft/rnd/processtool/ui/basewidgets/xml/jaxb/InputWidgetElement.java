package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "input")
@XStreamAlias("input")
@XmlAccessorType(XmlAccessType.FIELD)
public class InputWidgetElement extends WidgetElement {
    @XmlAttribute
    @XStreamAsAttribute
    private Boolean secret;
    @XmlAttribute
    @XStreamAsAttribute
    private Integer maxLength;
    @XmlAttribute
    @XStreamAsAttribute
    private String regexp;
    @XmlAttribute
    @XStreamAsAttribute
    private String prompt;
    @XmlAttribute
    @XStreamAsAttribute
    private String errorKey;
    @XmlAttribute
    @XStreamAsAttribute
    private String baseText;
    @XmlAttribute
    @XStreamAsAttribute
    private Boolean required;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getBaseText() {
        return baseText;
    }

    public void setBaseText(String baseText) {
        this.baseText = baseText;
    }

    public String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public Boolean getSecret() {
        return secret;
    }

    public void setSecret(Boolean secret) {
        this.secret = secret;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
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
        if (!StringUtil.hasText(regexp) && StringUtil.hasText(errorKey) || StringUtil.hasText(regexp) && !StringUtil.hasText(errorKey)) {
            errors.add(new XmlValidationError("input", "[regexp & errorKey]", XmlConstants.XML_TAG_INVALID));
        }
        return errors;
    }
}

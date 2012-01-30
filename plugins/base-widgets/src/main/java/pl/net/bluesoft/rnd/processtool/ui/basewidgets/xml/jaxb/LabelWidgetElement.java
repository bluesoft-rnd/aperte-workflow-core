package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.AvailableOptions;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.RequiredAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "label")
@XStreamAlias("label")
public class LabelWidgetElement extends WidgetElement {
    @XmlAttribute
    @XStreamAsAttribute
    @AvailableOptions(
            value = {"0", "1", "2", "3", "4", "5"}
    )
    private Integer mode;

    @XmlAttribute
    @XStreamAsAttribute
    @RequiredAttribute
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    @Override
    public List<XmlValidationError> validateElement() {
        List<XmlValidationError> errors = new ArrayList<XmlValidationError>();
        if (!StringUtil.hasText(text)) {
            errors.add(new XmlValidationError("label", "[text | messageKey]", XmlConstants.XML_TAG_EMPTY));
        }
        return errors;
    }
}

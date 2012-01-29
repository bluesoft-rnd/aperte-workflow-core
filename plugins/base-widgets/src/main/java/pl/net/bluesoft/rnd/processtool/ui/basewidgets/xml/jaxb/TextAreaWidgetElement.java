package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "text")
@XStreamAlias("text")
public class TextAreaWidgetElement extends WidgetElement {
    @XmlAttribute
    @XStreamAsAttribute
    private Boolean rich;
    @XmlAttribute
    @XStreamAsAttribute
    private Integer visibleLines;
    @XmlAttribute
    @XStreamAsAttribute
    private Integer limit;
    @XmlAttribute
    @XStreamAsAttribute
    private Boolean required;

    public Boolean getRich() {
        return rich;
    }

    public void setRich(Boolean rich) {
        this.rich = rich;
    }

    public Integer getVisibleLines() {
        return visibleLines;
    }

    public void setVisibleLines(Integer visibleLines) {
        this.visibleLines = visibleLines;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}

package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.RequiredAttribute;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "text")
@XStreamAlias("text")
public class TextAreaWidgetElement extends WidgetElement {

    @XmlAttribute
    @XStreamAsAttribute
    @AperteDoc(humanNameKey = "textArea.rich.humanName", descriptionKey = "textArea.rich.description")
    private Boolean rich;

    @XmlAttribute
    @XStreamAsAttribute
    @AperteDoc(humanNameKey = "textArea.visibleLines.humanName", descriptionKey = "textArea.visibleLines.description")
    private Integer visibleLines;

    @XmlAttribute
    @XStreamAsAttribute
    @RequiredAttribute
    @AperteDoc(humanNameKey = "any.maxLength.humanName", descriptionKey = "any.maxLength.description")
    private Integer limit;

    @XmlAttribute
    @XStreamAsAttribute
    @AperteDoc(humanNameKey = "any.required.humanName", descriptionKey = "any.required.description")
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

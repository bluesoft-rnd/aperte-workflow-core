package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public abstract class HasWidgetsElement extends WidgetElement {
    @XmlElements({
            @XmlElement(name = "align", type = AlignElement.class),
            @XmlElement(name = "checkbox", type = CheckBoxWidgetElement.class),
            @XmlElement(name = "date", type = DateWidgetElement.class),
            @XmlElement(name = "form", type = FormWidgetElement.class),
            @XmlElement(name = "grid", type = GridWidgetElement.class),
            @XmlElement(name = "hl", type = HorizontalLayoutWidgetElement.class),
            @XmlElement(name = "input", type = InputWidgetElement.class),
            @XmlElement(name = "label", type = LabelWidgetElement.class),
            @XmlElement(name = "link", type = LinkWidgetElement.class),
            @XmlElement(name = "script", type = ScriptElement.class),
            @XmlElement(name = "select", type = SelectWidgetElement.class),
            @XmlElement(name = "text", type = TextAreaWidgetElement.class),
            @XmlElement(name = "vl", type = VerticalLayoutWidgetElement.class),
            @XmlElement(name = "upload", type = UploadWidgetElement.class)
    })
    @XStreamImplicit
    protected List<WidgetElement> widgets;

    @XmlAttribute
    @XStreamAsAttribute
    protected Boolean spacing;

    public List<WidgetElement> getWidgets() {
        return widgets == null ? (widgets = new ArrayList<WidgetElement>()) : widgets;
    }

    public void setWidgets(List<WidgetElement> widgets) {
        this.widgets = widgets;
    }

    public Boolean getSpacing() {
        return spacing;
    }

    public void setSpacing(Boolean spacing) {
        this.spacing = spacing;
    }

    @Override
    public List<XmlValidationError> validate() {
        List<XmlValidationError> errors = new ArrayList<XmlValidationError>();
        for (WidgetElement we : getWidgets()) {
            errors.addAll(we.validate());
        }
        return errors;
    }
}

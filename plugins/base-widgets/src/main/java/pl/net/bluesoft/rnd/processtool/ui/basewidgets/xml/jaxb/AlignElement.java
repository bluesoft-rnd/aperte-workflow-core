package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.AvailableOptions;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.RequiredAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import static pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants.*;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "align")
@XStreamAlias("align")
public class AlignElement extends HasWidgetsElement {
    @XmlAttribute
    @XStreamAsAttribute
    @RequiredAttribute
    @AvailableOptions(
            value = {
                    ALIGN_POS_LEFT_TOP,
                    ALIGN_POS_LEFT_BOTTOM,
                    ALIGN_POS_LEFT_MIDDLE,
                    ALIGN_POS_CENTER_TOP,
                    ALIGN_POS_CENTER_BOTTOM,
                    ALIGN_POS_CENTER_MIDDLE,
                    ALIGN_POS_RIGHT_TOP,
                    ALIGN_POS_RIGHT_BOTTOM,
                    ALIGN_POS_RIGHT_MIDDLE
            }
    )
    private String pos;

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    @Override
    public List<XmlValidationError> validateElement() {
        List<XmlValidationError> errors = new ArrayList<XmlValidationError>();
        if (!StringUtil.hasLength(pos)) {
            errors.add(new XmlValidationError("align", "pos", XML_TAG_EMPTY));
        }
        else if (!(ALIGN_POS_LEFT_TOP.equalsIgnoreCase(pos) || ALIGN_POS_LEFT_BOTTOM.equalsIgnoreCase(pos) || ALIGN_POS_LEFT_MIDDLE.equalsIgnoreCase(pos)
                || ALIGN_POS_CENTER_TOP.equalsIgnoreCase(pos) || ALIGN_POS_CENTER_BOTTOM.equalsIgnoreCase(pos) || ALIGN_POS_CENTER_MIDDLE.equalsIgnoreCase(pos)
                || ALIGN_POS_RIGHT_TOP.equalsIgnoreCase(pos) || ALIGN_POS_RIGHT_BOTTOM.equalsIgnoreCase(pos) || ALIGN_POS_RIGHT_MIDDLE.equalsIgnoreCase(pos))) {
            errors.add(new XmlValidationError("align", "pos", XML_TAG_INVALID));
        }
        return errors;
    }
}

package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor.RequiredAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "grid")
@XStreamAlias("grid")
public class GridWidgetElement extends HasWidgetsElement {
    @XmlAttribute
    @XStreamAsAttribute
    @RequiredAttribute
    @AperteDoc(humanNameKey = "grid.rows", descriptionKey = "grid.rows.description")
    private Integer rows;

    @XmlAttribute
    @XStreamAsAttribute
	@RequiredAttribute
    @AperteDoc(humanNameKey = "grid.cols", descriptionKey = "grid.cols.description")
    private Integer cols;

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getCols() {
        return cols;
    }

    public void setCols(Integer cols) {
        this.cols = cols;
    }

    @Override
    public List<XmlValidationError> validateElement() {
        List<XmlValidationError> errors = new ArrayList<XmlValidationError>();
        if (rows == null || rows == 0) {
            errors.add(new XmlValidationError("grid", "rows", rows != null ? XmlConstants.XML_TAG_INVALID : XmlConstants.XML_TAG_EMPTY));
        }
        return errors;
    }
}

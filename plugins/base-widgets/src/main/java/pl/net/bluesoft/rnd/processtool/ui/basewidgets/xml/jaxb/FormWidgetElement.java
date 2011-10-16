package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "form")
@XStreamAlias("form")
public class FormWidgetElement extends HasWidgetsElement {

}

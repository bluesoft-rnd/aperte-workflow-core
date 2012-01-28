package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation.XmlValidationError;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "script")
@XStreamAlias("script")
public class ScriptElement extends WidgetElement {
    @XmlAttribute
    @XStreamAsAttribute
    private String lang;

    @XmlValue
    @XStreamAsAttribute
    private String script;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}

package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

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

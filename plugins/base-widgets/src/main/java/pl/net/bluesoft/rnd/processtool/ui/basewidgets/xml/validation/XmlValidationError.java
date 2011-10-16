package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.validation;

import java.io.Serializable;

public class XmlValidationError implements Serializable {
    private String parent;
    private String field;
    private String messageKey;

    public XmlValidationError(String parent, String field, String messageKey) {
        this.parent = parent;
        this.field = field;
        this.messageKey = messageKey;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
}

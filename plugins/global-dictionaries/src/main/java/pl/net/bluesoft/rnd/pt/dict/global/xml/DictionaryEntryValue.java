package pl.net.bluesoft.rnd.pt.dict.global.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XStreamAlias("val")
public class DictionaryEntryValue {
    @XStreamAsAttribute
    private String value;
    @XStreamAsAttribute
    private Date validStartDate;
    @XStreamAsAttribute
    private Date validEndDate;
    @XStreamAsAttribute
    private Date validSingleDate;
    @XStreamImplicit
    private List<DictionaryEntryExtension> extensions;

    public List<DictionaryEntryExtension> getExtensions() {
        return extensions == null ? (extensions = new ArrayList<DictionaryEntryExtension>()) : extensions;
    }

    public void setExtensions(List<DictionaryEntryExtension> extensions) {
        this.extensions = extensions;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getValidStartDate() {
        return validStartDate;
    }

    public void setValidStartDate(Date validStartDate) {
        this.validStartDate = validStartDate;
    }

    public Date getValidEndDate() {
        return validEndDate;
    }

    public void setValidEndDate(Date validEndDate) {
        this.validEndDate = validEndDate;
    }

    public Date getValidSingleDate() {
        return validSingleDate;
    }

    public void setValidSingleDate(Date validSingleDate) {
        this.validSingleDate = validSingleDate;
    }
}

package pl.net.bluesoft.rnd.processtool.dict.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@XStreamAlias("val")
public class DictionaryEntryValue {
    @XStreamAsAttribute
    private String value;
	@XStreamImplicit(itemFieldName = "i18n-value")
	private List<DictionaryI18N> localizedValues;
    @XStreamAsAttribute
    private Date validFrom;
    @XStreamAsAttribute
    private Date validTo;
    @XStreamAsAttribute
    private Date validDay;
    @XStreamImplicit
    private List<DictionaryEntryExtension> extensions;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<DictionaryI18N> getLocalizedValues() {
		return localizedValues != null ? localizedValues : Collections.<DictionaryI18N>emptyList();
	}

	public void setLocalizedValues(List<DictionaryI18N> localizedValues) {
		this.localizedValues = localizedValues;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	public Date getValidDay() {
		return validDay;
	}

	public void setValidDay(Date validDay) {
		this.validDay = validDay;
	}

	public List<DictionaryEntryExtension> getExtensions() {
		return extensions != null ? extensions : Collections.<DictionaryEntryExtension>emptyList();
	}

	public void setExtensions(List<DictionaryEntryExtension> extensions) {
		this.extensions = extensions;
	}
}

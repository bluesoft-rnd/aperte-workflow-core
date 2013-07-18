package pl.net.bluesoft.rnd.processtool.model.dict.db;

import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;

@Entity
@Table(name = "pt_dictionary_item_value")
public class ProcessDBDictionaryItemValue extends PersistentEntity implements ProcessDictionaryItemValue {
	public static String _DEFAULT_VALUE = "defaultValue";
	public static String _VALID_FROM = "validFrom";
	public static String _VALID_TO = "validTo";
	public static String _LOCALIZED_VALUES = "localizedValues";
	public static String _EXTENSIONS = "extensions";

    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessDBDictionaryItem item;

	private Date validFrom;
	private Date validTo;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String defaultValue;

	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
	@Cascade(value = CascadeType.ALL)
	@JoinColumn(name = "dictionary_item_value_id", nullable = true)
	private List<ProcessDBDictionaryI18N> localizedValues = new ArrayList<ProcessDBDictionaryI18N>();

	@OneToMany(mappedBy = "itemValue", fetch = FetchType.EAGER, orphanRemoval = true, cascade=javax.persistence.CascadeType.ALL)
    @Cascade(value = {CascadeType.ALL})
    private Set<ProcessDBDictionaryItemExtension> extensions = new HashSet<ProcessDBDictionaryItemExtension>();

	public ProcessDBDictionaryItemValue() {
	}

	private ProcessDBDictionaryItemValue(ProcessDBDictionaryItemValue itemValue) {
		this.id = itemValue.getId();
		this.validFrom = itemValue.validFrom;
		this.validTo = itemValue.validTo;
		this.defaultValue = itemValue.defaultValue;

		for (ProcessDBDictionaryI18N localizedValue : itemValue.localizedValues) {
			setValue(localizedValue.getLanguageCode(), localizedValue.getText());
		}

		for (ProcessDBDictionaryItemExtension ext : itemValue.extensions) {
			addExtension(ext.exactCopy());
		}
	}

	public ProcessDBDictionaryItemValue exactCopy() {
		return new ProcessDBDictionaryItemValue(this);
	}

	public ProcessDBDictionaryItemValue shallowCopy() {
		ProcessDBDictionaryItemValue val = exactCopy();
		val.setId(null);
		val.item = null;

		for (ProcessDBDictionaryI18N localizedValue : val.localizedValues) {
			localizedValue.setId(null);
		}
		for (ProcessDBDictionaryItemExtension ext : val.extensions) {
			ext.setId(null);
		}
		return val;
	}

	public ProcessDBDictionaryItem getItem() {
		return item;
	}

	public void setItem(ProcessDBDictionaryItem item) {
		this.item = item;
	}

	@Override
	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	@Override
	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<ProcessDBDictionaryI18N> getLocalizedValues() {
		return localizedValues;
	}

	public void setLocalizedValues(List<ProcessDBDictionaryI18N> localizedValues) {
		this.localizedValues = localizedValues;
	}

	public Set<ProcessDBDictionaryItemExtension> getExtensions() {
		return extensions;
	}

	public void setExtensions(Set<ProcessDBDictionaryItemExtension> extensions) {
		this.extensions = extensions;
	}

	public void addExtension(ProcessDBDictionaryItemExtension extension) {
		extensions.add(extension);
		extension.setItemValue(this);
	}

	@Override
	public String getValue(String languageCode) {
		return ProcessDBDictionaryI18N.getLocalizedText(localizedValues, languageCode, defaultValue);
	}

	@Override
	public String getValue(Locale locale) {
		return getValue(locale.toString());
	}

	public void setValue(String languageCode, String value) {
		if (languageCode == null) {
			this.defaultValue = value;
			return;
		}
		ProcessDBDictionaryI18N.setLocalizedText(localizedValues, languageCode, value);
	}

	@Override
	public Collection<ProcessDictionaryItemExtension> getItemExtensions() {
    	return Collections.unmodifiableCollection((Set)extensions);
	}

    public void setValidityDates(Date validStartDate, Date validEndDate) {
        this.validFrom = validStartDate;
        this.validTo = validEndDate;
    }

    public boolean hasDatesSet() {
        return validFrom != null && validTo != null;
    }

    public boolean hasFullDatesRange() {
        return validFrom == null && validTo == null;
    }

    @Override
	public boolean isValidForDate(Date date) {
        if (date == null) {
            return validFrom == null && validTo == null;
        }
        if (validFrom != null && date.before(validFrom) && !DateUtils.isSameDay(date, validFrom)) {
            return false;
        }
        else if (validTo != null && date.after(validTo) && !DateUtils.isSameDay(date, validTo)) {
            return false;
        }
        return true;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ProcessDBDictionaryItemValue that = (ProcessDBDictionaryItemValue)o;

		if (extensions != null ? !extensions.equals(that.extensions) : that.extensions != null) return false;
		if (localizedValues != null ? !localizedValues.equals(that.localizedValues) : that.localizedValues != null)
			return false;
		if (validTo != null ? !validTo.equals(that.validTo) : that.validTo != null) return false;
		if (validFrom != null ? !validFrom.equals(that.validFrom) : that.validFrom != null)
			return false;
		if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = defaultValue != null ? defaultValue.hashCode() : 0;
		result = 31 * result + (validFrom != null ? validFrom.hashCode() : 0);
		result = 31 * result + (validTo != null ? validTo.hashCode() : 0);
		result = 31 * result + (localizedValues != null ? localizedValues.hashCode() : 0);
		result = 31 * result + (extensions != null ? extensions.hashCode() : 0);
		return result;
	}
}

package pl.net.bluesoft.rnd.processtool.model.dict.db;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "pt_dictionary_item_value")
public class ProcessDBDictionaryItemValue extends AbstractPersistentEntity implements ProcessDictionaryItemValue {
	public static final String _DEFAULT_VALUE = "defaultValue";
	public static final String _VALID_FROM = "validFrom";
	public static final String _VALID_TO = "validTo";
	public static final String _LOCALIZED_VALUES = "localizedValues";
	public static final String _EXTENSIONS = "extensions";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_DB_DICT_ITEM_VAL")
			}
	)
    @Index(name="idx_p_dict_i_value_id")
	@Column(name = "id")
	protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessDBDictionaryItem item;

	private Date validFrom;
	private Date validTo;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String defaultValue;

	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
	@Cascade(value = org.hibernate.annotations.CascadeType.ALL)
	@JoinColumn(name = "dictionary_item_value_id", nullable = true)
	private List<ProcessDBDictionaryI18N> localizedValues = new ArrayList<ProcessDBDictionaryI18N>();

	@OneToMany(mappedBy = "itemValue", fetch = FetchType.EAGER, orphanRemoval = true, cascade=javax.persistence.CascadeType.ALL)
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
	@OrderBy
    private Set<ProcessDBDictionaryItemExtension> extensions = new LinkedHashSet<ProcessDBDictionaryItemExtension>();

	public ProcessDBDictionaryItemValue() {
	}

	private ProcessDBDictionaryItemValue(ProcessDBDictionaryItemValue itemValue) {
		this.id = itemValue.id;
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
		val.id = null;
		val.item = null;

		for (ProcessDBDictionaryI18N localizedValue : val.localizedValues) {
			localizedValue.setId(null);
		}
		for (ProcessDBDictionaryItemExtension ext : val.extensions) {
			ext.setId(null);
		}
		return val;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
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

	public void addExtension(ProcessDBDictionaryDefaultItemExtension defaultExt) {
		addExtension(defaultExt.getName(), defaultExt.getValue(), defaultExt.getDescription(), defaultExt.getValueType())
				.setDefault_(true);
	}

	public ProcessDBDictionaryItemExtension addExtension(String name, String value, String description, String valueType) {
		ProcessDBDictionaryItemExtension ext = getExt(name);
		boolean toAdd = ext == null;

		if (ext == null) {
			ext = new ProcessDBDictionaryItemExtension();
		}
		ext.setName(name);
		ext.setValue(value);
		if (description != null) {
			ext.setDescription(description);
		}
		if (valueType != null) {
			ext.setValueType(valueType);
		}
		if (toAdd) {
			addExtension(ext);
		}
		return ext;
	}

	public ProcessDBDictionaryItemExtension addOrUpdateExtension(ProcessDBDictionaryItemExtension ext) {
		return addExtension(ext.getName(), ext.getValue(), ext.getDescription(), ext.getValueType());
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

	@Override
	public String getExtValue(String name) {
		ProcessDBDictionaryItemExtension ext = getExt(name);
		return ext != null ? ext.getValue() : null;
	}

	private ProcessDBDictionaryItemExtension getExt(String name) {
		for (ProcessDBDictionaryItemExtension ext : extensions) {
			if(name.equals(ext.getName())) {
				return ext;
			}
		}
		return null;
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
	public boolean isEmptyValue() {
		return false;
	}

	public void addLocalizedValue(String langCode, String value) {
		localizedValues.add(new ProcessDBDictionaryI18N(langCode, value));
	}
}

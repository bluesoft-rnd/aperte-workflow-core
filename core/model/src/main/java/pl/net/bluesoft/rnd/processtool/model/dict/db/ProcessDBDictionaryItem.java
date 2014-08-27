package pl.net.bluesoft.rnd.processtool.model.dict.db;

import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity
@Table(name = "pt_dictionary_item")
public class ProcessDBDictionaryItem extends AbstractPersistentEntity implements ProcessDictionaryItem {
    public static final String _DICTIONARY = "dictionary";
    public static final String _KEY = "key";
    public static final String _VALUE_TYPE = "valueType";
    public static final String _DESCRIPTION = "description";
    public static final String _VALUES = "values";

    @Id
    @GeneratedValue(generator = "idGenerator")
    @GenericGenerator(
            name = "idGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
                    @org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_DB_DICT_ITEM")
            }
    )
    @Index(name = "idx_p_dict_item_id")
    @Column(name = "id")
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(value = {CascadeType.REFRESH})
    private ProcessDBDictionary dictionary;

    @Column(name = "key_", nullable = false)
    @Index(name = "idx_p_dict_item_key")
    private String key;
    private String valueType;

    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER)
    @Cascade(value = CascadeType.ALL)
    @JoinColumn(name = "dictionary_item_id", nullable = true)
    @Fetch(value = FetchMode.SELECT)
    private List<ProcessDBDictionaryI18N> localizedDescriptions = new ArrayList<ProcessDBDictionaryI18N>();

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String defaultDescription;

    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(value = CascadeType.ALL)
    private Set<ProcessDBDictionaryItemValue> values = new HashSet<ProcessDBDictionaryItemValue>();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setDictionary(ProcessDBDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public ProcessDBDictionary getDictionary() {
        return dictionary;
    }


    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Set<ProcessDBDictionaryItemValue> getValues() {
        return values;
    }

    public void setValues(Set<ProcessDBDictionaryItemValue> values) {
        this.values = values;
    }

    public void addValue(ProcessDBDictionaryItemValue value) {
        value.setItem(this);
        values.add(value);
    }

    public void removeValue(ProcessDBDictionaryItemValue value) {
        value.setItem(null);
        values.remove(value);
    }

    @Override
    public Collection<ProcessDictionaryItemValue> values() {
        return Collections.unmodifiableCollection((Set) values);
    }

    @Override
    public ProcessDBDictionaryItemValue getValueForCurrentDate() {
        return getValueForDate(new Date());
    }

    @Override
    public ProcessDBDictionaryItemValue getValueForDate(Date date) {
        for (ProcessDBDictionaryItemValue value : values) {
            if (value.isValidForDate(date)) {
                return value;
            }
        }
        return new EMPTY_VALUE(getDictionary(), this, date);
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public List<ProcessDBDictionaryI18N> getLocalizedDescriptions() {
        return localizedDescriptions;
    }

    public void setLocalizedDescriptions(List<ProcessDBDictionaryI18N> localizedDescriptions) {
        this.localizedDescriptions = localizedDescriptions;
    }

    public String getDescription() {
        return getDescription((String) null);
    }

    @Override
    public String getDescription(String languageCode) {
        return ProcessDBDictionaryI18N.getLocalizedText(localizedDescriptions, languageCode, defaultDescription);
    }


    @Override
    public String getDescription(Locale locale) {
        return getDescription(locale.toString());
    }

    public void setDescription(String languageCode, String name) {
        if (languageCode == null) {
            this.defaultDescription = name;
            return;
        }
        ProcessDBDictionaryI18N.setLocalizedText(localizedDescriptions, languageCode, name);
    }

    public final class EMPTY_VALUE extends ProcessDBDictionaryItemValue {
        private final String NO_VALUE = "No value defined for dictionary=%s, item=%s, languageCode=%s and date=%s";
        private Date date;

        private EMPTY_VALUE(ProcessDBDictionary dictionary, ProcessDBDictionaryItem item, Date date) {
            setDictionary(dictionary);
            setItem(item);
            this.date = date;
        }

        @Override
        public String getValue(String languageCode) {
            return String.format(NO_VALUE, getDictionary().getDefaultName(), getItem().getKey(), languageCode, date.toString());
        }

        @Override
        public String getValue(Locale locale) {
            return getValue(locale.getLanguage());
        }

        @Override
        public String getDefaultValue() {
            return getValue("default");
        }
    }
}

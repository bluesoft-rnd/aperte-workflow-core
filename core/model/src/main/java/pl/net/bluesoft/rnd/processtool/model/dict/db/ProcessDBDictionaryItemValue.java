package pl.net.bluesoft.rnd.processtool.model.dict.db;

//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.Parameter;
import javax.persistence.Table;

import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

@Entity
@Table(name = "pt_dictionary_item_value")
public class ProcessDBDictionaryItemValue extends AbstractPersistentEntity implements ProcessDictionaryItemValue<String>
{
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
	@Column(name = "id")
	protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessDBDictionaryItem item;

    @Lob
    @Column(name="value_")
    @Type(type = "org.hibernate.type.StringClobType")
    private String value;
    private Date validStartDate;
    private Date validEndDate;

    @OneToMany(mappedBy = "itemValue", fetch = FetchType.EAGER, orphanRemoval = true)
    @Cascade(value = {CascadeType.ALL})
    @MapKey(name = "name")
    private Map<String, ProcessDBDictionaryItemExtension> extensions = new HashMap<String, ProcessDBDictionaryItemExtension>();

    public ProcessDBDictionaryItemValue() {
    }

    private ProcessDBDictionaryItemValue(ProcessDBDictionaryItemValue itemValue) {
        this.value = itemValue.getValue();
        this.id = itemValue.getId();
        this.validStartDate = itemValue.getValidStartDate();
        this.validEndDate = itemValue.getValidEndDate();
        for (ProcessDBDictionaryItemExtension ext : itemValue.getExtensions().values()) {
            addItemExtension(ext.exactCopy());
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProcessDBDictionaryItemValue exactCopy() {
        return new ProcessDBDictionaryItemValue(this);
    }

    public ProcessDBDictionaryItemValue shallowCopy() {
        ProcessDBDictionaryItemValue val = exactCopy();
        val.setId(null);
        val.setItem(null);
        for (ProcessDBDictionaryItemExtension ext : val.getExtensions().values()) {
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	public String getStringValue() {
		return getValue();
	}

	public void setStringValue(String value) {
		setValue(value);
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

    public void setValidityDates(Date validStartDate, Date validEndDate) {
        this.validStartDate = validStartDate;
        this.validEndDate = validEndDate;
    }

    public boolean hasDatesSet() {
        return validStartDate != null && validEndDate != null;
    }

    public boolean hasFullDatesRange() {
        return validStartDate == null && validEndDate == null;
    }

    public boolean isValidForDate(Date date) {
        if (date == null) {
            return validStartDate == null && validEndDate == null;
        }
        if (validStartDate != null && date.before(validStartDate) && !(DateUtils.isSameDay(date, validStartDate))) {
            return false;
        }
        else if (validEndDate != null && date.after(validEndDate) && !(DateUtils.isSameDay(date, validEndDate))) {
            return false;
        }
        return true;
    }

    public void removeItemExtension(String name) {
        ProcessDBDictionaryItemExtension ext = extensions.get(name);
        if (ext != null) {
            ext.setItemValue(null);
            extensions.remove(name);
        }
    }

    public void addItemExtension(ProcessDBDictionaryItemExtension itemExtension) {
        itemExtension.setItemValue(this);
        extensions.put(itemExtension.getName(), itemExtension);
    }

    public Map<String, ProcessDBDictionaryItemExtension> getExtensions() {
        return extensions;
    }

    public Collection<ProcessDictionaryItemExtension> extensions() {
        Set<ProcessDictionaryItemExtension> set = new HashSet<ProcessDictionaryItemExtension>();
        for (ProcessDictionaryItemExtension ext : extensions.values()) {
            set.add(ext);
        }
        return set;
    }

    public Collection<String> getExtensionNames() {
        return extensions.keySet();
    }

    public ProcessDBDictionaryItemExtension getExtensionByName(String extensionName) {
        return extensions.get(extensionName);
    }

    public void setExtensions(Map<String, ProcessDBDictionaryItemExtension> extensions) {
        this.extensions = extensions;
    }
}

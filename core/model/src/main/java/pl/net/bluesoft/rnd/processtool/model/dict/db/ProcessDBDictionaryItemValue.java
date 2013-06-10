package pl.net.bluesoft.rnd.processtool.model.dict.db;

//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;

@Entity
@Table(name = "pt_dictionary_item_value")
public class ProcessDBDictionaryItemValue extends PersistentEntity implements ProcessDictionaryItemValue<String> 
{
    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessDBDictionaryItem item;

    @Lob
    @Column(name="value_")
    @Type(type = "org.hibernate.type.StringClobType")
    private String value;
    private Date validStartDate;
    private Date validEndDate;


    @OneToMany(mappedBy = "itemValue", fetch = FetchType.EAGER, orphanRemoval = true, cascade=javax.persistence.CascadeType.ALL)
    @Cascade(value = {CascadeType.ALL})
    private Set<ProcessDBDictionaryItemExtension> extensions = new HashSet<ProcessDBDictionaryItemExtension>();

    public Set<ProcessDBDictionaryItemExtension> getExtensions() {
		return extensions;
	}
    
    public Set<ProcessDictionaryItemExtension<String>> getItemExtensions() 
    {
    	Set<ProcessDictionaryItemExtension<String>> itemExtensions = new HashSet<ProcessDictionaryItemExtension<String>>();
    	
    	for(ProcessDBDictionaryItemExtension item: extensions)
    		itemExtensions.add(item);
    	
		return itemExtensions;
	}

	public void setExtensions(Set<ProcessDBDictionaryItemExtension> extensions) {
		this.extensions = extensions;
	}

	public ProcessDBDictionaryItemValue() {
    }

    private ProcessDBDictionaryItemValue(ProcessDBDictionaryItemValue itemValue) {
        this.value = itemValue.getValue();
        this.id = itemValue.getId();
        this.validStartDate = itemValue.getValidStartDate();
        this.validEndDate = itemValue.getValidEndDate();
        for (ProcessDBDictionaryItemExtension ext : itemValue.getExtensions())
        {
        	addExtension(ext.exactCopy());
        }
    }

    public ProcessDBDictionaryItemValue exactCopy() {
        return new ProcessDBDictionaryItemValue(this);
    }

    public ProcessDBDictionaryItemValue shallowCopy() {
        ProcessDBDictionaryItemValue val = exactCopy();
        val.setId(null);
        val.setItem(null);
        for (ProcessDBDictionaryItemExtension ext : val.getExtensions()) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessDBDictionaryItemValue other = (ProcessDBDictionaryItemValue) obj;
		if (id == null) {
			if (other.id != null)
				return false;
			else
				return this == other;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	public void addExtension(ProcessDBDictionaryItemExtension extension) {
		extensions.add(extension);
		extension.setItemValue(this);
	}
}

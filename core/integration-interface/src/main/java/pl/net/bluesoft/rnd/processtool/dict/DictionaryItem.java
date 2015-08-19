package pl.net.bluesoft.rnd.processtool.dict;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class DictionaryItem
{
    private String key;
    private String value;
    private String description;
    private boolean isValid;
	private Date validFrom;
	private Date validTo;
    private Collection<DictionaryItemExt> extensions = new LinkedList<DictionaryItemExt>();

    public boolean getisValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        if(description == null)
            description = "";
        this.description = description;
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

	public Collection<DictionaryItemExt> getExtensions() {
        return extensions;
    }

    public void setExtensions(Collection<DictionaryItemExt> extensions) {
        this.extensions = extensions;
    }

    public DictionaryItemExt getExtensionByKey(String key)
    {
        for(DictionaryItemExt ext: extensions)
            if(ext.getKey().equals(key))
                return ext;

        return null;
    }

    public boolean hasExtenstion(String key)
    {
        return getExtensionByKey(key) != null;
    }
}

package pl.net.bluesoft.rnd.pt.dict.global.bean;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class DictionaryItem
{
    private String key;
    private String value;
    private String description;
    private Collection<DictionaryItemExt> extensions = new LinkedList<DictionaryItemExt>();

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

    public Collection<DictionaryItemExt> getExtensions() {
        return extensions;
    }

    public void setExtensions(Collection<DictionaryItemExt> extensions) {
        this.extensions = extensions;
    }
}

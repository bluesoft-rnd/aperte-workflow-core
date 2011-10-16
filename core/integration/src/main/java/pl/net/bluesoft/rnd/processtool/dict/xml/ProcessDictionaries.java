package pl.net.bluesoft.rnd.processtool.dict.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("process-dictionaries")
public class ProcessDictionaries {
    @XStreamAsAttribute
    private String processBpmDefinitionKey;
    @XStreamAsAttribute
    private Boolean overwrite;
    @XStreamAsAttribute
    private String defaultLanguage;

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public Boolean getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getProcessBpmDefinitionKey() {
        return processBpmDefinitionKey;
    }

    public void setProcessBpmDefinitionKey(String processBpmDefinitionKey) {
        this.processBpmDefinitionKey = processBpmDefinitionKey;
    }

    @XStreamImplicit
    protected List<Dictionary> dictionaries;

    public List<Dictionary> getDictionaries() {
        return dictionaries == null ? (dictionaries = new ArrayList<Dictionary>()) : dictionaries;
    }

    public void setDictionaries(List<Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }
}


package pl.net.bluesoft.rnd.processtool.dict.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.Collections;
import java.util.List;

@XStreamAlias("process-dictionaries")
public class ProcessDictionaries {
    @XStreamAsAttribute
    private boolean overwrite;
    @XStreamImplicit
    private List<DictionaryPermission> permissions;
    @XStreamImplicit
    protected List<Dictionary> dictionaries;

    public boolean isOverwrite() {
            return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public List<Dictionary> getDictionaries() {
        return dictionaries != null ? dictionaries : Collections.<Dictionary>emptyList();
    }

    public void setDictionaries(List<Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
    }

    public List<DictionaryPermission> getPermissions() {
        return permissions != null ? permissions : Collections.<DictionaryPermission>emptyList();
    }

    public void setPermissions(List<DictionaryPermission> permissions) {
        this.permissions = permissions;
    }
}


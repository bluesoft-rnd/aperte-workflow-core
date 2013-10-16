package pl.net.bluesoft.rnd.processtool.model.processdata;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/7/12
 * Time: 3:18 PM
 */
@Entity
@Table(name = "pt_pi_dict_attr")
public class ProcessInstanceDictionaryAttribute extends ProcessInstanceAttribute {
    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name="dictionary_id")
    private Set<ProcessInstanceDictionaryItem> items;

    public ProcessInstanceDictionaryAttribute() {
    }

    public ProcessInstanceDictionaryAttribute(String key) {
        setKey(key);
    }

    public Set<ProcessInstanceDictionaryItem> getItems() {
        if(items == null)
            items = new HashSet<ProcessInstanceDictionaryItem>();
        return items;
    }

    public void setItems(Set<ProcessInstanceDictionaryItem> items) {
        this.items = items;
    }

    public void put(String key, String value) {
        getItems().add(new ProcessInstanceDictionaryItem(key, value));
    }
}

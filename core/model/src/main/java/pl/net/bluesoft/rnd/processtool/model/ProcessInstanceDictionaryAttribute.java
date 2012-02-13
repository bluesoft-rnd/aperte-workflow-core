package pl.net.bluesoft.rnd.processtool.model;

import org.aperteworkflow.search.ProcessInstanceSearchAttribute;
import org.aperteworkflow.search.Searchable;

import javax.persistence.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/7/12
 * Time: 3:18 PM
 */
@Entity
@Table(name = "pt_process_instance_dictionary_attribute")
public class ProcessInstanceDictionaryAttribute extends ProcessInstanceAttribute implements Searchable{

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


    @Override
    public Collection<ProcessInstanceSearchAttribute> getAttributes() {
        List<ProcessInstanceSearchAttribute> attrs = new ArrayList<ProcessInstanceSearchAttribute>();
        for (ProcessInstanceDictionaryItem e : items) {
            attrs.add(new ProcessInstanceSearchAttribute("map_key", e.getKey()));
            attrs.add(new ProcessInstanceSearchAttribute("map_value", e.getValue()));
        }
        return attrs;
    }

    public void put(String key, String value) {
        getItems().add(new ProcessInstanceDictionaryItem(key, value));
    }
}

package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/7/12
 * Time: 3:31 PM
 */

@Entity
@Table(name = "pt_process_instance_dictionary_item")
public class ProcessInstanceDictionaryItem extends PersistentEntity{

    @Column(name = "_key")
    private String key;

    @Column(name = "_value")
    private String value;

    @ManyToOne
    @JoinColumn
    private ProcessInstanceDictionaryAttribute dictionary;

    public ProcessInstanceDictionaryItem() {
    }

    public ProcessInstanceDictionaryItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public ProcessInstanceDictionaryAttribute getDictionary() {
        return dictionary;
    }

    public void setDictionary(ProcessInstanceDictionaryAttribute dictionary) {
        this.dictionary = dictionary;
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




}

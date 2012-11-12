package pl.net.bluesoft.rnd.processtool.model.dict.db;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

import javax.persistence.*;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "pt_dictionary_prms")
public class ProcessDBDictionaryPermission extends AbstractPermission 
{
    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessDBDictionary dictionary;

    public ProcessDBDictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(ProcessDBDictionary dictionary) {
        this.dictionary = dictionary;
    }
}

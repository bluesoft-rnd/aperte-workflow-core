package pl.net.bluesoft.casemanagement.model;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * Created by pkuciapski on 2014-04-22.
 */
@MappedSuperclass
public class AbstractCaseAttributeBase extends PersistentEntity {
    @Column(name = "key")

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}

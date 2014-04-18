package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case",
        indexes = {
                @Index(name = "idx_pt_case_pk",
                        columnNames = {"id"}
                )
        })
public class Case extends PersistentEntity {

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "creation_date", nullable = false)
    private Date createDate;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Column(name = "modification_date", nullable = false)
    private Date modificationDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

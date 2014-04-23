package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_s_attr")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_s_attr",
        indexes = {
                @Index(name = "idx_pt_case_s_attr_pk",
                        columnNames = {"id"}
                ),
                @Index(name = "idx_pt_case_s_attr_case_id", columnNames = Case.CASE_ID)
        })
public class CaseSimpleAttribute extends AbstractCaseAttribute implements Comparable<CaseSimpleAttribute> {
    @Column(name = "value")
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(final CaseSimpleAttribute other) {
        if (this.getKey() != null && other != null)
            return this.getKey().compareTo(other.getKey());
        return 0;
    }
}

package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.IAttribute;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_s_attr", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_s_attr",
        indexes = {
                @Index(name = "idx_pt_case_s_attr_pk",
                        columnNames = {"id"}
                ),
                @Index(name = "idx_pt_case_s_attr_case_id", columnNames = Case.CASE_ID)
        })
public class CaseSimpleAttribute extends AbstractCaseAttribute implements Comparable<CaseSimpleAttribute>, IAttribute {
    public static final String TABLE = CASES_SCHEMA + "." + CaseSimpleAttribute.class.getAnnotation(Table.class).name();
    @Column(name = "value")
    private String value;

	public CaseSimpleAttribute(){
		super();
	}

	public CaseSimpleAttribute(String key, String value) {
		setKey(key);
		this.value = value;
	}

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

	@Override
	public String toString() {
		return getKey() + '=' + value;
	}
}

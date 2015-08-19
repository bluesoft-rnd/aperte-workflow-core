package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;

import javax.persistence.*;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_attr", schema = CASES_SCHEMA,
    uniqueConstraints = @UniqueConstraint(columnNames = {Case.CASE_ID, "key"})
)
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_attr",
        indexes = {
                @Index(name = "idx_pt_case_attr_pk",
                        columnNames = {"id"}
                ),
                @Index(name = "idx_pt_case_attr_case_id", columnNames = Case.CASE_ID)
        }
)
public abstract class CaseAttribute extends AbstractCaseAttribute {
    public static final String TABLE = CASES_SCHEMA + "." + CaseAttribute.class.getAnnotation(Table.class).name();
}

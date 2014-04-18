package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_attr")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_attr",
        indexes = {
                @Index(name = "idx_pt_case_attr_pk",
                        columnNames = {"id"}
                ),
                @Index(name = "idx_pt_case_attr_case_id", columnNames = Case.CASE_ID)
        })
public abstract class CaseAttribute extends AbstractCaseAttribute {
}

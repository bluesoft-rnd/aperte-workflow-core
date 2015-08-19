package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@MappedSuperclass
public abstract class AbstractCaseAttribute extends AbstractCaseAttributeBase {
    final static String CASE_INSTANCE_ID = "caseInstance." + _ID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = Case.CASE_ID)
    private Case caseInstance;

    public Case getCase() {
        return caseInstance;
    }

    public void setCase(Case caseInstance) {
        this.caseInstance = caseInstance;
    }
}

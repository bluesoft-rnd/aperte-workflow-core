package pl.net.bluesoft.lot.casemanagement.dao;

import org.hibernate.Session;
import pl.net.bluesoft.lot.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseStateDefinitionDAOImpl extends SimpleHibernateBean<CaseStateDefinition> implements CaseStateDefinitionDAO {
    public CaseStateDefinitionDAOImpl(final Session session) {
        super(session);
    }

    @Override
    public CaseStateDefinition getStateDefinitionById(long caseStateDefinitionId) {
        return (CaseStateDefinition) this.session.get(CaseStateDefinition.class, caseStateDefinitionId);
    }
}

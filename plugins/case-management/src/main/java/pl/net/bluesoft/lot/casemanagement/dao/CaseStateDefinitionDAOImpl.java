package pl.net.bluesoft.lot.casemanagement.dao;

import org.hibernate.Session;
import pl.net.bluesoft.lot.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseStateDefinitionDAOImpl extends SimpleHibernateBean<CaseStateDefinition> implements CaseStateDefinitionDAO {
    private CaseDefinitionDAO caseDefinitionDAO;

    public CaseStateDefinitionDAOImpl(final Session session) {
        super(session);
    }

    public CaseStateDefinitionDAOImpl(final Session session, final CaseDefinitionDAO caseDefinitionDAO) {
        super(session);
        this.caseDefinitionDAO = caseDefinitionDAO;
    }

    @Override
    public CaseStateDefinition getStateDefinitionById(long caseStateDefinitionId) {
        return (CaseStateDefinition) this.session.get(CaseStateDefinition.class, caseStateDefinitionId);
    }

    @Override
    public CaseStateDefinition createStateDefinition(String name, long caseDefinitionId) {
        final CaseStateDefinition def = new CaseStateDefinition();
        def.setName(name);
        def.setDefinition(this.caseDefinitionDAO.getDefinitionById(caseDefinitionId));
        saveOrUpdate(def);
        return def;
    }
}

package pl.net.bluesoft.casemanagement.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import pl.net.bluesoft.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

import static org.hibernate.criterion.Restrictions.eq;

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
    public CaseStateDefinition getStateDefinitionById(final long caseStateDefinitionId) {
        return (CaseStateDefinition) this.session.get(CaseStateDefinition.class, caseStateDefinitionId);
    }

    @Override
    public CaseStateDefinition createStateDefinition(final String name, final long caseDefinitionId) {
        CaseDefinition caseDef = this.caseDefinitionDAO.getDefinitionById(caseDefinitionId);
        final CaseStateDefinition def = new CaseStateDefinition();
        def.setName(name);
        def.setDefinition(caseDef);
        caseDef.getPossibleStates().add(def);
        saveOrUpdate(def);
        getSession().update(caseDef);
        return def;
    }

    @Override
    public CaseStateDefinition getStateDefinitionByName(final String caseStateDefinitionName, final CaseDefinition caseDefinition) {
        return (CaseStateDefinition) session.createCriteria(CaseStateDefinition.class)
                .add(eq(CaseStateDefinition.NAME, caseStateDefinitionName))
                .add(eq("definition.id", caseDefinition.getId()))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .uniqueResult();
    }
}

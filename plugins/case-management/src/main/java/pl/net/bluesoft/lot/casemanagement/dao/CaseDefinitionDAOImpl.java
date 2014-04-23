package pl.net.bluesoft.lot.casemanagement.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import pl.net.bluesoft.lot.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.lot.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseDefinitionDAOImpl extends SimpleHibernateBean<CaseDefinition> implements CaseDefinitionDAO {

    public CaseDefinitionDAOImpl(final Session session) {
        super(session);
    }

    @Override
    public CaseDefinition getDefinitionById(final long caseDefinitionId) {
        return (CaseDefinition) this.session.get(CaseDefinition.class, caseDefinitionId);
    }

    @Override
    public CaseDefinition createDefinition(final String name) {
        final CaseDefinition def = new CaseDefinition();
        def.setName(name);
        saveOrUpdate(def);
        return def;
    }

    @Override
    public void setInitialState(final CaseDefinition caseDefinition, final long initialCaseStateDefinitionId) {
        addCaseStateDefinition(caseDefinition, initialCaseStateDefinitionId);
        this.session.update(caseDefinition);
    }

    private void addCaseStateDefinition(final CaseDefinition caseDefinition, final long initialCaseStateDefinitionId) {
        final CaseStateDefinition csd = new CaseStateDefinition();
        csd.setId(initialCaseStateDefinitionId);
        caseDefinition.setInitialState(csd);
    }

    @Override
    public CaseDefinition createDefinition(final String name, final long initialCaseStateDefinitionId) {
        final CaseDefinition def = new CaseDefinition();
        def.setName(name);
        addCaseStateDefinition(def, initialCaseStateDefinitionId);
        saveOrUpdate(def);
        return def;
    }

    @Override
    public CaseDefinition getDefinitionByName(final String name) {
        return (CaseDefinition) session.createCriteria(CaseDefinition.class)
                .add(eq(CaseDefinition.NAME, name))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();
    }
}

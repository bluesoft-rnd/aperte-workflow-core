package pl.net.bluesoft.lot.casemanagement.dao;

import org.hibernate.Session;
import pl.net.bluesoft.lot.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

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
}

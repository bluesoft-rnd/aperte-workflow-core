package pl.net.bluesoft.lot.casemanagement.dao;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.lot.casemanagement.model.*;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

import java.util.*;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseDAOImpl extends SimpleHibernateBean<Case> implements CaseDAO {
    private CaseStateDefinitionDAO caseStateDefinitionDAO;

    public CaseDAOImpl(final Session session) {
        super(session);
    }

    public CaseDAOImpl(final Session session, final CaseStateDefinitionDAO caseStateDefinitionDAO) {
        this(session);
        this.caseStateDefinitionDAO = caseStateDefinitionDAO;
    }

    @Override
    public Case createCase(final CaseDefinition definition, final String name, final String number, final Map<String, String> simpleAttributes) {
        final Case newCase = new Case();
        newCase.setName(name);
        newCase.setNumber(number);
        newCase.setCreateDate(new Date());
        newCase.setDefinition(definition);
        // get the initial state from the case definition
        final CaseStateDefinition initialStateDef = caseStateDefinitionDAO.getStateDefinitionById(newCase.getDefinition().getInitialState().getId());
        // add the initial stage
        final CaseStage initialStage = addStage(newCase, initialStateDef);
        newCase.setCurrentStage(initialStage);
        if (simpleAttributes != null) {
            final Set<CaseSimpleAttribute> attrs = addSimpleAttributes(newCase, simpleAttributes);
            newCase.getSimpleAttributes().addAll(attrs);
        }
        saveOrUpdate(newCase);
        return newCase;
    }

    private Set<CaseSimpleAttribute> addSimpleAttributes(final Case caseInstance, final Map<String, String> simpleAttributes) {
        final Set<CaseSimpleAttribute> attrs = new HashSet<CaseSimpleAttribute>();
        for (Map.Entry<String, String> entry : simpleAttributes.entrySet()) {
            final CaseSimpleAttribute a = new CaseSimpleAttribute();
            a.setKey(entry.getKey());
            a.setValue(entry.getValue());
            a.setCase(caseInstance);
            attrs.add(a);
        }
        return attrs;
    }

    private CaseStage addStage(final Case caseInstance, final CaseStateDefinition stateDefinition) {
        final CaseStage stage = new CaseStage();
        stage.setStartDate(new Date());
        stage.setCaseStateDefinition(stateDefinition);
        stage.setCase(caseInstance);
        stage.setName(stateDefinition.getName());
        caseInstance.getStages().add(stage);
        return stage;
    }

    @Override
    public Case getCaseById(final long caseId) {
        return (Case) this.session.get(Case.class, caseId);
    }

    @Override
    public Collection<Case> getAllCases() {
        return getDetachedCriteria().getExecutableCriteria(getSession()).list();
    }

    @Override
    public Collection<Case> getAllCasesPaged(final String sortColumnProperty, final boolean sortAscending, final int pageLength, final int pageOffset) {
        return this.session.createCriteria(Case.class)
                .addOrder(sortAscending ?
                        Order.asc(sortColumnProperty) :
                        Order.desc(sortColumnProperty))
                .setMaxResults(pageLength)
                .setFirstResult(pageOffset)
                .list();
    }

    @Override
    public Long getAllCasesCount() {
        return (Long) this.session.createCriteria(Case.class).setProjection(Projections.rowCount()).uniqueResult();
    }
}

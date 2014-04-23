package pl.net.bluesoft.lot.casemanagement.dao;

import org.hibernate.Session;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.lot.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.lot.casemanagement.model.CaseStage;
import pl.net.bluesoft.lot.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

import java.util.Date;
import java.util.Map;

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
        final CaseStateDefinition initialState = caseStateDefinitionDAO.getStateDefinitionById(newCase.getDefinition().getInitialState().getId());
        // add the initial stage
        final CaseStage initialStage = new CaseStage();
        initialStage.setStartDate(new Date());
        initialStage.setCaseStateDefinition(initialState);
        initialStage.setCase(newCase);
        initialStage.setName(initialState.getName());
        newCase.setCurrentStage(initialStage);
        newCase.getStages().add(initialStage);
        saveOrUpdate(newCase);
        return newCase;
    }

    @Override
    public Case getCaseById(long caseId) {
        return (Case) this.session.get(Case.class, caseId);
    }
}

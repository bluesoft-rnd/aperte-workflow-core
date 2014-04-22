package pl.net.bluesoft.lot.casemanagement.dao;

import org.hibernate.Session;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.lot.casemanagement.model.CaseStage;
import pl.net.bluesoft.lot.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

import java.util.Date;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public class CaseStageDAOImpl extends SimpleHibernateBean<CaseStage> implements CaseStageDAO {
    private CaseStateDefinitionDAO caseStateDefinitionDAO;

    public CaseStageDAOImpl(final Session session, final CaseStateDefinitionDAO caseStateDefinitionDAO) {
        super(session);
        this.caseStateDefinitionDAO = caseStateDefinitionDAO;
    }

    @Override
    public CaseStage createStage(final long caseId, final long caseStateDefinitionId, final String name) {
        final CaseStateDefinition stateDefinition = caseStateDefinitionDAO.getStateDefinitionById(caseStateDefinitionId);
        final Case case_ = new Case();
        case_.setId(caseId);
        final CaseStage stage = new CaseStage();
        stage.setName(name);
        stage.setCaseStateDefinition(stateDefinition);
        stage.setStartDate(new Date());
        stage.setCase(case_);
        case_.getStages().add(stage);
        saveOrUpdate(stage);
        return stage;
    }
}

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
    public CaseStage createStage(final Case caseInstance, final long caseStateDefinitionId, final String name) {
        final CaseStateDefinition stateDefinition = caseStateDefinitionDAO.getStateDefinitionById(caseStateDefinitionId);
        final CaseStage stage = new CaseStage();
        if (name != null)
            stage.setName(name);
        else
            stage.setName(stateDefinition.getName());
        stage.setCaseStateDefinition(stateDefinition);
        stage.setStartDate(new Date());
        stage.setCase(caseInstance);
        caseInstance.getStages().add(stage);
        caseInstance.setModificationDate(new Date());
        saveOrUpdate(stage);
        this.session.update(caseInstance);
        return stage;
    }

    @Override
    public void deleteStage(final CaseStage stage) {
        final Case caseInstance = stage.getCase();
        caseInstance.setModificationDate(new Date());
        caseInstance.getStages().remove(stage);
        delete(stage);
        this.session.update(caseInstance);
    }

    @Override
    public CaseStage getStageById(long caseStageId) {
        return (CaseStage) this.session.get(CaseStage.class, caseStageId);
    }
}

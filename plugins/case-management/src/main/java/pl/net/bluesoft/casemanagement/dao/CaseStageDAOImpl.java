package pl.net.bluesoft.casemanagement.dao;

import org.hibernate.Session;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseStage;
import pl.net.bluesoft.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;

import java.util.Date;
import java.util.Map;

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
    public CaseStage createStage(final Case caseInstance, final long caseStateDefinitionId, final String name, final Map<String, String> simpleAttributes) {
        final CaseStage stage = createStageObject(caseInstance, caseStateDefinitionId, name, simpleAttributes);
        saveOrUpdate(stage);
        this.session.update(caseInstance);
        return stage;
    }

    private CaseStage createStageObject(final Case caseInstance, final long caseStateDefinitionId, final String name, final Map<String, String> simpleAttributes) {
        final CaseStateDefinition stateDefinition = caseStateDefinitionDAO.getStateDefinitionById(caseStateDefinitionId);
        final CaseStage stage = new CaseStage();
        if (name != null)
            stage.setName(name);
        else
            stage.setName(stateDefinition.getName());
        stage.setCaseStateDefinition(stateDefinition);
        stage.setStartDate(new Date());
        stage.setCase(caseInstance);
        addSimpleAttributes(caseInstance, stage, simpleAttributes);

        caseInstance.getStages().add(stage);
        caseInstance.setModificationDate(new Date());
        if (caseInstance.getCurrentStage() != null) {
            caseInstance.getCurrentStage().setEndDate(new Date());
        }
        caseInstance.setCurrentStage(stage);
        return stage;
    }

    private void addSimpleAttributes(final Case caseInstance, final CaseStage stage, final Map<String, String> simpleAttributes) {
        if (simpleAttributes != null)
            for (Map.Entry<String, String> entry : simpleAttributes.entrySet()) {
                caseInstance.setSimpleAttribute(entry.getKey(), entry.getValue());
                stage.setSimpleAttribute(entry.getKey(), entry.getValue());
            }
    }

    @Override
    public void deleteStage(final CaseStage stage) {
        final Case caseInstance = stage.getCase();
        caseInstance.setModificationDate(new Date());
        caseInstance.getStages().remove(stage);
        if (caseInstance.getCurrentStage() != null && caseInstance.getCurrentStage().getId().equals(stage.getId())) {
            caseInstance.setCurrentStage(null);
        }
        this.session.update(caseInstance);
        delete(stage);
    }

    @Override
    public CaseStage getStageById(long caseStageId) {
        return (CaseStage) this.session.get(CaseStage.class, caseStageId);
    }

    @Override
    public void updateStage(final CaseStage stage) {
        this.session.update(stage);
    }
}

package pl.net.bluesoft.lot.casemanagement.dao;

import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.lot.casemanagement.model.CaseStage;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public interface CaseStageDAO {
    CaseStage createStage(Case caseInstance, long caseStateDefinitionId, String name);

    void deleteStage(CaseStage stage);

    CaseStage getStageById(long caseStageId);
}

package pl.net.bluesoft.casemanagement.dao;

import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseStage;

import java.util.Map;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public interface CaseStageDAO {
    CaseStage createStage(Case caseInstance, long caseStateDefinitionId, String name, Map<String, String> simpleAttributes);

    void updateStage(CaseStage stage);

    void deleteStage(CaseStage stage);

    CaseStage getStageById(long caseStageId);
}

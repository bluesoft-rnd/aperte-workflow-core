package pl.net.bluesoft.lot.casemanagement.dao;

import pl.net.bluesoft.lot.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.lot.casemanagement.model.CaseStateDefinition;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public interface CaseDefinitionDAO {
    CaseDefinition getDefinitionById(long caseDefinitionId);

    CaseDefinition createDefinition(String name);

    void setInitialState(CaseDefinition caseDefinition, long initialCaseStateDefinitionId);
}

package pl.net.bluesoft.casemanagement.dao;

import pl.net.bluesoft.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.casemanagement.model.CaseStateDefinition;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public interface CaseStateDefinitionDAO {
    CaseStateDefinition getStateDefinitionById(long caseStateDefinitionId);

    CaseStateDefinition getStateDefinitionByName(String caseStateDefinitionName, CaseDefinition caseDefinition);

    CaseStateDefinition createStateDefinition(String name, long caseDefinitionId);
}

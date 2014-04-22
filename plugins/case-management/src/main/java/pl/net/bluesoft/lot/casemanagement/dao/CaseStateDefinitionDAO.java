package pl.net.bluesoft.lot.casemanagement.dao;

import pl.net.bluesoft.lot.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.lot.casemanagement.model.CaseStateDefinition;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public interface CaseStateDefinitionDAO {
    CaseStateDefinition getStateDefinitionById(long caseStateDefinitionId);

    CaseStateDefinition createStateDefinition(String name, long caseDefinitionId);
}

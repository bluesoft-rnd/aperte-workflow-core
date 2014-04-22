package pl.net.bluesoft.lot.casemanagement.dao;

import pl.net.bluesoft.lot.casemanagement.model.Case;

import java.util.Map;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public interface CaseDAO {
    Case createCase(long caseDefinitionId, String name, String number, long caseStateDefinitionId, Map<String, String> simpleAttributes);
}

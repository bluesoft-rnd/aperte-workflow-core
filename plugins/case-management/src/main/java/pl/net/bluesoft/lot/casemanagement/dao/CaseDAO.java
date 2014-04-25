package pl.net.bluesoft.lot.casemanagement.dao;

import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.lot.casemanagement.model.CaseDefinition;

import java.util.Collection;
import java.util.Map;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public interface CaseDAO {
    Case createCase(CaseDefinition definition, String name, String number, Map<String, String> simpleAttributes);
    Case getCaseById(long caseId);
    Collection<Case> getAllCases();
    Long getAllCasesCount();
    Collection<Case> getAllCasesPaged(String sortColumnProperty, boolean sortAscending, int pageLength, int pageOffset);
}

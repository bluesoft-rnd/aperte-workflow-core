package pl.net.bluesoft.lot.casemanagement;

import pl.net.bluesoft.lot.casemanagement.exception.CaseManagementException;
import pl.net.bluesoft.lot.casemanagement.exception.CreateCaseException;
import pl.net.bluesoft.lot.casemanagement.model.Case;

import java.util.Collection;
import java.util.Map;

/**
 * Created by pkuciapski on 2014-04-23.
 */
public interface ICaseManagementFacade {
    Case createCase(String definitionName, String caseName, String caseNumber, Map<String, String> caseAttributes) throws CreateCaseException;

    Collection<Case> getAllCases() throws CaseManagementException;

    Long getAllCasesCount() throws CaseManagementException;

    Collection<Case> getAllCasesPaged(String sortColumnProperty, boolean sortAscending,
                                      int pageLength, int pageOffset) throws CaseManagementException;
}

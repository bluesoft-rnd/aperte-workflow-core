package pl.net.bluesoft.casemanagement.dao;

import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by pkuciapski on 2014-04-22.
 */
public interface CaseDAO {
    Case createCase(CaseDefinition definition, String name, String number);
    Case createCase(CaseDefinition definition, String name, String number, String initialState);
    void updateCase(Case caseInstance);
    Case getCaseById(long caseId);
    Collection<Case> getAllCases();
    Long getAllCasesCount();
    Long getAllNotClosedCasesCount();
    Collection<Case> getAllCasesPaged(String sortColumnProperty, boolean sortAscending, int pageLength, int pageOffset);

    Collection<Case> findCasesByName(String name);
    Collection<Case> findCasesByNumberAndSimpleAttr(String number, String key, String value, int pageLength, int pageOffset);
    Long getCasesByNumberAndSimpleAttrCount(String number, String key, String value);
	Case findCaseByNo(String number);


	List<Case> getCasesPaged(String sortColumnProperty, final boolean sortAscending, final int pageLength, final int pageOffset, Map<String, Object> params);
	Long getCasesCount(Map<String, Object> params);
    Long getCasesCountAfterPage();
}

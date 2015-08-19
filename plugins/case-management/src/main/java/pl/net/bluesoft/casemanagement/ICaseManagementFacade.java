package pl.net.bluesoft.casemanagement;

import pl.net.bluesoft.casemanagement.exception.AddCaseStageException;
import pl.net.bluesoft.casemanagement.exception.CaseManagementException;
import pl.net.bluesoft.casemanagement.exception.CreateCaseException;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.casemanagement.model.CaseStage;
import pl.net.bluesoft.casemanagement.model.CaseStateDefinition;
import pl.net.bluesoft.rnd.processtool.bpm.StartProcessResult;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by pkuciapski on 2014-04-23.
 */
public interface ICaseManagementFacade {
    Case createCase(String definitionName, String caseName, String caseNumber, String initialState) throws CreateCaseException;

    void updateCase(Case caseInstance);

    Collection<Case> getAllCases() throws CaseManagementException;

    Collection<Case> findCasesByName(String name) throws CaseManagementException;

    Collection<Case> findCasesByNumberAndSimpleAttr(String number, String key, String value, int pageLength, int pageNumber) throws CaseManagementException;

    Long getCasesByNumberAndSimpleAttrCount(String number, String key, String value);

    Case findCaseByNo(String number) throws CaseManagementException;

    Long getAllCasesCount() throws CaseManagementException;
    Long getAllCasesCountAfterPage() throws CaseManagementException;

    Long getAllNotClosedCasesCount() throws CaseManagementException;

    Collection<Case> getAllCasesPaged(String sortColumnProperty, boolean sortAscending,
                                      int pageLength, int pageOffset) throws CaseManagementException;

    Case getCaseById(long caseId);

    CaseStage addCaseStage(Case caseInstance, String caseStateDefinitionName, String caseStageName, Map<String, String> stageSimpleAttributes) throws AddCaseStageException;

    StartProcessResult startProcessInstance(final Case caseInstance, final String bpmDefinitionKey, IProcessToolRequestContext context) throws CaseManagementException;

    List<Case> getCasesPaged(String sortColumnProperty, final boolean sortAscending, final int pageLength, final int pageOffset, Map<String, Object> params);

    Long getCasesCount(Map<String, Object> params);

    CaseStateDefinition getCaseStateDefinitionByName(String name, CaseDefinition definition);

    void updateCaseStage(CaseStage caseStage);
}

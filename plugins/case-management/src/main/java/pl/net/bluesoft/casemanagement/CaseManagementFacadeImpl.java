package pl.net.bluesoft.casemanagement;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.casemanagement.dao.*;
import pl.net.bluesoft.casemanagement.exception.AddCaseStageException;
import pl.net.bluesoft.casemanagement.exception.CaseManagementException;
import pl.net.bluesoft.casemanagement.exception.CreateCaseException;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.StartProcessResult;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-04-23.
 */
public class CaseManagementFacadeImpl implements ICaseManagementFacade {
    private final Logger logger = Logger.getLogger(CaseManagementFacadeImpl.class.getName());

    private final String PROCESS_SOURCE = "case";

    @Autowired
    private ProcessToolRegistry processToolRegistry;

    @Override
    public Case createCase(String definitionName, String caseName, String caseNumber, String initialState) throws CreateCaseException {
        logger.info("Creating the new case");

        CaseDefinition definition = getCaseDefinitionDAO().getDefinitionByName(definitionName);

        if (definition == null)
            throw new CreateCaseException(String.format("Case definition for name '%s' not found", definitionName));

        return getCaseDAO().createCase(definition, caseName, caseNumber, initialState);
    }

    private CaseDefinitionDAO getCaseDefinitionDAO() {
        return new CaseDefinitionDAOImpl(ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession());
    }

    private CaseStateDefinitionDAO getCaseStateDefinitionDAO() {
        return new CaseStateDefinitionDAOImpl(ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession());
    }

    private CaseDAO getCaseDAO() {
        return new CaseDAOImpl(ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession(), getCaseStateDefinitionDAO());
    }

    private CaseStageDAO getCaseStageDAO() {
        return new CaseStageDAOImpl(ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession(), getCaseStateDefinitionDAO());
    }

    @Override
    public Collection<Case> getAllCases() throws CaseManagementException {
        return getCaseDAO().getAllCases();
    }

    @Override
    public Collection<Case> findCasesByName(String name) throws CaseManagementException {
        return getCaseDAO().findCasesByName(name);
    }

    @Override
    public Collection<Case> findCasesByNumberAndSimpleAttr(String number, String key, String value, int pageLength, int pageNumber) {
        return getCaseDAO().findCasesByNumberAndSimpleAttr(number, key, value, pageLength, pageNumber);
    }

    @Override
    public Long getCasesByNumberAndSimpleAttrCount(String number, String key, String value) {
        return getCaseDAO().getCasesByNumberAndSimpleAttrCount(number, key, value);
    }

    @Override
	public Case findCaseByNo(String number) throws CaseManagementException {
		return getCaseDAO().findCaseByNo(number);
	}

	@Override
    public Collection<Case> getAllCasesPaged(String sortColumnProperty, boolean sortAscending,
                                             int pageLength, int pageOffset) throws CaseManagementException {
        return getCaseDAO().getAllCasesPaged(sortColumnProperty, sortAscending, pageLength, pageOffset);
    }



    @Override
    public Long getAllCasesCount() throws CaseManagementException {
        return getCaseDAO().getAllCasesCount();
    }

    @Override
    public Long getAllCasesCountAfterPage() throws CaseManagementException {
        return getCaseDAO().getCasesCountAfterPage();
    }

    @Override
    public Long getAllNotClosedCasesCount() {return getCaseDAO().getAllNotClosedCasesCount();}

    @Override
    public Case getCaseById(long caseId) {
        return getCaseDAO().getCaseById(caseId);
    }

    @Override
    public CaseStage addCaseStage(Case caseInstance, String caseStateDefinitionName, String caseStageName,
                                  Map<String, String> stageSimpleAttributes) throws AddCaseStageException {
        logger.info("Adding the new case stage");
        CaseStageDAO dao = getCaseStageDAO();
        CaseStateDefinition stateDefinition = getCaseStateDefinitionDAO().getStateDefinitionByName(caseStateDefinitionName, caseInstance.getDefinition());
        if (stateDefinition == null)
            throw new AddCaseStageException(String.format("Case state definition for caseStateDefinitionName=%s and caseDefinitionName=%s was not found", caseStateDefinitionName, caseInstance.getDefinition().getName()));
        return dao.createStage(caseInstance, stateDefinition.getId(), caseStageName, stageSimpleAttributes);
    }

    @Override
    public void updateCase(Case caseInstance) {
        getCaseDAO().updateCase(caseInstance);
    }

    @Override
    public StartProcessResult startProcessInstance(final Case caseInstance, final String bpmDefinitionKey, IProcessToolRequestContext context) throws CaseManagementException {
        // verify that the process with bpmDefinitionKey can be started for the current case state
		if (caseInstance != null) {
			if (caseInstance.getCurrentStage() == null || !caseInstance.getCurrentStage().getCaseStateDefinition().hasProcess(bpmDefinitionKey)) {
				throw new CaseManagementException(String.format("Can't start the new process instance [bpmDefinitionKey=%s] for the current case state [caseId=%d]", bpmDefinitionKey, caseInstance.getId()));
			}
		}
        // start the new process instance
        Map<String, Object> params = new HashMap<String, Object>();
		if (caseInstance != null) {
			params.put(CaseAttributes.CASE_ID.value(), String.valueOf(caseInstance.getId()));
		}
        final StartProcessResult startResult = context.getBpmSession().startProcess(bpmDefinitionKey, null, PROCESS_SOURCE, params);
        if (startResult == null || startResult.getProcessInstance() == null)
            throw new CaseManagementException(String.format("Can't start the new process instance [bpmDefinitionKey=%s] for the given case [caseId=%d]", bpmDefinitionKey, caseInstance.getId()));
        // add the process instance to the case
		if (caseInstance != null) {
			caseInstance.getProcessInstances().add(startResult.getProcessInstance());
			getCaseDAO().updateCase(caseInstance);
		}
        return startResult;
    }

	@Override
	public List<Case> getCasesPaged(final String sortColumnProperty, final boolean sortAscending, final int pageLength, final int pageOffset, Map<String, Object> params) {
		return getCaseDAO().getCasesPaged(sortColumnProperty, sortAscending, pageLength, pageOffset, params);
	}

	@Override
	public Long getCasesCount(Map<String, Object> params) {
		return getCaseDAO().getCasesCount(params);
	}

    @Override
    public CaseStateDefinition getCaseStateDefinitionByName(String name, CaseDefinition definition) {
        return getCaseStateDefinitionDAO().getStateDefinitionByName(name, definition);
    }

    @Override
    public void updateCaseStage(CaseStage caseStage) {
        getCaseStageDAO().updateStage(caseStage);
    }
}

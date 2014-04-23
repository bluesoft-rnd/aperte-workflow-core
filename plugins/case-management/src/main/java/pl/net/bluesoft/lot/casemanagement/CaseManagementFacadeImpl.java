package pl.net.bluesoft.lot.casemanagement;

import pl.net.bluesoft.lot.casemanagement.dao.*;
import pl.net.bluesoft.lot.casemanagement.exception.CreateCaseException;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.lot.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-04-23.
 */
public class CaseManagementFacadeImpl implements ICaseManagementFacade {
    private final Logger logger = Logger.getLogger(CaseManagementFacadeImpl.class.getName());

    @Override
    public Case createCase(String definitionName, String caseName, String caseNumber, Map<String, String> caseAttributes) throws CreateCaseException {
        logger.info("Creating the new case");
        final CaseDefinition definition = getCaseDefinitionDAO().getDefinitionByName(definitionName);
        if (definition == null)
            throw new CreateCaseException(String.format("Case definition for name '%s' not found", definitionName ));
        return getCaseDAO().createCase(definition, caseName, caseNumber, caseAttributes);
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
}

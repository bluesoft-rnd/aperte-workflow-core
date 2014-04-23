package pl.net.bluesoft.lot.casemanagement;

import pl.net.bluesoft.lot.casemanagement.exception.CreateCaseException;
import pl.net.bluesoft.lot.casemanagement.model.Case;

import java.util.Map;

/**
 * Created by pkuciapski on 2014-04-23.
 */
public interface ICaseManagementFacade {
    Case createCase(String definitionName, String caseName, String caseNumber, Map<String, String> caseAttributes) throws CreateCaseException;
}

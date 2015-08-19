package pl.net.bluesoft.casemanagement.step;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.casemanagement.ICaseManagementFacade;
import pl.net.bluesoft.casemanagement.exception.CaseNotFoundException;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-05-29
 */
@AliasName(name = "AddCaseVariablesToProcess")
public class AddCaseVariablesToProcessStep implements ProcessToolProcessStep {
	@AutoWiredProperty(required = true, substitute = true)
	private String caseId;
	@AutoWiredProperty(substitute = true)
	private String variablesList;
	@AutoWiredProperty(substitute = true)
	private String stageVariablesList;

	@Autowired
	private ICaseManagementFacade caseManagement;

	@Override
	public String invoke(BpmStep step, Map<String, String> params) throws Exception {
		Case caseInstance = caseManagement.getCaseById(Long.valueOf(caseId));

		if (caseInstance == null) {
			throw new CaseNotFoundException(String.format("A case with id=%s was not found", caseId));
		}

		// tylko proste

		for (String attrName : StepUtil.evaluateList(variablesList)) {
			String value = caseInstance.getSimpleAttributeValue(attrName);
			step.getProcessInstance().setSimpleAttribute(attrName, value);
		}

		for (String attrName : StepUtil.evaluateList(stageVariablesList)) {
			String value = caseInstance.getCurrentStage().getSimpleAttributeValue(attrName);
			step.getProcessInstance().setSimpleAttribute(attrName, value);
		}

		return STATUS_OK;
	}
}

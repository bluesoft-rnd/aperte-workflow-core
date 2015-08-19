package pl.net.bluesoft.casemanagement.step;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.casemanagement.ICaseManagementFacade;
import pl.net.bluesoft.casemanagement.exception.CaseNotFoundException;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.processor.CaseProcessor;
import pl.net.bluesoft.casemanagement.step.util.CaseStepUtil;
import pl.net.bluesoft.rnd.processtool.auditlog.AuditLogContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.processdata.AbstractProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.HandlingResult;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * Created by pkuciapski on 2014-05-07.
 */
@AliasName(name = "AddVariablesToCaseStage")
public class AddVariablesToCaseStageStep implements ProcessToolProcessStep {
    private final Logger logger = Logger.getLogger(AddVariablesToCaseStageStep.class.getName());

    @AutoWiredProperty(required = true, substitute = true)
    private String caseId;
    @AutoWiredProperty
    private String variablesList;
    @AutoWiredProperty
    private boolean addAllVariables = true;
	@AutoWiredProperty(substitute = true)
	private String changeAuthor;

    @Autowired
    private ICaseManagementFacade caseManagement;

    @Override
    public String invoke(BpmStep step,Map<String, String> params) throws Exception
    {
        Long caseId = Long.valueOf(this.caseId);
        Case caseInstance = caseManagement.getCaseById(caseId);

        if (caseInstance == null) {
			throw new CaseNotFoundException(String.format("A case with id=%d was not found", caseId));
		}

        final List<AbstractProcessInstanceAttribute> attributes = CaseStepUtil.getProcessAttributes(variablesList, null, step.getProcessInstance(), addAllVariables);

        if (!attributes.isEmpty() || addAllVariables) {
            // copy process instance attributes and other variables
            final CaseProcessor processor = new CaseProcessor(caseInstance, step.getProcessInstance(), null, null, null);

			List<HandlingResult> results = AuditLogContext.withContext(caseInstance, new AuditLogContext.Callback() {
				@Override
				public void invoke() throws Exception {
					processor.copyAllAttributes(attributes);
				}
			});
			CaseStepUtil.auditLog(caseInstance, nvl(changeAuthor, "auto"), results);

            caseManagement.updateCase(caseInstance);
        }
        return STATUS_OK;
    }
}

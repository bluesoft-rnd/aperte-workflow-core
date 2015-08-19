package pl.net.bluesoft.casemanagement.step;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.casemanagement.ICaseManagementFacade;
import pl.net.bluesoft.casemanagement.exception.CaseNotFoundException;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseStage;
import pl.net.bluesoft.casemanagement.processor.CaseMapperContextParams;
import pl.net.bluesoft.casemanagement.processor.CaseProcessor;
import pl.net.bluesoft.casemanagement.step.util.CaseStepUtil;
import pl.net.bluesoft.rnd.processtool.auditlog.AuditLogContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.processdata.AbstractProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.plugins.MapperContext;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.HandlingResult;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Created by pkuciapski on 2014-05-06.
 */
@AliasName(name = "AddCaseStage")
public class AddCaseStageStep implements ProcessToolProcessStep {
    private final Logger logger = Logger.getLogger(AddCaseStageStep.class.getName());

    @AutoWiredProperty(required = true, substitute = true)
    private String caseId;
    @AutoWiredProperty(required = true, substitute = true)
    private String newStateDefinitionName;
    @AutoWiredProperty(substitute = true)
    private String newStageName;
    @AutoWiredProperty
    private String newStageAttributesQuery = "";
	@AutoWiredProperty
	private String newStageLargeAttributesQuery = "";
    @AutoWiredProperty
    private String copyAttributes;
	@AutoWiredProperty(substitute = true)
	private String changeAuthor;

	@AutoWiredProperty
	private String out_stageId;

    @Autowired
    private ICaseManagementFacade caseManagement;

    @Override
    public String invoke(final BpmStep step, final Map<String, String> params) throws Exception
    {
        logger.info("Adding stage to case " + caseId);

        Long caseId = Long.valueOf(this.caseId);
		final Case caseInstance = caseManagement.getCaseById(caseId);

        if (caseInstance == null) {
			throw new CaseNotFoundException(String.format("A case with id=%d was not found", caseId));
		}

        Map<String, String> sattrs = new HashMap<String, String>();

        if(step.getProcessInstance().getSimpleAttributeValue("sendFormLink") != null) {
            sattrs.put("sendFormLink", step.getProcessInstance().getSimpleAttributeValue("sendFormLink"));
        }

        final CaseStage stage = caseManagement.addCaseStage(caseInstance, newStateDefinitionName, newStageName, sattrs);

		CaseStepUtil.copyAttributesToStep(caseInstance, newStageAttributesQuery, step.getProcessInstance());
		CaseStepUtil.copyLargeAttributesToStep(caseInstance, newStageLargeAttributesQuery, step.getProcessInstance());

        /* Copy attributes to new case */
        final List<AbstractProcessInstanceAttribute> attributes = CaseStepUtil.getProcessAttributes(copyAttributes, null, step.getProcessInstance(), false);

		List<HandlingResult> results = AuditLogContext.withContext(caseInstance, new AuditLogContext.Callback() {
			@Override
			public void invoke() throws Exception {
				// copy process instance attributes and other variables
				final CaseProcessor processor = new CaseProcessor(caseInstance, step.getProcessInstance(), null, null, null);

				MapperContext mapperContext = new MapperContext();
				CaseMapperContextParams.setStage(mapperContext, stage);
				processor.copyAllAttributes(attributes, mapperContext);

				AuditLogContext.get().addPost(stage);
			}
		});
		CaseStepUtil.auditLog(caseInstance, nvl(changeAuthor, "auto"), results);

		caseManagement.updateCase(caseInstance);

		if (hasText(out_stageId)) {
			step.getProcessInstance().setSimpleAttribute(out_stageId, String.valueOf(stage.getId()));
		}

        return STATUS_OK;
    }
}

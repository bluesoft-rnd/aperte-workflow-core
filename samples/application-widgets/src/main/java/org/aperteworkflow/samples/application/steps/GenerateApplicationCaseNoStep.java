package org.aperteworkflow.samples.application.steps;

import org.aperteworkflow.samples.application.util.CaseSignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Map;

/**
 * Created by Dominik Dębowczyk on 2015-08-12.
 */
@AliasName(name = "GenerateApplicationCaseNo")
public class GenerateApplicationCaseNoStep implements ProcessToolProcessStep {

    @AutoWiredProperty(required = true)
    private String outputAttribute;

    @Autowired
    protected CaseSignatureService caseSignatureService;

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception {
        String caseSignature = caseSignatureService.createSignature();

        step.getProcessInstance().setSimpleAttribute(outputAttribute, caseSignature);

        return ProcessToolProcessStep.STATUS_OK;
    }
}

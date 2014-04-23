package pl.net.bluesoft.lot.casemanagement.step;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.lot.casemanagement.ICaseManagementFacade;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by pkuciapski on 2014-04-23.
 */
public class CreateCaseStep implements ProcessToolProcessStep {
    private final Logger logger = Logger.getLogger(CreateCaseStep.class.getName());

    @AutoWiredProperty(required = true)
    private String caseDefinitionName;
    @AutoWiredProperty(required = true)
    private String caseName;
    @AutoWiredProperty(required = true)
    private String caseNumber;
    @AutoWiredProperty
    private String caseAttributesQuery;

    @Autowired
    private ICaseManagementFacade caseManagement;

    @Override
    public String invoke(final BpmStep step, final Map<String, String> params) throws Exception {
        // evaluate the given query to set the case attributes
        final Map<String, String> attributes = StepUtil.evaluateQuery(caseAttributesQuery);

        ProcessToolRegistry.Util.getRegistry().withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext ctx) {
                for (Map.Entry<String, String> a : attributes.entrySet()) {
                    final String value = StepUtil.extractVariable(a.getValue(), ctx, step.getProcessInstance());
                    a.setValue(value);
                }
            }
        });

        caseManagement.createCase(caseDefinitionName, caseName, caseNumber, attributes);
        return STATUS_OK;
    }
}

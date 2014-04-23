package pl.net.bluesoft.lot.casemanagement.step;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.lot.casemanagement.ICaseManagementFacade;
import pl.net.bluesoft.lot.casemanagement.model.Case;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

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
        // todo evaluate the given query to set the case attributes
        final Map<String, String> attributes = new HashMap<String, String>();
        final Case created = caseManagement.createCase(caseDefinitionName, caseName, caseNumber, attributes);
        return STATUS_OK;
    }
}

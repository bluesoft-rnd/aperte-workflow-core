package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.apache.commons.beanutils.BeanUtils;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmVariable;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class JbpmStepAction {
	private static final Logger logger = Logger.getLogger(JbpmStepAction.class.getName());

    public String processInstanceId;
    public String stepName;
    public Map params = new HashMap();

    @SuppressWarnings("unused") // it's called directly from BPM engine
    public String invoke() {
        ProcessToolContext ptc = ProcessToolContext.Util.getThreadProcessToolContext();
        ProcessInstanceDAO dao = ptc.getProcessInstanceDAO();
        ProcessInstance pi = dao.getProcessInstance(Long.parseLong(processInstanceId));

        String res;
        try {
        	ProcessToolProcessStep stepInstance = ptc.getRegistry().getStep(stepName);
            if (stepInstance == null) {
                throw new IllegalArgumentException("No step defined by name: " + stepName);
            }
        	processAutowiredProperties(stepInstance, params);
            res = stepInstance.invoke(pi, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (ptc instanceof ProcessToolContextImpl) {
            ProcessEngine engine = ((ProcessToolContextImpl) ptc).getProcessEngine();
            ExecutionService es = engine.getExecutionService();
            for (ProcessInstanceAttribute pia : pi.getProcessAttributes()) {
                if (pia instanceof BpmVariable) {
                    BpmVariable bpmVar = (BpmVariable) pia;
                    if (hasText(bpmVar.getBpmVariableName())) {
                        es.setVariable(pi.getInternalId(),
                                bpmVar.getBpmVariableName(),
                                bpmVar.getBpmVariableValue());
                    }
                }
            }
        }
        return res;
    }
    
    private void processAutowiredProperties(Object object, Map<String, String> m) {
        Class cls = object.getClass();

        for (Field f : cls.getDeclaredFields()) {
            String autoName = null;
            for (Annotation a : f.getAnnotations()) {
                if (a instanceof AutoWiredProperty) {
                    AutoWiredProperty awp = (AutoWiredProperty) a;
                    if (AutoWiredProperty.DEFAULT.equals(awp.name())) {
                        autoName = f.getName();
                    } else {
                        autoName = awp.name();
                    }
                }
            }
            String value = nvl(
                    m.get(autoName),
                    ProcessToolContext.Util.getThreadProcessToolContext().getSetting("autowire." + autoName)
            );
            if (autoName != null && value != null) {
                try {
                    logger.fine("Setting attribute " + autoName + " to " + value);
                    BeanUtils.setProperty(object, autoName, value);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error setting attribute " + autoName + ": " +e.getMessage(), e);
                }
            }
        }
    }
}

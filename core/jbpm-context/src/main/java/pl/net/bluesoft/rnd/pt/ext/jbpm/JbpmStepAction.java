package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.apache.commons.beanutils.PropertyUtils;
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
	private Logger logger = Logger.getLogger(JbpmStepAction.class.getName());

    public String processInstanceId;
    public String stepName;
    public Map params = new HashMap();

    public String invoke() {
        ProcessToolContext ptc = ProcessToolContext.Util.getProcessToolContextFromThread();
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
            String v = nvl(m.get(autoName),
                           ProcessToolContext.Util.getProcessToolContextFromThread().getSetting("autowire." + autoName));
            if (autoName != null && v != null) {
                try {
                    logger.warning("Setting attribute " + autoName + " to " + v);
                    if (f.getType().equals(String.class)) {
                        PropertyUtils.setProperty(object, autoName, v);
                    }
                    else if (f.getType().isPrimitive()) {
                        String name = f.getType().getName();
                        if (name.equals("int")) {
                            PropertyUtils.setProperty(object, autoName, Integer.parseInt(v));
                        }
                        else if (name.equals("boolean")) {
                            PropertyUtils.setProperty(object, autoName, Boolean.parseBoolean(v));
                        }
                        else {
                            PropertyUtils.setProperty(object, autoName, v);
                        }
                    }
                    else {
                        logger.warning("attribute " + autoName + " with type " + f.getType() + " is not supported!");
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }
}

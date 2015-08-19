package org.aperteworkflow.ext.activiti;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.runtime.Execution;
import org.apache.commons.beanutils.BeanUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.BpmVariable;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmStep;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ActivitiStepAction implements JavaDelegate {
    private static final Logger logger = Logger.getLogger(ActivitiStepAction.class.getName());

    public Expression stepName;
    public Expression params = null;

    @SuppressWarnings("unused") // it's called directly from BPM engine
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        ProcessToolContext ptc = ProcessToolContext.Util.getThreadProcessToolContext();
        ProcessInstanceDAO dao = ptc.getProcessInstanceDAO();
        String processInstanceId = execution.getProcessInstanceId();
        ProcessInstance pi = dao.getProcessInstanceByInternalId(processInstanceId);

        String res;
        String stepName = (String) this.stepName.getValue(execution);
        Map params = new HashMap();
        if (this.params != null) {
            String xml = (String) this.params.getValue(execution);
            if (xml != null) {
                XStream xs = new XStream();
                xs.alias("map", java.util.Map.class);
                xs.registerConverter(new Converter() {
                    public boolean canConvert(Class clazz) {
                        return AbstractMap.class.isAssignableFrom(clazz);
                    }

                    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
                        AbstractMap<String, String> map = (AbstractMap<String, String>) value;
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            writer.startNode(entry.getKey().toString());
                            writer.setValue(entry.getValue().toString());
                            writer.endNode();
                        }
                    }

                    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                        Map<String, String> map = new HashMap<String, String>();

                        while (reader.hasMoreChildren()) {
                            reader.moveDown();
                            map.put(reader.getNodeName(), reader.getValue());
                            reader.moveUp();
                        }
                        return map;
                    }
                });
                params = (Map) xs.fromXML(xml);
            }
        }

        try {
            ProcessToolProcessStep stepInstance = ptc.getRegistry().getStep(stepName);
            if (stepInstance == null) {
                throw new IllegalArgumentException("No step defined by name: " + stepName);
            }
            processAutowiredProperties(stepInstance, params);
            res = stepInstance.invoke(prepareStep(pi, execution), params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (ProcessInstanceAttribute pia : pi.getProcessAttributes()) {
            if (pia instanceof BpmVariable) {
                BpmVariable bpmVar = (BpmVariable) pia;
                if (hasText(bpmVar.getBpmVariableName())) {
                    execution.setVariable(bpmVar.getBpmVariableName(), bpmVar.getBpmVariableValue());
                }
            }
        }
        execution.setVariable("RESULT", res);

    }
    private BpmStep prepareStep(ProcessInstance pi, DelegateExecution exec) {
        MutableBpmStep step = new MutableBpmStep();
        step.setProcessInstance(pi);
        step.setExecutionId(exec.getId());
        step.setStateName((String) this.stepName.getValue(exec));
//        makes no sense in BPMN2.0, anyway step should not rely its logic on its placement on process map
//        List<String> transitionNames = new ArrayList<String>();
//        for (Transition transition : exec.getActivity().getOutgoingTransitions()) {
//            transitionNames.add(transition.getDestination().getName());
//        }
//        step.setOutgoingTransitions(transitionNames);
        return step;
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
                    logger.log(Level.SEVERE, "Error setting attribute " + autoName + ": " + e.getMessage(), e);
                }
            }
        }
    }

}

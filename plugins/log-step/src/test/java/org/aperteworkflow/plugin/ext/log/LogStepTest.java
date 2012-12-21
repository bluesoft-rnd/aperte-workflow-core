package org.aperteworkflow.plugin.ext.log;

import junit.framework.Assert;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmStep;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmTask;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LogStepTest {

    private static final Logger logger = Logger.getLogger(LogStepTest.class.getName());
    
    @Test
    public void testParse() throws Exception {
        ProcessInstance process = new ProcessInstance();
        process.addAttribute(new ProcessInstanceSimpleAttribute("p1", "my-very-first-param"));
        process.addAttribute(new ProcessInstanceSimpleAttribute("p2", "second_param_123"));

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("message", "${p1} and ${p2} or ${something}");

        String result = processStep(new LogStep(), process, properties);

        Assert.assertEquals(result, "my-very-first-param and second_param_123 or ${something}");
    }

    @Test
    public void testNoParse() throws Exception {
        final String message = "no message here ${this-is-my-attribute}";
        
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("message", message);

        String result = processStep(new LogStep(), new ProcessInstance(), properties);

        Assert.assertEquals(result, message);
    }

    // TODO move to common test case class for Steps
    private String processStep(final ProcessToolProcessStep step, final ProcessInstance process, final Map<String, String> properties)
            throws Exception{
        processAutowiredProperties(step, properties);
        BpmStep bpmTask = new MutableBpmStep() {
            @Override
            public ProcessInstance getProcessInstance() {
                return process;
            }
        };
        return step.invoke(bpmTask, properties);
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
            String value = m.get(autoName);
            if (autoName != null && value != null) {
                try {
                    BeanUtils.setProperty(object, autoName, value);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error setting attribute " + autoName + ": " +e.getMessage(), e);
                }
            }
        }
    }

}

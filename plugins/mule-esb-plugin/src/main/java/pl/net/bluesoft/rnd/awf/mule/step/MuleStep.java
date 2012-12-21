package pl.net.bluesoft.rnd.awf.mule.step;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;
import pl.net.bluesoft.rnd.awf.mule.MulePluginManager;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * Created by IntelliJ IDEA.
 *
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name = "MuleStep")
public class MuleStep implements ProcessToolProcessStep {

    private static final Logger logger = Logger.getLogger(MuleStep.class.getName());
    
    @AutoWiredProperty
    private String destinationEndpointUrl;

    private Object payload;

    @AutoWiredProperty
    private Boolean asynchronous = false;

    @AutoWiredProperty
    private Long timeout = Long.valueOf(-1);

    private MulePluginManager mulePluginManager;

    public MuleStep(MulePluginManager mulePluginManager) {
        this.mulePluginManager = mulePluginManager;
    }

    @Override
    public String invoke(BpmStep step, Map params) throws Exception {
        try {
            ProcessInstance processInstance = step.getProcessInstance();
            payload = params.get("payload");
            LocalMuleClient client = mulePluginManager.getMuleContext().getClient();
//            XStream xs = new XStream();
//            xs.registerConverter(new MyPersistentSetConverter(xs.getMapper()), XStream.PRIORITY_VERY_HIGH);
//            xs.omitField(ProcessInstance.class, "definition");
//            xs.omitField(ProcessInstance.class, "processLogs");
//            String input = xs.toXML(processInstance);
            if (asynchronous) {
                client.dispatch(destinationEndpointUrl, processInstance, null);
            } else {
                MuleMessage muleMessage = client.send(destinationEndpointUrl,
                                                      payload != null ? payload : processInstance,
                                                      null, timeout);
                if (muleMessage != null) {
                    ExceptionPayload exceptionPayload = muleMessage.getExceptionPayload();
                    if (exceptionPayload != null) {
                        logger.log(Level.SEVERE, "Mule step has failed: " + exceptionPayload.getMessage(), exceptionPayload.getException());
                        return "FAIL";
                    }
                    Object payload = muleMessage.getPayload();
                    if (payload instanceof String) {
                        return (String)payload;
                    } else if (payload instanceof ProcessInstanceAttribute) {
                        ProcessInstanceAttribute pia = (ProcessInstanceAttribute) payload;
                        ProcessInstanceAttribute attributeByKey = processInstance.findAttributeByKey(pia.getKey());
                        if (attributeByKey != null) {
                            processInstance.removeAttribute(attributeByKey);
                        }
                        processInstance.addAttribute(pia);
                        return pia.toString();
                    } else if (payload instanceof ProcessInstanceAttribute[]) {
                        ProcessInstanceAttribute[] pias = (ProcessInstanceAttribute[]) payload;
                        for (ProcessInstanceAttribute pia : pias) {
                            ProcessInstanceAttribute attributeByKey = processInstance.findAttributeByKey(pia.getKey());
                            if (attributeByKey != null) {
                                processInstance.removeAttribute(attributeByKey);
                            }
                            processInstance.addAttribute(pia);
                        }
                    } else if (payload instanceof Iterable) {
                        Iterable pias = (Iterable) payload;
                        for (Object o : pias) {
                            if (o instanceof ProcessInstanceAttribute) {
                                ProcessInstanceAttribute pia = (ProcessInstanceAttribute) o;
                                ProcessInstanceAttribute attributeByKey = processInstance.findAttributeByKey(pia.getKey());
                                if (attributeByKey != null) {
                                    processInstance.removeAttribute(attributeByKey);
                                }
                                processInstance.addAttribute(pia);
                            }
                        }
                    }

                }
            }
            return "OK";
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error invoking MuleStep: " + e.getMessage(), e);
            return "FAIL";
        }

    }

    public String getDestinationEndpointUrl() {
        return destinationEndpointUrl;
    }

    public void setDestinationEndpointUrl(String destinationEndpointUrl) {
        this.destinationEndpointUrl = destinationEndpointUrl;
    }

    public boolean isAsynchronous() {
        return nvl(asynchronous, false);
    }

    public void setAsynchronous(boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}

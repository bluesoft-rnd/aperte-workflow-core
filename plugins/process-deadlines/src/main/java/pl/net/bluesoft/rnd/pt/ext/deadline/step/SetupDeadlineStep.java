package pl.net.bluesoft.rnd.pt.ext.deadline.step;

import org.apache.commons.beanutils.PropertyUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolException;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.util.lang.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

@AliasName(name = "SetupDeadlineStep")
public class SetupDeadlineStep implements ProcessToolProcessStep {
    public static final String DEFAULT_TEMPLATE_NAME = "deadlineEmailTemplate";

    private static final Logger logger = Logger.getLogger(SetupDeadlineStep.class.getName());

    @AutoWiredProperty
    private String taskName;
    @AutoWiredProperty
    private String templateName = DEFAULT_TEMPLATE_NAME;
    @AutoWiredProperty
    private String unit = "day";
    @AutoWiredProperty
    private String value;

    @Override
    public String invoke(ProcessInstance processInstance, Map params) throws Exception {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        List<String> taskNames = new ArrayList<String>();
        if (StringUtil.hasText(taskName)) {
            taskNames.addAll(Arrays.asList(taskName.split(",")));
        }
        else {
            ProcessToolBpmSession bpmSession = ctx.getProcessToolSessionFactory().createSession(new UserData("system", "System", "awf@bluesoft.net.pl"),
                    new HashSet<String>());
            taskNames.addAll(bpmSession.getOutgoingTransitionNames(processInstance.getInternalId(), ctx));
        }

        Date dueDate = extractDate("dueDate", processInstance, params);
        if (dueDate == null) {
            Date baseDate = extractDate("baseDate", processInstance, params);
            if (!StringUtil.hasText(value)) {
                throw new ProcessToolException("Unable to calculate due date");
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(baseDate != null ? baseDate : new Date());
            cal.add("min".equals(unit) ? Calendar.MINUTE : Calendar.DAY_OF_YEAR, Integer.valueOf(value));
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.SECOND, 0);
            if (!"min".equals(unit)) {
                cal.set(Calendar.MINUTE, 0);
            }
            dueDate = cal.getTime();
        }

        for (String tn : taskNames) {
            String attrKey = "deadline_" + tn;
            ProcessDeadline pid = null;
            for (ProcessInstanceAttribute attr : processInstance.getProcessAttributes()) {
                if (attr.getKey() == null) {
                    logger.info("Attribute key is null! Process instance: "
                            + processInstance.getInternalId() + " state: " + processInstance.getState());
                }
                if (attrKey.equals(attr.getKey())) {
                    pid = (ProcessDeadline) attr;
                    break;
                }
            }
            if (pid == null) {
                pid = new ProcessDeadline();
                pid.setKey("deadline_" + tn);
                pid.setProcessInstance(processInstance);
                processInstance.getProcessAttributes().add(pid);
            }
            pid.setTemplateName(templateName);
            pid.setTaskName(tn);
            pid.setDueDate(dueDate);
        }

        ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);

        return STATUS_OK;
    }

    private Date extractDate(String prefix, ProcessInstance processInstance, Map params) throws InvocationTargetException,
            NoSuchMethodException, IllegalAccessException {
        Date date = null;
        if (params.containsKey(prefix)) {
            date = (Date) params.get(prefix);
        }
        else if (params.containsKey(prefix + "Attribute")) {
            String[] paramValue = ((String) params.get(prefix + "Attribute")).split("\\.", 2);
            for (ProcessInstanceAttribute attr : processInstance.getProcessAttributes()) {
                if (attr.getKey().equals(paramValue[0])) {
                    date = (Date) PropertyUtils.getProperty(attr, paramValue.length > 1 ? paramValue[1] : "value");
                    break;
                }
            }
        }
        return date;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

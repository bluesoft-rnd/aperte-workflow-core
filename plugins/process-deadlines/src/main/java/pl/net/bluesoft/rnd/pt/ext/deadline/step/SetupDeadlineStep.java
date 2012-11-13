package pl.net.bluesoft.rnd.pt.ext.deadline.step;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.beanutils.PropertyUtils;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolException;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessDeadline;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.util.lang.Strings;

@AliasName(name = "SetupDeadlineStep")
public class SetupDeadlineStep implements ProcessToolProcessStep {
    public static final String DEFAULT_TEMPLATE_NAME = "deadlineEmailTemplate";
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); // multithread correctness

    private static final Logger logger = Logger.getLogger(SetupDeadlineStep.class.getName());

    @AutoWiredProperty
    private String taskName;
    @AutoWiredProperty
    private String templateName = DEFAULT_TEMPLATE_NAME;
    @AutoWiredProperty
    private String unit = "day";
    @AutoWiredProperty
    private String value;
    @AutoWiredProperty
    private String notifyUsersWithLogin;
    @AutoWiredProperty
    private String notifyUsersWithRole;
    @AutoWiredProperty
    private String skipAssignee;
    @AutoWiredProperty
    private String workingDays;
    
    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception 
    {
        ProcessInstance processInstance = step.getProcessInstance();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        List<String> taskNames = Strings.hasText(taskName) ? Arrays.asList(taskName.split(",")) : step.getOutgoingTransitions();
        
        

        Date dueDate = extractDate("dueDate", processInstance, params);
        if (dueDate == null) {
            Date baseDate = extractDate("baseDate", processInstance, params);
            if (!Strings.hasText(value)) {
                throw new ProcessToolException("Unable to calculate due date");
            }
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(baseDate != null ? baseDate : new Date());
            
            if(useWorkingDays())
            {
        		int nv = Integer.valueOf(Integer.valueOf(value));
        		nv = getWeekOffset(cal.getTime(),nv);
        		cal.add(Calendar.DAY_OF_YEAR, nv);
        		
        		dueDate = cal.getTime();
            	
            }
            else
            {
                cal.add("min".equals(unit) ? Calendar.MINUTE : Calendar.DAY_OF_YEAR, Integer.valueOf(value));
                cal.set(Calendar.HOUR, 0);
                cal.set(Calendar.SECOND, 0);
                if (!"min".equals(unit)) {
                    cal.set(Calendar.MINUTE, 0);
                }
                dueDate = cal.getTime();
            }
        }

        for (String tn : taskNames) 
        {
            String attrKey = "deadline_" + tn;
            ProcessDeadline pid = null;
            for (ProcessInstanceAttribute attr : processInstance.getProcessAttributes()) {
                if (attr.getKey() == null) {
                    logger.info("Attribute key is null! Process instance: " + processInstance.getInternalId());
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
            pid.setNotifyUsersWithLogin(notifyUsersWithLogin);
            pid.setNotifyUsersWithRole(notifyUsersWithRole);
            pid.setSkipAssignee("true".equalsIgnoreCase(skipAssignee));
            pid.setTemplateName(templateName);
            pid.setTaskName(tn);
            pid.setAlreadyNotified(false);
            pid.setDueDate(dueDate);
        }

        ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);

        return STATUS_OK;
    }
    
	public static int getWeekOffset(Date start,int offset){
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        int weekday = cal.get(Calendar.DAY_OF_WEEK);

        int days = offset%5;
        int fullweeks = ((offset - days)/5)*7;
        int final_offset = 0;

        if(weekday == 0){       // start sobota
            final_offset = 2;
            weekday = 2; //poniedziale
        }else if(weekday == 1){ // start niedziela
            final_offset = 1;
            weekday = 2; //poniedziale
        }

        if(weekday+days > 6){
            final_offset +=2;
        }
        offset = final_offset + fullweeks +days;
        return  offset;
    }
    
    private boolean useWorkingDays()
    {
    	return Strings.hasText(workingDays) && workingDays.equals("true");
    }

    private Date extractDate(String prefix, ProcessInstance processInstance, Map<String, String> params) throws InvocationTargetException,
            NoSuchMethodException, IllegalAccessException, ParseException {
        Date date = null;
        if (params.containsKey(prefix)) {
            date = DATE_FORMAT.parse(params.get(prefix));
        }
        else if (params.containsKey(prefix + "Attribute")) {
            String[] paramValue = params.get(prefix + "Attribute").split("\\.", 2);
            for (ProcessInstanceAttribute attr : processInstance.getProcessAttributes()) {
                if (attr.getKey().equals(paramValue[0])) {
                    date = (Date) PropertyUtils.getProperty(attr, paramValue.length > 1 ? paramValue[1] : "value");
                    break;
                }
            }
        }
        return date;
    }
}

package pl.net.bluesoft.casemanagement.step.util;

import org.codehaus.jackson.map.ObjectMapper;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseLog;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.processdata.AbstractProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.ui.widgets.HandlingResult;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class CaseStepUtil
{
	private static final Logger logger = Logger.getLogger(CaseStepUtil.class.getName());

    public static List<AbstractProcessInstanceAttribute> getProcessAttributes(String variablesList, String ignoredAttributes, ProcessInstance pi, boolean addAllVariables) {
        final List<AbstractProcessInstanceAttribute> attrs = new ArrayList<AbstractProcessInstanceAttribute>();

        if (addAllVariables)
        {
            attrs.addAll(pi.getAllProcessAttributes());
        }
        else if (variablesList != null)
        {
            for (String key : StepUtil.evaluateList(variablesList))
            {
                AbstractProcessInstanceAttribute attr = getProcessAttribute(pi, key);
                if (attr != null)
                    attrs.add(attr);
            }
        }

		List<String> ignoredList = StepUtil.evaluateList(ignoredAttributes);

		if (!ignoredList.isEmpty()) {
			for (Iterator<AbstractProcessInstanceAttribute> iterator = attrs.iterator(); iterator.hasNext(); ) {
				AbstractProcessInstanceAttribute attr = iterator.next();
				if (ignoredList.contains(attr.getKey())) {
					iterator.remove();
				}
			}
		}
        return attrs;
    }

    public static AbstractProcessInstanceAttribute getProcessAttribute(final ProcessInstance pi, String key) {
        for (AbstractProcessInstanceAttribute attr : pi.getAllProcessAttributes()) {
            if (key != null && key.equals(attr.getKey()))
                return attr;
        }
        return null;
    }

	public static List<AbstractProcessInstanceAttribute> removeNullAttributes(List<AbstractProcessInstanceAttribute> attributes, String ignoredNullAttributes) {
		if (!hasText(ignoredNullAttributes)) {
			return attributes;
		}

		List<String> attrsToIgnore = StepUtil.evaluateList(ignoredNullAttributes);

		for (Iterator<AbstractProcessInstanceAttribute> it = attributes.iterator(); it.hasNext(); ) {
			AbstractProcessInstanceAttribute attr = it.next();

			if (attr instanceof ProcessInstanceSimpleAttribute) {
				if (attrsToIgnore.contains(attr.getKey())) {
					String value = ((ProcessInstanceSimpleAttribute)attr).getValue();

					if (!hasText(value)) {
						it.remove();
					}
				}
			}
		}
		return attributes;
	}

	public static void copyAttributesToStep(Case caseInstance, String attributesQuery, ProcessInstance processInstance) {
		Map<String, String> stageAttributes = StepUtil.evaluateQuery(attributesQuery, processInstance);

		for (Map.Entry<String, String> entry : stageAttributes.entrySet()) {
			caseInstance.getCurrentStage().setSimpleAttribute(entry.getKey(), entry.getValue());
		}
	}

	public static void copyLargeAttributesToStep(Case caseInstance, String attributesQuery, ProcessInstance processInstance) {
		Map<String, String> stageLargeAttributes = StepUtil.evaluateQuery(attributesQuery, processInstance);

		for (Map.Entry<String, String> entry : stageLargeAttributes.entrySet()) {
			caseInstance.getCurrentStage().setSimpleLargeAttribute(entry.getKey(), entry.getValue());
		}
	}

	private static final ObjectMapper mapper = new ObjectMapper();

	public static void auditLog(Case caseInstance, String user, Collection<HandlingResult> results) {
		if (results == null || results.isEmpty()) {
			return;
		}

		String json = null;
		try {
			json = mapper.writeValueAsString(results);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}

		CaseLog log = new CaseLog();
		log.setEntryDate(new Date());
		log.setEventI18NKey("case.log.case-change");
		log.setLogType(CaseLog.LOG_TYPE_CASE_CHANGE);
		log.setLogValue(json);
		log.setUserLogin(nvl(user));
		if (caseInstance.getCaseLog() == null) {
			caseInstance.setCaseLog(new ArrayList<CaseLog>());
		}
		caseInstance.getCaseLog().add(log);
	}
}

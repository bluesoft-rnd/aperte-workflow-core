package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author kkolodziej@bluesoft.net.pl
 */
public interface ProcessStateActionDAO extends HibernateBean<ProcessStateAction> {
	List<ProcessStateAction> getActionsListByDefinition(ProcessDefinitionConfig processDefinitionConfig);

	List<ProcessStateAction> getActionByNameFromDefinition(
			ProcessDefinitionConfig processDefinitionConfig, String bpmName);
 
	List<ProcessStateAction> getActionsBasedOnStateAndDefinitionId(
			String state, Long definitionId); 
}

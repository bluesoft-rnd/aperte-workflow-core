package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;

import java.util.List;

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

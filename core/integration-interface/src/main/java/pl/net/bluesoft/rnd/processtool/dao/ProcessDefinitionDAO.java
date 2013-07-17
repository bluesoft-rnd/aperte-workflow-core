package pl.net.bluesoft.rnd.processtool.dao;

import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessDefinitionDAO extends HibernateBean<ProcessDefinitionConfig> {

	Collection<ProcessDefinitionConfig> getAllConfigurations();
	Collection<ProcessDefinitionConfig> getActiveConfigurations();

	ProcessDefinitionConfig getActiveConfigurationByKey(String key);
	ProcessDefinitionConfig getConfigurationByProcessId(String processId);

	ProcessDefinitionConfig getCachedDefinitionById(Long id);
	ProcessDefinitionConfig getCachedDefinitionById(ProcessInstance processInstance);

	Collection<ProcessQueueConfig> getQueueConfigs();
	ProcessStateConfiguration getProcessStateConfiguration(BpmTask task);
	ProcessStateConfiguration getProcessStateConfiguration(Long processStateConfigurationId);
	
	ProcessStateWidget getProcessStateWidget(Long widgetStateId);

	boolean differsFromTheLatest(ProcessDefinitionConfig cfg);
	void updateOrCreateProcessDefinitionConfig(ProcessDefinitionConfig cfg);

    void setConfigurationEnabled(ProcessDefinitionConfig cfg, boolean enabled);

    Collection<ProcessDefinitionConfig> getConfigurationVersions(ProcessDefinitionConfig cfg);

    void updateOrCreateQueueConfigs(Collection<ProcessQueueConfig> cfgs);
    void removeQueueConfigs(Collection<ProcessQueueConfig> cfgs);

	int getNextProcessVersion(String bpmDefinitionKey);
}

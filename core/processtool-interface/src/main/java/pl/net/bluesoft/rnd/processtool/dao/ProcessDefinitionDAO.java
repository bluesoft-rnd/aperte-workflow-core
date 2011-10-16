package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessDefinitionDAO {

	Collection<ProcessDefinitionConfig> getAllConfigurations();
	Collection<ProcessDefinitionConfig> getActiveConfigurations();

	ProcessDefinitionConfig getActiveConfigurationByKey(String key);

	Collection<ProcessQueueConfig> getQueueConfigs();
	ProcessStateConfiguration getProcessStateConfiguration(ProcessInstance pi);

	void updateOrCreateProcessDefinitionConfig(ProcessDefinitionConfig cfg);
	void updateOrCreateQueueConfigs(ProcessQueueConfig[] cfgs);
}

package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessDefinitionDAO extends HibernateBean<ProcessDefinitionConfig> {

	Collection<ProcessDefinitionConfig> getAllConfigurations();
	Collection<ProcessDefinitionConfig> getActiveConfigurations();

	ProcessDefinitionConfig getActiveConfigurationByKey(String key);

	Collection<ProcessQueueConfig> getQueueConfigs();
	ProcessStateConfiguration getProcessStateConfiguration(BpmTask task);

	void updateOrCreateProcessDefinitionConfig(ProcessDefinitionConfig cfg);
//	void updateOrCreateQueueConfigs(ProcessQueueConfig[] cfgs);

    void setConfigurationEnabled(ProcessDefinitionConfig cfg, boolean enabled);

    Collection<ProcessDefinitionConfig> getConfigurationVersions(ProcessDefinitionConfig cfg);

    void updateOrCreateQueueConfigs(Collection<ProcessQueueConfig> cfgs);
    void removeQueueConfigs(Collection<ProcessQueueConfig> cfgs);
}

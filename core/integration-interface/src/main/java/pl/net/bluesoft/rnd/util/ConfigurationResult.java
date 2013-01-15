package pl.net.bluesoft.rnd.util;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

public class ConfigurationResult 
{
	private ProcessDefinitionConfig newOne;
	private ProcessDefinitionConfig oldOne;
	public ProcessDefinitionConfig getNewOne() {
		return newOne;
	}
	public void setNewOne(ProcessDefinitionConfig newOne) {
		this.newOne = newOne;
	}
	public ProcessDefinitionConfig getOldOne() {
		return oldOne;
	}
	public void setOldOne(ProcessDefinitionConfig oldOne) {
		this.oldOne = oldOne;
	}


}

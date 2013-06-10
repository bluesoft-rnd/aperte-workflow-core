package pl.net.bluesoft.awf.ext.droolsstep.settings;

import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;

/**
 * Drools Step Settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public enum DroolsStepSettings implements IProcessToolSettings 
{
	RULES_BASEURL("drools.rules.baseurl");
	
	private String key;
	private DroolsStepSettings(String key)
	{
		this.key = key;
	}
	
	@Override
	public String toString() 
	{
		return key;
	}

}

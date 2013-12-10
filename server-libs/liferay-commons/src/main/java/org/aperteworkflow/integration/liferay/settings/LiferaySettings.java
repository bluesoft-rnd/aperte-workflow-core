package org.aperteworkflow.integration.liferay.settings;

import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;

/**
 * Liferay Settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public enum LiferaySettings implements IProcessToolSettings 
{
	LDAP_CUSTOM_USER_ATTRIBUTES("ldap.user.attributes");
	
	private String key;
	private LiferaySettings(String key)
	{
		this.key = key;
	}
	
	@Override
	public String toString() 
	{
		return key;
	}

}

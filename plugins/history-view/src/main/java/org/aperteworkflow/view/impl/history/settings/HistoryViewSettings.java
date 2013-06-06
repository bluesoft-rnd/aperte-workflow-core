package org.aperteworkflow.view.impl.history.settings;

import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;

/**
 * Liferay Settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public enum HistoryViewSettings implements IProcessToolSettings 
{
	SUPERUSER_ROLES("history.superuser.roles");
	
	private String key;
	private HistoryViewSettings(String key)
	{
		this.key = key;
	}
	
	@Override
	public String toString() 
	{
		return key;
	}

}

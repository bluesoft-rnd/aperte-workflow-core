package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.settings;

import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;

/**
 * Trip Widget Settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public enum NotificationsSettings implements IProcessToolSettings 
{
	PROVIDER_TYPE("mail.settings.provider.type"),
	REFRESH_INTERVAL("mail.settings.refresh.interval");
	
	private String key;
	private NotificationsSettings(String key)
	{
		this.key = key;
	}
	
	@Override
	public String toString() 
	{
		return key;
	}

}

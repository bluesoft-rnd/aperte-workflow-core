package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.settings;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

/**
 * Provider class for cmis settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class NotificationsSettingsProvider 
{
	/** Get provider type name */
	public static String getProviderType()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		return ctx.getSetting(NotificationsSettings.PROVIDER_TYPE);
	}
	
	/** Get settings refresh interval */
	public static String getRefreshInterval()
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		return ctx.getSetting(NotificationsSettings.REFRESH_INTERVAL);
	}

}

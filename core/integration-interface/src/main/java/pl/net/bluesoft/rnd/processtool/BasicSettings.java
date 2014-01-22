package pl.net.bluesoft.rnd.processtool;

/**
 * Basic Aperte Workflow settings
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public enum BasicSettings implements IProcessToolSettings 
{
    /** Full URL to current portlet */
    ACTIVITY_PORTLET_URL("activity.portlet.url"),
    
    /** Full URL to fast link */
    ACTIVITY_STANDALONE_SERVLET_URL("activity.standalone.url"),
    
    /** Refresh interval in seconds */
    REFRESHER_INTERVAL_SETTINGS_KEY("refresher.interval");



	private String key;
	private BasicSettings(String key)
	{
		this.key = key;
	}
	
	@Override
	public String toString() 
	{
		return key;
	}
}

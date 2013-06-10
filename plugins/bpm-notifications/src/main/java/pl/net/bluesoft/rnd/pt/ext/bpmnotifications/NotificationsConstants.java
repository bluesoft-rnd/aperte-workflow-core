package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

/**
 * Constants for notifications  
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface NotificationsConstants 
{
    public static final long CONFIG_DEFAULT_CACHE_REFRESH_INTERVAL = 5* 1000;
    
    public static final String SUBJECT_TEMPLATE_SUFFIX = "_subject";
    
    /** Mail body encoding */
    public static final String MAIL_ENCODING = "UTF-8";
    
    /** Provider Type enumeration */
    public static enum ProviderType
    {
    	DATABASE("database"),
    	JNDI("jndi");
    	
    	private String paramterName;
    	
    	ProviderType(String paramterName)
    	{
    		this.paramterName = paramterName;
    	}
    	
    	public String getParamterName()
    	{
    		return this.paramterName;
    	}
    	
    	
    }
}

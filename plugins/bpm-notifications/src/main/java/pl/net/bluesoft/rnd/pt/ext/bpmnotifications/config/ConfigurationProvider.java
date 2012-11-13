package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.config;

import java.util.Collection;
import java.util.HashSet;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;

/**
 * Provider for the notification configuration
 * 
 * @author mpawlak
 *
 */
public class ConfigurationProvider
{
	private Collection<BpmNotificationConfig> configCache = new HashSet<BpmNotificationConfig>();
	
    public void refreshConfig() 
    {
        Session session = ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
        
        configCache = session
                .createCriteria(BpmNotificationConfig.class)
                .add(Restrictions.eq("active", true))
                .list();
    }

	public Collection<BpmNotificationConfig> getConfigurations()
	{
		return configCache;
	}
}

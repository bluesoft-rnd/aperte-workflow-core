package pl.net.bluesoft.rnd.processtool.usersource.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.di.annotations.AutoInject;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryService;
import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryServicePropertiesProvider;

public class LdapUsersSource implements IDirectoryService 
{
	private Properties ldapAttributeMappings;
	
    @AutoInject
    protected IDirectoryServicePropertiesProvider propertiesProvider;
	
	public LdapUsersSource()
	{
    	/* init user source */
		ObjectFactory.inject(this);
		
		propertiesProvider = ObjectFactory.create(IDirectoryServicePropertiesProvider.class);
		
		ldapAttributeMappings = new Properties();
		Properties defaultLdapAttributeMappings = propertiesProvider.getDefaultProperties();
		
        for (String key : defaultLdapAttributeMappings.stringPropertyNames()) {
        	ldapAttributeMappings.setProperty(key, defaultLdapAttributeMappings.getProperty(key));
        }
	}

	@Override
	public Map<String, Properties> getUsersAttributes(Collection<UserData> users) 
	{
		return new HashMap<String, Properties>();
//		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
//		
//		return LdapBridge.getLdapUsersAttributes(users, ldapAttributeMappings, ctx);
	}

}

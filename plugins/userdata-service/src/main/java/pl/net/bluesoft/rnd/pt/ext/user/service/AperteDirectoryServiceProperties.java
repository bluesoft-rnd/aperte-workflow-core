package pl.net.bluesoft.rnd.pt.ext.user.service;

import java.util.Properties;

import pl.net.bluesoft.rnd.processtool.usersource.IDirectoryServicePropertiesProvider;

public class AperteDirectoryServiceProperties implements IDirectoryServicePropertiesProvider {
	@Override
	public Properties getDefaultProperties() 
	{
		return new Properties();
	}
}
